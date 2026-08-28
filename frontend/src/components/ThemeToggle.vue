<template>
  <button
    class="icon-button theme-toggle"
    type="button"
    :aria-label="dark ? '切换到浅色' : '切换到深色'"
    :title="dark ? '切换到浅色' : '切换到深色'"
    @click="toggle"
  >
    <Sun v-if="dark" :size="18" :stroke-width="1.7" />
    <Moon v-else :size="18" :stroke-width="1.7" />
  </button>
</template>

<script setup>
import { Moon, Sun } from 'lucide-vue-next'
import { onMounted, shallowRef } from 'vue'

const dark = shallowRef(false)

const apply = (value) => {
  dark.value = value
  document.documentElement.dataset.theme = value ? 'dark' : 'light'
  try {
    localStorage.setItem('qiaomu-theme', value ? 'dark' : 'light')
  } catch (_) {
    /* ignore */
  }
}

const toggle = () => apply(!dark.value)

onMounted(() => {
  let saved = null
  try {
    saved = localStorage.getItem('qiaomu-theme')
  } catch (_) {
    /* ignore */
  }
  if (saved) {
    apply(saved === 'dark')
  } else {
    apply(window.matchMedia('(prefers-color-scheme: dark)').matches)
  }
})
</script>