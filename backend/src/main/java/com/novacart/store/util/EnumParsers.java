package com.novacart.store.util;

import com.novacart.store.exception.BusinessRuleException;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class EnumParsers {

    private EnumParsers() {}

    public static <E extends Enum<E>> E optional(Class<E> enumType, String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return required(enumType, value, fieldName);
    }

    public static <E extends Enum<E>> E required(Class<E> enumType, String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(fieldName + " is required.");
        }
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException(fieldName + " must be one of " + allowedValues(enumType) + ".");
        }
    }

    private static <E extends Enum<E>> String allowedValues(Class<E> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
