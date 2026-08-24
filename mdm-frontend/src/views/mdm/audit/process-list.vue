<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="Plus" @click="goDesigner">新建流程</el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="definitions">
      <el-table-column label="流程名称" align="center" prop="name" />
      <el-table-column label="流程Key" align="center" prop="key" />
      <el-table-column label="版本" align="center" width="60" prop="version" />
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.suspended ? 'danger' : 'success'">
            {{ scope.row.suspended ? '已挂起' : '运行中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="220">
        <template #default="scope">
          <el-button link type="warning" icon="VideoPause" v-if="!scope.row.suspended"
            @click="handleSuspend(scope.row)">挂起</el-button>
          <el-button link type="success" icon="VideoPlay" v-else
            @click="handleActivate(scope.row)">激活</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { listProcessDefinitions, deleteProcessDefinition, suspendProcessDefinition, activateProcessDefinition } from '@/api/mdm/audit'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const definitions = ref<Array<Record<string, unknown>>>([])

function getList() {
  loading.value = true
  listProcessDefinitions().then(res => {
    definitions.value = res.data || []
    loading.value = false
  })
}

function goDesigner() {
  router.push('/mdm/audit/designer')
}

function handleSuspend(row: Record<string, unknown>) {
  suspendProcessDefinition(row.id as string).then(() => {
    ElMessage.success('已挂起')
    getList()
  })
}

function handleActivate(row: Record<string, unknown>) {
  activateProcessDefinition(row.id as string).then(() => {
    ElMessage.success('已激活')
    getList()
  })
}

function handleDelete(row: Record<string, unknown>) {
  ElMessageBox.confirm('删除流程定义将同时删除运行中实例，确认？', '提示', { type: 'warning' }).then(() => {
    deleteProcessDefinition(row.deploymentId as string).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  })
}

getList()
</script>