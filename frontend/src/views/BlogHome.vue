<template>
  <div class="shell">
    <div class="topbar">
      <div class="brand">个人博客</div>
      <router-link class="btn secondary" to="/admin/login">后台管理</router-link>
    </div>

    <div class="grid grid-3" style="margin-bottom: 20px;">
      <div class="card" v-for="item in featured" :key="item.id">
        <div class="muted">{{ item.categoryName || '未分类' }}</div>
        <h3>{{ item.title }}</h3>
        <p class="muted">{{ item.summary }}</p>
        <router-link :to="`/post/${item.slug}`">阅读详情</router-link>
      </div>
    </div>

    <div class="toolbar">
      <input class="input" v-model="keyword" placeholder="搜索文章" style="max-width: 260px;" />
      <select class="select" v-model="categorySlug" style="max-width: 220px;">
        <option value="">全部分类</option>
        <option v-for="cat in categories" :key="cat.id" :value="cat.slug">{{ cat.name }}</option>
      </select>
      <button class="btn" @click="loadPosts">搜索</button>
    </div>

    <div class="list">
      <div class="post-item" v-for="post in posts" :key="post.id">
        <div class="muted">{{ post.categoryName || '未分类' }}</div>
        <h2>{{ post.title }}</h2>
        <p>{{ post.summary }}</p>
        <div class="toolbar">
          <span class="muted">{{ post.publishedAt }}</span>
          <router-link class="btn secondary" :to="`/post/${post.slug}`">查看详情</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { publicApi } from '../api/blog'

const posts = ref([])
const categories = ref([])
const featured = ref([])
const keyword = ref('')
const categorySlug = ref('')

const loadPosts = async () => {
  const res = await publicApi.posts({ page: 1, size: 20, keyword: keyword.value, categorySlug: categorySlug.value })
  posts.value = res.data.items
}

onMounted(async () => {
  categories.value = (await publicApi.categories()).data
  featured.value = (await publicApi.featured()).data
  await loadPosts()
})
</script>
