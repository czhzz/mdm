<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab1 分发配置 -->
      <el-tab-pane label="分发配置" name="config">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">新增配置</el-button>
          </el-col>
        </el-row>
        <el-table v-loading="loading" :data="distList" border>
          <el-table-column label="订阅应用" prop="appName" min-width="130" />
          <el-table-column label="数据对象" prop="objectName" min-width="130" />
          <el-table-column label="触发时机" width="120" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.triggerType === 'IMMEDIATE' ? 'success' : 'info'">
                {{ scope.row.triggerType === 'IMMEDIATE' ? '变更即推' : '手动' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分发方式" width="90" align="center">
            <template #default="scope">
              <el-tag :type="(scope.row.channel || 'HTTP') === 'MQ' ? 'warning' : 'primary'" size="small">
                {{ (scope.row.channel || 'HTTP') === 'MQ' ? 'MQ队列' : 'HTTP' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="回调地址/队列" prop="endpointUrl" min-width="220" show-overflow-tooltip>
            <template #default="scope">
              <span v-if="scope.row.channel === 'MQ'">{{ scope.row.queueName || 'mdm.dist.<对象编码>' }}</span>
              <span v-else>{{ scope.row.endpointUrl }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.enabled === '1' ? 'success' : 'info'">
                {{ scope.row.enabled === '1' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="total > 0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
        />
      </el-tab-pane>

      <!-- Tab2 分发监控 -->
      <el-tab-pane label="分发监控" name="monitor">
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
      </el-tab-pane>
    </el-tabs>

    <!-- 分发配置表单 -->
    <el-dialog v-model="open" :title="title" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="订阅应用" prop="appId">
          <el-select v-model="form.appId" placeholder="选择订阅应用" style="width: 100%">
            <el-option v-for="app in appList" :key="app.appId!" :label="app.appName" :value="app.appId" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据对象" prop="objectId">
          <el-select v-model="form.objectId" placeholder="选择数据对象" style="width: 100%">
            <el-option
              v-for="obj in objectOptions"
              :key="obj.objectId!"
              :label="obj.objectName + '（' + obj.objectCode + '）'"
              :value="obj.objectId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="触发时机" prop="triggerType">
          <el-radio-group v-model="form.triggerType">
            <el-radio value="IMMEDIATE">变更即推</el-radio>
            <el-radio value="MANUAL">手动重推</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分发方式" prop="channel">
          <el-radio-group v-model="form.channel">
            <el-radio value="HTTP">HTTP 回调</el-radio>
            <el-radio value="MQ">RabbitMQ 队列</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.channel === 'MQ'" label="队列名称" prop="queueName">
          <el-input v-model="form.queueName" placeholder="默认 mdm.dist.<对象编码>，可自定义" />
        </el-form-item>
        <el-form-item v-if="form.channel !== 'MQ'" label="回调地址" prop="endpointUrl">
          <el-input v-model="form.endpointUrl" placeholder="如 http://erp.example.com/mdm/push" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.enabled" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { MdmApp, MdmDistributeApi, MdmObject } from '@/types'
import { listObject } from '@/api/mdm/model'
import { listApp, listDist, addDist, editDist, delDist, getDistributeMonitor } from '@/api/mdm/integration'

const activeTab = ref('config')
const objectOptions = ref<MdmObject[]>([])
const appList = ref<MdmApp[]>([])

// ===== 分发配置 =====
const loading = ref(false)
const distList = ref<MdmDistributeApi[]>([])
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10 })
const open = ref(false)
const title = ref('')
const form = ref<MdmDistributeApi>({ triggerType: 'IMMEDIATE', enabled: '1', channel: 'HTTP' })
const formRef = ref<FormInstance>()
const rules = {
  appId: [{ required: true, message: '请选择订阅应用', trigger: 'change' }],
  objectId: [{ required: true, message: '请选择数据对象', trigger: 'change' }],
  endpointUrl: [{ required: true, message: '回调地址不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listDist(queryParams.value)
    distList.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.value = { triggerType: 'IMMEDIATE', enabled: '1', channel: 'HTTP' }
  title.value = '新增分发配置'
  open.value = true
}

const handleEdit = (row: MdmDistributeApi) => {
  form.value = { ...row }
  title.value = '编辑分发配置'
  open.value = true
}

const submitForm = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    if (form.value.distId != null) {
      await editDist(form.value)
    } else {
      await addDist(form.value)
    }
    ElMessage.success('保存成功')
    open.value = false
    getList()
  })
}

const handleDelete = (row: MdmDistributeApi) => {
  ElMessageBox.confirm('确定删除该分发配置吗？', '系统提示', { type: 'warning' })
    .then(async () => {
      await delDist(row.distId!)
      ElMessage.success('删除成功')
      getList()
    })
    .catch(() => {})
}

// ===== 分发监控 =====
const monitor = ref<Record<string, unknown>>({})
let timer: ReturnType<typeof setInterval> | null = null

const loadMonitor = async () => {
  const res = await getDistributeMonitor()
  monitor.value = res.data || {}
}

onMounted(async () => {
  getList()
  loadMonitor()
  timer = setInterval(loadMonitor, 30000)
  const [objRes, appRes] = await Promise.all([listObject({ pageSize: 100 }), listApp({ pageSize: 100 })])
  objectOptions.value = objRes.rows
  appList.value = appRes.rows
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
