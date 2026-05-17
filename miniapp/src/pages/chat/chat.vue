<template>
  <view class="page-container" :style="bgStyle">
    <view class="chat-overlay">
      <view class="top-bar">
        <text class="char-name">{{ characterName }}</text>
      </view>

      <scroll-view class="message-list" scroll-y :scroll-into-view="scrollTarget" :scroll-with-animation="true">
        <view v-if="messages.length === 0 && !assistantTyping" class="empty-chat">
          <text>开始和 {{ characterName }} 对话吧</text>
        </view>

        <ChatBubble v-for="(msg, idx) in messages" :key="msg.id" :role="msg.role" :text="msg.content"
          :showRegenerate="idx === messages.length - 1 && msg.role === 'assistant' && !assistantTyping"
          @regenerate="handleRegenerate" />

        <view v-if="assistantTyping" id="typing-bubble">
          <ChatBubble role="assistant" :text="streamingText || '...'" />
        </view>

        <view id="scroll-bottom" style="height: 10px;"></view>
      </scroll-view>

      <!-- AI Suggestions -->
      <view v-if="suggestions.length > 0 && !assistantTyping" class="suggestions-bar">
        <text v-for="(s, i) in suggestions" :key="i" class="suggestion-chip" @click="useSuggestion(s)">{{ s }}</text>
      </view>

      <view class="input-bar">
        <input class="msg-input" v-model="inputText" placeholder="输入消息..." :disabled="assistantTyping"
          confirm-type="send" @confirm="handleSend" />
        <button class="send-btn" :disabled="!inputText.trim() || assistantTyping" @click="handleSend" size="mini">发送</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ChatBubble from '@/components/ChatBubble.vue'
import { sendMessage, getHistory, regenerateMessage, getSuggestions } from '@/api/chat'
import { getCharacter } from '@/api/character'

const messages = ref([])
const inputText = ref('')
const characterName = ref('')
const characterId = ref(null)
const assistantTyping = ref(false)
const streamingText = ref('')
const scrollTarget = ref('')
const suggestions = ref([])
const backgroundId = ref('')

const BASE_URL = 'http://localhost:8080'

const bgStyle = computed(() => {
  if (!backgroundId.value) return ''
  return {
    backgroundImage: `url(${BASE_URL}/api/backgrounds/${backgroundId.value})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center'
  }
})

onLoad((query) => {
  characterId.value = Number(query.characterId)
  characterName.value = query.characterName || ''
  loadCharacterInfo()
  loadHistory()
})

async function loadCharacterInfo() {
  try {
    const char = await getCharacter(characterId.value)
    if (char.name) characterName.value = char.name
    if (char.backgroundId) backgroundId.value = char.backgroundId
  } catch (e) { /* silently ignore */ }
}

async function loadHistory() {
  try {
    messages.value = await getHistory(characterId.value)
    scrollToBottom()
  } catch (e) { console.error('Load history failed', e) }
}

function scrollToBottom() {
  nextTick(() => { scrollTarget.value = 'scroll-bottom' })
}

function handleSend() {
  if (!inputText.value.trim() || assistantTyping.value) return

  const msg = inputText.value.trim()
  inputText.value = ''
  suggestions.value = []

  messages.value.push({ id: Date.now(), role: 'user', content: msg })
  scrollToBottom()

  assistantTyping.value = true
  streamingText.value = ''

  sendMessage(characterId.value, msg, null,
    (chunk) => { streamingText.value += chunk; scrollToBottom() },
    () => {
      messages.value.push({ id: Date.now() + 1, role: 'assistant', content: streamingText.value })
      streamingText.value = ''
      assistantTyping.value = false
      scrollToBottom()
      loadSuggestions()
    },
    (err) => {
      if (streamingText.value) {
        messages.value.push({ id: Date.now() + 1, role: 'assistant', content: streamingText.value })
      } else {
        messages.value.push({ id: Date.now() + 1, role: 'assistant', content: '[回复失败: ' + (err.message || '未知错误') + ']' })
      }
      streamingText.value = ''
      assistantTyping.value = false
      scrollToBottom()
    }
  )
}

async function loadSuggestions() {
  try {
    const result = await getSuggestions(characterId.value)
    if (Array.isArray(result) && result.length > 0) {
      suggestions.value = result.slice(0, 3)
    }
  } catch (e) { suggestions.value = [] }
}

function useSuggestion(text) {
  inputText.value = text
  suggestions.value = []
}

function handleRegenerate() {
  if (assistantTyping.value) return

  // Remove last assistant message
  const lastIdx = messages.value.length - 1
  if (lastIdx >= 0 && messages.value[lastIdx].role === 'assistant') {
    messages.value.pop()
  }

  assistantTyping.value = true
  streamingText.value = ''
  suggestions.value = []

  regenerateMessage(characterId.value, null,
    (chunk) => { streamingText.value += chunk; scrollToBottom() },
    () => {
      messages.value.push({ id: Date.now(), role: 'assistant', content: streamingText.value })
      streamingText.value = ''
      assistantTyping.value = false
      scrollToBottom()
      loadSuggestions()
    },
    (err) => {
      if (streamingText.value) {
        messages.value.push({ id: Date.now(), role: 'assistant', content: streamingText.value })
      } else {
        messages.value.push({ id: Date.now(), role: 'assistant', content: '[重新生成失败]' })
      }
      streamingText.value = ''
      assistantTyping.value = false
    }
  )
}
</script>

<style scoped>
.page-container { width: 100%; height: 100vh; }
.chat-overlay { display: flex; flex-direction: column; height: 100%; background: rgba(245, 245, 245, 0.85); }
.top-bar { padding: 12px 16px; background: rgba(255,255,255,0.9); border-bottom: 1px solid #eee; }
.char-name { font-size: 17px; font-weight: 600; }
.message-list { flex: 1; padding: 16px; overflow-y: auto; }
.empty-chat { text-align: center; padding: 80px 0; color: #999; }
.suggestions-bar { display: flex; gap: 8px; padding: 8px 16px; flex-wrap: wrap; background: rgba(255,255,255,0.7); }
.suggestion-chip { padding: 6px 14px; border-radius: 16px; font-size: 13px; background: #7B68EE; color: #fff; }
.input-bar { display: flex; padding: 10px 12px; background: rgba(255,255,255,0.95); border-top: 1px solid #eee; align-items: center; gap: 8px; }
.msg-input { flex: 1; border: 1px solid #ddd; border-radius: 20px; padding: 8px 16px; font-size: 15px; background: #f5f5f5; }
.send-btn { background: #7B68EE; color: #fff; border: none; border-radius: 20px; padding: 6px 16px; }
</style>
