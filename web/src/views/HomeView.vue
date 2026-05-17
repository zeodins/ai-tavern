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

const characters = ref([])
const selectedCharacterId = ref(null)

const selectedCharacter = computed(() =>
  characters.value.find(c => c.id === selectedCharacterId.value) || null
)

function onSelectCharacter(character) {
  selectedCharacterId.value = character.id
  characters.value = characters.value.map(c => ({
    ...c,
    _selected: c.id === character.id
  }))
}

function loadCharacters() {
  // CharacterSidebar handles its own loading; this is for parent coordination
}
</script>

<style scoped>
.home-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--bg, #F4F7F6);
}
</style>
