<template>
  <div class="app-container">
    <!-- 对象选择 -->
    <el-form :inline="true">
      <el-form-item label="数据对象">
        <el-select v-model="objectId" placeholder="请选择数据对象" style="width: 260px" @change="handleObjectChange">
          <el-option
            v-for="obj in objectOptions"
            :key="obj.objectId"
            :label="obj.objectName + '（' + obj.objectCode + '）'"
            :value="obj.objectId"
          />
        </el-select>
      </el-form-item>
    </el-form>

    <el-tabs v-if="objectId != null" v-model="activeTab" type="border-card">
      <!-- Tab1 校验规则 -->
      <el-tab-pane label="校验规则" name="rule">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAddRule">新增规则</el-button>
          </el-col>
        </el-row>
        <el-table v-loading="ruleLoading" :data="ruleList" border>
          <el-table-column label="规则名称" prop="ruleName" min-width="120" />
          <el-table-column label="作用目标" width="90" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.targetType === 'OBJECT'" type="info">对象级</el-tag>
              <el-tag v-else type="primary">属性级</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="目标属性" prop="targetValue" width="110" align="center">
            <template #default="scope">{{ scope.row.targetValue || '-' }}</template>
          </el-table-column>
          <el-table-column label="规则类型" width="90" align="center">
            <template #default="scope">{{ ruleTypeLabel(scope.row.ruleType) }}</template>
          </el-table-column>
          <el-table-column label="规则表达式" prop="ruleExpr" min-width="140" show-overflow-tooltip />
          <el-table-column label="提示信息" prop="ruleMsg" min-width="140" show-overflow-tooltip />
          <el-table-column label="状态" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
                {{ scope.row.status === '0' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleEditRule(scope.row)">修改</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDelRule(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab2 重复检测 -->
      <el-tab-pane label="重复检测" name="duplicate">
        <el-form :inline="true">
          <el-form-item label="查重字段">
            <el-select v-model="duplicateFields" multiple placeholder="选择查重字段（可多选）" style="width: 360px">
              <el-option
                v-for="attr in attributes"
                :key="attr.attrCode"
                :label="attr.attrName + '（' + attr.attrCode + '）'"
                :value="attr.attrCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" :loading="dupLoading" @click="handleDuplicate">执行查重</el-button>
          </el-form-item>
        </el-form>
        <el-alert
          v-if="dupGroups.length === 0 && dupChecked"
          title="未发现重复数据"
          type="success"
          :closable="false"
          show-icon
          style="margin-bottom: 10px"
        />
        <el-table v-if="dupGroups.length" :data="dupGroups" border>
          <el-table-column label="重复字段值" min-width="200">
            <template #default="scope">
              <el-tag v-for="(v, k) in scope.row" :key="k" v-show="k !== 'cnt'" style="margin-right: 6px">
                {{ k }}={{ v }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="重复条数" prop="cnt" width="100" align="center">
            <template #default="scope">
              <el-tag type="danger">{{ scope.row.cnt }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Tab3 质量台账 -->
      <el-tab-pane label="质量台账" name="issue">
        <el-form :inline="true" class="mb8">
          <el-form-item label="问题类型">
            <el-select v-model="issueQuery.issueType" placeholder="全部" clearable style="width: 130px">
              <el-option label="校验失败" value="VALIDATE" />
              <el-option label="重复" value="DUPLICATE" />
              <el-option label="缺失" value="MISSING" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态">
            <el-select v-model="issueQuery.handleStatus" placeholder="全部" clearable style="width: 130px">
              <el-option label="未处理" value="0" />
              <el-option label="已处理" value="1" />
              <el-option label="忽略" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleIssueQuery">搜索</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="issueLoading" :data="issueList" border>
          <el-table-column label="问题类型" width="100" align="center">
            <template #default="scope">
              <el-tag :type="issueTypeTag(scope.row.issueType)">{{ issueTypeLabel(scope.row.issueType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="问题描述" prop="issueDesc" min-width="200" show-overflow-tooltip />
          <el-table-column label="数据ID" prop="dataId" width="80" align="center" />
          <el-table-column label="处理状态" width="90" align="center">
            <template #default="scope">
              <el-tag :type="handleTag(scope.row.handleStatus)">{{ handleLabel(scope.row.handleStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理人" prop="handleBy" width="90" align="center" />
          <el-table-column label="处理时间" prop="handleTime" width="160" align="center" />
          <el-table-column label="操作" width="130" align="center">
            <template #default="scope">
              <template v-if="scope.row.handleStatus === '0'">
                <el-button link type="success" @click="handleIssueHandle(scope.row, '1')">已处理</el-button>
                <el-button link type="info" @click="handleIssueHandle(scope.row, '2')">忽略</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="issueTotal > 0"
          :total="issueTotal"
          v-model:page="issueQuery.pageNum"
          v-model:limit="issueQuery.pageSize"
          @pagination="getIssueList"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 校验规则表单对话框 -->
    <el-dialog v-model="ruleOpen" :title="ruleTitle" width="520px" append-to-body>
      <el-form ref="ruleRef" :model="ruleForm" :rules="ruleRules" label-width="100px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="作用目标" prop="targetType">
          <el-radio-group v-model="ruleForm.targetType">
            <el-radio value="ATTRIBUTE">属性级</el-radio>
            <el-radio value="OBJECT">对象级</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="ruleForm.targetType === 'ATTRIBUTE'" label="目标属性" prop="targetValue">
          <el-select v-model="ruleForm.targetValue" placeholder="选择属性" style="width: 100%">
            <el-option
              v-for="attr in attributes"
              :key="attr.attrCode"
              :label="attr.attrName + '（' + attr.attrCode + '）'"
              :value="attr.attrCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="ruleForm.ruleType" style="width: 100%">
            <el-option label="必填" value="REQUIRED" />
            <el-option label="正则校验" value="REGEX" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="ruleForm.ruleType === 'REGEX'" label="正则表达式" prop="ruleExpr">
          <el-input v-model="ruleForm.ruleExpr" placeholder="如 ^1[3-9]\\d{9}$" />
        </el-form-item>
        <el-form-item label="提示信息" prop="ruleMsg">
          <el-input v-model="ruleForm.ruleMsg" placeholder="违规时提示内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="ruleForm.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitRuleForm">确 定</el-button>
        <el-button @click="ruleOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { MdmObject, MdmAttribute, MdmQualityRule, MdmQualityIssue } from '@/types'
import { listObject, getObjectMeta } from '@/api/mdm/model'
import { listRule, addRule, editRule, delRule, duplicateCheck, listIssue, handleIssue } from '@/api/mdm/quality'

const objectOptions = ref<MdmObject[]>([])
const objectId = ref<number>()
const attributes = ref<MdmAttribute[]>([])
const activeTab = ref('rule')

const loadObjects = async () => {
  const res = await listObject({ status: '1', pageSize: 100 })
  objectOptions.value = res.rows
}

const handleObjectChange = async (id: number) => {
  const res = await getObjectMeta(id)
  attributes.value = res.data?.attributes ?? []
  getRuleList()
  getIssueList()
}

// ===== 校验规则 =====
const ruleList = ref<MdmQualityRule[]>([])
const ruleLoading = ref(false)
const ruleOpen = ref(false)
const ruleTitle = ref('')
const ruleForm = ref<MdmQualityRule>({})
const ruleRef = ref<FormInstance>()
const ruleRules = {
  ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
  targetType: [{ required: true, message: '作用目标不能为空', trigger: 'change' }],
  ruleType: [{ required: true, message: '规则类型不能为空', trigger: 'change' }]
}

const ruleTypeLabel = (t?: string) => {
  return t === 'REGEX' ? '正则' : t === 'REQUIRED' ? '必填' : t || '-'
}

const getRuleList = async () => {
  ruleLoading.value = true
  try {
    const res = await listRule({ objectId: objectId.value, pageSize: 100 })
    ruleList.value = res.rows
  } finally {
    ruleLoading.value = false
  }
}

const handleAddRule = () => {
  ruleForm.value = { objectId: objectId.value, targetType: 'ATTRIBUTE', ruleType: 'REQUIRED', status: '0' }
  ruleOpen.value = true
  ruleTitle.value = '新增校验规则'
}

const handleEditRule = (row: MdmQualityRule) => {
  ruleForm.value = { ...row }
  ruleOpen.value = true
  ruleTitle.value = '修改校验规则'
}

const submitRuleForm = () => {
  ruleRef.value?.validate(async (valid) => {
    if (!valid) return
    if (ruleForm.value.targetType === 'ATTRIBUTE' && !ruleForm.value.targetValue) {
      ElMessage.warning('属性级规则必须选择目标属性')
      return
    }
    if (ruleForm.value.ruleType === 'REGEX' && !ruleForm.value.ruleExpr) {
      ElMessage.warning('正则规则必须填写表达式')
      return
    }
    if (ruleForm.value.ruleId != null) {
      await editRule(ruleForm.value)
      ElMessage.success('修改成功')
    } else {
      await addRule(ruleForm.value)
      ElMessage.success('新增成功')
    }
    ruleOpen.value = false
    getRuleList()
  })
}

const handleDelRule = (row: MdmQualityRule) => {
  ElMessageBox.confirm('确定删除规则【' + row.ruleName + '】吗？', '系统提示', { type: 'warning' })
    .then(async () => {
      await delRule(row.ruleId!)
      ElMessage.success('删除成功')
      getRuleList()
    })
    .catch(() => {})
}

// ===== 重复检测 =====
const duplicateFields = ref<string[]>([])
const dupGroups = ref<Record<string, any>[]>([])
const dupLoading = ref(false)
const dupChecked = ref(false)

const handleDuplicate = async () => {
  if (!duplicateFields.value.length) {
    ElMessage.warning('请选择查重字段')
    return
  }
  dupLoading.value = true
  try {
    const code = objectOptions.value.find((o) => o.objectId === objectId.value)?.objectCode
    const res = await duplicateCheck(code!, duplicateFields.value)
    dupGroups.value = res.data ?? []
    dupChecked.value = true
  } finally {
    dupLoading.value = false
  }
}

// ===== 质量台账 =====
const issueList = ref<MdmQualityIssue[]>([])
const issueLoading = ref(false)
const issueTotal = ref(0)
const issueQuery = reactive({ pageNum: 1, pageSize: 10, objectId: undefined as number | undefined, issueType: '', handleStatus: '' })

const issueTypeLabel = (t?: string) => (t === 'DUPLICATE' ? '重复' : t === 'VALIDATE' ? '校验失败' : t === 'MISSING' ? '缺失' : t || '-')
const issueTypeTag = (t?: string) => (t === 'DUPLICATE' ? 'warning' : t === 'VALIDATE' ? 'danger' : 'info')
const handleLabel = (s?: string) => (s === '0' ? '未处理' : s === '1' ? '已处理' : '忽略')
const handleTag = (s?: string) => (s === '0' ? 'danger' : s === '1' ? 'success' : 'info')

const getIssueList = async () => {
  issueLoading.value = true
  try {
    const res = await listIssue(issueQuery)
    issueList.value = res.rows
    issueTotal.value = res.total
  } finally {
    issueLoading.value = false
  }
}

const handleIssueQuery = () => {
  issueQuery.pageNum = 1
  getIssueList()
}

const handleIssueHandle = (row: MdmQualityIssue, status: string) => {
  ElMessageBox.confirm(status === '1' ? '确定将该问题标记为已处理？' : '确定忽略该问题？', '系统提示', { type: 'warning' })
    .then(async () => {
      await handleIssue({ issueId: row.issueId, handleStatus: status })
      ElMessage.success('操作成功')
      getIssueList()
    })
    .catch(() => {})
}

loadObjects()
</script>
