<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ArrowUpRight, BookOpen, Folder, Search, Sparkles, Tag } from 'lucide-vue-next'
import SiteHeader from '../components/SiteHeader.vue'
import SiteFooter from '../components/SiteFooter.vue'
import SectionHeading from '../components/SectionHeading.vue'
import { kbApi, renderMarkdown } from '../api/kb'

const KbStandalone = 'http://127.0.0.1:3200'
const SiyuanUrl = 'http://127.0.0.1:6806'

const loading = ref(true)
const offline = ref(false)
const offlineMsg = ref('')
const notes = ref([])
const folders = ref([])
const tags = ref([])

const q = ref('')
const folder = ref('')
const activeTag = ref('')
const hits = ref(null) // 全文检索结果（有 q 时用服务端检索）

const activeId = ref('')
const note = ref(null)
const content = ref('')
const html = ref('')
const backlinks = ref([])

const stats = computed(() => [
  { value: notes.value.length, label: '篇笔记' },
  { value: folders.value.length, label: '个文件夹' },
  { value: tags.value.length, label: '个标签' },
  { value: lastUpdated.value, label: '最近更新' }
])

const lastUpdated = computed(() => {
  const t = notes.value.reduce((m, n) => Math.max(m, n.updated || 0), 0)
  if (!t) return '—'
  const d = new Date(t)
  return `${d.getMonth() + 1}/${d.getDate()}`
})

// 有搜索词时用服务端全文检索，否则用本地元数据过滤（秒开，不打扰服务）
const shown = computed(() => {
  let list = hits.value || notes.value
  if (!hits.value) {
    if (folder.value) list = list.filter((n) => (n.folder || '') === folder.value || (n.folder || '').startsWith(folder.value + '/'))
    if (activeTag.value) list = list.filter((n) => (n.tags || []).includes(activeTag.value))
    const kw = q.value.trim().toLowerCase()
    if (kw) list = list.filter((n) => (n.title || '').toLowerCase().includes(kw) || (n.tags || []).join(' ').toLowerCase().includes(kw))
  }
  return list
})

