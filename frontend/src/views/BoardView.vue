<script setup>
import {
  ArrowUpRight,
  Check,
  Clock,
  FileText,
  Github,
  Monitor,
  RefreshCw,
  Search,
  Trash2
} from 'lucide-vue-next'
import { computed, onMounted, ref, shallowRef } from 'vue'
import { boardApi } from '../api/board'
import SectionHeading from '../components/SectionHeading.vue'
import SiteFooter from '../components/SiteFooter.vue'
import SiteHeader from '../components/SiteHeader.vue'

const board = shallowRef(null)
const todos = shallowRef({ today: [], carry: [] })
const loading = shallowRef(true)
const refreshing = shallowRef(false)
const notice = shallowRef('')
const error = shallowRef('')

const newTodo = ref('')
const activeTab = ref('apps')
const keyword = ref('')
const hits = shallowRef([])
const searchMode = shallowRef('')
const searching = shallowRef(false)
const searched = shallowRef(false)

const session = shallowRef(null)
const sessionLoading = shallowRef(false)

const gh = computed(() => board.value?.sources?.github || {})
const hm = computed(() => board.value?.sources?.hermes || {})
const ap = computed(() => board.value?.sources?.apps || {})
const br = computed(() => board.value?.sources?.browser || {})
const fl = computed(() => board.value?.sources?.files || {})
const sh = computed(() => board.value?.sources?.shell || {})
const jobLog = computed(() => board.value?.job_log || [])
const report = computed(() => board.value?.cron_report || null)
const errors = computed(() => board.value?.errors || [])

