<template>
  <div class="section-heading">
    <div>
      <span>{{ index }}</span>
      <h2>{{ title }}</h2>
    </div>
    <a v-if="href && action" class="section-heading__action" :href="href" @click.prevent="onAction">
      {{ action }} <ArrowRight :size="16" />
    </a>
  </div>
</template>

<script setup>
import { ArrowRight } from 'lucide-vue-next'

const props = defineProps({
  index: { type: String, required: true },
  title: { type: String, required: true },
  href: { type: String, default: '' },
  action: { type: String, default: '' }
})

const emit = defineEmits(['action'])
const onAction = () => {
  if (props.href) window.location.hash = props.href
  emit('action')
}
</script>