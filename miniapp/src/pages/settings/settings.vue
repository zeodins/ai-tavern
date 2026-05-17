<template>
  <view class="container">
    <text class="page-title">主题设置</text>
    <text class="page-subtitle">选择你喜欢的配色方案</text>

    <view class="theme-grid">
      <view v-for="t in themes" :key="t.key"
        :class="['theme-card', currentTheme === t.key ? 'theme-active' : '']"
        @click="handleThemeChange(t.key)">
        <view class="theme-preview" :style="{ background: t.primary + '20' }">
          <view class="mini-bar" :style="{ background: t.primary }"></view>
          <view class="mini-bubble" :style="{ background: t.primaryLight }"></view>
          <view class="mini-input" :style="{ borderColor: t.primary }"></view>
        </view>
        <text class="theme-name">{{ t.name }}</text>
        <text v-if="currentTheme === t.key" class="theme-check">&#10003;</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllThemes, getThemeName, setTheme } from '@/theme'

const themes = ref([])
const currentTheme = ref('mint')

onMounted(() => {
  themes.value = getAllThemes()
  currentTheme.value = getThemeName()
})

function handleThemeChange(key) {
  currentTheme.value = key
  setTheme(key)
}
</script>

<style scoped>
.container {
  padding: 20px;
}
.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #3D4A46;
  display: block;
}
.page-subtitle {
  font-size: 13px;
  color: #92A69E;
  margin-top: 4px;
  display: block;
  margin-bottom: 20px;
}
.theme-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.theme-card {
  width: calc(50% - 6px);
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(60,80,70,0.06);
  position: relative;
  border: 2px solid transparent;
  box-sizing: border-box;
}
.theme-active {
  border-color: #7CB9A8;
}
.theme-preview {
  height: 60px;
  border-radius: 10px;
  position: relative;
  margin-bottom: 10px;
  overflow: hidden;
  padding: 8px;
}
.mini-bar {
  height: 14px;
  border-radius: 4px;
  opacity: 0.3;
}
.mini-bubble {
  width: 40px;
  height: 20px;
  border-radius: 8px 8px 2px 8px;
  position: absolute;
  top: 28px;
  right: 12px;
  opacity: 0.5;
}
.mini-input {
  width: 60%;
  height: 14px;
  border: 1px solid;
  border-radius: 7px;
  position: absolute;
  bottom: 12px;
  left: 8px;
  opacity: 0.3;
}
.theme-name {
  font-size: 14px;
  font-weight: 500;
  color: #3D4A46;
  display: block;
}
.theme-check {
  position: absolute;
  top: 8px;
  right: 12px;
  font-size: 16px;
  color: #7CB9A8;
  font-weight: 600;
}
</style>
