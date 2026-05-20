<template>
  <Teleport to="body">
    <div v-if="open" class="confirmation-backdrop" @click.self="$emit('cancel')">
      <section
        ref="dialogRef"
        class="confirmation-dialog"
        :class="{ danger: tone === 'danger' }"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="titleId"
        :aria-describedby="messageId"
        tabindex="-1"
        @keydown.esc.prevent="$emit('cancel')"
      >
        <div class="confirmation-dialog-copy">
          <p class="eyebrow">{{ eyebrow }}</p>
          <h2 :id="titleId">{{ title }}</h2>
          <p :id="messageId">{{ message }}</p>
        </div>
        <div class="confirmation-dialog-actions">
          <button class="secondary-button" type="button" @click="$emit('cancel')">
            {{ cancelLabel }}
          </button>
          <button class="primary-button" type="button" @click="$emit('confirm')">
            {{ confirmLabel }}
          </button>
        </div>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  eyebrow: {
    type: String,
    default: 'Confirm action'
  },
  title: {
    type: String,
    default: 'Continue with this action?'
  },
  message: {
    type: String,
    default: 'This change may affect storefront operations.'
  },
  confirmLabel: {
    type: String,
    default: 'Continue'
  },
  cancelLabel: {
    type: String,
    default: 'Cancel'
  },
  tone: {
    type: String,
    default: 'default'
  }
})

defineEmits(['confirm', 'cancel'])

const dialogRef = ref(null)
const titleId = `confirmation-title-${Math.random().toString(36).slice(2)}`
const messageId = `confirmation-message-${Math.random().toString(36).slice(2)}`

watch(
  () => props.open,
  async (isOpen) => {
    if (!isOpen) return
    await nextTick()
    dialogRef.value?.focus()
  }
)
</script>
