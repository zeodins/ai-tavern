<template>
  <div class="tools-sidebar" style="width:300px;flex-shrink:0;background:var(--white,#fff);border-left:1px solid var(--border,#E8EEEA)">
    <div v-if="!characterId" style="padding:40px 16px;text-align:center;color:var(--text-light,#B0C3BA);font-size:14px;">
      选择角色以查看工具
    </div>
    <div v-else style="padding:16px;color:var(--text-light,#B0C3BA);font-size:14px;text-align:center;">
      工具面板
    </div>
  </div>
</template>

<script setup>
defineProps({ characterId: Number })
defineEmits(['refresh'])
</script>
