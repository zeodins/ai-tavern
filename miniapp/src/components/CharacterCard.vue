<template>
  <view class="card" @click="$emit('click')" @longpress="$emit('longpress')">
    <image
      v-if="avatar"
      class="avatar"
      :src="avatar"
      mode="aspectFill"
    />
    <view v-else class="avatar-placeholder">
      <text class="avatar-text">{{ name.charAt(0) }}</text>
    </view>
    <text class="name">{{ name }}</text>
    <text v-if="description" class="desc">{{ description }}</text>
  </view>
</template>

<script setup>
defineProps({
  name: { type: String, required: true },
  description: { type: String, default: '' },
  avatar: { type: String, default: '' }
})

defineEmits(['click', 'longpress'])
</script>

<style scoped>
.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  margin-right: 14px;
  flex-shrink: 0;
}
.avatar-placeholder {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #7B68EE, #a78bfa);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
}
.avatar-text {
  color: #fff;
  font-size: 22px;
  font-weight: bold;
}
.name {
  font-size: 16px;
  font-weight: 600;
}
.desc {
  font-size: 12px;
  color: #999;
  margin-left: auto;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