// 极简 markdown → HTML（只处理加粗/斜体/行内代码/列表），用于展示 AI 汇报正文
const escapeHtml = value =>
  String(value ?? '').replace(/[&<>"']/g, ch => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  }[ch]))

const mdLite = md => {
  const out = []
  let inList = false
  escapeHtml(md)
    .split(/\r?\n/)
    .forEach(raw => {
      const line = raw.trim()
      if (!line) {
        if (inList) {
          out.push('</ul>')
          inList = false
        }
        return
      }
      const isItem = /^([-*]|\d+[.．])\s+/.test(line)
      const body = line
        .replace(/^([-*]|\d+[.．])\s+/, '')
        .replace(/\*\*(.+?)\*\*/g, '<b>$1</b>')
        .replace(/`(.+?)`/g, '<code>$1</code>')
        .replace(/\*(.+?)\*/g, '<i>$1</i>')
      if (isItem) {
        if (!inList) {
          out.push('<ul>')
          inList = true
        }
        out.push(`<li>${body}</li>`)
      } else {
        if (inList) {
          out.push('</ul>')
          inList = false
        }
        out.push(`<p>${body}</p>`)
      }
    })
  if (inList) out.push('</ul>')
  return out.join('')
}

const reportHtml = computed(() => (report.value ? mdLite(report.value.text) : ''))

const stats = computed(() => [
  { label: '今日提交', value: gh.value.commit_count ?? 0 },
  { label: 'AI 对话', value: (hm.value.today || []).length },
  { label: '启动软件', value: (ap.value.apps || []).length },
  { label: '改动文件', value: fl.value.changed_count ?? 0 },
  { label: '待办', value: `${(todos.value.today || []).filter(t => t.done).length}/${(todos.value.today || []).length}` }
])

const tabs = [
  { key: 'apps', label: '软件使用' },
  { key: 'sites', label: '浏览网站' },
  { key: 'docs', label: '打开文档' },
  { key: 'files', label: '改动文件' },
  { key: 'shell', label: '命令' }
]

const fmtWhen = value => (value ? String(value).replace('T', ' ').slice(0, 16) : '')

const loadTodos = async () => {
  try {
    todos.value = await boardApi.todos()
  } catch (err) {
    todos.value = { today: [], carry: [] }
  }
}

const loadBoard = async () => {
  loading.value = true
  error.value = ''
  try {
    board.value = await boardApi.board()
  } catch (err) {
    error.value = `读不到工作台数据（${err.message}）。请确认本机看板服务在运行：双击桌面「每日工作台」或执行 python app.py`
  } finally {
    loading.value = false
  }
}

const loadPage = async () => {
  await Promise.all([loadBoard(), loadTodos()])
}

const refresh = async () => {
  refreshing.value = true
  notice.value = ''
  try {
    const res = await boardApi.refresh()
    board.value = res.board
    await loadTodos()
    notice.value = '已重新采集本机数据'
  } catch (err) {
    notice.value = `刷新失败：${err.message}`
  } finally {
    refreshing.value = false
    setTimeout(() => (notice.value = ''), 3000)
  }
}

const addTodo = async () => {
  const text = newTodo.value.trim()
  if (!text) return
  todos.value = await boardApi.addTodo(text)
  newTodo.value = ''
}

const toggleTodo = async item => {
  todos.value = await boardApi.toggleTodo(item.id, !item.done)
}

const removeTodo = async item => {
  todos.value = await boardApi.deleteTodo(item.id)
}

const moveTodo = async item => {
  todos.value = await boardApi.moveTodo(item.id)
}

const runSearch = async () => {
  const q = keyword.value.trim()
  if (!q) return
  searching.value = true
  searched.value = true
  try {
    const res = await boardApi.search(q)
    hits.value = res.hits || []
    searchMode.value = res.mode
  } catch (err) {
    hits.value = []
    searchMode.value = 'error'
  } finally {
    searching.value = false
  }
}

const openSession = async id => {
  sessionLoading.value = true
  session.value = { id, title: '加载中…', messages: [] }
  try {
    session.value = await boardApi.session(id)
  } catch (err) {
    session.value = { id, title: '加载失败', messages: [] }
  } finally {
    sessionLoading.value = false
  }
}

const closeSession = () => {
  session.value = null
}

const maxOf = (list, key) => Math.max(1, ...list.map(item => Number(item[key]) || 0))

onMounted(loadPage)
</script>

<template>
  <div class="page board-page">
    <SiteHeader />

    <main id="top">
      <section class="hero shell board-hero">
        <div class="hero__copy">
          <span class="eyebrow">每日工作台 · 自动记录</span>
          <h1>
            <span>每天做过的事，</span>
            <em>都留成可以翻回的一页</em>
          </h1>
          <p>
            GitHub 提交、arXiv 日报、AI 对话和本机操作，每天早上 07:05 由本机的计划任务自动汇总到这里。
            数据存在你自己的电脑上，不上传任何服务器。
          </p>

          <div class="hero__actions">
            <button class="button button--primary" type="button" :disabled="refreshing" @click="refresh">
              {{ refreshing ? '采集中…' : '重新采集' }} <RefreshCw :size="16" />
            </button>
            <router-link class="button button--quiet" to="/">回到博客首页</router-link>
          </div>

          <div class="stat-row">
            <div v-for="item in stats" :key="item.label">
              <strong>{{ item.value }}</strong>
              <span>{{ item.label }}</span>
            </div>
          </div>
        </div>

        <aside class="board-status">
          <span class="board-status__label">自动任务</span>
          <template v-if="jobLog.length">
            <p class="board-status__main">
              <i :class="['dot', { 'dot--warn': errors.length }]" />
              {{ jobLog[0].date }} 已执行
            </p>
            <ul>
              <li>
                arXiv 日报
                <b v-if="jobLog[0].digest_date">命中 {{ jobLog[0].hit }} / {{ jobLog[0].fetched }} 篇</b>
                <b v-else>缺失</b>
              </li>
              <li>今日提交 <b>{{ jobLog[0].commits_today }}</b> 次</li>
              <li v-if="jobLog[0].local_push === 'yes'">已由本机补推</li>
              <li class="muted">下次运行：每天 07:05</li>
            </ul>
          </template>
          <template v-else>
            <p class="board-status__main">还没有执行记录</p>
            <ul><li class="muted">首次运行后这里会显示结果</li></ul>
          </template>
          <p class="board-status__stamp">数据更新于 {{ board?.generated_at || '—' }}</p>
        </aside>
      </section>

      <section v-if="error" class="home-section shell">
        <div class="board-error">{{ error }}</div>
      </section>

      <section class="home-section shell">
        <SectionHeading index="01" title="每日待办" />
        <div class="board-cols">
          <div class="board-card">
            <label class="board-input">
              <input
                v-model="newTodo"
                class="input"
                placeholder="加一条今天要做的事，回车确认"
                @keyup.enter="addTodo"
              />
              <button class="btn" type="button" @click="addTodo">添加</button>
            </label>

            <ul class="todo-list">
              <li v-for="item in todos.today" :key="item.id" :class="{ done: item.done }">
                <button class="todo-check" type="button" @click="toggleTodo(item)">
                  <Check v-if="item.done" :size="13" />
                </button>
                <span>{{ item.text }}</span>
                <button class="todo-del" type="button" title="删除" @click="removeTodo(item)">
                  <Trash2 :size="14" />
                </button>
              </li>
              <li v-if="!todos.today.length" class="muted">今天还没安排任务。</li>
            </ul>

            <div v-if="todos.carry && todos.carry.length" class="todo-carry">
              <p class="muted">之前没做完的</p>
              <ul class="todo-list">
                <li v-for="item in todos.carry" :key="item.id">
                  <span>{{ item.text }}</span>
                  <button class="btn btn--tiny" type="button" @click="moveTodo(item)">顺延到今天</button>
                </li>
              </ul>
            </div>
          </div>

          <div class="board-card">
            <h3 class="board-card__title">自动任务历史</h3>
            <table class="board-table">
              <thead>
                <tr><th>日期</th><th>提交</th><th>日报</th></tr>
              </thead>
              <tbody>
                <tr v-for="row in jobLog" :key="row.date">
                  <td>{{ row.date }}</td>
                  <td>{{ row.commits_today ?? '—' }}</td>
                  <td>
                    <template v-if="row.digest_date">命中 {{ row.hit }} / {{ row.fetched }}</template>
                    <template v-else>—</template>
                    <span v-if="row.local_push === 'yes'" class="tag">本机补推</span>
                  </td>
                </tr>
                <tr v-if="!jobLog.length"><td colspan="3" class="muted">暂无记录</td></tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section class="home-section shell">
        <SectionHeading index="02" title="今日上传" />
        <div class="board-cols board-cols--wide">
          <div class="board-card board-card--accent">
            <h3 class="board-card__title">
              <Github :size="16" /> arXiv 论文雷达
            </h3>
            <template v-if="gh.digest">
              <p class="muted">
                {{ gh.digest.date }} · 抓到 {{ gh.digest.fetched }} 篇 / 命中 {{ gh.digest.hit }} 篇
              </p>
              <ol class="digest-list">
                <li v-for="paper in gh.digest.top" :key="paper.url">
                  <a :href="paper.url" target="_blank" rel="noreferrer">{{ paper.title }}</a>
                  <span class="tag">{{ paper.score }} 分</span>
                </li>
              </ol>
              <a class="board-link" :href="gh.digest.url" target="_blank" rel="noreferrer">
                查看完整日报 <ArrowUpRight :size="15" />
              </a>
            </template>
            <p v-else class="muted">今天的日报还没生成（每天 06:17 自动跑）。</p>
          </div>

          <div class="board-card">
            <h3 class="board-card__title">今日提交（{{ gh.commit_count ?? 0 }} 次）</h3>
            <ul class="commit-list">
              <li v-for="commit in gh.commits" :key="commit.sha">
                <span class="commit-time">{{ commit.time }}</span>
                <a :href="commit.url" target="_blank" rel="noreferrer">{{ commit.message }}</a>
                <span class="tag">{{ commit.repo }}</span>
              </li>
              <li v-if="!(gh.commits || []).length" class="muted">今天还没有提交。</li>
            </ul>
          </div>
        </div>
      </section>

      <section v-if="report" class="home-section shell">
        <SectionHeading index="03" title="今日汇报" />
        <div class="board-card board-report">
          <p class="muted">{{ report.when }} 生成 · 已存档 {{ report.total }} 份</p>
          <!-- 内容来自本机的定时任务，已先做 HTML 转义再渲染 -->
          <div class="board-report__body" v-html="reportHtml" />
        </div>
      </section>

      <section class="home-section shell">
        <SectionHeading index="04" title="今日对话" />
        <p class="muted board-lead">
          今天在 Hermes 里的 {{ (hm.today || []).length }} 个会话（累计 {{ hm.totals?.sessions ?? 0 }} 个 /
          {{ hm.totals?.messages ?? 0 }} 条消息）。点开任意一条可以看到对话片段。
        </p>
        <div class="chat-grid">
          <button
            v-for="item in hm.today"
            :key="item.id"
            class="chat-card"
            type="button"
            @click="openSession(item.id)"
          >
            <span class="chat-card__title">{{ item.title || '（无标题）' }}</span>
            <span v-if="item.opening && item.opening !== item.title" class="chat-card__opening">
              {{ item.opening }}
            </span>
            <span class="chat-card__meta">
              <em><Clock :size="12" /> {{ item.from }}–{{ item.to }}</em>
              <em>消息 {{ item.messages }}</em>
              <em>工具 {{ item.tool_calls }}</em>
            </span>
          </button>
          <p v-if="!(hm.today || []).length" class="muted">今天还没对话。</p>
        </div>
      </section>

      <section class="home-section shell">
        <SectionHeading index="05" title="本机操作记录" />
        <div class="board-tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            :class="['board-tab', { active: activeTab === tab.key }]"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="board-card">
          <template v-if="activeTab === 'apps'">
            <div v-for="app in ap.apps" :key="app.exe" class="bar-row">
              <span class="bar-name" :title="app.exe">{{ app.name }}</span>
              <span class="bar-track">
                <span class="bar-fill" :style="{ width: (app.launches / maxOf(ap.apps, 'launches')) * 100 + '%' }" />
              </span>
              <span class="bar-value">{{ app.launches }}</span>
            </div>
            <p class="muted board-lead">
              来自 Windows Prefetch（已剔除系统进程）。另有命令行工具 {{ ap.cli_count ?? 0 }} 次由脚本触发。
            </p>
          </template>

          <template v-else-if="activeTab === 'sites'">
            <div v-for="site in br.domains" :key="site.domain" class="bar-row">
              <span class="bar-name">{{ site.domain }}</span>
              <span class="bar-track">
                <span class="bar-fill" :style="{ width: (site.count / maxOf(br.domains, 'count')) * 100 + '%' }" />
              </span>
              <span class="bar-value">{{ site.count }}</span>
            </div>
            <ul class="plain-list">
              <li v-for="page in br.pages" :key="page.url + page.time">
                <span class="commit-time">{{ page.time }}</span>
                <a :href="page.url" target="_blank" rel="noreferrer">{{ page.title }}</a>
              </li>
            </ul>
          </template>

          <template v-else-if="activeTab === 'docs'">
            <ul class="plain-list">
              <li v-for="doc in fl.documents" :key="doc.name + doc.time">
                <span class="commit-time">{{ doc.time }}</span><FileText :size="14" /> {{ doc.name }}
              </li>
              <li v-if="!(fl.documents || []).length" class="muted">今天没有打开过文档。</li>
            </ul>
            <p v-if="(fl.folders || []).length" class="muted board-lead">
              另外打开过这些文件夹：{{ (fl.folders || []).map(f => f.name).join('、') }}
            </p>
          </template>

          <template v-else-if="activeTab === 'files'">
            <p class="muted board-lead">今天共改动 {{ fl.changed_count ?? 0 }} 个文件，按目录汇总：</p>
            <div v-for="group in fl.groups" :key="group.path" class="bar-row">
              <span class="bar-name">{{ group.path }}</span>
              <span class="bar-track">
                <span class="bar-fill" :style="{ width: (group.count / maxOf(fl.groups, 'count')) * 100 + '%' }" />
              </span>
              <span class="bar-value">{{ group.count }}</span>
            </div>
            <ul class="plain-list">
              <li v-for="file in (fl.changed || []).slice(0, 18)" :key="file.path">
                <span class="commit-time">{{ file.time }}</span>
                <code>{{ file.path.replace(/^[A-Za-z]:/, '') }}</code>
              </li>
            </ul>
          </template>

          <template v-else>
            <p class="muted board-lead">PowerShell 命令历史（该文件不记录时间，故不按天过滤）：</p>
            <ul class="plain-list plain-list--mono">
              <li v-for="(cmd, index) in (sh.recent_commands || []).slice().reverse()" :key="index">
                {{ cmd }}
              </li>
              <li v-if="!(sh.recent_commands || []).length" class="muted">没有读到命令历史。</li>
            </ul>
          </template>
        </div>
      </section>

      <section class="home-section shell">
        <SectionHeading index="06" title="历史对话检索" />
        <div class="filters-surface">
          <label class="search-field">
            <Search :size="16" :stroke-width="1.7" />
            <input
              v-model="keyword"
              class="input"
              placeholder="搜索全部历史对话，例如：开题 / 算法作业 / 多模态推荐"
              @keyup.enter="runSearch"
            />
          </label>
          <button class="btn" type="button" @click="runSearch">搜索</button>
        </div>

        <div v-if="searching" class="loading-cards"><div /><div /></div>
        <div v-else-if="hits.length" class="hit-list">
          <button v-for="(hit, index) in hits" :key="index" class="hit" type="button" @click="openSession(hit.session_id)">
            <span class="hit__title">{{ hit.title }}</span>
            <span class="hit__snippet">{{ hit.snippet }}</span>
            <span class="hit__meta">{{ hit.time }} · {{ hit.role === 'user' ? '我' : 'AI' }}</span>
          </button>
        </div>
        <div v-else-if="searched" class="empty-state">没有找到相关的历史对话。</div>
      </section>
    </main>

    <SiteFooter />

    <div v-if="session" class="board-modal" @click.self="closeSession">
      <div class="board-modal__panel">
        <header>
          <h3>{{ session.title }}</h3>
          <button class="button button--quiet" type="button" @click="closeSession">关闭</button>
        </header>
        <p class="muted">
          {{ session.id }} · {{ session.model }} · {{ session.message_count }} 条消息 / {{ session.tool_calls }} 次工具调用
        </p>
        <div v-if="sessionLoading" class="loading-cards"><div /><div /></div>
        <div v-else class="msg-list">
          <div v-for="(msg, index) in session.messages" :key="index" :class="['msg', msg.role]">
            <span class="msg__who">{{ msg.role === 'user' ? '我' : 'AI' }} · {{ msg.time }}</span>
            <p>{{ msg.text }}</p>
          </div>
        </div>
      </div>
    </div>

    <transition name="fade">
      <div v-if="notice" class="board-toast">{{ notice }}</div>
    </transition>
  </div>
</template>

<style scoped>
.board-hero {
  align-items: stretch;
  gap: 42px;
}

.board-status {
  border: 1px solid var(--line);
  background: var(--surface-raised);
  border-radius: 18px;
  padding: 22px 24px;
  box-shadow: var(--shadow);
  min-width: 260px;
  align-self: center;
}

.board-status__label {
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--ink-faint);
}

.board-status__main {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 18px;
  font-family: var(--font-display, serif);
  margin: 10px 0 14px;
  color: var(--ink);
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: var(--moss);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--moss) 22%, transparent);
}

