<template>
  <div v-if="labels.length" class="promotion-tags" aria-label="促销标签">
    <el-tag
      v-for="label in labels"
      :key="label"
      effect="dark"
      round
      type="danger"
    >
      {{ label }}
    </el-tag>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  promotion: {
    type: Object,
    default: () => ({ labels: [] })
  }
})

const labels = computed(() => props.promotion?.labels || [])
</script>

<style scoped>
.promotion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
