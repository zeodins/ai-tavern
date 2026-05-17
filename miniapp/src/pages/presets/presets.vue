<template>
  <view class="container">
    <view class="header">
      <text class="title">预设管理</text>
      <button class="add-btn" @click="openForm()" size="mini">+ 添加</button>
    </view>
    <view v-if="presets.length === 0" class="empty"><text>暂无预设，点击添加</text></view>
    <view v-for="item in presets" :key="item.id" class="card">
      <text class="card-title">{{ item.name }}</text>
      <text class="card-content">{{ item.content }}</text>
      <view class="card-actions">
        <button size="mini" @click="openForm(item)">编辑</button>
        <button size="mini" type="warn" @click="handleDelete(item.id)">删除</button>
      </view>
      <view class="toggle-bar" @click="handleToggle(item.id)">
        <view :class="['toggle-switch', item.isActive ? 'toggle-on' : 'toggle-off']">
          <view class="toggle-knob"></view>
        </view>
        <text class="toggle-label">{{ item.isActive ? '已启用' : '未启用' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listPresets, deletePreset, togglePreset } from '@/api/preset'

const presets = ref([])
onMounted(() => { load() })
async function load() { try { presets.value = await listPresets() } catch(e) {} }
function openForm(item) { uni.navigateTo({ url: `/pages/presets/preset-form${item ? '?id=' + item.id : ''}` }) }
async function handleDelete(id) {
  const r = await uni.showModal({ title: '确认删除' })
  if (r.confirm) { await deletePreset(id); load(); uni.showToast({ title: '已删除', icon: 'success' }) }
}

async function handleToggle(id) {
  try { await togglePreset(id); load() } catch(e) {}
}
</script>

<style scoped>
.container { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { font-size: 20px; font-weight: bold; }
.add-btn { background-color: #7CB9A8; color: #fff; border: none; }
.empty { text-align: center; padding: 40px 0; color: #B0C3BA; }
.card { background: #fff; border-radius: 18px; padding: 16px; margin-bottom: 12px; box-shadow: 0 1px 3px rgba(60,80,70,0.06); }
.card-title { font-size: 17px; font-weight: 600; display: block; margin-bottom: 6px; }
.card-content { font-size: 13px; color: #92A69E; display: block; margin-bottom: 8px; white-space: pre-wrap; max-height: 80px; overflow: hidden; }
.card-actions { display: flex; gap: 8px; }
.toggle-bar { display: flex; align-items: center; gap: 8px; margin-top: 10px; cursor: pointer; }
.toggle-switch { width: 44px; height: 24px; border-radius: 12px; position: relative; transition: .2s; }
.toggle-on { background: #7CB9A8; }
.toggle-off { background: #ccc; }
.toggle-knob { width: 20px; height: 20px; border-radius: 50%; background: #fff; position: absolute; top: 2px; left: 2px; transition: .2s; }
.toggle-on .toggle-knob { left: 22px; }
.toggle-label { font-size: 12px; color: #92A69E; }
</style>
