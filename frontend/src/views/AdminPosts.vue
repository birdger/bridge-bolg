<script setup>
import { onMounted, reactive, shallowRef } from 'vue'
import { adminApi } from '../api/blog'
import { formatDate } from '../lib/format'

const posts = shallowRef([])
const categories = shallowRef([])
const keyword = shallowRef('')
const editorOpen = shallowRef(false)
const form = reactive({
  id: null,
  title: '',
  summary: '',
  content: '',
  coverImageUrl: '',
  categoryId: null,
  status: 'DRAFT'
})

const load = async () => {
  const [postRes, categoryRes] = await Promise.all([
    adminApi.posts({ page: 1, size: 50, keyword: keyword.value }),
    adminApi.categories()
  ])

  posts.value = postRes.data.items || []
  categories.value = categoryRes.data || []
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    title: '',
    summary: '',
    content: '',
    coverImageUrl: '',
    categoryId: categories.value[0]?.id ?? null,
    status: 'DRAFT'
  })
}

const openEditor = async (id = null) => {
  editorOpen.value = true
  resetForm()

  if (id) {
    const res = await adminApi.post(id)
    Object.assign(form, res.data)
    form.categoryId = categories.value.find(item => item.slug === res.data.categorySlug)?.id ?? categories.value[0]?.id ?? null
    form.status = res.data.status
  }
}

const save = async () => {
  const payload = { ...form }

  if (payload.id) {
    await adminApi.updatePost(payload.id, payload)
  } else {
    await adminApi.createPost(payload)
  }

  editorOpen.value = false
  await load()
}

const remove = async (id) => {
  await adminApi.deletePost(id)
  await load()
}

onMounted(async () => {
  await load()
})
</script>

<template>
  <div class="admin-section">
    <div class="section-head compact">
      <div>
        <p class="eyebrow">文章管理</p>
        <h2>管理你的内容发布</h2>
      </div>
      <div class="section-actions">
        <button class="btn" type="button" @click="openEditor()">新增文章</button>
      </div>
    </div>

    <div class="filters-surface admin-filters">
      <input class="input" v-model="keyword" placeholder="搜索文章" />
      <button class="btn secondary" type="button" @click="load">查询</button>
    </div>

    <div class="table-shell">
      <table class="table">
        <thead>
          <tr>
            <th>标题</th>
            <th>分类</th>
            <th>状态</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in posts" :key="item.id">
            <td><strong>{{ item.title }}</strong></td>
            <td>{{ item.categoryName || '未分类' }}</td>
            <td>
              <span class="tag" :class="item.status === 'PUBLISHED' ? 'tag--published' : ''">{{ item.status }}</span>
            </td>
            <td>{{ formatDate(item.createdAt) }}</td>
            <td>
              <div class="inline-actions">
                <button class="btn secondary" type="button" @click="openEditor(item.id)">编辑</button>
                <button class="btn danger" type="button" @click="remove(item.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <section v-if="editorOpen" class="editor-panel">
      <div class="editor-panel__head">
        <div>
          <p class="eyebrow">{{ form.id ? '编辑文章' : '新增文章' }}</p>
          <h3>{{ form.id ? '编辑文章' : '创建新文章' }}</h3>
        </div>
      </div>

      <div class="grid grid-2">
        <input class="input" v-model="form.title" placeholder="标题" />
        <select class="select" v-model="form.categoryId">
          <option :value="null">选择分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
      </div>

      <div class="grid grid-2">
        <select class="select" v-model="form.status">
          <option value="DRAFT">草稿</option>
          <option value="PUBLISHED">发布</option>
        </select>
        <input class="input" v-model="form.coverImageUrl" placeholder="封面图地址" />
      </div>

      <textarea class="textarea" v-model="form.summary" placeholder="摘要"></textarea>
      <textarea class="textarea textarea--tall" v-model="form.content" placeholder="正文内容"></textarea>

      <div class="toolbar">
        <button class="btn" type="button" @click="save">保存</button>
        <button class="btn secondary" type="button" @click="editorOpen = false">取消</button>
      </div>
    </section>
  </div>
</template>