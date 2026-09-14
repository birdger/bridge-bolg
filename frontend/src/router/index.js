import { createRouter, createWebHistory } from 'vue-router'
import BlogHome from '../views/BlogHome.vue'
import BlogPost from '../views/BlogPost.vue'
import AdminLogin from '../views/AdminLogin.vue'
import AdminLayout from '../views/AdminLayout.vue'
import AdminDashboard from '../views/AdminDashboard.vue'
import AdminPosts from '../views/AdminPosts.vue'
import AdminCategories from '../views/AdminCategories.vue'
import BoardView from '../views/BoardView.vue'

const routes = [
  { path: '/', component: BlogHome },
  { path: '/board', component: BoardView },
  { path: '/post/:slug', component: BlogPost },
  { path: '/admin/login', component: AdminLogin },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      { path: '', component: AdminDashboard },
      { path: 'posts', component: AdminPosts },
      { path: 'categories', component: AdminCategories }
    ]
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
