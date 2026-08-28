<script setup>
import { ArrowRight, FileText, Layers, LogIn } from 'lucide-vue-next'

const cards = [
  {
    label: '总览',
    title: '个人博客管理系统',
    text: '在这里管理文章、分类和后续的发布节奏。',
    icon: FileText
  },
  {
    label: '登录状态',
    title: '已登录',
    text: '本地 token 会话已就绪，可以开始编辑内容。',
    icon: LogIn
  },
  {
    label: '快捷入口',
    title: '快速开始',
    text: '先去管理文章，再补分类和更完整的内容组织。',
    icon: Layers,
    actions: [
      { to: '/admin/posts', label: '去管理文章' },
      { to: '/admin/categories', label: '去管理分类' }
    ]
  }
]
</script>

<template>
  <div class="dashboard-grid">
    <article v-for="card in cards" :key="card.label" class="dashboard-card">
      <div class="dashboard-card__head">
        <p class="eyebrow">{{ card.label }}</p>
        <component :is="card.icon" :size="18" :stroke-width="1.7" />
      </div>
      <h3>{{ card.title }}</h3>
      <p>{{ card.text }}</p>
      <div v-if="card.actions" class="dashboard-actions">
        <router-link
          v-for="action in card.actions"
          :key="action.to"
          class="button button--quiet"
          :to="action.to"
        >
          {{ action.label }} <ArrowRight :size="15" />
        </router-link>
      </div>
    </article>
  </div>
</template>