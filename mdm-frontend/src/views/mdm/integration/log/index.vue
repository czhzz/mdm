<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab1 接收日志 -->
      <el-tab-pane label="接收日志" name="receive">
        <el-form :inline="true" class="mb8">
          <el-form-item label="应用">
            <el-input v-model="receiveQuery.appCode" placeholder="应用编码" clearable style="width: 160px" @keyup.enter="handleReceiveQuery" />
          </el-form-item>
          <el-form-item label="对象">
            <el-input v-model="receiveQuery.objectCode" placeholder="对象编码" clearable style="width: 140px" @keyup.enter="handleReceiveQuery" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="receiveQuery.success" placeholder="全部" clearable style="width: 100px" @change="handleReceiveQuery">
              <el-option label="成功" value="0" />
              <el-option label="失败" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleReceiveQuery">搜索</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="receiveLoading" :data="receiveList" border>
          <el-table-column label="应用" prop="appCode" width="150" />
          <el-table-column label="对象" prop="objectCode" width="120" align="center" />
          <el-table-column label="业务键" prop="businessCode" width="140" show-overflow-tooltip />
          <el-table-column label="结果" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.success === '0' ? 'success' : 'danger'">
                {{ scope.row.success === '0' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="错误信息" prop="errorMsg" min-width="150" show-overflow-tooltip />
          <el-table-column label="耗时" prop="costMs" width="80" align="center">
            <template #default="scope">{{ scope.row.costMs }}ms</template>
          </el-table-column>
          <el-table-column label="IP" prop="ip" width="130" />
          <el-table-column label="时间" prop="createTime" width="160" align="center" />
        </el-table>
        <pagination
          v-show="receiveTotal > 0"
          :total="receiveTotal"
          v-model:page="receiveQuery.pageNum"
          v-model:limit="receiveQuery.pageSize"
          @pagination="getReceiveList"
        />
      </el-tab-pane>

      <!-- Tab2 查询日志 -->
      <el-tab-pane label="查询日志" name="query">
        <el-form :inline="true" class="mb8">
          <el-form-item label="应用">
            <el-input v-model="queryLogQuery.appCode" placeholder="应用编码" clearable style="width: 160px" @keyup.enter="handleQueryLogQuery" />
          </el-form-item>
          <el-form-item label="对象">
            <el-input v-model="queryLogQuery.objectCode" placeholder="对象编码" clearable style="width: 140px" @keyup.enter="handleQueryLogQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQueryLogQuery">搜索</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="queryLogLoading" :data="queryLogList" border>
          <el-table-column label="应用" prop="appCode" width="150" />
          <el-table-column label="对象" prop="objectCode" width="120" align="center" />
          <el-table-column label="结果" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.success === '0' ? 'success' : 'danger'">
                {{ scope.row.success === '0' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="返回条数" prop="resultCount" width="90" align="center" />
          <el-table-column label="错误信息" prop="errorMsg" min-width="150" show-overflow-tooltip />
          <el-table-column label="耗时" prop="costMs" width="80" align="center">
            <template #default="scope">{{ scope.row.costMs }}ms</template>
          </el-table-column>
          <el-table-column label="IP" prop="ip" width="130" />
          <el-table-column label="时间" prop="createTime" width="160" align="center" />
        </el-table>
        <pagination
          v-show="queryLogTotal > 0"
          :total="queryLogTotal"
          v-model:page="queryLogQuery.pageNum"
          v-model:limit="queryLogQuery.pageSize"
          @pagination="getQueryLogList"
        />
      </el-tab-pane>

      <!-- Tab3 分发日志 -->
      <el-tab-pane label="分发日志" name="distribute">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="distQuery.status" placeholder="全部" clearable style="width: 120px" @change="handleDistQuery">
              <el-option label="待发送" value="0" />
              <el-option label="成功" value="1" />
              <el-option label="失败" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="对象">
            <el-input v-model="distQuery.objectCode" placeholder="对象编码" clearable style="width: 140px" @keyup.enter="handleDistQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleDistQuery">搜索</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="distLoading" :data="distList" border>
          <el-table-column label="应用" prop="appName" min-width="120" show-overflow-tooltip />
          <el-table-column label="应用编码" prop="appCode" width="150" />
          <el-table-column label="对象" prop="objectCode" width="110" align="center" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.actionType === 'INSERT' ? 'success' : 'primary'">
                {{ scope.row.actionType === 'INSERT' ? '新增' : '修改' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="数据ID" prop="dataId" width="70" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="scope">
              <el-tag :type="recordStatusTag(scope.row.status!)">
                {{ recordStatusLabel(scope.row.status!) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发送时间" prop="sendTime" width="160" align="center" />
          <el-table-column label="失败原因" prop="errorMsg" min-width="130" show-overflow-tooltip />
          <el-table-column label="操作" width="90" align="center">
            <template #default="scope">
              <el-button v-if="scope.row.status === '2'" link type="primary" icon="Refresh" @click="handleRetry(scope.row)">重推</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="distTotal > 0"
          :total="distTotal"
          v-model:page="distQuery.pageNum"
          v-model:limit="distQuery.pageSize"
          @pagination="getDistList"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 手动清理 -->
    <el-row :gutter="10" style="margin-top: 16px">
      <el-col :span="6">
        <el-date-picker
          v-model="cleanTime"
          type="datetime"
          placeholder="清理截止时间"
          style="width: 100%"
          value-format="YYYY-MM-DD HH:mm:ss"
        />
      </el-col>
      <el-col :span="18">
        <el-button type="danger" plain icon="Delete" :disabled="!cleanTime" @click="handleClean">清理截止时间前的日志</el-button>
        <span style="margin-left: 8px; color: #909399; font-size: 12px">清理当前 Tab 对应日志表，删除后不可恢复</span>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { MdmDistributeLog, MdmQueryLog, MdmReceiveLog } from '@/types'
import {
  listReceiveLog, listQueryLog, listDistributeLog, retryDistributeLog, cleanLog
} from '@/api/mdm/integration'

const activeTab = ref('receive')
const cleanTime = ref('')

// ===== 接收日志 =====
const receiveList = ref<MdmReceiveLog[]>([])
const receiveLoading = ref(false)
const receiveTotal = ref(0)
const receiveQuery = ref({ pageNum: 1, pageSize: 10, appCode: '', objectCode: '', success: '' })

const getReceiveList = async () => {
  receiveLoading.value = true
  try {
    const res = await listReceiveLog(receiveQuery.value)
    receiveList.value = res.rows
    receiveTotal.value = res.total
  } finally {
    receiveLoading.value = false
  }
}
const handleReceiveQuery = () => { receiveQuery.value.pageNum = 1; getReceiveList() }

// ===== 查询日志 =====
const queryLogList = ref<MdmQueryLog[]>([])
const queryLogLoading = ref(false)
const queryLogTotal = ref(0)
const queryLogQuery = ref({ pageNum: 1, pageSize: 10, appCode: '', objectCode: '' })

const getQueryLogList = async () => {
  queryLogLoading.value = true
  try {
    const res = await listQueryLog(queryLogQuery.value)
    queryLogList.value = res.rows
    queryLogTotal.value = res.total
  } finally {
    queryLogLoading.value = false
  }
}
const handleQueryLogQuery = () => { queryLogQuery.value.pageNum = 1; getQueryLogList() }

// ===== 分发日志 =====
const distList = ref<MdmDistributeLog[]>([])
const distLoading = ref(false)
const distTotal = ref(0)
const distQuery = ref({ pageNum: 1, pageSize: 10, status: '', objectCode: '' })

const recordStatusLabel = (s: string) => (s === '1' ? '成功' : s === '2' ? '失败' : '待发送')
const recordStatusTag = (s: string) => (s === '1' ? 'success' : s === '2' ? 'danger' : 'warning')

const getDistList = async () => {
  distLoading.value = true
  try {
    const res = await listDistributeLog(distQuery.value)
    distList.value = res.rows
    distTotal.value = res.total
  } finally {
    distLoading.value = false
  }
}
const handleDistQuery = () => { distQuery.value.pageNum = 1; getDistList() }

const handleRetry = (row: MdmDistributeLog) => {
  ElMessageBox.confirm('确定重推该记录吗？', '系统提示', { type: 'warning' })
    .then(async () => {
      await retryDistributeLog(row.recordId!)
      ElMessage.success('已重推')
      getDistList()
    })
    .catch(() => {})
}

// ===== 手动清理 =====
const handleClean = () => {
  ElMessageBox.confirm(
    `确定清理 ${activeTab.value} 日志表中 ${cleanTime.value} 之前的所有日志吗？删除后不可恢复。`,
    '系统提示',
    { type: 'warning', confirmButtonText: '确认清理' }
  ).then(async () => {
    await cleanLog(activeTab.value, cleanTime.value)
    ElMessage.success('清理完成')
    if (activeTab.value === 'receive') getReceiveList()
    else if (activeTab.value === 'query') getQueryLogList()
    else getDistList()
  }).catch(() => {})
}

onMounted(() => {
  getReceiveList()
  getQueryLogList()
  getDistList()
})
</script>
