<template>
  <div class="shell">
    <router-link class="btn secondary" to="/">返回首页</router-link>
    <div class="card" style="margin-top: 16px;">
      <div class="muted">{{ post.categoryName }}</div>
      <h1>{{ post.title }}</h1>
      <p class="muted">{{ post.summary }}</p>
      <pre style="white-space: pre-wrap; line-height: 1.8;">{{ post.content }}</pre>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { publicApi } from '../api/blog'

const route = useRoute()
const post = reactive({})

onMounted(async () => {
  const res = await publicApi.post(route.params.slug)
  Object.assign(post, res.data)
})
</script>
