<template>
  <article class="article-card" :class="{ 'article-card--featured': featured }">
    <div v-if="featured" class="article-card__visual">
      <img v-if="article.coverImageUrl" class="article-card__cover" :src="article.coverImageUrl" :alt="article.title" />
      <template v-else>
        <span class="topography topography--one" />
        <span class="topography topography--two" />
        <span class="topography topography--three" />
        <span class="article-card__number">{{ padIndex }}</span>
      </template>
    </div>

    <div class="article-card__content">
      <div class="content-meta">
        <time :datetime="article.publishedAt">{{ formatDate(article.publishedAt) }}</time>
        <span>{{ article.categoryName || '未分类' }}</span>
      </div>
      <h3>
        <router-link :to="`/post/${article.slug}`">{{ article.title }}</router-link>
      </h3>
      <p>{{ article.summary }}</p>
      <div class="tag-row">
        <span class="tag">{{ article.categoryName || '随笔' }}</span>
      </div>
      <router-link class="text-link" :to="`/post/${article.slug}`">
        阅读全文 <ArrowUpRight :size="16" />
      </router-link>
    </div>
  </article>
</template>

<script setup>
import { ArrowUpRight } from 'lucide-vue-next'
import { computed } from 'vue'
import { formatDate } from '../lib/format'

const props = defineProps({
  article: { type: Object, required: true },
  featured: { type: Boolean, default: false },
  index: { type: Number, default: 1 }
})

const padIndex = computed(() => String(props.index).padStart(2, '0'))
</script>