.dot--warn {
  background: var(--amber);
  box-shadow: 0 0 0 4px color-mix(in srgb, var(--amber) 22%, transparent);
}

.board-status ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 7px;
  font-size: 14px;
  color: var(--ink-soft);
}

.board-status li b {
  color: var(--ink);
}

.board-status__stamp {
  margin: 16px 0 0;
  font-size: 12px;
  color: var(--ink-faint);
}

.board-cols {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 20px;
}

.board-card {
  border: 1px solid var(--line);
  background: var(--surface-raised);
  border-radius: 18px;
  padding: 20px 22px;
}

.board-card--accent {
  background: linear-gradient(150deg, var(--surface-raised), var(--surface));
  border-color: var(--line-strong);
}

.board-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  margin: 0 0 12px;
  color: var(--ink);
}

.board-input {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.todo-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 8px;
}

.todo-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--ink);
  border-bottom: 1px dashed var(--line);
  padding-bottom: 8px;
}

.todo-list li span {
  flex: 1;
}

.todo-list li.done span {
  color: var(--ink-faint);
  text-decoration: line-through;
}

.todo-check {
  width: 19px;
  height: 19px;
  border-radius: 6px;
  border: 1.5px solid var(--line-strong);
  background: transparent;
  color: var(--forest);
  display: grid;
  place-items: center;
  cursor: pointer;
}

