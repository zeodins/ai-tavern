<template>
  <view class="card" @click="$emit('click')" @longpress="$emit('longpress')">
    <view class="avatar-wrap">
      <image v-if="avatar" class="avatar" :src="avatar" mode="aspectFill" />
      <view v-else class="avatar-placeholder">
        <text class="avatar-text">{{ name.charAt(0) }}</text>
      </view>
    </view>
    <view class="info">
      <text class="name">{{ name }}</text>
      <text v-if="description" class="desc">{{ description }}</text>
    </view>
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
  border-radius: 18px;
  padding: 14px 16px;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 3px rgba(60,80,70,0.06);
  border: 1px solid transparent;
}
.avatar-wrap {
  margin-right: 14px;
  flex-shrink: 0;
}
.avatar {
  width: 50px;
  height: 50px;
  border-radius: 16px;
}
.avatar-placeholder {
  width: 50px;
  height: 50px;
  border-radius: 16px;
  background: linear-gradient(135deg, #B7DDD2 0%, #7CB9A8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-text {
  color: #fff;
  font-size: 20px;
  font-weight: 600;
}
.info {
  flex: 1;
  min-width: 0;
}
.name {
  font-size: 15px;
  font-weight: 500;
  color: #3D4A46;
}
.desc {
  font-size: 12px;
  color: #92A69E;
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  max-width: 180px;
}
</style>
