<script setup>
import { onMounted, reactive, shallowRef } from 'vue'
import { adminApi } from '../api/blog'

const categories = shallowRef([])
const form = reactive({
  id: null,
  name: '',
  description: '',
  enabled: true
})

const load = async () => {
  categories.value = (await adminApi.categories()).data || []
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

<template>
  <div class="admin-section">
    <div class="section-head compact">
      <div>
        <p class="eyebrow">分类管理</p>
        <h2>整理文章的入口结构</h2>
      </div>
      <button class="btn" type="button" @click="resetForm">新增分类</button>
    </div>

    <div class="table-shell">
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
            <td><strong>{{ item.name }}</strong></td>
            <td>{{ item.slug }}</td>
            <td>{{ item.postCount }}</td>
            <td>
              <div class="inline-actions">
                <button class="btn secondary" type="button" @click="edit(item)">编辑</button>
                <button class="btn danger" type="button" @click="remove(item.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <section class="editor-panel">
      <div class="editor-panel__head">
        <div>
          <p class="eyebrow">{{ form.id ? '编辑分类' : '新增分类' }}</p>
          <h3>{{ form.id ? '编辑分类' : '创建分类' }}</h3>
        </div>
      </div>

      <div class="grid grid-2">
        <input class="input" v-model="form.name" placeholder="分类名称" />
        <select class="select" v-model="form.enabled">
          <option :value="true">启用</option>
          <option :value="false">停用</option>
        </select>
      </div>

      <textarea class="textarea" v-model="form.description" placeholder="分类描述"></textarea>

      <div class="toolbar">
        <button class="btn" type="button" @click="save">保存</button>
        <button class="btn secondary" type="button" @click="resetForm">清空</button>
      </div>
    </section>
  </div>
</template>