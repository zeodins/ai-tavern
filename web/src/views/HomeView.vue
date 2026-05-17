<template>
  <div class="home-layout">
    <CharacterSidebar
      :selectedId="selectedCharacterId"
      @select="onSelectCharacter"
      @refresh="loadCharacters"
    />
    <ChatPanel
      :character="selectedCharacter"
      @refresh="loadCharacters"
    />
    <ToolsSidebar
      :characterId="selectedCharacterId"
      @refresh="loadCharacters"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import CharacterSidebar from '../components/CharacterSidebar.vue'
import ChatPanel from '../components/ChatPanel.vue'
import ToolsSidebar from '../components/ToolsSidebar.vue'

const selectedCharacter = ref(null)
const selectedCharacterId = computed(() => selectedCharacter.value?.id || null)

function onSelectCharacter(character) {
  selectedCharacter.value = character
}

function loadCharacters() {}
</script>

<style scoped>
.home-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--bg, #F4F7F6);
}
</style>
