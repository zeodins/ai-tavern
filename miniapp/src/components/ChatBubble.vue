<template>
  <view :class="['bubble', role === 'user' ? 'bubble-user' : 'bubble-assistant']">
    <view :class="['content', role === 'user' ? 'content-user' : 'content-assistant']">
      <text>{{ text }}</text>
      <view v-if="showRegenerate && role === 'assistant'" class="regenerate-btn" @click.stop="$emit('regenerate')">
        <text class="regenerate-icon">&#x21BB;</text>
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
.bubble { display: flex; margin-bottom: 16px; }
.bubble-user { justify-content: flex-end; }
.bubble-assistant { justify-content: flex-start; }
.content { max-width: 75%; padding: 12px 16px; border-radius: 16px; font-size: 15px; line-height: 1.5; position: relative; }
.content-user { background: #7B68EE; color: #fff; border-bottom-right-radius: 4px; }
.content-assistant { background: #fff; color: #333; border-bottom-left-radius: 4px; }
.regenerate-btn { position: absolute; bottom: -8px; right: 8px; width: 24px; height: 24px; background: #f0f0f0; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.regenerate-icon { font-size: 14px; color: #999; }
</style>
