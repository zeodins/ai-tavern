<template>
  <view class="container">
    <view class="header">
      <text class="title">用户人设</text>
      <button class="add-btn" @click="openForm()" size="mini">+ 添加</button>
    </view>
    <view v-if="personas.length === 0" class="empty"><text>暂无人设，点击添加</text></view>
    <view v-for="item in personas" :key="item.id" class="card">
      <text class="card-title">{{ item.name }}</text>
      <text class="card-content">{{ item.content }}</text>
      <view class="card-actions">
        <button size="mini" @click="openForm(item)">编辑</button>
        <button size="mini" type="warn" @click="handleDelete(item.id)">删除</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listPersonas, deletePersona } from '@/api/persona'

const personas = ref([])
onMounted(() => { load() })
async function load() { try { personas.value = await listPersonas() } catch(e) {} }
function openForm(item) { uni.navigateTo({ url: `/pages/personas/persona-form${item ? '?id=' + item.id : ''}` }) }
async function handleDelete(id) {
  const r = await uni.showModal({ title: '确认删除' })
  if (r.confirm) { await deletePersona(id); load(); uni.showToast({ title: '已删除', icon: 'success' }) }
}
</script>

<style scoped>
.container { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { font-size: 20px; font-weight: bold; }
.add-btn { background-color: #7B68EE; color: #fff; border: none; }
.empty { text-align: center; padding: 40px 0; color: #999; }
.card { background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.card-title { font-size: 17px; font-weight: 600; display: block; margin-bottom: 6px; }
.card-content { font-size: 13px; color: #666; display: block; margin-bottom: 8px; white-space: pre-wrap; max-height: 80px; overflow: hidden; }
.card-actions { display: flex; gap: 8px; }
</style>
