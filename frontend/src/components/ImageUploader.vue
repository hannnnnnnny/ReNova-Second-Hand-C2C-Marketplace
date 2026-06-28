<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { uploadApi } from '../api/endpoints'
import { apiError } from '../api/client'
import { useToastStore } from '../stores/toast'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 8 },
  acceptedTypes: {
    type: Array,
    default: () => ['image/jpeg', 'image/png', 'image/webp']
  },
  maxBytesPerFile: { type: Number, default: 5 * 1024 * 1024 }
})
const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()
const toast = useToastStore()

const fileInput = ref(null)
const dragging = ref(false)
const uploading = ref(false)

const slotsLeft = computed(() => Math.max(0, props.max - props.modelValue.length))
const isFull = computed(() => slotsLeft.value === 0)
const acceptAttr = computed(() => props.acceptedTypes.join(','))

function openPicker() {
  if (isFull.value || uploading.value) return
  fileInput.value?.click()
}

function onPick(event) {
  const list = Array.from(event.target?.files || [])
  event.target.value = '' // reset so picking the same file again still fires change
  if (list.length) handleFiles(list)
}

function onDrop(event) {
  event.preventDefault()
  dragging.value = false
  if (isFull.value || uploading.value) return
  const list = Array.from(event.dataTransfer?.files || [])
  if (list.length) handleFiles(list)
}

async function handleFiles(list) {
  // Client-side validation just for fast feedback. The backend re-runs
  // every check (magic bytes + size + count) so this UI is never the
  // only line of defense.
  const accepted = []
  for (const file of list) {
    if (accepted.length >= slotsLeft.value) {
      toast.info(t('upload.tooManyTrim', { max: props.max }))
      break
    }
    if (file.size <= 0) {
      toast.error(t('upload.errorEmpty', { name: file.name }))
      continue
    }
    if (file.size > props.maxBytesPerFile) {
      toast.error(t('upload.errorTooLarge', { name: file.name, max: Math.round(props.maxBytesPerFile / (1024 * 1024)) }))
      continue
    }
    if (file.type && !props.acceptedTypes.includes(file.type)) {
      toast.error(t('upload.errorType', { name: file.name }))
      continue
    }
    accepted.push(file)
  }
  if (!accepted.length) return

  uploading.value = true
  try {
    const result = await uploadApi.images(accepted)
    const urls = result.images.map((i) => i.url)
    emit('update:modelValue', [...props.modelValue, ...urls])
    toast.success(t('upload.uploaded', { n: urls.length }))
  } catch (err) {
    toast.error(apiError(err, t('upload.errorGeneric')))
  } finally {
    uploading.value = false
  }
}

function removeAt(index) {
  const next = props.modelValue.slice()
  next.splice(index, 1)
  emit('update:modelValue', next)
}

function moveTo(index, dir) {
  const next = props.modelValue.slice()
  const target = index + dir
  if (target < 0 || target >= next.length) return
  const tmp = next[index]
  next[index] = next[target]
  next[target] = tmp
  emit('update:modelValue', next)
}
</script>

<template>
  <div class="image-uploader">
    <!-- Drop zone / click target -->
    <div
      class="dropzone"
      :class="{ 'is-dragging': dragging, 'is-disabled': isFull || uploading }"
      role="button"
      tabindex="0"
      @click="openPicker"
      @keydown.enter.prevent="openPicker"
      @keydown.space.prevent="openPicker"
      @dragenter.prevent="dragging = true"
      @dragover.prevent="dragging = true"
      @dragleave.prevent="dragging = false"
      @drop="onDrop"
    >
      <input
        ref="fileInput" type="file"
        :accept="acceptAttr" multiple
        class="hidden-input"
        @change="onPick"
      />

      <div class="dropzone-art" aria-hidden="true">📸</div>
      <div class="dropzone-headline">
        <template v-if="uploading">{{ t('upload.uploading') }}</template>
        <template v-else-if="isFull">{{ t('upload.reachedMax', { max }) }}</template>
        <template v-else>{{ t('upload.dropOrClick') }}</template>
      </div>
      <div class="dropzone-sub">
        {{ t('upload.hint', { max: Math.round(maxBytesPerFile / (1024 * 1024)), formats: 'JPG · PNG · WebP' }) }}
        <span v-if="!isFull"> · {{ t('upload.slotsLeft', { left: slotsLeft }) }}</span>
      </div>
    </div>

    <!-- Thumbnails -->
    <div v-if="modelValue.length" class="thumbs-grid">
      <figure
        v-for="(url, i) in modelValue" :key="url + i"
        class="thumb-tile"
      >
        <div class="thumb-img" :style="{ backgroundImage: `url('${url}')` }">
          <span v-if="i === 0" class="cover-badge">{{ t('upload.coverBadge') }}</span>
        </div>
        <figcaption class="thumb-actions">
          <button type="button" class="thumb-btn" :disabled="i === 0" @click="moveTo(i, -1)" :title="t('upload.moveLeft')">←</button>
          <button type="button" class="thumb-btn" :disabled="i === modelValue.length - 1" @click="moveTo(i, 1)" :title="t('upload.moveRight')">→</button>
          <button type="button" class="thumb-btn thumb-btn-danger" @click="removeAt(i)" :title="t('upload.remove')">✕</button>
        </figcaption>
      </figure>
    </div>
  </div>
