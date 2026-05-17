<template>
  <view :class="['bubble', role === 'user' ? 'bubble-user' : 'bubble-assistant']">
    <view :class="['content', role === 'user' ? 'content-user' : 'content-assistant']">
      <text>{{ text }}</text>
      <view v-if="showRegenerate && role === 'assistant'" class="regenerate-btn" @click.stop="$emit('regenerate')">
        <text class="regenerate-icon">↻</text>
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  role: { type: String, required: true },
  text: { type: String, required: true },
  showRegenerate: { type: Boolean, default: false }
})
defineEmits(['regenerate'])
</script>

<style scoped>
.bubble { display: flex; margin-bottom: 12px; }
.bubble-user { justify-content: flex-end; }
.bubble-assistant { justify-content: flex-start; }
.content { max-width: 78%; padding: 11px 15px; border-radius: 18px; font-size: 14px; line-height: 1.55; position: relative; }
.content-user { background: #7CB9A8; color: #fff; border-bottom-right-radius: 6px; }
.content-assistant { background: #fff; color: #3D4A46; border-bottom-left-radius: 6px; box-shadow: 0 1px 2px rgba(60,80,70,0.04); }
.regenerate-btn { position: absolute; bottom: -8px; right: 10px; width: 24px; height: 24px; background: #F4F7F6; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 1px solid #E8EEEA; }
.regenerate-icon { font-size: 12px; color: #B0C3BA; }
</style>
