<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab1 待办任务 -->
      <el-tab-pane label="我的待办" name="todo">
        <el-table v-loading="todoLoading" :data="todoList">
          <el-table-column label="任务名称" align="center" prop="name" />
          <el-table-column label="对象" align="center" prop="objectCode" width="120" />
          <el-table-column label="操作类型" align="center" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.actionType === 'INSERT' ? 'success' : 'warning'" size="small">
                {{ scope.row.actionType === 'INSERT' ? '新增' : '修改' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交人" align="center" prop="submitter" width="110" />
          <el-table-column label="创建时间" align="center" width="170">
            <template #default="scope">{{ formatTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="300">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
              <el-button link type="success" icon="Check" @click="handleApprove(scope.row)">通过</el-button>
              <el-button link type="danger" icon="Close" @click="handleReject(scope.row)">驳回</el-button>
              <el-button link type="warning" icon="RefreshLeft" @click="handleBack(scope.row)">退回</el-button>
              <el-dropdown style="margin-left: 8px" @command="(cmd: string) => handleTaskCommand(cmd, scope.row)">
                <el-button link type="primary" icon="More">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="delegate">转办</el-dropdown-item>
                    <el-dropdown-item command="addSign">加签</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab2 已办任务 -->
      <el-tab-pane label="我的已办" name="done">
        <el-table v-loading="doneLoading" :data="doneList">
          <el-table-column label="任务名称" align="center" prop="name" />
          <el-table-column label="处理人" align="center" prop="assignee" width="110" />
          <el-table-column label="开始时间" align="center" width="170">
            <template #default="scope">{{ formatTime(scope.row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="完成时间" align="center" width="170">
            <template #default="scope">{{ formatTime(scope.row.endTime) }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab3 流程管理入口 -->
      <el-tab-pane label="流程管理" name="process">
        <el-button type="primary" icon="Promotion" @click="goProcessList">流程定义列表</el-button>
        <el-button type="success" icon="EditPen" @click="goDesigner">流程设计器</el-button>
      </el-tab-pane>
    </el-tabs>

    <!-- 任务详情弹窗 -->
    <el-dialog title="任务详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务名称">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ detail.submitter }}</el-descriptions-item>
        <el-descriptions-item label="对象">{{ detail.objectCode }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          {{ detail.actionType === 'INSERT' ? '新增' : '修改' }}
        </el-descriptions-item>
      </el-descriptions>
      <el-divider>数据快照</el-divider>
      <pre class="data-snapshot">{{ JSON.stringify(detail.data, null, 2) }}</pre>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { listTodoTasks, listDoneTasks, getTaskDetail, approveTask, rejectTask, backTask, delegateTask, addSignTask } from '@/api/mdm/audit'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const activeTab = ref('todo')
const todoLoading = ref(false)
const doneLoading = ref(false)
const todoList = ref<Array<Record<string, unknown>>>([])
const doneList = ref<Array<Record<string, unknown>>>([])
const detailOpen = ref(false)
const detail = ref<Record<string, unknown>>({})

function formatTime(t: unknown) {
  return t ? String(t).replace('T', ' ').substring(0, 19) : ''
}

function loadTodo() {
  todoLoading.value = true
  listTodoTasks().then(res => {
    todoList.value = res.rows || []
    todoLoading.value = false
  })
}

function loadDone() {
  doneLoading.value = true
  listDoneTasks().then(res => {
    doneList.value = res.rows || []
    doneLoading.value = false
  })
}

function handleDetail(row: Record<string, unknown>) {
  getTaskDetail(row.id as string).then(res => {
    const vars = (res.data?.variables || {}) as Record<string, unknown>
    detail.value = {
      name: res.data?.name,
      objectCode: vars.objectCode,
      submitter: vars.submitter,
      actionType: vars.actionType,
      data: vars.data
    }
    detailOpen.value = true
  })
}

function handleApprove(row: Record<string, unknown>) {
  ElMessageBox.prompt('审批意见（可选）', '审批通过', { confirmButtonText: '通过', cancelButtonText: '取消' }).then(({ value }) => {
    approveTask(row.id as string, value || '').then(() => {
      ElMessage.success('已通过')
      loadTodo()
    })
  }).catch(() => {})
}

function handleReject(row: Record<string, unknown>) {
  ElMessageBox.prompt('驳回原因', '驳回', { confirmButtonText: '驳回', cancelButtonText: '取消' }).then(({ value }) => {
    rejectTask(row.id as string, value || '').then(() => {
      ElMessage.success('已驳回')
      loadTodo()
    })
  }).catch(() => {})
}

function handleBack(row: Record<string, unknown>) {
  ElMessageBox.prompt('退回原因', '退回', { confirmButtonText: '退回', cancelButtonText: '取消' }).then(({ value }) => {
    backTask(row.id as string, value || '').then(() => {
      ElMessage.success('已退回')
      loadTodo()
    })
  }).catch(() => {})
}

function handleTaskCommand(cmd: string, row: Record<string, unknown>) {
  if (cmd === 'delegate') {
    ElMessageBox.prompt('转办给（用户名）', '转办', { confirmButtonText: '转办', cancelButtonText: '取消' }).then(({ value }) => {
      delegateTask(row.id as string, value).then(() => {
        ElMessage.success('已转办')
        loadTodo()
      })
    }).catch(() => {})
  } else if (cmd === 'addSign') {
    ElMessageBox.prompt('加签给（用户名）', '加签', { confirmButtonText: '加签', cancelButtonText: '取消' }).then(({ value }) => {
      addSignTask(row.id as string, value).then(() => {
        ElMessage.success('已加签')
        loadTodo()
      })
    }).catch(() => {})
  }
}

function goProcessList() {
  router.push('/mdm/audit/process')
}

function goDesigner() {
  router.push('/mdm/audit/designer')
}

loadTodo()
loadDone()
</script>

<style scoped>
.data-snapshot {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  max-height: 300px;
  overflow: auto;
  font-size: 12px;
}
</style>