function fmtDate(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const p = (x) => String(x).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

async function loadState() {
  loading.value = true
  try {
    const s = await kbApi.state()
    notes.value = s.notes || []
    folders.value = s.folders || []
    tags.value = s.tags || []
    offline.value = false
    if (notes.value.length && !activeId.value) {
      const first = notes.value.find((n) => n.pinned) || notes.value[0]
      await openNote(first)
    }
  } catch (e) {
    offline.value = true
    offlineMsg.value = e.message
  } finally {
    loading.value = false
  }
}

async function openNote(n) {
  if (!n) return
  activeId.value = n.id
  try {
    const r = await kbApi.note(n.id)
    note.value = r.note || null
    content.value = r.content || ''
    html.value = await renderMarkdown(content.value)
    backlinks.value = []
    kbApi.backlinks(n.id).then((b) => (backlinks.value = b.items || [])).catch(() => {})
    requestAnimationFrame(() => {
      const el = document.querySelector('.kb-read')
      if (el && window.innerWidth < 900) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    })
  } catch (e) {
    note.value = null
    html.value = ''
  }
}

// [[双链]] 点击 → 找同名笔记跳过去
function onContentClick(e) {
  const a = e.target.closest('.kb-wikilink')
  if (!a) return
  e.preventDefault()
  const title = (a.dataset.title || '').trim()
  const target =
    notes.value.find((n) => (n.title || '').trim() === title) ||
    notes.value.find((n) => (n.title || '').includes(title))
  if (target) openNote(target)
  else q.value = title
}

let timer = null
watch(q, (v) => {
  clearTimeout(timer)
  const kw = v.trim()
  if (!kw) {
    hits.value = null
    return
  }
  timer = setTimeout(async () => {
    try {
      const r = await kbApi.search(kw)
      hits.value = r.items || []
    } catch {
      hits.value = null
    }
  }, 300)
})

watch([folder, activeTag], () => {
  q.value = ''
  hits.value = null
})

function pickTag(t) {
  activeTag.value = activeTag.value === t ? '' : t
}

onMounted(loadState)
</script>

<template>
  <div class="page page-kb">
    <SiteHeader />

    <main id="top">
      <section class="shell kb-hero">
        <span class="eyebrow">知识库 · 第二大脑</span>
        <h1><span>读过的、想明白的，</span><em>都留在这一处</em></h1>
        <p>
          和 AIO·知识库（本机 :3200）是同一批数据：笔记、文件夹、标签、双链都能在这里直接翻。
          原始编辑、图谱和 AI 助手仍在独立版里。
        </p>
        <div class="stat-row">
          <div v-for="s in stats" :key="s.label">
            <strong>{{ s.value }}</strong><span>{{ s.label }}</span>
          </div>
        </div>
      </section>

      <section v-if="offline" class="shell">
        <div class="kb-offline">
          <h3>知识库服务没有启动</h3>
          <p>{{ offlineMsg }}</p>
          <p class="kb-offline__hint">
            双击 <code>G:\code\knowledge\一键启动知识库们.bat</code>，或运行
            <code>node G:\code\knowledge\aio-kb\server.js</code>，然后刷新本页。
          </p>
        </div>
      </section>

      <section v-else class="shell kb-body">
        <aside class="kb-side">
          <label class="kb-search">
            <Search :size="15" />
            <input v-model="q" type="text" placeholder="搜索标题 / 标签 / 全文…" />
          </label>

          <div class="kb-chips" v-if="folders.length">
            <button :class="{ on: !folder }" @click="folder = ''; activeTag = ''">全部</button>
            <button v-for="f in folders" :key="f" :class="{ on: folder === f }" @click="folder = f; activeTag = ''">
              <Folder :size="12" />{{ f }}
            </button>
          </div>

          <div class="kb-chips kb-chips--tags" v-if="tags.length">
            <button
              v-for="t in tags"
              :key="t.name"
              :class="{ on: activeTag === t.name }"
              @click="pickTag(t.name)"
            >
              <Tag :size="11" />{{ t.name }}<span>{{ t.count }}</span>
            </button>
          </div>

          <p class="kb-count">
            {{ shown.length }} 篇<span v-if="hits">（全文检索「{{ q }}」）</span>
          </p>

          <ul class="kb-list">
            <li
              v-for="n in shown"
              :key="n.id"
              :class="{ on: n.id === activeId }"
              @click="openNote(n)"
            >
              <strong>{{ n.title }}</strong>
              <span class="kb-item__meta">
                {{ n.pinned ? '📌 ' : '' }}{{ (n.tags || []).join(' · ') }} · {{ fmtDate(n.updated) }}
              </span>
            </li>
          </ul>
          <p v-if="!shown.length && !loading" class="kb-empty-hint">没有匹配的笔记</p>
        </aside>

        <article class="kb-read" @click="onContentClick">
          <template v-if="note">
            <header class="kb-read__head">
              <h2>{{ note.title }}</h2>
              <p class="kb-read__meta">
                <span v-if="note.folder"><Folder :size="12" />{{ note.folder }}</span>
                <span v-for="t in note.tags || []" :key="t" class="kb-read__tag">#{{ t }}</span>
                <span>更新于 {{ fmtDate(note.updated) }}</span>
                <span>{{ content.length }} 字</span>
              </p>
            </header>

            <div class="prose kb-read__body" v-html="html" />

            <section v-if="backlinks.length" class="kb-backlinks">
              <h4>反向链接（{{ backlinks.length }}）</h4>
              <ul>
                <li v-for="b in backlinks" :key="b.id" @click="openNote(b)">{{ b.title }}</li>
              </ul>
            </section>
          </template>
          <p v-else class="kb-empty-hint">
            <BookOpen :size="16" /> 从左边选一篇笔记开始读
          </p>
        </article>
      </section>

      <section class="shell kb-others">
        <SectionHeading index="02" title="其他知识库入口" />
        <div class="kb-cards">
          <a class="kb-card" :href="KbStandalone" target="_blank" rel="noreferrer">
            <span class="kb-card__badge"><Sparkles :size="14" /></span>
            <h3>AIO·知识库（独立版）</h3>
            <p>完整编辑器、双链补全、知识图谱、回收站、AI 助手与 ZIP 导出都在这边。</p>
            <span class="text-link">打开独立版 <ArrowUpRight :size="14" /></span>
          </a>
          <a class="kb-card" :href="SiyuanUrl" target="_blank" rel="noreferrer">
            <span class="kb-card__badge"><BookOpen :size="14" /></span>
            <h3>思源笔记（SiYuan）</h3>
            <p>块级编辑器与本地内核，端口 6806。若打不开，先运行「一键启动知识库们.bat」。</p>
            <span class="text-link">打开思源笔记 <ArrowUpRight :size="14" /></span>
          </a>
        </div>
      </section>
    </main>

    <SiteFooter />
  </div>
</template>

<style scoped>
.kb-hero {
  padding: 96px 0 26px;
}

.kb-hero h1 {
  font-family: var(--font-display);
  font-size: clamp(30px, 4vw, 46px);
  line-height: 1.18;
  margin: 14px 0 12px;
  letter-spacing: -0.01em;
}

.kb-hero h1 em {
  font-style: italic;
  color: var(--moss);
}

.kb-hero p {
  max-width: 620px;
  color: var(--ink-soft);
  line-height: 1.9;
}

.kb-body {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 40px;
  padding: 26px 0 40px;
  align-items: start;
}

.kb-side {
  position: sticky;
  top: 92px;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--surface);
  padding: 16px;
}