.todo-list li.done .todo-check {
  background: var(--moss-light);
  border-color: var(--moss);
}

.todo-del {
  border: none;
  background: none;
  color: var(--ink-faint);
  cursor: pointer;
  padding: 2px;
}

.todo-del:hover {
  color: var(--amber);
}

.todo-carry {
  margin-top: 16px;
  border-top: 1px solid var(--line);
  padding-top: 12px;
}

.btn--tiny {
  padding: 4px 10px;
  font-size: 12px;
}

.board-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.board-table th {
  text-align: left;
  font-weight: 500;
  color: var(--ink-faint);
  font-size: 12px;
  padding-bottom: 6px;
}

.board-table td {
  padding: 7px 0;
  border-top: 1px solid var(--line);
  color: var(--ink-soft);
}

.tag {
  display: inline-block;
  margin-left: 8px;
  padding: 1px 8px;
  border-radius: 999px;
  border: 1px solid var(--line);
  font-size: 11px;
  color: var(--ink-faint);
  white-space: nowrap;
}

.digest-list {
  margin: 8px 0 12px;
  padding-left: 18px;
  display: grid;
  gap: 8px;
}

.digest-list li a {
  color: var(--ink);
  text-decoration: none;
  border-bottom: 1px solid var(--line);
}

.digest-list li a:hover {
  color: var(--forest-bright);
  border-color: var(--forest-bright);
}

