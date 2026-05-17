<template>
  <view class="container">
    <view class="header">
      <text class="title">我的角色</text>
      <button class="add-btn" @click="handleImport" size="mini">+ 导入</button>
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
      :avatar="char.avatarPath"
      @click="goToChat(char)"
      @longpress="confirmDelete(char)"
    />
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import CharacterCard from '@/components/CharacterCard.vue'
import { listCharacters, importCharacter, deleteCharacter } from '@/api/character'
import { listModels } from '@/api/model'

const characters = ref([])

onMounted(() => {
  loadCharacters()
})

async function loadCharacters() {
  try {
    characters.value = await listCharacters()
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
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
.add-btn { background-color: #7B68EE; color: #fff; border: none; }
.empty { text-align: center; padding: 60px 0; color: #999; }
.empty-icon { font-size: 48px; display: block; margin-bottom: 10px; }
</style>
