<template>
  <view class="container">
    <view class="form-group">
      <text class="label">名称</text>
      <input class="input" v-model="form.name" placeholder="角色名" />
    </view>
    <view class="form-group">
      <text class="label">描述</text>
      <input class="input" v-model="form.description" placeholder="一句话描述" />
    </view>
    <view class="form-group">
      <text class="label">标签 (逗号分隔)</text>
      <input class="input" v-model="form.tags" placeholder="奇幻, 日系" />
    </view>
    <view class="form-group">
      <text class="label">系统提示词</text>
      <textarea class="textarea" v-model="form.systemPrompt" placeholder="角色的 system prompt" />
    </view>
    <view class="form-group">
      <text class="label">作者注记</text>
      <textarea class="textarea" v-model="form.authorNote" placeholder="注入到上下文中的注记" style="height:80px" />
    </view>

    <view class="preset-section" v-if="activePresets.length > 0">
      <text class="section-title">套用预设</text>
      <view v-for="p in activePresets" :key="p.id" class="preset-item">
        <text class="preset-name">{{ p.name }}</text>
        <text class="preset-preview">{{ p.content.substring(0, 50) }}...</text>
        <button size="mini" class="preset-apply-btn" @click="handleApplyPreset(p.id)">套用</button>
      </view>
    </view>

    <button class="submit-btn" @click="handleSave">保存</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getCharacter, updateCharacter, applyPreset } from '@/api/character'
import { listPersonas } from '@/api/persona'
import { listPresets } from '@/api/preset'

const form = ref({ name: '', description: '', tags: '', systemPrompt: '', authorNote: '' })
const activePresets = ref([])
let editId = null

onLoad(async (query) => {
  if (query.id) {
    editId = query.id
    try {
      const char = await getCharacter(query.id)
      form.value = {
        name: char.name || '',
        description: char.description || '',
        tags: char.tags || '',
        systemPrompt: char.systemPrompt || '',
        authorNote: char.authorNote || ''
      }
    } catch(e) {}
  }
  loadPresets()
})

async function loadPresets() {
  try {
    const presets = await listPresets()
    activePresets.value = presets.filter(p => p.isActive)
  } catch(e) {}
}

async function handleApplyPreset(presetId) {
  try {
    await applyPreset(editId, presetId)
    uni.showToast({ title: '已套用', icon: 'success' })
    const char = await getCharacter(editId)
    form.value.systemPrompt = char.systemPrompt || ''
  } catch(e) {
    uni.showToast({ title: '套用失败', icon: 'none' })
  }
}

async function handleSave() {
  try {
    await updateCharacter(editId, form.value)
    uni.showToast({ title: '保存成功', icon: 'success' })
    uni.navigateBack()
  } catch(e) {
    uni.showToast({ title: '保存失败', icon: 'none' })
  }
}
</script>

<style scoped>
.container { padding: 16px; }
.form-group { margin-bottom: 16px; }
.label { display: block; font-size: 14px; color: #3D4A46; margin-bottom: 6px; }
.input { border: 1px solid #E8EEEA; border-radius: 8px; padding: 10px 12px; font-size: 15px; background: #fff; }
.textarea { border: 1px solid #E8EEEA; border-radius: 8px; padding: 10px 12px; font-size: 15px; background: #fff; height: 120px; width: 100%; }
.section-title { font-size: 16px; font-weight: 500; color: #3D4A46; margin-bottom: 10px; display: block; }
.preset-section { margin-bottom: 20px; margin-top: 8px; }
.preset-item { background: #F4F7F6; border-radius: 10px; padding: 10px 14px; margin-bottom: 8px; display: flex; align-items: center; gap: 10px; }
.preset-name { font-size: 14px; font-weight: 500; color: #3D4A46; flex-shrink: 0; min-width: 60px; }
.preset-preview { font-size: 12px; color: #92A69E; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.preset-apply-btn { background: #7CB9A8; color: #fff; border: none; border-radius: 12px; padding: 2px 10px; font-size: 11px; flex-shrink: 0; }
.submit-btn { background: #7CB9A8; color: #fff; border: none; border-radius: 12px; padding: 13px; width: 100%; font-size: 15px; font-weight: 500; margin-top: 4px; }
</style>
