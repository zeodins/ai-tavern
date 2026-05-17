<template>
  <view class="container">
    <view class="top-bar">
      <text class="char-name">{{ characterName }}</text>
    </view>

    <scroll-view
      class="message-list"
      scroll-y
      :scroll-into-view="scrollTarget"
      :scroll-with-animation="true"
    >
      <view v-if="messages.length === 0 && !assistantTyping" class="empty-chat">
        <text>开始和 {{ characterName }} 对话吧</text>
      </view>

      <ChatBubble
        v-for="msg in messages"
        :key="msg.id"
        :role="msg.role"
        :text="msg.content"
      />

      <view v-if="assistantTyping" id="typing-bubble">
        <ChatBubble
          role="assistant"
          :text="streamingText || '...'"
        />
      </view>

      <view id="scroll-bottom" style="height: 10px;"></view>
    </scroll-view>

    <view class="input-bar">
      <input
        class="msg-input"
        v-model="inputText"
        placeholder="输入消息..."
        :disabled="assistantTyping"
        confirm-type="send"
        @confirm="handleSend"
      />
      <button
        class="send-btn"
        :disabled="!inputText.trim() || assistantTyping"
        @click="handleSend"
        size="mini"
      >发送</button>
    </view>
  </view>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ChatBubble from '@/components/ChatBubble.vue'
import { sendMessage, getHistory } from '@/api/chat'

const messages = ref([])
const inputText = ref('')
const characterName = ref('')
const characterId = ref(null)
const assistantTyping = ref(false)
const streamingText = ref('')
const scrollTarget = ref('')

onLoad((query) => {
  characterId.value = Number(query.characterId)
  characterName.value = query.characterName || '角色'
  loadHistory()
})

async function loadHistory() {
  try {
    messages.value = await getHistory(characterId.value)
    scrollToBottom()
  } catch (e) {
    console.error('Load history failed', e)
  }
}

function scrollToBottom() {
  nextTick(() => {
    scrollTarget.value = 'scroll-bottom'
  })
}

function handleSend() {
  if (!inputText.value.trim() || assistantTyping.value) return

  const msg = inputText.value.trim()
  inputText.value = ''

  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: msg
  })
  scrollToBottom()

  assistantTyping.value = true
  streamingText.value = ''

  sendMessage(
    characterId.value,
    msg,
    null,
    (chunk) => {
      streamingText.value += chunk
      scrollToBottom()
    },
    () => {
      messages.value.push({
        id: Date.now() + 1,
        role: 'assistant',
        content: streamingText.value
      })
      streamingText.value = ''
      assistantTyping.value = false
      scrollToBottom()
    },
    (err) => {
      if (streamingText.value) {
        messages.value.push({
          id: Date.now() + 1,
          role: 'assistant',
          content: streamingText.value
        })
      } else {
        messages.value.push({
          id: Date.now() + 1,
          role: 'assistant',
          content: '[回复失败: ' + (err.message || '未知错误') + ']'
        })
      }
      streamingText.value = ''
      assistantTyping.value = false
      scrollToBottom()
    }
  )
}
</script>

<style scoped>
.container { display: flex; flex-direction: column; height: 100vh; }
.top-bar { padding: 12px 16px; background: #fff; border-bottom: 1px solid #eee; }
.char-name { font-size: 17px; font-weight: 600; }
.message-list { flex: 1; padding: 16px; overflow-y: auto; }
.empty-chat { text-align: center; padding: 80px 0; color: #999; }
.input-bar { display: flex; padding: 10px 12px; background: #fff; border-top: 1px solid #eee; align-items: center; gap: 8px; }
.msg-input { flex: 1; border: 1px solid #ddd; border-radius: 20px; padding: 8px 16px; font-size: 15px; background: #f5f5f5; }
.send-btn { background: #7B68EE; color: #fff; border: none; border-radius: 20px; padding: 6px 16px; }
</style>
