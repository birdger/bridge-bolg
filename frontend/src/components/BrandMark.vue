<template>
  <router-link class="brand-lockup" :class="{ 'brand-lockup--compact': compact }" to="/">
    <svg class="brand-mark__symbol" viewBox="0 0 40 40" aria-hidden="true">
      <circle cx="20" cy="20" r="15.5" />
      <circle cx="20" cy="20" r="10.5" />
      <circle cx="20" cy="20" r="5.5" />
      <path d="M20 20 L20 6 M20 20 L27 14 M20 20 L13 14 M20 20 L25 27" />
    </svg>
    <span class="brand-mark__word">
      <span>Birdger</span>
      <span>NOTES · 乔木</span>
    </span>
  </router-link>
</template>

<script setup>
defineProps({
  compact: { type: Boolean, default: false }
})
</script>