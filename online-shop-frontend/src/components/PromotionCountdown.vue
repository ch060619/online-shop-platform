<template>
  <div v-if="hasCountdown" class="promotion-countdown">
    <span class="countdown-title">{{ promotion.title }}</span>
    <strong v-if="remaining.total > 0">{{ formattedTime }}</strong>
    <strong v-else>活动已结束</strong>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  promotion: {
    type: Object,
    default: () => ({})
  }
})

const now = ref(Date.now())
let timerId

const endTimestamp = computed(() => {
  const timestamp = new Date(props.promotion?.endsAt || '').getTime()
  return Number.isNaN(timestamp) ? 0 : timestamp
})

const hasCountdown = computed(() => endTimestamp.value > 0)

const remaining = computed(() => {
  const total = Math.max(endTimestamp.value - now.value, 0)
  const seconds = Math.floor(total / 1000)
  return {
    total,
    days: Math.floor(seconds / 86400),
    hours: Math.floor((seconds % 86400) / 3600),
    minutes: Math.floor((seconds % 3600) / 60),
    seconds: seconds % 60
  }
})

const padTime = (value) => String(value).padStart(2, '0')

const formattedTime = computed(() => (
  `${remaining.value.days}天 ${padTime(remaining.value.hours)}:${padTime(remaining.value.minutes)}:${padTime(remaining.value.seconds)}`
))

onMounted(() => {
  timerId = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onBeforeUnmount(() => {
  window.clearInterval(timerId)
})
</script>

<style scoped>
.promotion-countdown {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-top: 1px solid #f1b6a3;
  border-bottom: 1px solid #f1b6a3;
  color: #9a3412;
}

.countdown-title {
  color: #4b5c76;
}

.promotion-countdown strong {
  color: #c2410c;
  font-size: 18px;
  white-space: nowrap;
}

@media (max-width: 860px) {
  .promotion-countdown {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
