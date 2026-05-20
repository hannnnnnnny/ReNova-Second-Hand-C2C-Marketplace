import { reactive } from 'vue'

const DEFAULT_CONFIRMATION = {
  open: false,
  eyebrow: 'Confirm action',
  title: 'Continue with this action?',
  message: 'This change may affect storefront operations.',
  confirmLabel: 'Continue',
  cancelLabel: 'Cancel',
  tone: 'default'
}

export function useConfirmDialog(defaults = {}) {
  const confirmation = reactive({ ...DEFAULT_CONFIRMATION, ...defaults })
  let resolveConfirmation = null

  function requestConfirmation(options = {}) {
    if (resolveConfirmation) {
      resolveConfirmation(false)
    }

    Object.assign(confirmation, DEFAULT_CONFIRMATION, defaults, options, { open: true })

    return new Promise((resolve) => {
      resolveConfirmation = resolve
    })
  }

  function confirm() {
    close(true)
  }

  function cancel() {
    close(false)
  }

  function close(result) {
    confirmation.open = false
    if (!resolveConfirmation) return
    resolveConfirmation(result)
    resolveConfirmation = null
  }

  return {
    confirmation,
    requestConfirmation,
    confirm,
    cancel
  }
}
