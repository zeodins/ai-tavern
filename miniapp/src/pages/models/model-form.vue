<template>
  <view class="container">
    <view class="form-group">
      <text class="label">名称</text>
      <input class="input" v-model="form.name" placeholder="如 DeepSeek" />
    </view>
    <view class="form-group">
      <text class="label">API 地址</text>
      <input class="input" v-model="form.apiBaseUrl" placeholder="https://api.deepseek.com" />
    </view>
    <view class="form-group">
      <text class="label">API Key</text>
      <input class="input" v-model="form.apiKey" placeholder="sk-xxx" password />
    </view>
    <view class="form-group">
      <text class="label">模型名</text>
      <input class="input" v-model="form.modelName" placeholder="deepseek-chat" />
    </view>
    <view class="form-row">
      <view class="form-group half">
        <text class="label">Temperature</text>
        <input class="input" v-model.number="form.temperature" type="digit" placeholder="0.7" />
      </view>
      <view class="form-group half">
        <text class="label">Max Tokens</text>
        <input class="input" v-model.number="form.maxTokens" type="number" placeholder="2048" />
      </view>
    </view>
    <button class="submit-btn" @click="handleSubmit">保存</button>
    <button v-if="isEdit" class="delete-btn" @click="handleDelete">删除</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createModel, updateModel, getModel, deleteModel } from '@/api/model'

const form = ref({
  name: '',
  apiBaseUrl: '',
  apiKey: '',
  modelName: '',
  temperature: 0.7,
  maxTokens: 2048
})

const isEdit = ref(false)
let editId = null

onLoad(async (query) => {
  if (query.id) {
    isEdit.value = true
    editId = query.id
    try {
      const model = await getModel(query.id)
      form.value = {
        name: model.name,
        apiBaseUrl: model.apiBaseUrl,
        apiKey: model.apiKey,
        modelName: model.modelName,
        temperature: model.temperature,
        maxTokens: model.maxTokens
      }
    } catch (e) {
      uni.showToast({ title: '加载失败', icon: 'none' })
    }
  }
})

async function handleSubmit() {
  try {
    if (!form.value.name || !form.value.apiBaseUrl || !form.value.apiKey || !form.value.modelName) {
      uni.showToast({ title: '请填写完整', icon: 'none' })
      return
    }

    const payload = {
      ...form.value,
      temperature: Number(form.value.temperature),
      maxTokens: Number(form.value.maxTokens)
    }

    if (isEdit.value) {
      await updateModel(editId, payload)
    } else {
      await createModel(payload)
    }
    uni.showToast({ title: '保存成功', icon: 'success' })
    uni.navigateBack()
  } catch (e) {
    uni.showToast({ title: '保存失败: ' + (e.message || ''), icon: 'none' })
  }
}

async function handleDelete() {
  const res = await uni.showModal({ title: '确认删除' })
  if (res.confirm) {
    try {
      await deleteModel(editId)
      uni.showToast({ title: '已删除', icon: 'success' })
      uni.navigateBack()
    } catch (e) {
      uni.showToast({ title: '删除失败', icon: 'none' })
    }
  }
}
</script>

<style scoped>
.container { padding: 16px; }
.form-group { margin-bottom: 16px; }
.label { display: block; font-size: 14px; color: #3D4A46; margin-bottom: 6px; }
.input { border: 1px solid #E8EEEA; border-radius: 8px; padding: 10px 12px; font-size: 15px; background: #fff; }
.form-row { display: flex; gap: 12px; }
.half { flex: 1; }
.submit-btn { background: #7CB9A8; color: #fff; border: none; margin-top: 20px; border-radius: 12px; padding: 12px; }
.delete-btn { background: #e74c3c; color: #fff; border: none; border-radius: 12px; padding: 12px; margin-top: 10px; }
</style>
