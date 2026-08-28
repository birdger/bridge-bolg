<script setup>
import { ArrowDown, ArrowUpRight, Search } from 'lucide-vue-next'
import { computed, onMounted, shallowRef } from 'vue'
import { publicApi } from '../api/blog'
import ArticleCard from '../components/ArticleCard.vue'
import GrowthArt from '../components/GrowthArt.vue'
import SectionHeading from '../components/SectionHeading.vue'
import SiteFooter from '../components/SiteFooter.vue'
import SiteHeader from '../components/SiteHeader.vue'

const posts = shallowRef([])
const categories = shallowRef([])
const featured = shallowRef([])
const keyword = shallowRef('')
const categorySlug = shallowRef('')
const loading = shallowRef(false)

const featuredLead = computed(() => featured.value[0] ?? posts.value[0] ?? null)
const featuredList = computed(() => featured.value.slice(1, 4))
const latestPosts = computed(() => posts.value.slice(0, 6))

const stats = computed(() => [
  { label: '文章', value: posts.value.length },
  { label: '分类', value: categories.value.length },
  { label: '精选', value: featured.value.length }
])

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await publicApi.posts({
      page: 1,
      size: 20,
      keyword: keyword.value,
      categorySlug: categorySlug.value
    })
    posts.value = res.data.items || []
  } finally {
    loading.value = false
  }
}

const applyCategory = (slug) => {
  categorySlug.value = slug
  loadPosts()
  const target = document.getElementById('writing')
  if (target) target.scrollIntoView({ behavior: 'smooth' })
}

const loadPage = async () => {
  const [categoryRes, featuredRes] = await Promise.all([
    publicApi.categories(),
    publicApi.featured()
  ])

  categories.value = categoryRes.data || []
  featured.value = featuredRes.data || []
  await loadPosts()
}

onMounted(loadPage)
</script>

<template>
  <div class="page page-home">
    <SiteHeader />

    <main id="top">
      <section class="hero shell">
        <div class="hero__copy">
          <span class="eyebrow">个人博客 · 技术 / 生活 / 复盘</span>
          <h1>
            <span>把每一次思考，</span>
            <em>都长成值得翻回的页面</em>
          </h1>
          <p>
            这里是 Birdger 的个人站点，收纳技术笔记、项目复盘与零碎灵感。
            先让阅读体验安静、清楚、耐看，内容再慢慢填满。
          </p>

          <div class="hero__actions">
            <router-link
              v-if="featuredLead"
              class="button button--primary"
              :to="`/post/${featuredLead.slug}`"
            >
              阅读精选 <ArrowUpRight :size="17" />
            </router-link>
            <button class="button button--quiet" type="button" @click="loadPosts">
              刷新文章 <ArrowDown :size="16" />
            </button>
          </div>

          <div class="stat-row">
            <div v-for="item in stats" :key="item.label">
              <strong>{{ item.value }}</strong>
              <span>{{ item.label }}</span>
            </div>
          </div>
        </div>

        <GrowthArt />
      </section>

      <section v-if="featuredLead" id="featured" class="home-section shell">
        <SectionHeading index="01" title="精选文章" />
        <ArticleCard :article="featuredLead" :featured="true" :index="1" />
      </section>

      <section id="writing" class="home-section shell">
        <SectionHeading index="02" title="最近更新" />

        <div class="filters-surface">
          <label class="search-field">
            <Search :size="16" :stroke-width="1.7" />
            <input class="input" v-model="keyword" placeholder="搜索文章" @keyup.enter="loadPosts" />
          </label>
          <select class="select" v-model="categorySlug">
            <option value="">全部分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.slug">{{ cat.name }}</option>
          </select>
          <button class="btn" type="button" @click="loadPosts">搜索</button>
        </div>

        <div v-if="loading" class="loading-cards">
          <div /><div />
        </div>

        <div v-else class="article-grid">
          <ArticleCard
            v-for="(post, index) in latestPosts"
            :key="post.id"
            :article="post"
            :index="index + 2"
          />
        </div>

        <div v-if="!loading && !latestPosts.length" class="empty-state">
          暂时还没有匹配的文章，先换一个关键词或分类试试。
        </div>
      </section>

      <section id="topics" class="home-section shell">
        <SectionHeading index="03" title="分类导航" />
        <div class="category-grid">
          <a
            v-for="(cat, index) in categories"
            :key="cat.id"
            class="category-card"
            href="#writing"
            @click.prevent="applyCategory(cat.slug)"
          >
            <span class="category-card__index">{{ String(index + 1).padStart(2, '0') }}</span>
            <div>
              <h3>{{ cat.name }}</h3>
              <p>{{ cat.description || '按主题整理的文章入口。' }}</p>
            </div>
            <ArrowUpRight :size="18" />
          </a>
          <div v-if="!categories.length" class="empty-state">还没有分类，稍后会在后台补充。</div>
        </div>
      </section>

      <section id="about" class="roots-cta shell">
        <div class="roots-cta__art" aria-hidden="true">
          <span /><span /><span /><span />
        </div>
        <div>
          <span class="eyebrow">保持根系连接</span>
          <h2>这里会慢慢长成你的个人博客</h2>
          <p>
            从一篇篇文章开始，把经验、判断和偶尔的灵光一现固定下来。
            内容由你管理，页面负责安静地把它讲清楚。
          </p>
          <div class="roots-cta__links">
            <router-link to="/admin/login">后台管理 <ArrowUpRight :size="16" /></router-link>
            <a href="https://github.com/birdger" target="_blank" rel="noreferrer">GitHub <ArrowUpRight :size="16" /></a>
          </div>
        </div>
      </section>
    </main>

    <SiteFooter />
  </div>
</template>