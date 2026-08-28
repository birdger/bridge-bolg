<script setup>
import { ArrowLeft, ArrowUpRight, CalendarDays, Clock3 } from 'lucide-vue-next'
import { computed, onMounted, reactive } from 'vue'
import { useRoute } from 'vue-router'
import { publicApi } from '../api/blog'
import SiteFooter from '../components/SiteFooter.vue'
import SiteHeader from '../components/SiteHeader.vue'
import { formatDate, readingMinutes } from '../lib/format'

const route = useRoute()
const post = reactive({})

const paragraphs = computed(() =>
  (post.content || '')
    .split(/\n{2,}/)
    .map((item) => item.trim())
    .filter(Boolean)
)

const minutes = computed(() => readingMinutes(post.content))

onMounted(async () => {
  const res = await publicApi.post(route.params.slug)
  Object.assign(post, res.data)
})
</script>

<template>
  <div class="page page-article">
    <SiteHeader />

    <main id="top" class="article-page shell">
      <router-link class="back-link" to="/">
        <ArrowLeft :size="15" /> 返回首页
      </router-link>

      <article>
        <header class="article-header">
          <div class="tag-row">
            <span class="tag">{{ post.categoryName || '随笔' }}</span>
          </div>
          <h1>{{ post.title }}</h1>
          <p v-if="post.summary">{{ post.summary }}</p>

          <div class="article-header__meta">
            <span><CalendarDays :size="13" /> {{ formatDate(post.publishedAt) }}</span>
            <span><Clock3 :size="13" /> {{ minutes }} 分钟阅读</span>
          </div>
        </header>

        <div class="article-body-layout">
          <aside class="article-aside" aria-hidden="true">
            <span>BIR</span>
            <div class="article-aside__line" />
            <p>向下扎根<br />向上生长</p>
          </aside>

          <div class="prose">
            <p v-for="(block, index) in paragraphs" :key="index">{{ block }}</p>
          </div>

          <aside class="article-aside article-aside--right" aria-hidden="true">
            <span>{{ String(minutes).padStart(2, '0') }} MIN</span>
            <div class="article-aside__line" />
            <p>{{ formatDate(post.createdAt) }}</p>
          </aside>
        </div>
      </article>
    </main>

    <SiteFooter />
  </div>
</template>