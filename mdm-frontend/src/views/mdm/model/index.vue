<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 分类树 -->
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-input
            v-model="filterText"
            placeholder="输入分类名称过滤"
            clearable
            size="small"
            style="margin-bottom: 8px"
          />
          <el-tree
            ref="categoryTreeRef"
            :data="categoryOptions"
            :props="categoryProps"
            node-key="categoryId"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            highlight-current
            default-expand-all
            @node-click="handleNodeClick"
          />
          <el-button type="primary" plain size="small" icon="Plus" style="margin-top: 8px" @click="handleAddCategory">
            新增分类
          </el-button>
        </div>
      </el-col>
      <!-- 对象列表 -->
      <el-col :span="20" :xs="24">
        <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="68px">
          <el-form-item label="对象编码" prop="objectCode">
            <el-input
              v-model="queryParams.objectCode"
              placeholder="请输入对象编码"
              clearable
              style="width: 180px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="对象名称" prop="objectName">
            <el-input
              v-model="queryParams.objectName"
              placeholder="请输入对象名称"
              clearable
              style="width: 180px"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAdd">新增对象</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()">删除</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </el-row>

        <el-table v-loading="loading" :data="objectList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="对象编码" align="center" prop="objectCode" />
          <el-table-column label="对象名称" align="center" prop="objectName" />
          <el-table-column label="状态" align="center" width="90">
            <template #default="scope">
              <dict-tag :options="objectStatusOptions" :value="scope.row.status" />
            </template>
          </el-table-column>
          <el-table-column label="版本号" align="center" prop="version" width="80" />
          <el-table-column label="显示顺序" align="center" prop="orderNum" width="80" />
          <el-table-column label="操作" align="center" width="260">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
              <el-button link type="primary" icon="Collection" @click="handleAttrConfig(scope.row)">属性</el-button>
              <el-button
                v-if="scope.row.status !== '1'"
                link
                type="success"
                icon="Promotion"
                @click="handlePublish(scope.row)"
              >发布</el-button>
              <el-button
                v-if="scope.row.status === '1'"
                link
                type="warning"
                icon="VideoPause"
                @click="handleStop(scope.row)"
              >停用</el-button>
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
      </el-col>
    </el-row>

    <!-- 对象表单对话框 -->
    <el-dialog v-model="open" :title="title" width="500px" append-to-body>
      <el-form ref="objectRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="对象编码" prop="objectCode">
          <el-input v-model="form.objectCode" placeholder="请输入对象编码" :disabled="form.objectId != null" />
        </el-form-item>
        <el-form-item label="对象名称" prop="objectName">
          <el-input v-model="form.objectName" placeholder="请输入对象名称" />
        </el-form-item>
        <el-form-item label="所属分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryOptions"
            :props="{ label: 'categoryName', value: 'categoryId', children: 'children' }"
            check-strictly
            placeholder="请选择所属分类"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="form.orderNum" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 属性配置对话框 -->
    <el-dialog v-model="attrOpen" :title="'属性配置 - ' + currentObject.objectName" width="900px" append-to-body>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" :disabled="currentObject.status === '1'" @click="handleAddAttr">
            新增属性
          </el-button>
        </el-col>
      </el-row>
      <el-table v-loading="attrLoading" :data="attrList" border>
        <el-table-column label="属性编码" align="center" prop="attrCode" />
        <el-table-column label="属性名称" align="center" prop="attrName" />
        <el-table-column label="数据类型" align="center" prop="dataType">
          <template #default="scope">
            <dict-tag :options="dataTypeOptions" :value="scope.row.dataType" />
          </template>
        </el-table-column>
        <el-table-column label="必填" align="center" width="55">
          <template #default="scope">
            <el-tag v-if="scope.row.requiredFlag === 'Y'" type="danger">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column label="唯一" align="center" width="55">
          <template #default="scope">
            <el-tag v-if="scope.row.uniqueFlag === 'Y'" type="warning">是</el-tag>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="150">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" :disabled="currentObject.status === '1'" @click="handleEditAttr(scope.row)">修改</el-button>
            <el-button link type="danger" icon="Delete" :disabled="currentObject.status === '1'" @click="handleDelAttr(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 属性表单对话框 -->
    <el-dialog v-model="attrFormOpen" :title="attrTitle" width="560px" append-to-body>
      <el-form ref="attrRef" :model="attrForm" :rules="attrRules" label-width="90px">
        <el-form-item label="属性编码" prop="attrCode">
          <el-input v-model="attrForm.attrCode" placeholder="请输入属性编码" :disabled="attrForm.attrId != null" />
        </el-form-item>
        <el-form-item label="属性名称" prop="attrName">
          <el-input v-model="attrForm.attrName" placeholder="请输入属性名称" />
        </el-form-item>
        <el-form-item label="数据类型" prop="dataType">
          <el-select v-model="attrForm.dataType" placeholder="请选择数据类型" style="width: 100%">
            <el-option v-for="dict in dataTypeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源类型" prop="sourceType">
          <el-select v-model="attrForm.sourceType" placeholder="请选择数据源类型" style="width: 100%">
            <el-option label="手工输入" value="input" />
            <el-option label="字典" value="dict" />
            <el-option label="枚举" value="enum" />
            <el-option label="数值范围" value="range" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="attrForm.sourceType === 'dict'" label="字典类型" prop="dictType">
          <el-input v-model="attrForm.dictType" placeholder="请输入标准字典类型" />
        </el-form-item>
        <el-form-item v-if="attrForm.sourceType === 'enum'" label="枚举值" prop="enumValues">
          <el-input v-model="attrForm.enumValues" type="textarea" placeholder="多个枚举值用英文逗号分隔" />
        </el-form-item>
        <el-form-item v-if="attrForm.sourceType === 'range'" label="最小值/最大值">
          <el-input v-model="attrForm.minValue" placeholder="最小值" style="width: 45%" />
          <span style="width: 10%; text-align: center">~</span>
          <el-input v-model="attrForm.maxValue" placeholder="最大值" style="width: 45%" />
        </el-form-item>
        <el-form-item label="默认值" prop="defaultValue">
          <el-input v-model="attrForm.defaultValue" placeholder="请输入默认值" />
        </el-form-item>
        <el-form-item label="必填/唯一/主属性">
          <el-checkbox v-model="attrForm.requiredFlag" true-value="Y" false-value="N">必填</el-checkbox>
          <el-checkbox v-model="attrForm.uniqueFlag" true-value="Y" false-value="N">唯一</el-checkbox>
          <el-checkbox v-model="attrForm.primaryFlag" true-value="Y" false-value="N">主属性</el-checkbox>
        </el-form-item>
        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="attrForm.orderNum" controls-position="right" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitAttrForm">确 定</el-button>
        <el-button @click="cancelAttr">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 分类表单对话框 -->
    <el-dialog v-model="categoryOpen" :title="categoryTitle" width="460px" append-to-body>
      <el-form ref="categoryRef" :model="categoryForm" :rules="categoryRules" label-width="80px">
        <el-form-item label="上级分类" prop="parentId">
          <el-tree-select
            v-model="categoryForm.parentId"
            :data="categoryOptions"
            :props="{ label: 'categoryName', value: 'categoryId', children: 'children' }"
            check-strictly
            placeholder="请选择上级分类"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="categoryForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="分类编码" prop="categoryCode">
          <el-input v-model="categoryForm.categoryCode" placeholder="请输入分类编码" :disabled="categoryForm.categoryId != null" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="categoryForm.orderNum" controls-position="right" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitCategoryForm">确 定</el-button>
        <el-button @click="cancelCategory">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDict } from '@/utils/dict'
