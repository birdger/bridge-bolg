<template>
  <div class="shell" style="max-width: 480px;">
    <div class="card">
      <h2>后台登录</h2>
      <div class="grid" style="margin-top: 16px;">
        <input class="input" v-model="form.username" placeholder="用户名" />
        <input class="input" v-model="form.password" type="password" placeholder="密码" />
        <button class="btn" @click="submit">登录</button>
        <p class="muted">{{ message }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { adminApi } from '../api/blog'

const router = useRouter()
const form = reactive({ username: 'admin', password: 'admin123' })
const message = ref('')

const submit = async () => {
  try {
    const res = await adminApi.login(form)
    localStorage.setItem('admin_token', res.data.token)
    router.push('/admin')
  } catch (error) {
    message.value = error.message
  }
}
</script>
