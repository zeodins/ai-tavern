<template>
  <div :class="['char-card', { selected: isSelected }]" @click="$emit('click', character)">
    <div class="avatar" v-if="!character.avatarId">
      {{ character.name?.charAt(0) }}
    </div>
    <img v-else class="avatar-img" :src="`/api/avatars/${character.avatarId}`" />
    <div class="info">
      <div class="name">{{ character.name }}</div>
      <div class="desc">{{ character.description }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({ character: Object, isSelected: Boolean })
defineEmits(['click', 'edit', 'delete'])
</script>

<style scoped>
.char-card {
  display: flex; align-items: center; padding: 10px 12px; border-radius: 12px;
  margin-bottom: 6px; cursor: pointer; transition: .15s;
  border: 1px solid transparent;
}
.char-card:hover { background: var(--primary-bg, #EAF5F1); }
.char-card.selected { background: var(--primary-bg, #EAF5F1); border-color: var(--primary, #7CB9A8); }
.avatar, .avatar-img {
  width: 42px; height: 42px; border-radius: 12px; margin-right: 10px; flex-shrink: 0;
  background: linear-gradient(135deg, var(--primary-light, #B7DDD2), var(--primary, #7CB9A8));
  display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 17px; font-weight: 600;
}
.avatar-img { object-fit: cover; }
.info { flex: 1; min-width: 0; }
.name { font-size: 14px; font-weight: 500; color: var(--text, #3D4A46); }
.desc { font-size: 12px; color: var(--text-secondary, #92A69E); margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
