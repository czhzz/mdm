<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增应用</el-button>
      </el-col>
    </el-row>
    <el-table v-loading="loading" :data="appList" border>
      <el-table-column label="应用名称" prop="appName" min-width="140" />
      <el-table-column label="AppID" prop="appid" min-width="220">
        <template #default="scope">
          <el-tag type="info">{{ scope.row.appid }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="scope">
          <el-switch :model-value="scope.row.enabled === '1'" @change="(v: boolean | string | number) => handleEnabled(scope.row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
      <el-table-column label="操作" width="220" align="center">
        <template #default="scope">
          <el-button link type="warning" icon="Key" @click="handleResetSecret(scope.row)">重置密钥</el-button>
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

    <el-dialog v-model="open" :title="title" width="460px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="form.appName" placeholder="接入方系统名称" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="接入用途说明" />
        </el-form-item>
      </el-form>
      <el-alert
        v-if="credential"
        type="success"
        :closable="false"
        title="创建成功，请立即保存以下凭据（密钥不再显示）"
        style="margin-top: 8px"
      >
        <div style="margin-top: 6px">
          <div>AppID：<b>{{ credential.appid }}</b></div>
          <div>Secret：<b>{{ credential.secret }}</b></div>
        </div>
      </el-alert>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="open = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { MdmApp } from '@/types'
import { listApp, addApp, editApp, delApp, resetSecret } from '@/api/mdm/integration'

const loading = ref(false)
const appList = ref<MdmApp[]>([])
const total = ref(0)
const queryParams = ref({ pageNum: 1, pageSize: 10 })
const open = ref(false)
const title = ref('')
const form = ref<MdmApp>({ enabled: '1' })
const formRef = ref<FormInstance>()
const credential = ref<MdmApp | null>(null)
const rules = { appName: [{ required: true, message: '应用名称不能为空', trigger: 'blur' }] }

const getList = async () => {
  loading.value = true
  try {
    const res = await listApp(queryParams.value)
    appList.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  form.value = { enabled: '1' }
  credential.value = null
  title.value = '新增应用'
  open.value = true
}

const handleEdit = (row: MdmApp) => {
  form.value = { ...row }
  credential.value = null
  title.value = '编辑应用'
  open.value = true
}

const submitForm = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    if (form.value.appId != null) {
      await editApp(form.value)
      ElMessage.success('保存成功')
    } else {
      const res = await addApp(form.value)
      credential.value = { appid: res.data?.appid, secret: res.data?.secret }
      ElMessage.success('应用创建成功，请在弹窗中保存凭据')
    }
    open.value = false
    getList()
  })
}

const handleEnabled = async (row: MdmApp, v: boolean | string | number) => {
  await editApp({ appId: row.appId, enabled: v ? '1' : '0' })
  ElMessage.success(v ? '已启用' : '已停用')
  getList()
}

const handleResetSecret = (row: MdmApp) => {
  ElMessageBox.confirm('重置后原密钥立即失效、仅展示一次，确认重置？', '系统提示', { type: 'warning' })
    .then(async () => {
      const res = await resetSecret(row.appId!)
      ElMessageBox.alert(`应用「${row.appName}」新 Secret：\n${res.data}`, '重置成功', { confirmButtonText: '我已保存' })
      getList()
    })
    .catch(() => {})
}

const handleDelete = (row: MdmApp) => {
  ElMessageBox.confirm(`确定删除应用「${row.appName}」及其分发配置吗？`, '系统提示', { type: 'warning' })
    .then(async () => {
      await delApp(row.appId!)
      ElMessage.success('删除成功')
      getList()
    })
    .catch(() => {})
}

onMounted(getList)
</script>