.board-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--forest-bright);
  text-decoration: none;
}

.commit-list,
.plain-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 8px;
  font-size: 13.5px;
}

.commit-list li,
.plain-list li {
  display: flex;
  align-items: baseline;
  gap: 8px;
  color: var(--ink-soft);
}

.commit-list a,
.plain-list a {
  color: var(--ink);
  text-decoration: none;
  flex: 1;
}

.commit-list a:hover,
.plain-list a:hover {
  color: var(--forest-bright);
}

.commit-time {
  color: var(--ink-faint);
  font-size: 12px;
  min-width: 42px;
  font-variant-numeric: tabular-nums;
}

.plain-list--mono li {
  font-family: var(--font-mono, monospace);
  font-size: 12.5px;
}

.plain-list code {
  font-size: 12.5px;
  color: var(--ink-soft);
  word-break: break-all;
}

.board-report__body {
  font-size: 14px;
  line-height: 1.85;
  color: var(--ink-soft);
  margin-top: 10px;
}

.board-report__body :deep(p) {
  margin: 6px 0;
}

.board-report__body :deep(ul) {
  margin: 6px 0;
  padding-left: 20px;
  display: grid;
  gap: 4px;
}

.board-report__body :deep(b) {
  color: var(--ink);
}

.board-report__body :deep(code) {
  font-family: var(--font-mono, monospace);
  font-size: 12.5px;
  background: var(--paper-strong);
  border-radius: 5px;
  padding: 1px 6px;
}

