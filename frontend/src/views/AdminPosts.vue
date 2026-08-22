<template>
  <div class="card">
    <div class="toolbar">
      <button class="btn" @click="openEditor()">新增文章</button>
      <input class="input" v-model="keyword" placeholder="搜索文章" style="max-width: 260px;" />
      <button class="btn secondary" @click="load">查询</button>
    </div>

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
          <td>{{ item.title }}</td>
          <td>{{ item.categoryName }}</td>
          <td>{{ item.status }}</td>
          <td>{{ item.createdAt }}</td>
          <td>
            <button class="btn secondary" @click="openEditor(item.id)">编辑</button>
            <button class="btn danger" @click="remove(item.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="editorOpen" class="card" style="margin-top: 16px;">
      <h3>{{ form.id ? '编辑文章' : '新增文章' }}</h3>
      <div class="grid grid-2">
        <input class="input" v-model="form.title" placeholder="标题" />
        <select class="select" v-model="form.categoryId">
          <option :value="null">选择分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
      </div>
      <div class="grid grid-2" style="margin-top: 12px;">
        <select class="select" v-model="form.status">
          <option value="DRAFT">草稿</option>
          <option value="PUBLISHED">发布</option>
        </select>
        <input class="input" v-model="form.coverImageUrl" placeholder="封面图地址" />
      </div>
      <div style="margin-top: 12px;">
        <textarea class="textarea" v-model="form.summary" placeholder="摘要"></textarea>
      </div>
      <div style="margin-top: 12px;">
        <textarea class="textarea" v-model="form.content" placeholder="正文内容"></textarea>
      </div>
      <div class="toolbar" style="margin-top: 12px;">
        <button class="btn" @click="save">保存</button>
        <button class="btn secondary" @click="editorOpen = false">取消</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { adminApi } from '../api/blog'

const posts = ref([])
const categories = ref([])
const keyword = ref('')
const editorOpen = ref(false)
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
  const res = await adminApi.posts({ page: 1, size: 50, keyword: keyword.value })
  posts.value = res.data.items
  categories.value = (await adminApi.categories()).data
}

const openEditor = async (id = null) => {
  editorOpen.value = true
  Object.assign(form, {
    id: null, title: '', summary: '', content: '', coverImageUrl: '', categoryId: null, status: 'DRAFT'
  })
  if (id) {
    const res = await adminApi.post(id)
    Object.assign(form, res.data)
    form.categoryId = categories.value.find(c => c.slug === res.data.categorySlug)?.id ?? categories.value[0]?.id ?? null
    form.status = res.data.status
  } else {
    form.categoryId = categories.value[0]?.id ?? null
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

onMounted(load)
</script>
