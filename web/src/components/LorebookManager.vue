<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin:8px 0 12px">
      <span style="font-size:14px;font-weight:500;color:var(--text,#3D4A46)">世界书</span>
      <el-button size="small" type="primary" @click="openDialog()" :icon="Plus" :disabled="!characterId">添加</el-button>
    </div>
    <div v-if="!characterId" style="padding:32px 0;text-align:center;color:var(--text-secondary,#92A69E);font-size:13px;">
      请先选择角色
    </div>
    <template v-else>
      <div v-for="entry in entries" :key="entry.id" class="item-card">
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="item-name">关键词</span>
        </div>
        <div class="item-info">{{ entry.keys }}</div>
        <div class="item-content">{{ (entry.content || '').substring(0, 80) }}{{ (entry.content || '').length > 80 ? '...' : '' }}</div>
        <div style="display:flex;gap:6px;margin-top:8px">
          <el-button size="small" @click="openDialog(entry)">编辑</el-button>
          <el-popconfirm title="确认删除？" @confirm="handleDelete(entry.id)">
            <el-button size="small" type="danger">删除</el-button>
          </el-popconfirm>
        </div>
      </div>
      <el-empty v-if="entries.length === 0" description="暂无条目" />
    </template>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑条目' : '添加条目'" width="90%">
      <el-form :model="form" label-position="top">
        <el-form-item label="关键词（逗号分隔）"><el-input v-model="form.keys" placeholder="关键词1, 关键词2, ..." /></el-form-item>
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
import { ref, onMounted, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { listLorebook, createLorebookEntry, updateLorebookEntry, deleteLorebookEntry } from '../api/lorebook'
import { ElMessage } from 'element-plus'

const props = defineProps({ characterId: Number })
const entries = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ keys: '', content: '' })

onMounted(() => load())
watch(() => props.characterId, () => load())

async function load() {
  if (!props.characterId) { entries.value = []; return }
  try { const res = await listLorebook(props.characterId); entries.value = res.data } catch(e) {}
}

function openDialog(entry) {
  if (entry) {
    isEdit.value = true; editId.value = entry.id
    form.value = { keys: entry.keys, content: entry.content }
  } else {
    isEdit.value = false; editId.value = null
    form.value = { keys: '', content: '' }
  }
  dialogVisible.value = true
}

async function handleSave() {
  try {
    if (isEdit.value) await updateLorebookEntry(editId.value, form.value)
    else await createLorebookEntry(props.characterId, form.value)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch(e) { ElMessage.error('保存失败') }
}

async function handleDelete(id) { try { await deleteLorebookEntry(id); ElMessage.success('已删除'); load() } catch(e) {} }
</script>

<style scoped>
.item-card { background: var(--primary-bg, #EAF5F1); border-radius: 10px; padding: 12px; margin-bottom: 8px; border: 1px solid transparent; }
.item-name { font-size: 14px; font-weight: 500; color: var(--text, #3D4A46); }
.item-info { font-size: 12px; color: var(--text-secondary, #92A69E); margin-top: 4px; }
.item-content { font-size: 12px; color: var(--text, #3D4A46); margin-top: 6px; line-height: 1.4; }
</style>
