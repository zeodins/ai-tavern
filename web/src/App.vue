<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { getCurrentTheme, applyTheme } from './theme'

onMounted(() => {
  const theme = getCurrentTheme()
  applyTheme(theme.name ? getThemeName() : 'mint')
})
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: var(--bg, #F4F7F6); }
</style>
