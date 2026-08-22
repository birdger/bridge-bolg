<template>
  <div class="card">
    <div class="toolbar">
      <button class="btn" @click="resetForm">新增分类</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>名称</th>
          <th>Slug</th>
          <th>文章数</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in categories" :key="item.id">
          <td>{{ item.name }}</td>
          <td>{{ item.slug }}</td>
          <td>{{ item.postCount }}</td>
          <td>
            <button class="btn secondary" @click="edit(item)">编辑</button>
            <button class="btn danger" @click="remove(item.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="card" style="margin-top: 16px;">
      <h3>{{ form.id ? '编辑分类' : '新增分类' }}</h3>
      <div class="grid grid-2">
        <input class="input" v-model="form.name" placeholder="分类名称" />
        <select class="select" v-model="form.enabled">
          <option :value="true">启用</option>
          <option :value="false">停用</option>
        </select>
      </div>
      <div style="margin-top: 12px;">
        <textarea class="textarea" v-model="form.description" placeholder="分类描述"></textarea>
      </div>
      <div class="toolbar" style="margin-top: 12px;">
        <button class="btn" @click="save">保存</button>
        <button class="btn secondary" @click="resetForm">清空</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { adminApi } from '../api/blog'

const categories = ref([])
const form = reactive({
  id: null,
  name: '',
  description: '',
  enabled: true
})

const load = async () => {
  categories.value = (await adminApi.categories()).data
}

const resetForm = () => {
  Object.assign(form, { id: null, name: '', description: '', enabled: true })
}

const edit = (item) => {
  Object.assign(form, item)
}

const save = async () => {
  if (form.id) {
    await adminApi.updateCategory(form.id, form)
  } else {
    await adminApi.createCategory(form)
  }
  resetForm()
  await load()
}

const remove = async (id) => {
  await adminApi.deleteCategory(id)
  await load()
}

onMounted(load)
</script>