.board-lead {
  font-size: 13.5px;
  margin: 0 0 14px;
}

.chat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 14px;
}

.chat-card {
  text-align: left;
  border: 1px solid var(--line);
  background: var(--surface-raised);
  border-radius: 16px;
  padding: 16px 18px;
  cursor: pointer;
  display: grid;
  gap: 7px;
  transition: transform 0.16s ease, border-color 0.16s ease;
}

.chat-card:hover {
  transform: translateY(-2px);
  border-color: var(--line-strong);
}

.chat-card__title {
  font-size: 15px;
  color: var(--ink);
}

.chat-card__opening {
  font-size: 13px;
  color: var(--ink-faint);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.chat-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: var(--ink-faint);
}

.chat-card__meta em {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-style: normal;
}

.board-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 14px;
}

.board-tab {
  border: 1px solid var(--line);
  background: transparent;
  color: var(--ink-soft);
  border-radius: 999px;
  padding: 6px 16px;
  font-size: 13px;
  cursor: pointer;
}

.board-tab.active {
  background: var(--forest);
  border-color: var(--forest);
  color: var(--paper);
}

.bar-row {
  display: grid;
  grid-template-columns: 180px 1fr 42px;
  gap: 12px;
  align-items: center;
  font-size: 13px;
  margin-bottom: 9px;
}

