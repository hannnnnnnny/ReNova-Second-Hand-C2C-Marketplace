import { describe, expect, it } from 'vitest'
import { useConfirmDialog } from './useConfirmDialog'

describe('useConfirmDialog', () => {
  it('resolves confirmation decisions and resets open state', async () => {
    const { confirmation, requestConfirmation, confirm } = useConfirmDialog()
    const decision = requestConfirmation({
      title: 'Archive product?',
      confirmLabel: 'Archive'
    })

    expect(confirmation.open).toBe(true)
    expect(confirmation.title).toBe('Archive product?')
    expect(confirmation.confirmLabel).toBe('Archive')

    confirm()

    await expect(decision).resolves.toBe(true)
    expect(confirmation.open).toBe(false)
  })

  it('cancels a pending decision when a new confirmation replaces it', async () => {
    const { requestConfirmation, cancel } = useConfirmDialog()
    const firstDecision = requestConfirmation({ title: 'First action?' })
    const secondDecision = requestConfirmation({ title: 'Second action?' })

    await expect(firstDecision).resolves.toBe(false)
    cancel()
    await expect(secondDecision).resolves.toBe(false)
  })
})
