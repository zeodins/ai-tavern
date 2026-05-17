<template>
  <view class="container">
    <view class="header">
      <text class="title">模型配置</text>
      <button class="add-btn" @click="openForm()" size="mini">+ 添加</button>
    </view>

    <view v-if="models.length === 0" class="empty">
      <text>暂无模型配置，点击上方添加</text>
    </view>

    <view v-for="item in models" :key="item.id" class="model-card">
      <view class="card-header">
        <text class="model-name">{{ item.name }}</text>
      </view>
      <text class="model-info">{{ item.modelName }}</text>
      <text class="model-url">{{ item.apiBaseUrl }}</text>
      <view class="card-actions">
        <button size="mini" @click="openForm(item)">编辑</button>
        <button size="mini" type="warn" @click="handleDelete(item.id)">删除</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listModels, deleteModel } from '@/api/model'

const models = ref([])

onMounted(() => {
  loadModels()
})

async function loadModels() {
  try {
    models.value = await listModels()
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

function openForm(model) {
  const params = model ? `?id=${model.id}` : ''
  uni.navigateTo({ url: `/pages/models/model-form${params}` })
}

async function handleDelete(id) {
  const res = await uni.showModal({ title: '确认删除', content: '删除后不可恢复' })
  if (res.confirm) {
    await deleteModel(id)
    uni.showToast({ title: '已删除', icon: 'success' })
    loadModels()
  }
}
</script>

<style scoped>
.container { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { font-size: 20px; font-weight: bold; }
.add-btn { background-color: #7B68EE; color: #fff; border: none; }
.empty { text-align: center; padding: 40px 0; color: #999; }
.model-card { background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.card-header { margin-bottom: 8px; }
.model-name { font-size: 17px; font-weight: 600; }
.model-info { display: block; font-size: 13px; color: #666; margin-bottom: 4px; }
.model-url { display: block; font-size: 12px; color: #999; }
.card-actions { display: flex; gap: 8px; margin-top: 12px; }
</style>
