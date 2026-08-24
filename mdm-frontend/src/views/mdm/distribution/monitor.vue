<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="hover" class="monitor-card">
          <div class="monitor-value" :class="{ danger: (monitor.dlxCount as number) > 100 }">{{ monitor.dlxCount ?? '-' }}</div>
          <div class="monitor-label">死信队列积压</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="monitor-card">
          <div class="monitor-value success">{{ monitor.successRate ?? '-' }}%</div>
          <div class="monitor-label">分发成功率（近 100 条）</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="monitor-card">
          <div class="monitor-value info">{{ monitor.totalRecords ?? 0 }}</div>
          <div class="monitor-label">统计记录数</div>
        </el-card>
      </el-col>
    </el-row>
    <el-alert type="info" :closable="false" style="margin-top: 16px">
      MQ 通道说明：订阅方消费失败的消息进入死信队列 <code>mdm.distribution.dlx.queue</code>，
      积压超过 100 条建议检查订阅方消费状态。数据每 30s 自动刷新。
    </el-alert>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { getDistributionMonitor } from '@/api/mdm/distribution'

const monitor = reactive<Record<string, unknown>>({})
let timer: ReturnType<typeof setInterval> | null = null

async function loadMonitor() {
  const res = await getDistributionMonitor()
  Object.assign(monitor, res.data || {})
}

onMounted(() => {
  loadMonitor()
  timer = setInterval(loadMonitor, 30000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.monitor-card { text-align: center; }
.monitor-value { font-size: 36px; font-weight: bold; color: #409eff; }
.monitor-value.danger { color: #f56c6c; }
.monitor-value.success { color: #67c23a; }
.monitor-value.info { color: #909399; }
.monitor-label { margin-top: 8px; color: #909399; font-size: 14px; }
</style>