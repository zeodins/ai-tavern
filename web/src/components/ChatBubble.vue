<template>
  <div :class="['bubble', role === 'user' ? 'user' : 'assistant']">
    <div :class="['bubble-content', role]">
      <div class="text" v-text="text"></div>
      <el-button v-if="showRegen && role === 'assistant'" class="regen-btn" size="small" circle :icon="Refresh" @click="$emit('regenerate')" />
    </div>
  </div>
</template>

<script setup>
import { Refresh } from '@element-plus/icons-vue'
defineProps({ role: String, text: String, showRegen: Boolean })
defineEmits(['regenerate'])
</script>

<style scoped>
.bubble { display: flex; margin-bottom: 12px; }
.bubble.user { justify-content: flex-end; }
.bubble.assistant { justify-content: flex-start; }
.bubble-content { max-width: 75%; padding: 10px 15px; border-radius: 16px; font-size: 14px; line-height: 1.5; position: relative; }
.bubble-content.user { background: var(--primary, #7CB9A8); color: #fff; border-bottom-right-radius: 4px; }
.bubble-content.assistant { background: var(--white, #fff); color: var(--text, #3D4A46); border-bottom-left-radius: 4px; box-shadow: 0 1px 2px rgba(60,80,70,0.04); }
.regen-btn { position: absolute; bottom: -10px; right: 6px; width: 26px; height: 26px; opacity: 0.7; }
.text { white-space: pre-wrap; word-break: break-word; }
</style>
