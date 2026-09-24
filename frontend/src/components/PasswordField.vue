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
.password-field { display: grid; gap: 8px; margin-bottom: 20px; }
label { color: var(--text); font-size: 14px; font-weight: 600; }
.password-control { position: relative; }
input {
  width: 100%; min-width: 0; height: 50px; padding: 0 52px 0 16px;
  border: 1px solid transparent; border-radius: var(--radius); background: var(--bg); color: var(--text); font: inherit; font-size: 17px;
  transition: border-color var(--dur-fast) var(--ease), box-shadow var(--dur-fast) var(--ease), background-color var(--dur-fast) var(--ease);
}
input:focus { outline: none; border-color: var(--primary); background: var(--bg-elevated); box-shadow: var(--focus-ring); }
button {
  position: absolute; top: 50%; right: 5px; width: 38px; height: 38px; transform: translateY(-50%);
  display: grid; place-items: center; border: 0; border-radius: 50%; color: var(--text-soft); background: transparent; cursor: pointer;
  transition: background-color var(--dur-fast) var(--ease), color var(--dur-fast) var(--ease);
}
button:hover { color: var(--text); background: rgba(0, 0, 0, 0.05); }
button:active { transform: translateY(-50%) scale(0.94); }
button:focus-visible { outline: 2px solid var(--primary); outline-offset: 1px; }
.password-help { color: var(--text-soft); font-size: 12px; }
</style>
