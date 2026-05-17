<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin:8px 0 12px">
      <span style="font-size:14px;font-weight:500;color:var(--text,#3D4A46)">人设管理</span>
      <el-button size="small" type="primary" @click="openDialog()" :icon="Plus">添加</el-button>
    </div>
    <div v-for="p in personas" :key="p.id" class="item-card" :class="{ active: p.isActive }">
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span class="item-name">{{ p.name }}</span>
        <el-tag v-if="p.isActive" size="small" type="success" effect="dark">当前</el-tag>
      </div>
      <div class="item-info">{{ (p.content || '').substring(0, 60) }}{{ (p.content || '').length > 60 ? '...' : '' }}</div>
      <div style="display:flex;gap:6px;margin-top:8px">
        <el-button size="small" @click="openDialog(p)">编辑</el-button>
        <el-button v-if="!p.isActive" size="small" @click="handleActivate(p.id)">启用</el-button>
        <el-popconfirm title="确认删除？" @confirm="handleDelete(p.id)">
          <el-button size="small" type="danger">删除</el-button>
        </el-popconfirm>
      </div>
    </div>
    <el-empty v-if="personas.length === 0" description="暂无数据" />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑人设' : '添加人设'" width="90%">
      <el-form :model="form" label-position="top">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
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
import { listPersonas, getPersona, createPersona, updatePersona, deletePersona, activatePersona } from '../api/persona'
import { ElMessage } from 'element-plus'

const personas = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ name: '', content: '' })

onMounted(() => load())

async function load() {
  try { const res = await listPersonas(); personas.value = res.data } catch(e) {}
}

function openDialog(persona) {
  if (persona) {
    isEdit.value = true; editId.value = persona.id
    form.value = { ...persona }
  } else {
    isEdit.value = false; editId.value = null
    form.value = { name: '', content: '' }
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    if (isEdit.value) await updatePersona(editId.value, form.value)
    else await createPersona(form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch(e) { ElMessage.error('保存失败') }
}

async function handleActivate(id) { try { await activatePersona(id); ElMessage.success('已启用'); load() } catch(e) {} }
async function handleDelete(id) { try { await deletePersona(id); ElMessage.success('已删除'); load() } catch(e) {} }
</script>

<style scoped>
.item-card { background: var(--primary-bg, #EAF5F1); border-radius: 10px; padding: 12px; margin-bottom: 8px; border: 1px solid transparent; }
.item-card.active { border-color: var(--primary, #7CB9A8); }
.item-name { font-size: 14px; font-weight: 500; color: var(--text, #3D4A46); }
.item-info { font-size: 12px; color: var(--text-secondary, #92A69E); margin-top: 4px; }
</style>
