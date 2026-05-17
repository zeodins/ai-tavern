<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin:8px 0 12px">
      <span style="font-size:14px;font-weight:500;color:var(--text,#3D4A46)">预设管理</span>
      <el-button size="small" type="primary" @click="openDialog()" :icon="Plus">添加</el-button>
    </div>
    <div v-for="p in presets" :key="p.id" class="item-card">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span class="item-name">{{ p.name }}</span>
        <el-switch v-model="p.isActive" @change="handleToggle(p)" size="small" />
      </div>
      <div class="item-info">{{ (p.content || '').substring(0, 60) }}{{ (p.content || '').length > 60 ? '...' : '' }}</div>
      <div style="display:flex;gap:6px;margin-top:8px">
        <el-button size="small" @click="openDialog(p)">编辑</el-button>
        <el-button size="small" @click="handleApplyToCharacter(p.id)" :disabled="!characterId">应用至角色</el-button>
        <el-popconfirm title="确认删除？" @confirm="handleDelete(p.id)">
          <el-button size="small" type="danger">删除</el-button>
        </el-popconfirm>
      </div>
    </div>
    <el-empty v-if="presets.length === 0" description="暂无数据" />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑预设' : '添加预设'" width="90%">
      <el-form :model="form" label-position="top">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="8" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { listPresets, getPreset, createPreset, updatePreset, deletePreset, togglePreset } from '../api/preset'
import http from '../api/request'
import { ElMessage } from 'element-plus'

const props = defineProps({ characterId: Number })
const presets = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ name: '', content: '' })

onMounted(() => load())

async function load() {
  try { const res = await listPresets(); presets.value = res.data } catch(e) {}
}

function openDialog(preset) {
  if (preset) {
    isEdit.value = true; editId.value = preset.id
    form.value = { ...preset }
  } else {
    isEdit.value = false; editId.value = null
    form.value = { name: '', content: '' }
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    if (isEdit.value) await updatePreset(editId.value, form.value)
    else await createPreset(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch(e) { ElMessage.error('保存失败') }
}

async function handleToggle(preset) {
  try { await togglePreset(preset.id) } catch(e) { preset.isActive = !preset.isActive }
}

async function handleApplyToCharacter(presetId) {
  try {
    await http.put(`/characters/${props.characterId}/preset`, { presetId })
    ElMessage.success('已应用至角色')
  } catch(e) { ElMessage.error('应用失败') }
}

async function handleDelete(id) { try { await deletePreset(id); ElMessage.success('已删除'); load() } catch(e) {} }
</script>

<style scoped>
.item-card { background: var(--primary-bg, #EAF5F1); border-radius: 10px; padding: 12px; margin-bottom: 8px; border: 1px solid transparent; }
.item-name { font-size: 14px; font-weight: 500; color: var(--text, #3D4A46); }
.item-info { font-size: 12px; color: var(--text-secondary, #92A69E); margin-top: 4px; }
</style>
