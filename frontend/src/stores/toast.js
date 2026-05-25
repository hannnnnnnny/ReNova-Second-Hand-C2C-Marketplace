import { defineStore } from 'pinia'

let nextId = 1

export const useToastStore = defineStore('toast', {
  state: () => ({ items: [] }),
  actions: {
    push(message, type = 'info', durationMs = 3500) {
      if (!message) return
      const id = nextId++
      this.items.push({ id, message, type })
      setTimeout(() => this.dismiss(id), durationMs)
    },
    success(message) { this.push(message, 'success') },
    error(message) { this.push(message, 'error', 5000) },
    info(message) { this.push(message, 'info') },
    dismiss(id) { this.items = this.items.filter((t) => t.id !== id) }
  }
})
