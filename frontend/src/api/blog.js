import http from './http'

export const publicApi = {
  posts(params) {
    return http.get('/public/posts', { params })
  },
  post(slug) {
    return http.get(`/public/posts/${slug}`)
  },
  categories() {
    return http.get('/public/categories')
  },
  featured() {
    return http.get('/public/featured')
  }
}

export const adminApi = {
  login(data) {
    return http.post('/admin/auth/login', data)
  },
  posts(params) {
    return http.get('/admin/posts', { params })
  },
  post(id) {
    return http.get(`/admin/posts/${id}`)
  },
  createPost(data) {
    return http.post('/admin/posts', data)
  },
  updatePost(id, data) {
    return http.put(`/admin/posts/${id}`, data)
  },
  deletePost(id) {
    return http.delete(`/admin/posts/${id}`)
  },
  categories() {
    return http.get('/admin/categories')
  },
  createCategory(data) {
    return http.post('/admin/categories', data)
  },
  updateCategory(id, data) {
    return http.put(`/admin/categories/${id}`, data)
  },
  deleteCategory(id) {
    return http.delete(`/admin/categories/${id}`)
  }
}
