import axios from 'axios'

// 工作台数据来自本机的看板服务（默认 127.0.0.1:3300）。
// 开发环境通过 vite 的 /board-api 代理转发，避免跨域；部署时改 VITE_BOARD_BASE 即可。
const base = import.meta.env.VITE_BOARD_BASE || '/board-api'

const http = axios.create({ baseURL: base, timeout: 25000 })

http.interceptors.response.use(
  response => response.data,
  error => {
    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  }
)

export const boardApi = {
  board: () => http.get('/api/board'),
  todos: () => http.get('/api/todos'),
  addTodo: text => http.post('/api/todos', { text }),
  toggleTodo: (id, done) => http.post('/api/todos/update', { id, done }),
  deleteTodo: id => http.post('/api/todos/delete', { id }),
  moveTodo: id => http.post('/api/todos/move', { id }),
  refresh: () => http.post('/api/refresh'),
  search: q => http.get('/api/search', { params: { q } }),
  session: id => http.get('/api/session', { params: { id } })
}
