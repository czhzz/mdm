<template>
  <div class="app-container dashboard-container">
    <!-- 上排：总览卡片 -->
    <el-row :gutter="16" class="mb16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ overview.totalIssues ?? 0 }}</div>
          <div class="stat-label">累计问题数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-value">{{ overview.pendingIssues ?? 0 }}</div>
          <div class="stat-label">待处理问题</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-value">{{ overview.handledIssues ?? 0 }}</div>
          <div class="stat-label">已处理问题</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card info">
          <div class="stat-value">{{ overview.totalObjects ?? 0 }}</div>
          <div class="stat-label">已发布对象</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中排：趋势图 -->
    <el-row :gutter="16" class="mb16">
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>质量问题趋势（近 30 天）</template>
          <div ref="trendChartRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>问题类型分布</template>
          <div ref="typeChartRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 下排：排行 + 最近问题 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>对象问题排行 Top-10</template>
          <div ref="rankChartRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>最近问题</template>
          <el-table :data="recentIssues" size="small" max-height="330">
            <el-table-column label="对象" prop="objectName" width="100" />
            <el-table-column label="类型" width="90">
              <template #default="scope">
                <el-tag size="small" :type="issueTypeTag(scope.row.issueType)">
                  {{ issueTypeLabel(scope.row.issueType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="描述" prop="issueDesc" show-overflow-tooltip />
            <el-table-column label="状态" width="80">
              <template #default="scope">
                <el-tag size="small" :type="scope.row.handleStatus === '0' ? 'danger' : 'success'">
                  {{ scope.row.handleStatus === '0' ? '待处理' : '已处理' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getQualityDashboard } from '@/api/mdm/quality'

const trendChartRef = ref<HTMLElement>()
const typeChartRef = ref<HTMLElement>()
const rankChartRef = ref<HTMLElement>()
const overview = reactive<Record<string, number>>({})
const recentIssues = ref<Array<Record<string, unknown>>>([])

let trendChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null
let rankChart: echarts.ECharts | null = null
let refreshTimer: ReturnType<typeof setInterval> | null = null

function issueTypeLabel(type: string) {
  const map: Record<string, string> = { VALIDATE: '校验失败', DUPLICATE: '重复', MISSING: '缺失' }
  return map[type] || type
}

function issueTypeTag(type: string) {
  const map: Record<string, string> = { VALIDATE: 'warning', DUPLICATE: 'danger', MISSING: 'info' }
  return map[type] || 'info'
}

function renderCharts(data: Record<string, unknown>) {
  // 趋势折线图
  const trend = (data.trend as Array<{ day: string; cnt: number }>) || []
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'category', data: trend.map(t => t.day) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ type: 'line', smooth: true, areaStyle: { opacity: 0.2 }, data: trend.map(t => t.cnt) }]
    })
  }
  // 类型饼图
  const typeDist = (data.typeDist as Array<{ type: string; cnt: number }>) || []
  if (typeChartRef.value) {
    typeChart = echarts.init(typeChartRef.value)
    typeChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['40%', '65%'],
        data: typeDist.map(t => ({ name: issueTypeLabel(t.type), value: t.cnt }))
      }]
    })
  }
  // 对象排行柱状图
  const rank = (data.objectRank as Array<{ objectName: string; issueCount: number }>) || []
  if (rankChartRef.value) {
    rankChart = echarts.init(rankChartRef.value)
    rankChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 60, right: 20, top: 20, bottom: 30 },
      xAxis: { type: 'value', minInterval: 1 },
      yAxis: { type: 'category', data: rank.map(r => r.objectName).reverse() },
      series: [{ type: 'bar', data: rank.map(r => r.issueCount).reverse(), barMaxWidth: 20 }]
    })
  }
}

async function loadDashboard() {
  const res = await getQualityDashboard()
  const data = res.data || {}
  Object.assign(overview, data.overview || {})
  recentIssues.value = (data.recentIssues as Array<Record<string, unknown>>) || []
  await nextTick()
  renderCharts(data)
}

function resizeCharts() {
  trendChart?.resize()
  typeChart?.resize()
  rankChart?.resize()
}

onMounted(() => {
  loadDashboard()
  window.addEventListener('resize', resizeCharts)
  // 30s 自动刷新
  refreshTimer = setInterval(loadDashboard, 30000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  if (refreshTimer) clearInterval(refreshTimer)
  trendChart?.dispose()
  typeChart?.dispose()
  rankChart?.dispose()
})
</script>

<style scoped>
.dashboard-container { min-height: calc(100vh - 120px); }
.mb16 { margin-bottom: 16px; }
.stat-card { text-align: center; }
.stat-value { font-size: 36px; font-weight: bold; color: #409eff; }
.stat-card.warning .stat-value { color: #e6a23c; }
.stat-card.success .stat-value { color: #67c23a; }
.stat-card.info .stat-value { color: #909399; }
.stat-label { margin-top: 8px; color: #909399; font-size: 14px; }
.chart-box { height: 300px; }
</style>