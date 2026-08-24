<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px">
      <el-form-item label="源对象" prop="sourceObjectCode">
        <el-input v-model="queryParams.sourceObjectCode" placeholder="请输入源对象编码" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="目标对象" prop="targetObjectCode">
        <el-input v-model="queryParams.targetObjectCode" placeholder="请输入目标对象编码" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="relationList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="源对象" align="center" prop="sourceObjectCode" />
      <el-table-column label="目标对象" align="center" prop="targetObjectCode" />
      <el-table-column label="关系类型" align="center" width="120">
        <template #default="scope">
          <el-tag :type="relationTypeTag(scope.row.relationType)">
            {{ relationTypeLabel(scope.row.relationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="级联规则" align="center" width="100" prop="cascadeRule" />
      <el-table-column label="双向" align="center" width="60">
        <template #default="scope">
          <span>{{ scope.row.isBidirectional === '1' ? '是' : '否' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="源对象编码" prop="sourceObjectCode">
          <el-input v-model="form.sourceObjectCode" placeholder="请输入源对象编码" />
        </el-form-item>
        <el-form-item label="目标对象编码" prop="targetObjectCode">
          <el-input v-model="form.targetObjectCode" placeholder="请输入目标对象编码" />
        </el-form-item>
        <el-form-item label="关系类型" prop="relationType">
          <el-select v-model="form.relationType" placeholder="请选择关系类型" style="width: 100%">
            <el-option label="一对一" value="ONE_TO_ONE" />
            <el-option label="一对多" value="ONE_TO_MANY" />
            <el-option label="多对多" value="MANY_TO_MANY" />
          </el-select>
        </el-form-item>
        <el-form-item label="引用属性编码" prop="sourceFieldCode" v-if="form.relationType !== 'MANY_TO_MANY'">
          <el-input v-model="form.sourceFieldCode" placeholder="源对象中引用属性编码" />
        </el-form-item>
        <el-form-item label="级联规则" prop="cascadeRule">
          <el-select v-model="form.cascadeRule" placeholder="请选择级联规则" style="width: 100%">
            <el-option label="阻止删除" value="RESTRICT" />
            <el-option label="置空" value="SET_NULL" />
            <el-option label="级联删除" value="CASCADE" />
          </el-select>
        </el-form-item>
        <el-form-item label="双向" prop="isBidirectional">
          <el-switch v-model="form.isBidirectional" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { listRelation, getRelation, addRelation, editRelation, delRelation } from '@/api/mdm/relation'
import type { MdmRelation } from '@/api/mdm/relation'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const open = ref(false)
const title = ref('')
const multiple = ref(false)
const total = ref(0)
const relationList = ref<MdmRelation[]>([])
const ids = ref<number[]>([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  sourceObjectCode: '',
  targetObjectCode: ''
})

const form = reactive<MdmRelation>({
  sourceObjectCode: '',
  targetObjectCode: '',
  relationType: 'ONE_TO_ONE',
  sourceFieldCode: '',
  cascadeRule: 'RESTRICT',
  isBidirectional: '0'
})

const rules = {
  sourceObjectCode: [{ required: true, message: '源对象编码不能为空', trigger: 'blur' }],
  targetObjectCode: [{ required: true, message: '目标对象编码不能为空', trigger: 'blur' }],
  relationType: [{ required: true, message: '关系类型不能为空', trigger: 'change' }]
}

function relationTypeLabel(type: string) {
  const map: Record<string, string> = { ONE_TO_ONE: '1:1', ONE_TO_MANY: '1:N', MANY_TO_MANY: 'N:M' }
  return map[type] || type
}

function relationTypeTag(type: string) {
  const map: Record<string, string> = { ONE_TO_ONE: 'success', ONE_TO_MANY: 'primary', MANY_TO_MANY: 'warning' }
  return map[type] || 'info'
}

function getList() {
  loading.value = true
  listRelation(queryParams).then(res => {
    relationList.value = res.rows || []
    total.value = res.total || 0
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.sourceObjectCode = ''
  queryParams.targetObjectCode = ''
  handleQuery()
}

function handleSelectionChange(selection: MdmRelation[]) {
  ids.value = selection.map((item: MdmRelation) => item.id as number)
  multiple.value = !selection.length
}

function handleAdd() {
  Object.assign(form, {
    id: undefined, sourceObjectCode: '', targetObjectCode: '', relationType: 'ONE_TO_ONE',
    sourceFieldCode: '', cascadeRule: 'RESTRICT', isBidirectional: '0'
  })
  title.value = '新增关联关系'
  open.value = true
}

function handleUpdate(row: MdmRelation) {
  getRelation(row.id as number).then(res => {
    Object.assign(form, res.data)
    title.value = '修改关联关系'
    open.value = true
  })
}

function handleDelete(id?: number) {
  const targetIds = id ? String(id) : ids.value.join(',')
  ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }).then(() => {
    delRelation(targetIds).then(() => {
      getList()
      ElMessage.success('删除成功')
    })
  })
}

function submitForm() {
  const api = form.id ? editRelation : addRelation
  api(form).then(() => {
    ElMessage.success(form.id ? '修改成功' : '新增成功')
    open.value = false
    getList()
  })
}

getList()
</script>