import type { FormInstance } from 'element-plus'
import type { MdmObject, MdmCategory, MdmAttribute } from '@/types'
import {
  listObject,
  addObject,
  editObject,
  delObject,
  publishObject,
  getObjectMeta,
  listCategory,
  addCategory,
  editCategory,
  delCategory,
  addAttribute,
  editAttribute,
  delAttribute
} from '@/api/mdm/model'

const { data_type: dataTypeOptions } = useDict('mdm_data_type')

const objectStatusOptions = [
  { value: '0', label: '未发布', elTagType: 'primary' },
  { value: '1', label: '已发布', elTagType: 'success' },
  { value: '2', label: '停用', elTagType: 'info' }
]

// 分类树
const categoryOptions = ref<MdmCategory[]>([])
const categoryProps = { label: 'categoryName', children: 'children' }
const filterText = ref('')
const categoryTreeRef = ref()

// 对象列表
const objectList = ref<MdmObject[]>([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref('')
const objectIds = ref<number[]>([])
const multiple = ref(true)
const queryParams = reactive({ pageNum: 1, pageSize: 10, objectCode: '', objectName: '', categoryId: undefined as number | undefined })
const form = ref<MdmObject>({})
const objectRef = ref<FormInstance>()
const rules = {
  objectCode: [{ required: true, message: '对象编码不能为空', trigger: 'blur' }],
  objectName: [{ required: true, message: '对象名称不能为空', trigger: 'blur' }]
}

// 属性配置
const attrOpen = ref(false)
const attrLoading = ref(false)
const attrList = ref<MdmAttribute[]>([])
const currentObject = ref<MdmObject>({})
const attrFormOpen = ref(false)
const attrTitle = ref('')
const attrForm = ref<MdmAttribute>({})
const attrRef = ref<FormInstance>()
const attrRules = {
  attrCode: [{ required: true, message: '属性编码不能为空', trigger: 'blur' }],
  attrName: [{ required: true, message: '属性名称不能为空', trigger: 'blur' }],
  dataType: [{ required: true, message: '数据类型不能为空', trigger: 'change' }]
}

// 分类表单
const categoryOpen = ref(false)
const categoryTitle = ref('')
const categoryForm = ref<MdmCategory>({})
const categoryRef = ref<FormInstance>()
const categoryRules = {
  categoryName: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }],
  categoryCode: [{ required: true, message: '分类编码不能为空', trigger: 'blur' }]
}

