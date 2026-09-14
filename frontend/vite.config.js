import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
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
