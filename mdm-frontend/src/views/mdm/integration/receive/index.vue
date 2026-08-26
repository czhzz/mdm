<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px">
      <el-form-item label="接口编码" prop="apiCode">
        <el-input v-model="queryParams.apiCode" placeholder="请输入接口编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="对象编码" prop="objectCode">
        <el-input v-model="queryParams.objectCode" placeholder="请输入对象编码" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 110px" @change="handleQuery">
          <el-option label="启用" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增接口</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column label="接口编码" prop="apiCode" min-width="150">
        <template #default="scope">
          <el-tag type="info">{{ scope.row.apiCode }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="接口名称" prop="apiName" min-width="140" />
      <el-table-column label="目标对象" prop="objectCode" width="120" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
            {{ scope.row.status === '0' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
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

    <el-dialog :title="title" v-model="open" width="520px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="接口编码" prop="apiCode">
          <el-input v-model="form.apiCode" :disabled="form.id != null" placeholder="对外路径使用，如 customer-sync" />
        </el-form-item>
        <el-form-item label="接口名称" prop="apiName">
          <el-input v-model="form.apiName" placeholder="接口说明名称" />
        </el-form-item>
        <el-form-item label="目标对象" prop="objectCode">
          <el-select v-model="form.objectCode" placeholder="选择数据对象" style="width: 100%">
            <el-option
              v-for="obj in objectOptions"
              :key="obj.objectId"
              :label="obj.objectName + '（' + obj.objectCode + '）'"
              :value="obj.objectCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" active-value="0" inactive-value="1" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="接口用途说明" />
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
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { MdmReceiveApi, MdmObject } from '@/types'
import { listReceive, addReceive, editReceive, delReceive } from '@/api/mdm/integration'
import { listObject } from '@/api/mdm/model'

const loading = ref(false)
const list = ref<MdmReceiveApi[]>([])
const total = ref(0)
const objectOptions = ref<MdmObject[]>([])
const queryParams = ref({ pageNum: 1, pageSize: 10, apiCode: '', objectCode: '', status: '' })
const open = ref(false)
const title = ref('')
const form = ref<MdmReceiveApi>({ status: '0' })
const formRef = ref<FormInstance>()
const rules = {
  apiCode: [{ required: true, message: '接口编码不能为空', trigger: 'blur' }],
  apiName: [{ required: true, message: '接口名称不能为空', trigger: 'blur' }],
  objectCode: [{ required: true, message: '请选择目标对象', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await listReceive(queryParams.value)
    list.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.value.pageNum = 1; getList() }
const resetQuery = () => {
  queryParams.value = { pageNum: 1, pageSize: 10, apiCode: '', objectCode: '', status: '' }
  handleQuery()
}

const handleAdd = () => {
  form.value = { status: '0' }
  title.value = '新增接收接口'
  open.value = true
}

const handleEdit = (row: MdmReceiveApi) => {
  form.value = { ...row }
  title.value = '编辑接收接口'
  open.value = true
}

const submitForm = () => {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    if (form.value.id != null) {
      await editReceive(form.value)
    } else {
      await addReceive(form.value)
    }
    ElMessage.success('保存成功')
    open.value = false
    getList()
  })
}

const handleDelete = (row: MdmReceiveApi) => {
  ElMessageBox.confirm(`确定删除接收接口「${row.apiName}」吗？`, '系统提示', { type: 'warning' })
    .then(async () => {
      await delReceive(row.id!)
      ElMessage.success('删除成功')
      getList()
    })
    .catch(() => {})
}

onMounted(async () => {
  getList()
  const res = await listObject({ pageSize: 100 })
  objectOptions.value = res.rows
})
</script>
