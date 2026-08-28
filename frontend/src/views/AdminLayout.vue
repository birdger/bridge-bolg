<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div class="admin-brand">
        <BrandMark />
      </div>
      <p class="eyebrow admin-brand__label">内容工作台</p>

      <nav class="admin-nav">
        <router-link class="admin-link" to="/admin">仪表盘</router-link>
        <router-link class="admin-link" to="/admin/posts">文章管理</router-link>
        <router-link class="admin-link" to="/admin/categories">分类管理</router-link>
        <router-link class="admin-link ghost" to="/">回到前台</router-link>
      </nav>

      <div class="admin-sidebar__foot">
        <ThemeToggle />
        <span class="muted">浅色 / 深色</span>
      </div>
    </aside>

    <main class="admin-main">
      <header class="admin-topbar">
        <div>
          <p class="eyebrow">编辑与发布</p>
          <h2>个人博客内容中心</h2>
        </div>
        <router-link class="button button--quiet" to="/">查看前台</router-link>
      </header>

      <router-view />
    </main>
  </div>
</template>

<script setup>
import BrandMark from '../components/BrandMark.vue'
import ThemeToggle from '../components/ThemeToggle.vue'
</script>