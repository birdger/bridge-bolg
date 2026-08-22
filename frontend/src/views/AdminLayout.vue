<template>
  <div class="shell">
    <div class="topbar">
      <div class="brand">博客后台</div>
      <div class="toolbar">
        <router-link class="btn secondary" to="/admin">仪表盘</router-link>
        <router-link class="btn secondary" to="/admin/posts">文章管理</router-link>
        <router-link class="btn secondary" to="/admin/categories">分类管理</router-link>
        <router-link class="btn secondary" to="/">前台</router-link>
      </div>
    </div>
    <router-view />
  </div>
</template>
