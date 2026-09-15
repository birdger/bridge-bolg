import axios from 'axios'

// 知识库数据来自本机的 AIO·知识库服务（默认 127.0.0.1:3200）。
// 开发环境通过 vite 的 /kb-api 代理转发（同源，避免跨域）；部署时改 VITE_KB_BASE。
export const KB_BASE = import.meta.env.VITE_KB_BASE || '/kb-api'

const http = axios.create({ baseURL: KB_BASE, timeout: 20000 })

http.interceptors.response.use(
  (r) => r.data,
  (e) => {
    const msg = e?.response?.data?.error || e?.message || '知识库请求失败'
    return Promise.reject(new Error(msg))
  }
)

export const kbApi = {
  // { notes, folders, tags, aiConfigured, cfgPublic }
  state: () => http.get('/api/state'),
  // { note, content, history }
  note: (id) => http.get('/api/note/' + encodeURIComponent(id)),
  // { items }
  search: (q, folder = '') => http.get('/api/search', { params: { q, folder } }),
  // { items }
  backlinks: (id) => http.get('/api/backlinks/' + encodeURIComponent(id))
}

// 复用知识库自带的 markdown 渲染管线（marked + DOMPurify + highlight.js），
// 避免给博客新增 npm 依赖，也让两边的渲染结果完全一致。
const LIBS = [
  { src: KB_BASE + '/lib/marked.min.js', global: 'marked' },
  { src: KB_BASE + '/lib/purify.min.js', global: 'DOMPurify' }
]

function loadScript(src) {
  return new Promise((resolve, reject) => {
    const found = document.querySelector('script[data-ext="' + src + '"]')
    if (found) return resolve()
    const s = document.createElement('script')
    s.src = src
    s.async = true
    s.dataset.ext = src
    s.onload = () => resolve()
    s.onerror = () => reject(new Error('无法加载 ' + src))
    document.head.appendChild(s)
  })
}

let libsPromise = null

export function ensureMarkdownLibs() {
  if (!libsPromise) {
    libsPromise = Promise.all(LIBS.map((l) => loadScript(l.src))).catch((e) => {
      libsPromise = null
      throw e
    })
  }
  return libsPromise
}

// 极简降级渲染：拿不到 marked 时至少不把原文糊成一坨
function fallbackRender(md) {
  const esc = (s) =>
    String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]))
  return esc(md)
    .split(/\r?\n{2,}/)
    .map((p) => '<p>' + p.replace(/\r?\n/g, '<br>') + '</p>')
    .join('')
}

// [[双链]] → 可点击的站内链接
function linkifyWikilinks(md) {
  return String(md ?? '').replace(/\[\[([^\]|]+)(?:\|([^\]]+))?\]\]/g, (_, target, alias) => {
    const t = target.trim()
    return '<a class="kb-wikilink" data-title="' + t + '">' + (alias || t).trim() + '</a>'
  })
}

export async function renderMarkdown(md) {
  const prepared = linkifyWikilinks(md)
  try {
    await ensureMarkdownLibs()
    const M = window.marked
    const raw = typeof M?.parse === 'function' ? M.parse(prepared) : M(prepared)
    const clean = window.DOMPurify ? window.DOMPurify.sanitize(raw) : raw
    return clean
  } catch (e) {
    return fallbackRender(prepared)
  }
}
