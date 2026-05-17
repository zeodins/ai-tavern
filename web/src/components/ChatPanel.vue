<template>
  <div class="chat-panel" style="flex:1;display:flex;flex-direction:column;background:var(--white,#fff);margin:0;overflow:hidden;">
    <div v-if="!character" style="flex:1;display:flex;align-items:center;justify-content:center;color:var(--text-light,#B0C3BA);font-size:16px;">
      选择一个角色开始对话
    </div>
    <div v-else style="flex:1;display:flex;align-items:center;justify-content:center;color:var(--text-light,#B0C3BA);font-size:16px;">
      聊天面板 - {{ character.name }}
    </div>
  </div>
</template>

<script setup>
defineProps({ character: Object })
defineEmits(['refresh'])
</script>
