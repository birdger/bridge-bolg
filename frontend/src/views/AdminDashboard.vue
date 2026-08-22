<template>
  <div class="grid grid-3">
    <div class="card">
      <div class="muted">总览</div>
      <h2>个人博客管理系统</h2>
      <p>在这里管理你的文章和分类。</p>
    </div>
    <div class="card">
      <div class="muted">登录状态</div>
      <h2>已登录</h2>
      <p>当前使用本地 token 模拟登录。</p>
    </div>
    <div class="card">
      <div class="muted">快捷入口</div>
      <p><router-link to="/admin/posts">去管理文章</router-link></p>
      <p><router-link to="/admin/categories">去管理分类</router-link></p>
    </div>
  </div>
</template>
