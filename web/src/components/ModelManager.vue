<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin:8px 0 12px">
      <span style="font-size:14px;font-weight:500;color:var(--text,#3D4A46)">模型配置</span>
      <el-button size="small" type="primary" @click="openDialog()" :icon="Plus">添加</el-button>
    </div>
    <div v-for="m in models" :key="m.id" class="item-card" :class="{ active: m.isActive }">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span class="item-name">{{ m.name }}</span>
        <el-tag v-if="m.isActive" size="small" type="success" effect="dark">当前</el-tag>
      </div>
      <div class="item-info">{{ m.modelName }} · {{ m.apiBaseUrl }}</div>
      <div style="display:flex;gap:6px;margin-top:8px">
        <el-button size="small" @click="openDialog(m)">编辑</el-button>
        <el-button v-if="!m.isActive" size="small" @click="handleActivate(m.id)">启用</el-button>
        <el-popconfirm title="确认删除？" @confirm="handleDelete(m.id)">
          <el-button size="small" type="danger">删除</el-button>
        </el-popconfirm>
      </div>
    </div>
    <el-empty v-if="models.length === 0" description="暂无模型" />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑模型' : '添加模型'" width="90%">
      <el-form :model="form" label-position="top">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="API 地址"><el-input v-model="form.apiBaseUrl" placeholder="https://api.deepseek.com" /></el-form-item>
        <el-form-item label="API Key"><el-input v-model="form.apiKey" type="password" show-password /></el-form-item>
        <el-form-item label="模型名"><el-input v-model="form.modelName" placeholder="deepseek-chat" /></el-form-item>
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="Temperature"><el-input-number v-model="form.temperature" :min="0" :max="2" :step="0.1" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="Max Tokens"><el-input-number v-model="form.maxTokens" :min="100" :step="100" style="width:100%" /></el-form-item></el-col>
        </el-row>
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
import { listModels, getModel, createModel, updateModel, deleteModel, activateModel } from '../api/model'
import { ElMessage } from 'element-plus'

const models = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ name: '', apiBaseUrl: '', apiKey: '', modelName: '', temperature: 0.7, maxTokens: 2048 })

onMounted(() => load())

async function load() {
  try { const res = await listModels(); models.value = res.data } catch(e) {}
}

function openDialog(model) {
  if (model) {
    isEdit.value = true; editId.value = model.id
    form.value = { ...model }
  } else {
    isEdit.value = false; editId.value = null
    form.value = { name: '', apiBaseUrl: '', apiKey: '', modelName: '', temperature: 0.7, maxTokens: 2048 }
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    const payload = { ...form.value, temperature: Number(form.value.temperature), maxTokens: Number(form.value.maxTokens) }
    if (isEdit.value) await updateModel(editId.value, payload)
    else await createModel(payload)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch(e) { ElMessage.error('保存失败') }
}

async function handleActivate(id) { try { await activateModel(id); ElMessage.success('已启用'); load() } catch(e) {} }
async function handleDelete(id) { try { await deleteModel(id); ElMessage.success('已删除'); load() } catch(e) {} }
</script>

<style scoped>
.item-card { background: var(--primary-bg, #EAF5F1); border-radius: 10px; padding: 12px; margin-bottom: 8px; border: 1px solid transparent; }
.item-card.active { border-color: var(--primary, #7CB9A8); }
.item-name { font-size: 14px; font-weight: 500; color: var(--text, #3D4A46); }
.item-info { font-size: 12px; color: var(--text-secondary, #92A69E); margin-top: 4px; }
</style>
