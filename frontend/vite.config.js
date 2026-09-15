import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 知识库（AIO·知识库本机服务，默认 3200）。连同它自带的 /lib/* 一起转发，
      // 这样博客可以直接复用它的 marked / DOMPurify，不必新增 npm 依赖。
      '/kb-api': {
        target: process.env.KB_ORIGIN || 'http://127.0.0.1:3200',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/kb-api/, '')
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 每日工作台（本机看板服务，默认 3300）。用同一源的 /board-api 转发，
      // 这样浏览器不会遇到跨域问题，部署时也可以由 nginx 做同样的反代。
      '/board-api': {
        target: process.env.BOARD_ORIGIN || 'http://127.0.0.1:3300',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/board-api/, '')
      }
    }
  }
})
