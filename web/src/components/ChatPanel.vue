<template>
  <div class="chat-panel" :style="{ backgroundImage: bgUrl ? `url(${bgUrl})` : 'none', backgroundSize: 'cover', backgroundPosition: 'center' }">
    <div class="chat-overlay">
      <div class="chat-header" v-if="character">
        <div class="header-left">
          <div class="header-avatar">{{ character.name?.charAt(0) }}</div>
          <span class="header-name">{{ character.name }}</span>
        </div>
        <el-tag v-if="activeModel" size="small" type="info">{{ activeModel.modelName }}</el-tag>
      </div>
      <div class="chat-header chat-header-empty" v-else>
        <span style="color:var(--text-light,#B0C3BA)">选择一个角色开始对话</span>
      </div>

      <div class="messages-area" ref="msgArea">
        <div v-if="messages.length === 0 && !streaming" class="empty-hint">
          开始和 {{ character?.name || '角色' }} 对话吧
        </div>
        <ChatBubble v-for="(msg, idx) in messages" :key="idx" :role="msg.role" :text="msg.content"
          :showRegen="idx === messages.length - 1 && msg.role === 'assistant' && !streaming"
          @regenerate="handleRegenerate" />
        <div v-if="streaming" class="streaming-bubble">
          <ChatBubble role="assistant" :text="streamText || '...'" />
        </div>
        <div ref="scrollAnchor"></div>
      </div>

      <div class="suggestions-row" v-if="suggestions.length > 0 && !streaming">
        <el-tag v-for="(s, i) in suggestions" :key="i" class="suggestion-chip" @click="useSuggestion(s)" size="small">
          {{ s }}
        </el-tag>
      </div>

      <div class="input-row">
        <el-input v-model="input" placeholder="输入消息..." @keyup.enter="handleSend" :disabled="streaming" size="default" />
        <el-button type="primary" @click="handleSend" :disabled="!input.trim() || streaming" :icon="ChatDotRound">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, computed, onMounted } from 'vue'
import { ChatDotRound } from '@element-plus/icons-vue'
import ChatBubble from './ChatBubble.vue'
import { sendMessage, getHistory, regenerateMessage, getSuggestions } from '../api/chat'
import { getCharacter } from '../api/character'
import { listModels } from '../api/model'

const props = defineProps({ character: Object })
const emit = defineEmits(['refresh'])

const messages = ref([])
const input = ref('')
const streaming = ref(false)
const streamText = ref('')
const suggestions = ref([])
const msgArea = ref(null)
const scrollAnchor = ref(null)
const backgroundId = ref('')
const activeModel = ref(null)

const bgUrl = computed(() => backgroundId.value ? `/api/backgrounds/${backgroundId.value}` : '')

watch(() => props.character, async (char) => {
  if (char) {
    await loadCharacterInfo(char.id)
    await loadHistory()
    await loadActiveModel()
  } else {
    messages.value = []
    backgroundId.value = ''
  }
})

async function loadCharacterInfo(id) {
  try {
    const res = await getCharacter(id)
    backgroundId.value = res.data.backgroundId || ''
  } catch(e) {}
}

async function loadActiveModel() {
  try {
    const res = await listModels()
    activeModel.value = res.data.find(m => m.isActive) || res.data[0] || null
  } catch(e) {}
}

async function loadHistory() {
  if (!props.character) return
  try {
    const res = await getHistory(props.character.id)
    messages.value = res.data || []
    scrollDown()
  } catch(e) { messages.value = [] }
}

function scrollDown() {
  nextTick(() => { scrollAnchor.value?.scrollIntoView({ behavior: 'smooth' }) })
}

async function handleSend() {
  if (!input.value.trim() || streaming.value || !props.character) return
  const msg = input.value.trim()
  input.value = ''
  suggestions.value = []
  messages.value.push({ role: 'user', content: msg })
  scrollDown()

  streaming.value = true
  streamText.value = ''

  try {
    const modelId = activeModel.value?.id || null
    for await (const event of sendMessage(props.character.id, msg, modelId)) {
      if (event.type === 'chunk') {
        streamText.value += event.content
        scrollDown()
      } else if (event.type === 'done') {
        messages.value.push({ role: 'assistant', content: streamText.value })
        streamText.value = ''
        streaming.value = false
        scrollDown()
        loadSuggestions()
      } else if (event.type === 'error') {
        if (streamText.value) messages.value.push({ role: 'assistant', content: streamText.value })
        else messages.value.push({ role: 'assistant', content: '[回复失败]' })
        streamText.value = ''
        streaming.value = false
      }
    }
  } catch(e) {
    streaming.value = false
    messages.value.push({ role: 'assistant', content: '[发送失败: ' + (e.message || '未知') + ']' })
  }
}

async function loadSuggestions() {
  if (!props.character) return
  try {
    const res = await getSuggestions(props.character.id)
    if (Array.isArray(res.data)) suggestions.value = res.data.slice(0, 3)
  } catch(e) { suggestions.value = [] }
}

function useSuggestion(text) {
  input.value = text
  suggestions.value = []
}

async function handleRegenerate() {
  if (streaming.value || !props.character) return
  const lastIdx = messages.value.length - 1
  if (lastIdx >= 0 && messages.value[lastIdx].role === 'assistant') {
    messages.value.pop()
  }
  streaming.value = true
  streamText.value = ''
  suggestions.value = []

  try {
    const modelId = activeModel.value?.id || null
    for await (const event of regenerateMessage(props.character.id, modelId)) {
      if (event.type === 'chunk') { streamText.value += event.content; scrollDown() }
      else if (event.type === 'done') {
        messages.value.push({ role: 'assistant', content: streamText.value })
        streamText.value = ''
        streaming.value = false
        scrollDown()
        loadSuggestions()
      } else if (event.type === 'error') {
        streamText.value = ''
        streaming.value = false
      }
    }
  } catch(e) { streaming.value = false }
}
</script>

<style scoped>
.chat-panel { flex: 1; display: flex; flex-direction: column; min-width: 0; }
.chat-overlay { display: flex; flex-direction: column; height: 100%; background: rgba(248,250,249,0.88); }
.chat-header { display: flex; align-items: center; justify-content: space-between; padding: 12px 20px; background: rgba(255,255,255,0.9); border-bottom: 1px solid var(--border,#E8EEEA); flex-shrink: 0; }
.chat-header-empty { justify-content: center; }
.header-left { display: flex; align-items: center; gap: 10px; }
.header-avatar { width: 34px; height: 34px; border-radius: 10px; background: linear-gradient(135deg, var(--primary-light,#B7DDD2), var(--primary,#7CB9A8)); display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; font-weight: 600; }
.header-name { font-size: 15px; font-weight: 500; color: var(--text,#3D4A46); }
.messages-area { flex: 1; overflow-y: auto; padding: 16px 20px; }
.empty-hint { text-align: center; padding: 80px 0; color: var(--text-light,#B0C3BA); font-size: 14px; }
.streaming-bubble { opacity: 0.85; }
.suggestions-row { display: flex; gap: 8px; padding: 8px 20px; flex-wrap: wrap; flex-shrink: 0; background: rgba(255,255,255,0.7); }
.suggestion-chip { cursor: pointer; }
.input-row { display: flex; gap: 10px; padding: 12px 16px; background: rgba(255,255,255,0.95); border-top: 1px solid var(--border,#E8EEEA); align-items: center; flex-shrink: 0; }
</style>
