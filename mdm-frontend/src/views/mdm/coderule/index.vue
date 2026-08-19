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
      <el-form-item v-if="form.ruleId != null">
        <el-tag type="warning">已配置编码方案</el-tag>
      </el-form-item>
    </el-form>

    <el-card v-if="objectId != null" shadow="never">
      <el-form ref="ruleRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" style="width: 320px" />
        </el-form-item>
        <el-form-item label="流水重置周期" prop="resetType">
          <el-select v-model="form.resetType" style="width: 200px">
            <el-option label="不重置" value="NONE" />
            <el-option label="按日" value="DAY" />
            <el-option label="按月" value="MONTH" />
            <el-option label="按年" value="YEAR" />
          </el-select>
          <span style="margin-left: 10px; color: #909399; font-size: 12px">流水段达到位数上限时将提示溢出</span>
        </el-form-item>
        <el-form-item label="编码回填字段" prop="codeField">
          <el-select v-model="form.codeField" placeholder="选择生成编码回填到的属性" style="width: 240px">
            <el-option
              v-for="attr in attributes"
              :key="attr.attrCode"
              :label="attr.attrName + '（' + attr.attrCode + '）'"
              :value="attr.attrCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="编码分段">
          <el-table :data="form.segments" border size="small" style="width: 720px">
            <el-table-column label="分段类型" width="150">
              <template #default="scope">
                <el-select v-model="scope.row.segType" size="small" style="width: 120px">
                  <el-option label="常量" value="CONSTANT" />
                  <el-option label="日期" value="DATE" />
                  <el-option label="流水" value="SEQUENCE" />
                  <el-option label="属性值" value="ATTRIBUTE" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="分段值">
              <template #default="scope">
                <el-input v-model="scope.row.segValue" size="small" :placeholder="segPlaceholder(scope.row.segType)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" align="center">
              <template #default="scope">
                <el-button link type="danger" icon="Delete" @click="removeSegment(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="margin-top: 8px">
            <el-button link type="primary" icon="Plus" @click="addSegment">添加分段</el-button>
          </div>
        </el-form-item>
        <el-form-item label="示例编码">
          <el-tag v-if="previewCode">{{ previewCode }}</el-tag>
          <el-button link type="primary" icon="Refresh" @click="handlePreview">重新预览</el-button>
        </el-form-item>
      </el-form>
      <div style="margin-left: 110px">
        <el-button type="primary" @click="submitForm">保存方案</el-button>
        <el-button v-if="form.ruleId != null" type="danger" plain @click="handleDelete">删除方案</el-button>
      </div>
    </el-card>
    <el-empty v-else description="请先选择已发布的数据对象" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import type { MdmObject, MdmAttribute, MdmCodeRule, MdmCodeRuleSegment } from '@/types'
import { listObject, getObjectMeta } from '@/api/mdm/model'
import { listRule, addRule, editRule, delRule, previewRule } from '@/api/mdm/coderule'

const objectOptions = ref<MdmObject[]>([])
const attributes = ref<MdmAttribute[]>([])
const objectId = ref<number>()
const previewCode = ref('')
const ruleRef = ref<FormInstance>()
const form = ref<MdmCodeRule>({})
const rules = {
  ruleName: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
  resetType: [{ required: true, message: '流水重置周期不能为空', trigger: 'change' }],
  codeField: [{ required: true, message: '编码回填字段不能为空', trigger: 'change' }]
}

const loadObjects = async () => {
  const res = await listObject({ status: '1', pageSize: 100 })
  objectOptions.value = res.rows
}

const handleObjectChange = async (id: number) => {
  const res = await getObjectMeta(id)
  attributes.value = res.data?.attributes ?? []
  // 查询该对象已有方案
  const ruleRes = await listRule({ objectId: id, pageNum: 1, pageSize: 1 })
  if (ruleRes.rows.length) {
    form.value = { ...ruleRes.rows[0] }
  } else {
    form.value = { objectId: id, ruleName: '', resetType: 'NONE', codeField: '', segments: [] }
  }
  previewCode.value = ''
}

const segPlaceholder = (type?: string) => {
  switch (type) {
    case 'DATE':
      return '日期格式，如 yyyyMMdd'
    case 'SEQUENCE':
      return '流水位数，如 4'
    case 'ATTRIBUTE':
      return '属性编码'
    default:
      return '常量文本'
  }
}

const addSegment = () => {
  form.value.segments = form.value.segments ?? []
  form.value.segments.push({ segType: 'CONSTANT', segValue: '', orderNum: form.value.segments.length + 1 })
}

const removeSegment = (index: number) => {
  form.value.segments?.splice(index, 1)
  form.value.segments?.forEach((seg, i) => (seg.orderNum = i + 1))
}

const handlePreview = async () => {
  const payload: MdmCodeRule = {
    ...form.value,
    segments: (form.value.segments ?? []).map((seg) => ({ ...seg }))
  }
  const res = await previewRule(payload)
  previewCode.value = res.data ?? ''
}

const submitForm = () => {
  ruleRef.value?.validate(async (valid) => {
    if (!valid) return
    if (!form.value.segments?.length) {
      ElMessage.warning('请至少添加一个编码分段')
      return
    }
    form.value.segments?.forEach((seg, i) => (seg.orderNum = i + 1))
    if (form.value.ruleId != null) {
      await editRule(form.value)
      ElMessage.success('修改成功')
    } else {
      await addRule(form.value)
      ElMessage.success('新增成功')
    }
    handleObjectChange(objectId.value!)
  })
}

const handleDelete = () => {
  ElMessageBox.confirm('确定删除该编码方案吗？删除后新增数据将不再自动生成编码。', '系统提示', {
    type: 'warning'
  })
    .then(async () => {
      await delRule(form.value.ruleId!)
      ElMessage.success('删除成功')
      handleObjectChange(objectId.value!)
    })
    .catch(() => {})
}

loadObjects()
</script>
