<template>
  <view class="container">
    <view class="header">
      <text class="title">我的角色</text>
      <view style="display:flex;align-items:center;gap:6px">
        <button class="settings-btn" @click="goToSettings" size="mini">&#9881;</button>
        <button class="add-btn" @click="handleImport" size="mini">+ 导入</button>
      </view>
    </view>

    <view v-if="allTags.length > 0" class="tag-bar">
      <text :class="['tag-chip', !selectedTag ? 'tag-active' : '']" @click="filterByTag('')">全部</text>
      <text v-for="tag in allTags" :key="tag" :class="['tag-chip', selectedTag === tag ? 'tag-active' : '']" @click="filterByTag(tag)">{{ tag }}</text>
    </view>

    <view v-if="characters.length === 0" class="empty">
      <text class="empty-icon">🎭</text>
      <text>还没有角色，点击上方导入角色卡</text>
    </view>

    <CharacterCard
      v-for="char in characters"
      :key="char.id"
      :name="char.name"
      :description="char.description"
      :avatar="getAvatarUrl(char)"
      @click="goToChat(char)"
      @longpress="showCharMenu(char)"
    />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import CharacterCard from '@/components/CharacterCard.vue'
import { listCharacters, importCharacter, deleteCharacter } from '@/api/character'
import { listModels } from '@/api/model'

const BASE_URL = 'http://localhost:8080'

const characters = ref([])
const allCharacters = ref([])
const allTags = ref([])
const selectedTag = ref('')

function getAvatarUrl(char) {
  if (char.avatarId) {
    return BASE_URL + '/api/avatars/' + char.avatarId
  }
  return ''
}

onMounted(() => {
  loadCharacters()
})

async function loadCharacters() {
  try {
    const list = await listCharacters()
    allCharacters.value = list
    // Build tag set
    const tagSet = new Set()
    list.forEach(c => {
      if (c.tags) {
        c.tags.split(',').forEach(t => { const trimmed = t.trim(); if (trimmed) tagSet.add(trimmed) })
      }
    })
    allTags.value = Array.from(tagSet)
    filterByTag(selectedTag.value)
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

function filterByTag(tag) {
  selectedTag.value = tag
  if (!tag) {
    characters.value = allCharacters.value
  } else {
    characters.value = allCharacters.value.filter(c => c.tags && c.tags.split(',').map(t => t.trim()).includes(tag))
  }
}

async function handleImport() {
  try {
    const res = await uni.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['json', 'png']
    })
    const filePath = res.tempFiles[0].path

    const models = await listModels()
    let modelConfigId = null
    if (models.length > 0) {
      modelConfigId = models[0].id
    }

    await importCharacter(filePath, modelConfigId)
    uni.showToast({ title: '导入成功', icon: 'success' })
    loadCharacters()
  } catch (e) {
    if (e.errMsg && e.errMsg.includes('cancel')) return
    uni.showToast({ title: '导入失败: ' + (e.message || ''), icon: 'none' })
  }
}

function goToChat(char) {
  uni.navigateTo({ url: `/pages/chat/chat?characterId=${char.id}&characterName=${encodeURIComponent(char.name)}` })
}

function goToSettings() {
  uni.navigateTo({ url: '/pages/settings/settings' })
}

function showCharMenu(char) {
  uni.showActionSheet({
    itemList: ['编辑', '删除'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.navigateTo({ url: `/pages/characters/character-edit?id=${char.id}` })
      } else if (res.tapIndex === 1) {
        confirmDelete(char)
      }
    }
  })
}

async function confirmDelete(char) {
  const res = await uni.showModal({
    title: '删除角色',
    content: `确认删除「${char.name}」？`
  })
  if (res.confirm) {
    await deleteCharacter(char.id)
    uni.showToast({ title: '已删除', icon: 'success' })
    loadCharacters()
  }
}
</script>

<style scoped>
.container { padding: 16px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.title { font-size: 20px; font-weight: bold; }
.add-btn { background-color: #7CB9A8; color: #fff; border: none; }
.settings-btn { background: transparent; border: none; font-size: 20px; padding: 0 4px; color: #92A69E; }
.empty { text-align: center; padding: 60px 0; color: #B0C3BA; }
.empty-icon { font-size: 48px; display: block; margin-bottom: 10px; }
.tag-bar { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.tag-chip { padding: 4px 12px; border-radius: 14px; font-size: 12px; background: #f0f0f0; color: #92A69E; }
.tag-active { background: #7CB9A8; color: #fff; }
</style>
