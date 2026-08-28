<script setup>
import { ArrowLeft } from 'lucide-vue-next'
import { reactive, shallowRef } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../api/blog'
import ThemeToggle from '../components/ThemeToggle.vue'

const router = useRouter()
const form = reactive({ username: 'admin', password: 'admin123' })
const message = shallowRef('')
const loading = shallowRef(false)

const submit = async () => {
  loading.value = true
  message.value = ''

  try {
    const res = await adminApi.login(form)
    localStorage.setItem('admin_token', res.data.token)
    router.push('/admin')
  } catch (error) {
    message.value = error?.message || '登录失败，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="page page-auth">
    <div class="auth-actions">
      <router-link class="back-link" to="/"><ArrowLeft :size="15" /> 返回首页</router-link>
      <ThemeToggle />
    </div>

    <div class="shell auth-shell">
      <section class="auth-intro">
        <span class="eyebrow">后台入口</span>
        <h1>把文章、分类和发布节奏都放进同一个地方。</h1>
        <p class="hero-text">
          这是你的内容工作台。围绕文章、分类和发布状态，把写作到上线的路径收拢在一处。
        </p>
        <div class="auth-points">
          <span class="tag">文章管理</span>
          <span class="tag">分类整理</span>
          <span class="tag">后台发布</span>
        </div>
      </section>

      <section class="auth-panel">
        <div class="auth-panel__head">
          <div>
            <p class="eyebrow">欢迎回来</p>
            <h2>登录后台</h2>
          </div>
        </div>

        <div class="form-stack">
          <input class="input" v-model="form.username" placeholder="用户名" />
          <input class="input" v-model="form.password" type="password" placeholder="密码" />
          <button class="btn" type="button" :disabled="loading" @click="submit">
            {{ loading ? '登录中...' : '登录' }}
          </button>
          <p v-if="message" class="form-message">{{ message }}</p>
        </div>
      </section>
    </div>
  </div>
</template>