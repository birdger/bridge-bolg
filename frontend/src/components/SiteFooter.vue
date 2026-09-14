<template>
  <footer class="site-footer">
    <div class="site-footer__top">
      <div>
        <BrandMark />
        <p>向下扎根，向上生长。把技术笔记、项目复盘和生活灵感，慢慢长成一个值得翻回来的站点。</p>
      </div>
      <div class="site-footer__links">
        <router-link to="/">首页</router-link>
        <a href="#writing">文章</a>
        <a href="#topics">分类</a>
        <router-link to="/board">工作台</router-link>
        <router-link to="/admin/login">后台管理</router-link>
      </div>
      <div class="site-footer__social">
        <a :href="githubUrl" target="_blank" rel="noreferrer" aria-label="GitHub">
          <Github :size="17" />
        </a>
        <a href="#top" aria-label="回到顶部">
          <ArrowUp :size="17" />
        </a>
      </div>
    </div>
    <div class="site-footer__bottom">
      <span>© {{ year }} Birdger · 乔木笔记</span>
      <a href="#top">回到顶部 <ArrowUpRight :size="14" /></a>
    </div>
  </footer>
</template>

<script setup>
import { ArrowUp, ArrowUpRight, Github } from 'lucide-vue-next'
import BrandMark from './BrandMark.vue'

const year = new Date().getUTCFullYear()
const githubUrl = 'https://github.com/birdger'
</script>