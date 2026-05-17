<template>
  <div class="sidebar" style="width:280px;flex-shrink:0;display:flex;flex-direction:column;background:var(--white,#fff);border-right:1px solid var(--border,#E8EEEA)">
    <div style="padding:16px 16px 0">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:12px">
        <h3 style="font-size:18px;font-weight:600;color:var(--text,#3D4A46);margin:0">我的角色</h3>
        <el-button size="small" type="primary" @click="handleImport" :icon="Plus">导入</el-button>
      </div>
      <el-input v-model="search" placeholder="搜索角色..." size="small" clearable :prefix-icon="Search" style="margin-bottom:10px" />
      <div v-if="tags.length > 0" style="display:flex;flex-wrap:wrap;gap:6px;margin-bottom:10px">
        <el-tag :type="selectedTag === '' ? 'primary' : 'info'" size="small" @click="filterByTag('')" style="cursor:pointer">全部</el-tag>
        <el-tag v-for="tag in tags" :key="tag" :type="selectedTag === tag ? 'primary' : 'info'" size="small" @click="filterByTag(tag)" style="cursor:pointer">{{ tag }}</el-tag>
      </div>
    </div>
    <div style="flex:1;overflow-y:auto;padding:0 16px 16px">
      <div v-if="filteredCharacters.length === 0" style="text-align:center;padding:40px 0;color:var(--text-light,#B0C3BA)">暂无角色</div>
      <CharacterCard v-for="char in filteredCharacters" :key="char.id" :character="char" :isSelected="char.id === selectedId" @click="$emit('select', char)" @edit="handleEdit(char)" @delete="handleDelete(char)" />
    </div>
    <input ref="fileInput" type="file" accept=".json,.png" style="display:none" @change="onFileSelected" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { listCharacters, importCharacter, deleteCharacter } from '../api/character'
import { listModels } from '../api/model'
import CharacterCard from './CharacterCard.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({ selectedId: Number })
const emit = defineEmits(['select', 'refresh'])

const characters = ref([])
const search = ref('')
const selectedTag = ref('')
const tags = ref([])
const fileInput = ref(null)

onMounted(() => load())

async function load() {
  try {
    const res = await listCharacters()
    characters.value = res.data
    const tagSet = new Set()
    characters.value.forEach(c => {
      if (c.tags) c.tags.split(',').forEach(t => { const trim = t.trim(); if (trim) tagSet.add(trim) })
    })
    tags.value = Array.from(tagSet)
  } catch (e) { /* silent */ }
}

const filteredCharacters = computed(() => {
  let list = characters.value
  if (search.value) {
    const q = search.value.toLowerCase()
    list = list.filter(c => c.name.toLowerCase().includes(q))
  }
  if (selectedTag.value) {
    list = list.filter(c => c.tags && c.tags.split(',').map(t => t.trim()).includes(selectedTag.value))
  }
  return list
})

function filterByTag(tag) { selectedTag.value = tag }

function handleImport() { fileInput.value.click() }

async function onFileSelected(e) {
  const file = e.target.files[0]
  if (!file) return
  try {
    let modelConfigId = null
    try {
      const models = await listModels()
      if (models.data.length > 0) modelConfigId = models.data[0].id
    } catch(e) {}
    await importCharacter(file, modelConfigId)
    ElMessage.success('导入成功')
    load()
  } catch(e) { ElMessage.error('导入失败: ' + (e.response?.data?.message || e.message)) }
  fileInput.value.value = ''
}

function handleEdit(char) { emit('select', char) /* For now select = shows in chat, edit handled separately */ }

async function handleDelete(char) {
  try {
    await ElMessageBox.confirm(`确认删除「${char.name}」？`, '删除角色', { type: 'warning' })
    await deleteCharacter(char.id)
    ElMessage.success('已删除')
    if (props.selectedId === char.id) emit('select', { id: null })
    load()
  } catch(e) { if (e !== 'cancel') ElMessage.error('删除失败') }
}
</script>
