# ReNova Construction Gate 1: Password Safety

Status: PASS

Date: 2026-06-24

Branch: `codex/21st-motion-redesign`

## Result

ReNova stores account passwords only as BCrypt hashes. Default and test startup create no user or admin account with a known credential. Optional sample accounts require the explicit `demo` profile and a runtime-only password. Passwords and password hashes are not returned by APIs, serialized from `User`, displayed in the frontend, or written to application logs.

Passwords necessarily travel from the browser to the signup/login endpoints. Production must send those requests over HTTPS. The server never sends the password back to the browser.

## Registration code after the fix

Source: `backend/src/main/java/com/novacart/store/service/AuthService.java:30`

```java
@Transactional
public AuthDtos.AuthResponse signup(AuthDtos.SignupRequest request) {
    String email = request.email().trim().toLowerCase();
    if (userRepository.existsByEmailIgnoreCase(email)) {
        throw new DuplicateResourceException("An account with this email already exists.");
    }
    User user = new User();
    user.setEmail(email);
    user.setDisplayName(request.displayName().trim());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setLocation(request.location());
    user.setCreatedAt(Instant.now());
    user.setLastLoginAt(Instant.now());
    userRepository.save(user);
    return buildResponse(user);
}
```

## Login code after the fix

Source: `backend/src/main/java/com/novacart/store/service/AuthService.java:47`

```java
@Transactional
public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
    User user = userRepository.findByEmailIgnoreCase(request.email().trim())
            .orElseThrow(() -> new AuthenticationFailedException("Email or password is incorrect."));
    if (user.getStatus() != UserStatus.ACTIVE) {
        throw new AuthenticationFailedException("This account is not active.");
    }
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
        throw new AuthenticationFailedException("Email or password is incorrect.");
    }
    user.setLastLoginAt(Instant.now());
    userRepository.save(user);
    return buildResponse(user);
}
```

`SecurityConfig.passwordEncoder()` remains `BCryptPasswordEncoder`. No reversible encryption or custom password hashing was introduced.

## Before and after

| Area | Before | After |
| --- | --- | --- |
| Default startup | Created user/admin accounts with source-controlled passwords | Creates non-sensitive categories only |
| Optional sample data | Always enabled with known credentials | Requires `demo` profile plus `RENOVA_DEMO_PASSWORD` of at least 12 characters |
| Existing databases | Previously seeded accounts remained usable | Known legacy accounts are deactivated outside demo; demo sellers are rehashed from the current runtime password |
| Frontend | Displayed and autofilled a demo password | Contains no preset credential UI |
| Entity serialization | `User.passwordHash` could be serialized accidentally | Getter is protected with `@JsonIgnore` |
| Tests | Reused a committed demo password | Generate isolated credentials at runtime |
| Documentation | Published working demo credentials | Uses non-working placeholders only |

## Proof

`PasswordSecurityTests` verifies:

- non-demo startup does not create known credential accounts;
- a legacy known account is deactivated and cannot log in;
- signup persists a BCrypt value different from the submitted password;
- `PasswordEncoder.matches` validates the stored hash;
- signup and login responses contain no password or password hash;
- accidental `User` serialization contains no password hash;
- captured application output contains no submitted password.

Executed results:

```text
PasswordSecurityTests: 4 tests, 0 failures, 0 errors
Backend full suite:     10 tests, 0 failures, 0 errors
Frontend unit suite:    25 tests passed
Frontend build:         138 modules transformed, build succeeded
Gate 1 source scans:    PASS
```

The source scan found no previous demo password literals, no working password examples, no hard-coded `setPasswordHash("...")`, no login password assignment, and no password logging pattern in current source.

## Remaining gates

Gate 2 and Gate 3 remain blocked. In particular, browser bearer-token storage, complete cross-user authorization proof, runtime database/JWT defaults, and git-history secret reporting are intentionally not claimed as fixed by this gate.
