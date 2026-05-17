<template>
  <div>
    <h4 style="margin:8px 0 12px;color:var(--text,#3D4A46)">主题设置</h4>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
      <div v-for="t in themes" :key="t.key" :class="['theme-card', { active: current === t.key }]" @click="switchTheme(t.key)"
        :style="{ background: t.primary }">
        <span class="theme-label">{{ t.name }}</span>
        <span v-if="current === t.key" class="theme-check">&#10003;</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { getAllThemes, getThemeName, setTheme } from '../theme'

const themes = getAllThemes()
const current = ref(getThemeName())

function switchTheme(key) {
  current.value = key
  setTheme(key)
}
</script>

<style scoped>
.theme-card { height: 64px; border-radius: 12px; display: flex; align-items: center; justify-content: center; cursor: pointer; position: relative; transition: .15s; opacity: 0.8; }
.theme-card:hover { opacity: 1; transform: scale(1.03); }
.theme-card.active { opacity: 1; box-shadow: 0 0 0 3px rgba(255,255,255,0.8), 0 4px 12px rgba(0,0,0,0.15); }
.theme-label { color: #fff; font-size: 14px; font-weight: 600; text-shadow: 0 1px 2px rgba(0,0,0,0.2); }
.theme-check { position: absolute; top: 6px; right: 10px; color: #fff; font-size: 18px; font-weight: 700; }
</style>
