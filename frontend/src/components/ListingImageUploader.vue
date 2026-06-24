<script setup>
import { computed, onBeforeUnmount, ref } from 'vue'
import { ArrowLeft, ArrowRight, ImagePlus, LoaderCircle, Trash2 } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import { mediaApi } from '../api/endpoints'
import { apiError } from '../api/client'
import { useToastStore } from '../stores/toast'
import { createIdempotencyKey } from '../utils/idempotency'

const props = defineProps({
  modelValue: { type: Array, required: true }
})
const emit = defineEmits(['update:modelValue'])
const { t } = useI18n()
const toast = useToastStore()
const input = ref(null)
const dragging = ref(false)
const acceptedTypes = new Set(['image/jpeg', 'image/png', 'image/webp'])
const canAdd = computed(() => props.modelValue.length < 8)

function openPicker() {
  if (canAdd.value) input.value?.click()
}

async function selected(event) {
  await addFiles([...event.target.files])
  event.target.value = ''
}

async function dropped(event) {
  dragging.value = false
  await addFiles([...event.dataTransfer.files])
}

async function addFiles(files) {
  const available = 8 - props.modelValue.length
  if (files.length > available) toast.error(t('post.imageLimit'))
  for (const file of files.slice(0, available)) {
    if (!acceptedTypes.has(file.type) || file.size <= 0 || file.size > 10 * 1024 * 1024) {
      toast.error(t('post.invalidImage'))
      continue
    }

    const key = createIdempotencyKey()
    const previewUrl = URL.createObjectURL(file)
    update([...props.modelValue, { key, id: null, previewUrl, name: file.name, uploading: true }])
    try {
      const uploaded = await mediaApi.upload(file)
      replace(key, { key, id: uploaded.id, previewUrl, name: file.name, uploading: false })
    } catch (error) {
      URL.revokeObjectURL(previewUrl)
      update(props.modelValue.filter((image) => image.key !== key))
      toast.error(apiError(error))
    }
  }
}

function remove(index) {
  const image = props.modelValue[index]
  if (image?.previewUrl?.startsWith('blob:')) URL.revokeObjectURL(image.previewUrl)
  update(props.modelValue.filter((_, current) => current !== index))
}

function move(index, offset) {
  const destination = index + offset
  if (destination < 0 || destination >= props.modelValue.length) return
  const next = [...props.modelValue]
  ;[next[index], next[destination]] = [next[destination], next[index]]
  update(next)
}

function replace(key, replacement) {
  update(props.modelValue.map((image) => image.key === key ? replacement : image))
}

function update(images) {
  emit('update:modelValue', images)
}

onBeforeUnmount(() => {
  props.modelValue.forEach((image) => {
    if (image.previewUrl?.startsWith('blob:')) URL.revokeObjectURL(image.previewUrl)
  })
})
</script>

<template>
  <div class="listing-image-uploader">
    <input
      ref="input"
      class="sr-only"
      type="file"
      accept="image/jpeg,image/png,image/webp"
      multiple
      @change="selected"
    />

    <button
      class="image-dropzone"
      :class="{ 'is-dragging': dragging }"
      :disabled="!canAdd"
      type="button"
      @click="openPicker"
      @dragenter.prevent="dragging = true"
      @dragover.prevent="dragging = true"
      @dragleave.prevent="dragging = false"
      @drop.prevent="dropped"
    >
      <ImagePlus :size="28" aria-hidden="true" />
      <strong>{{ t('post.addPhotos') }}</strong>
      <span>{{ t('post.imagesHint') }}</span>
    </button>

    <div v-if="modelValue.length" class="image-preview-grid">
      <article v-for="(image, index) in modelValue" :key="image.key || image.id" class="image-preview-item">
        <img :src="image.previewUrl" :alt="image.name || `${t('post.photo')} ${index + 1}`" />
        <div v-if="image.uploading" class="image-uploading" :aria-label="t('post.uploading')">
          <LoaderCircle class="spin" :size="24" aria-hidden="true" />
        </div>
        <span v-if="index === 0" class="image-cover-badge">{{ t('post.cover') }}</span>
        <div class="image-preview-actions">
          <button type="button" :title="t('post.moveLeft')" :disabled="index === 0" @click="move(index, -1)">
            <ArrowLeft :size="17" aria-hidden="true" />
          </button>
          <button type="button" :title="t('post.moveRight')" :disabled="index === modelValue.length - 1" @click="move(index, 1)">
            <ArrowRight :size="17" aria-hidden="true" />
          </button>
          <button type="button" :title="t('common.delete')" @click="remove(index)">
            <Trash2 :size="17" aria-hidden="true" />
          </button>
        </div>
      </article>
    </div>
  </div>
</template>