watch(filterText, (val) => {
  categoryTreeRef.value?.filter(val)
})

const filterNode = (value: string, data: MdmCategory) => {
  if (!value) return true
  return data.categoryName?.includes(value)
}

/** 查询对象列表 */
const getList = async () => {
  loading.value = true
  try {
    const res = await listObject(queryParams)
    objectList.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

/** 查询分类树 */
const getCategoryList = async () => {
  const res = await listCategory()
  categoryOptions.value = res.data ?? []
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryParams.objectCode = ''
  queryParams.objectName = ''
  queryParams.categoryId = undefined
  handleQuery()
}

/** 点击分类节点过滤 */
const handleNodeClick = (data: MdmCategory) => {
  queryParams.categoryId = data.categoryId
  handleQuery()
}

const handleSelectionChange = (selection: MdmObject[]) => {
  objectIds.value = selection.map((item) => item.objectId!)
  multiple.value = !selection.length
}

/** 新增对象 */
const handleAdd = () => {
  form.value = {}
  open.value = true
  title.value = '新增对象'
}

/** 修改对象 */
const handleUpdate = (row: MdmObject) => {
  form.value = { ...row }
  open.value = true
  title.value = '修改对象'
}

const submitForm = () => {
  objectRef.value?.validate(async (valid) => {
    if (!valid) return
    if (form.value.objectId != null) {
      await editObject(form.value)
      ElMessage.success('修改成功')
    } else {
      await addObject(form.value)
      ElMessage.success('新增成功')
    }
    open.value = false
    getList()
  })
}

const cancel = () => {
  open.value = false
  reset()
}

const reset = () => {
  form.value = {}
}

/** 发布对象 */
const handlePublish = (row: MdmObject) => {
  ElMessageBox.confirm('发布将根据模型动态创建数据表，确定发布对象【' + row.objectName + '】吗？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await publishObject(row.objectId!)
      ElMessage.success('发布成功')
      getList()
    })
    .catch(() => {})
}

/** 停用对象 */
const handleStop = (row: MdmObject) => {
  ElMessageBox.confirm('停用后对象将不再作为可维护对象，确定停用【' + row.objectName + '】吗？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await editObject({ objectId: row.objectId, status: '2' })
      ElMessage.success('停用成功')
      getList()
    })
    .catch(() => {})
}

/** 删除对象 */
const handleDelete = (row?: MdmObject) => {
  const ids = row ? [row.objectId!] : objectIds.value
  ElMessageBox.confirm('确定删除选中的数据对象吗？已发布对象不可删除。', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await delObject(ids.join(','))
      getList()
      ElMessage.success('删除成功')
    })
    .catch(() => {})
}

/** 属性配置 */
const handleAttrConfig = async (row: MdmObject) => {
  currentObject.value = row
  attrOpen.value = true
  attrLoading.value = true
  try {
    const res = await getObjectMeta(row.objectId!)
    attrList.value = res.data?.attributes ?? []
  } finally {
    attrLoading.value = false
  }
}

const handleAddAttr = () => {
  attrForm.value = { objectId: currentObject.value.objectId, requiredFlag: 'N', uniqueFlag: 'N', primaryFlag: 'N', sourceType: 'input', dataType: 'text' }
  attrFormOpen.value = true
  attrTitle.value = '新增属性'
}

const handleEditAttr = (row: MdmAttribute) => {
  attrForm.value = { ...row }
  attrFormOpen.value = true
  attrTitle.value = '修改属性'
}

const submitAttrForm = () => {
  attrRef.value?.validate(async (valid) => {
    if (!valid) return
    if (attrForm.value.attrId != null) {
      await editAttribute(attrForm.value)
      ElMessage.success('修改成功')
    } else {
      await addAttribute(attrForm.value)
      ElMessage.success('新增成功')
    }
    attrFormOpen.value = false
    const res = await getObjectMeta(currentObject.value.objectId!)
    attrList.value = res.data?.attributes ?? []
  })
}

const cancelAttr = () => {
  attrFormOpen.value = false
}

const handleDelAttr = (row: MdmAttribute) => {
  ElMessageBox.confirm('确定删除属性【' + row.attrName + '】吗？已发布对象的属性不可删除。', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      await delAttribute(row.attrId!)
      ElMessage.success('删除成功')
      const res = await getObjectMeta(currentObject.value.objectId!)
      attrList.value = res.data?.attributes ?? []
    })
    .catch(() => {})
}

/** 分类新增 */
const handleAddCategory = () => {
  categoryForm.value = {}
  categoryOpen.value = true
  categoryTitle.value = '新增分类'
}

const submitCategoryForm = () => {
  categoryRef.value?.validate(async (valid) => {
    if (!valid) return
    if (categoryForm.value.categoryId != null) {
      await editCategory(categoryForm.value)
      ElMessage.success('修改成功')
    } else {
      await addCategory(categoryForm.value)
      ElMessage.success('新增成功')
    }
    categoryOpen.value = false
    getCategoryList()
  })
}

const cancelCategory = () => {
  categoryOpen.value = false
}

getList()
getCategoryList()
</script>