.kb-search {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 8px 10px;
  color: var(--ink-faint);
  background: var(--surface-raised);
}

.kb-search input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  font: inherit;
  font-size: 13px;
  color: var(--ink);
}

.kb-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 12px;
}

.kb-chips button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid var(--line);
  background: none;
  color: var(--ink-soft);
  border-radius: 999px;
  padding: 3px 10px;
  font-size: 11.5px;
  cursor: pointer;
  transition: 0.18s;
}

.kb-chips button:hover {
  border-color: var(--moss);
  color: var(--ink);
}

.kb-chips button.on {
  background: var(--moss-light);
  border-color: var(--moss);
  color: var(--ink);
}

.kb-chips--tags button span {
  color: var(--ink-faint);
  margin-left: 2px;
}

.kb-count {
  margin: 14px 0 8px;
  font-size: 11px;
  letter-spacing: 0.06em;
  color: var(--ink-faint);
  text-transform: uppercase;
}

.kb-list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 58vh;
  overflow: auto;
}

.kb-list li {
  padding: 9px 10px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: 0.16s;
}

.kb-list li:hover {
  background: var(--surface-raised);
}

.kb-list li.on {
  background: var(--moss-light);
  border-color: var(--moss);
}

.kb-list strong {
  display: block;
  font-size: 13.5px;
  font-weight: 600;
  line-height: 1.5;
}

.kb-item__meta {
  display: block;
  font-size: 11px;
  color: var(--ink-faint);
  margin-top: 2px;
}

.kb-read {
  min-height: 320px;
}

.kb-read__head {
  border-bottom: 1px solid var(--line);
  padding-bottom: 16px;
  margin-bottom: 22px;
}

.kb-read__head h2 {
  font-family: var(--font-display);
  font-size: 27px;
  margin: 0 0 10px;
}

.kb-read__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  font-size: 11.5px;
  color: var(--ink-faint);
}

.kb-read__meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.kb-read__tag {
  color: var(--amber);
}

.kb-read__body :deep(.kb-wikilink) {
  color: var(--forest-bright);
  border-bottom: 1px dashed var(--moss);
  cursor: pointer;
  text-decoration: none;
}

.kb-backlinks {
  margin-top: 34px;
  border-top: 1px solid var(--line);
  padding-top: 16px;
}

.kb-backlinks h4 {
  font-size: 11px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--ink-faint);
  margin: 0 0 8px;
}

.kb-backlinks ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.kb-backlinks li {
  font-size: 13px;
  color: var(--forest-bright);
  cursor: pointer;
  padding: 3px 0;
}

.kb-backlinks li:hover {
  text-decoration: underline;
}

.kb-empty-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--ink-faint);
  font-size: 13px;
}

.kb-offline {
  border: 1px solid var(--line-strong);
  background: var(--surface);
  border-radius: 16px;
  padding: 22px;
  margin-top: 10px;
}

.kb-offline h3 {
  margin: 0 0 8px;
  font-size: 16px;
}

.kb-offline p {
  color: var(--ink-soft);
  font-size: 13px;
  line-height: 1.8;
  margin: 4px 0;
}

.kb-offline code {
  background: var(--surface-raised);
  border: 1px solid var(--line);
  border-radius: 6px;
  padding: 1px 6px;
  font-size: 12px;
}

.kb-others {
  padding: 20px 0 90px;
}

.kb-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 18px;
  margin-top: 22px;
}

.kb-card {
  display: block;
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--surface);
  padding: 20px;
  text-decoration: none;
  color: inherit;
  transition: 0.2s;
}

.kb-card:hover {
  border-color: var(--moss);
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.kb-card__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: 9px;
  background: var(--moss-light);
  color: var(--forest);
}

.kb-card h3 {
  font-size: 16px;
  margin: 12px 0 6px;
}

.kb-card p {
  font-size: 13px;
  color: var(--ink-soft);
  line-height: 1.8;
  margin: 0 0 12px;
}

@media (max-width: 900px) {
  .kb-body {
    grid-template-columns: 1fr;
  }
  .kb-side {
    position: static;
  }
  .kb-list {
    max-height: 300px;
  }
}
</style>
