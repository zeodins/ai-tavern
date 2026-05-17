<template>
  <div class="tools-sidebar" style="width:320px;flex-shrink:0;background:var(--white,#fff);border-left:1px solid var(--border,#E8EEEA);display:flex;flex-direction:column">
    <el-tabs v-model="activeTab" class="tools-tabs" stretch>
      <el-tab-pane label="模型" name="models">
        <ModelManager />
      </el-tab-pane>
      <el-tab-pane label="人设" name="personas">
        <PersonaManager />
      </el-tab-pane>
      <el-tab-pane label="预设" name="presets">
        <PresetManager :characterId="characterId" />
      </el-tab-pane>
      <el-tab-pane label="世界书" name="lorebook">
        <LorebookManager :characterId="characterId" />
      </el-tab-pane>
      <el-tab-pane label="设置" name="settings">
        <SettingsPanel />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ModelManager from './ModelManager.vue'
import PersonaManager from './PersonaManager.vue'
import PresetManager from './PresetManager.vue'
import LorebookManager from './LorebookManager.vue'
import SettingsPanel from './SettingsPanel.vue'

defineProps({ characterId: Number })
const activeTab = ref('models')
</script>

<style scoped>
.tools-sidebar { overflow: hidden; }
.tools-tabs { height: 100%; display: flex; flex-direction: column; }
.tools-tabs :deep(.el-tabs__content) { flex: 1; overflow-y: auto; padding: 0 12px; }
.tools-tabs :deep(.el-tabs__header) { margin: 0; flex-shrink: 0; }
</style>