.bar-name {
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bar-track {
  height: 8px;
  border-radius: 999px;
  background: var(--paper-strong);
  overflow: hidden;
}

.bar-fill {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--moss), var(--forest-bright));
}

.bar-value {
  text-align: right;
  color: var(--ink-faint);
  font-variant-numeric: tabular-nums;
}

.hit-list {
  display: grid;
  gap: 12px;
}

.hit {
  text-align: left;
  border: 1px solid var(--line);
  background: var(--surface-raised);
  border-radius: 14px;
  padding: 14px 16px;
  cursor: pointer;
  display: grid;
  gap: 6px;
}

.hit:hover {
  border-color: var(--line-strong);
}

.hit__title {
  font-size: 14.5px;
  color: var(--ink);
}

.hit__snippet {
  font-size: 13px;
  color: var(--ink-soft);
  line-height: 1.7;
}

.hit__meta {
  font-size: 12px;
  color: var(--ink-faint);
}

.board-error {
  border: 1px solid var(--amber);
  background: var(--amber-light);
  color: var(--ink);
  border-radius: 14px;
  padding: 14px 18px;
  font-size: 14px;
}

.muted {
  color: var(--ink-faint);
  font-size: 13px;
}

.board-modal {
  position: fixed;
  inset: 0;
  background: color-mix(in srgb, var(--ink) 42%, transparent);
  backdrop-filter: blur(3px);
  display: grid;
  place-items: center;
  z-index: 60;
  padding: 24px;
}

.board-modal__panel {
  width: min(900px, 94vw);
  max-height: 84vh;
  overflow: auto;
  background: var(--surface-raised);
  border: 1px solid var(--line);
  border-radius: 20px;
  padding: 24px 26px;
  box-shadow: var(--shadow);
}

.board-modal__panel header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.board-modal__panel h3 {
  margin: 0;
  font-size: 17px;
  color: var(--ink);
}

.msg-list {
  display: grid;
  gap: 12px;
  margin-top: 14px;
}

.msg {
  border-left: 2px solid var(--line-strong);
  padding: 8px 14px;
  background: var(--surface);
  border-radius: 0 12px 12px 0;
}

.msg.user {
  border-left-color: var(--forest-bright);
}

.msg.assistant {
  border-left-color: var(--moss);
}

.msg__who {
  font-size: 11.5px;
  color: var(--ink-faint);
}

.msg p {
  margin: 4px 0 0;
  font-size: 13.5px;
  color: var(--ink-soft);
  white-space: pre-wrap;
  word-break: break-word;
}

.board-toast {
  position: fixed;
  left: 50%;
  bottom: 34px;
  transform: translateX(-50%);
  background: var(--forest);
  color: var(--paper);
  padding: 10px 20px;
  border-radius: 999px;
  font-size: 13.5px;
  z-index: 70;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 720px) {
  .bar-row {
    grid-template-columns: 120px 1fr 34px;
  }
}
</style>