</template>

<style scoped>
.image-uploader { display: flex; flex-direction: column; gap: 16px; }

.dropzone {
  position: relative;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 6px;
  padding: 36px 24px;
  background: var(--bg-elevated);
  border: 3px dashed var(--border-strong);
  border-radius: var(--radius-lg);
  text-align: center;
  cursor: pointer;
  transition: background 160ms ease, border-color 160ms ease, transform 120ms ease;
  outline: none;
}
.dropzone:hover { background: var(--primary-soft); border-color: var(--primary-strong); }
.dropzone:focus-visible { box-shadow: 0 0 0 4px rgba(76, 175, 108, 0.25); }
.dropzone.is-dragging {
  background: var(--primary-soft);
  border-color: var(--primary-strong);
  border-style: solid;
  transform: scale(1.005);
}
.dropzone.is-disabled {
  cursor: not-allowed;
  opacity: 0.65;
  background: var(--bg-muted);
}
.hidden-input {
  position: absolute; inset: 0; opacity: 0; pointer-events: none;
}
.dropzone-art { font-size: 38px; line-height: 1; margin-bottom: 4px; }
.dropzone-headline {
  font-family: var(--font-display);
  font-weight: 800;
  font-size: 1.1rem;
  color: var(--text);
}
.dropzone-sub {
  font-size: 13px;
  color: var(--text-muted);
  max-width: 460px;
}

.thumbs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 14px;
}
.thumb-tile {
  margin: 0;
  background: var(--bg-elevated);
  border: 2px solid var(--border-strong);
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: 0 3px 0 var(--border-strong);
  display: flex; flex-direction: column;
}
.thumb-img {
  width: 100%;
  aspect-ratio: 1 / 1;
  background: var(--bg-muted) center/cover no-repeat;
  position: relative;
}
.cover-badge {
  position: absolute; top: 8px; left: 8px;
  background: var(--primary);
  color: #fff;
  font-size: 11px; font-weight: 800;
  letter-spacing: 0.06em;
  padding: 3px 9px;
  border-radius: 999px;
  border: 1.5px solid var(--primary-strong);
  text-transform: uppercase;
}
.thumb-actions {
  display: flex; gap: 4px;
  padding: 6px;
  border-top: 2px dashed var(--border);
  background: var(--bg-elevated);
}
.thumb-btn {
  flex: 1;
  background: var(--bg-elevated);
  border: 2px solid var(--border-strong);
  border-radius: 10px;
  padding: 4px 0;
  font-weight: 700;
  cursor: pointer;
  font-size: 13px;
  box-shadow: 0 2px 0 var(--border-strong);
  transition: transform 100ms ease;
}
.thumb-btn:hover:not(:disabled) { transform: translateY(-1px); }
.thumb-btn:active:not(:disabled) { transform: translateY(1px); box-shadow: 0 1px 0 var(--border-strong); }
.thumb-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.thumb-btn-danger {
  background: var(--accent-soft); border-color: #b85731; box-shadow: 0 2px 0 #b85731; color: #8b3a26;
}

@media (max-width: 760px) {
  .dropzone { padding: 26px 18px; }
  .thumbs-grid { grid-template-columns: repeat(3, 1fr); gap: 10px; }
}
</style>
