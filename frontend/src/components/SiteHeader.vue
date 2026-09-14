<template>
  <header class="site-header">
    <div class="site-header__inner">
      <BrandMark />

      <nav class="desktop-nav" aria-label="主导航">
        <router-link class="nav-link" to="/">首页</router-link>
        <a class="nav-link" href="#writing">文章</a>
        <a class="nav-link" href="#topics">分类</a>
        <router-link class="nav-link" to="/board">工作台</router-link>
        <a class="nav-link" href="#about">关于</a>
      </nav>

      <div class="site-header__actions">
        <router-link class="icon-button" to="/admin/login" aria-label="后台管理" title="后台管理">
          <PenLine :size="18" :stroke-width="1.7" />
        </router-link>
        <ThemeToggle />
        <button
          class="icon-button mobile-menu-button"
          type="button"
          :aria-label="menuOpen ? '关闭菜单' : '打开菜单'"
          :aria-expanded="menuOpen"
          @click="menuOpen = !menuOpen"
        >
          <X v-if="menuOpen" :size="20" />
          <Menu v-else :size="20" />
        </button>
      </div>
    </div>

    <nav v-if="menuOpen" class="mobile-nav" aria-label="移动端导航">
      <router-link to="/" @click="menuOpen = false"><span>01</span>首页</router-link>
      <a href="#writing" @click="menuOpen = false"><span>02</span>文章</a>
      <a href="#topics" @click="menuOpen = false"><span>03</span>分类</a>
      <router-link to="/board" @click="menuOpen = false"><span>04</span>工作台</router-link>
      <a href="#about" @click="menuOpen = false"><span>05</span>关于</a>
      <router-link to="/admin/login" @click="menuOpen = false"><span>06</span>后台管理</router-link>
    </nav>
  </header>
</template>

<script setup>
import { Menu, PenLine, X } from 'lucide-vue-next'
import { shallowRef } from 'vue'
import BrandMark from './BrandMark.vue'
import ThemeToggle from './ThemeToggle.vue'

const menuOpen = shallowRef(false)
</script>