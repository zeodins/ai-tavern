<template>
  <view class="container">
    <view class="form-group">
      <text class="label">名称</text>
      <input class="input" v-model="form.name" placeholder="如: 默认人设" />
    </view>
    <view class="form-group">
      <text class="label">内容 (描述你是谁)</text>
      <textarea class="textarea" v-model="form.content" placeholder="如: 你是一个冒险者..." />
    </view>
    <button class="submit-btn" @click="handleSubmit">保存</button>
    <button v-if="isEdit" class="delete-btn" @click="handleDelete">删除</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createPersona, updatePersona, getPersona, deletePersona } from '@/api/persona'

const form = ref({ name: '', content: '' })
const isEdit = ref(false)
let editId = null

onLoad(async (query) => {
  if (query.id) {
    isEdit.value = true; editId = query.id
    try { const p = await getPersona(query.id); form.value = { name: p.name, content: p.content } } catch(e) {}
  }
})

async function handleSubmit() {
  if (!form.value.name || !form.value.content) { uni.showToast({ title: '请填写完整', icon: 'none' }); return }
  try {
    if (isEdit.value) await updatePersona(editId, form.value)
    else await createPersona(form.value)
    uni.showToast({ title: '保存成功', icon: 'success' }); uni.navigateBack()
  } catch(e) { uni.showToast({ title: '保存失败', icon: 'none' }) }
}

async function handleDelete() {
  const r = await uni.showModal({ title: '确认删除' })
  if (r.confirm) { try { await deletePersona(editId); uni.navigateBack() } catch(e) {} }
}
</script>

<style scoped>
.container { padding: 16px; }
.form-group { margin-bottom: 16px; }
.label { display: block; font-size: 14px; color: #333; margin-bottom: 6px; }
.input { border: 1px solid #ddd; border-radius: 8px; padding: 10px 12px; font-size: 15px; background: #fff; }
.textarea { border: 1px solid #ddd; border-radius: 8px; padding: 10px 12px; font-size: 15px; background: #fff; height: 160px; width: 100%; }
.submit-btn { background: #7B68EE; color: #fff; border: none; margin-top: 20px; border-radius: 8px; padding: 12px; }
.delete-btn { background: #e74c3c; color: #fff; border: none; border-radius: 8px; padding: 12px; margin-top: 10px; }
</style>
