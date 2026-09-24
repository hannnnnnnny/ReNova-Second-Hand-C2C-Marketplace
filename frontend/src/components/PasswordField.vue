<script setup>
import { ref } from 'vue'
import { Eye, EyeOff } from 'lucide-vue-next'

defineProps({
  id: { type: String, required: true },
  label: { type: String, required: true },
  autocomplete: { type: String, required: true },
  help: { type: String, default: '' },
  minlength: { type: Number, default: undefined },
  maxlength: { type: Number, default: undefined }
})

const model = defineModel({ type: String, required: true })
const visible = ref(false)
</script>

<template>
  <div class="password-field">
    <label :for="id">{{ label }}</label>
    <div class="password-control">
      <input
        :id="id" v-model="model" name="password" :type="visible ? 'text' : 'password'"
        :autocomplete="autocomplete" :minlength="minlength" :maxlength="maxlength" required
      />
      <button
        type="button" :aria-label="$t(visible ? 'authUi.hidePassword' : 'authUi.showPassword')"
        :aria-pressed="visible" @click="visible = !visible"
      >
        <EyeOff v-if="visible" :size="18" aria-hidden="true" />
        <Eye v-else :size="18" aria-hidden="true" />
      </button>
    </div>
    <span v-if="help" class="password-help">{{ help }}</span>
  </div>
</template>

<style scoped>
.password-field { display: grid; gap: 7px; margin-bottom: 17px; }
label { color: var(--text); font-size: 13px; font-weight: 750; }
.password-control { position: relative; }
input {
  width: 100%; min-width: 0; height: 46px; padding: 0 48px 0 13px;
  border: 1px solid var(--border-strong); border-radius: 11px; background: var(--bg-elevated); color: var(--text); font: inherit;
}
input:focus { outline: 3px solid var(--primary-soft); border-color: var(--primary); }
button {
  position: absolute; top: 50%; right: 5px; width: 38px; height: 38px; transform: translateY(-50%);
  display: grid; place-items: center; border: 0; border-radius: 9px; color: var(--text-soft); background: transparent; cursor: pointer;
}
button:hover { color: var(--primary-strong); background: var(--primary-soft); }
button:focus-visible { outline: 2px solid var(--primary); outline-offset: 1px; }
.password-help { color: var(--text-soft); font-size: 12px; }
</style>
