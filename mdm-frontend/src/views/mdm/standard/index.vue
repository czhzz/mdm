<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 字典类型列表 -->
      <el-col :span="9">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>标准字典类型</span>
              <el-button link type="primary" @click="goDictManage">去系统字典管理</el-button>
            </div>
          </template>
          <el-input
            v-model="queryParams.dictType"
            placeholder="输入字典类型过滤（如 mdm_）"
            clearable
            size="small"
            style="margin-bottom: 8px"
            @input="handleQuery"
          />
          <el-table
            v-loading="typeLoading"
            :data="typeList"
            highlight-current-row
            size="small"
            @row-click="handleTypeClick"
          >
            <el-table-column label="字典名称" prop="dictName" />
            <el-table-column label="字典类型" prop="dictType" />
          </el-table>
          <pagination
            v-show="typeTotal > 0"
            :total="typeTotal"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="getTypeList"
          />
        </el-card>
      </el-col>

      <!-- 字典项列表 -->
      <el-col :span="15">
        <el-card shadow="never">
          <template #header>
            <span>字典项 —— {{ currentDictName || '请选择字典类型' }}</span>
          </template>
          <el-table v-loading="dataLoading" :data="dataList" size="small">
            <el-table-column label="标签" prop="dictLabel" />
            <el-table-column label="值" prop="dictValue" />
            <el-table-column label="排序" prop="dictSort" width="70" align="center" />
            <el-table-column label="状态" width="80" align="center">
              <template #default="scope">
                <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!dataLoading && !dataList.length" description="暂无字典项" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useDict } from '@/utils/dict'
import { listType } from '@/api/system/dict/type'
import { listData } from '@/api/system/dict/data'
import type { SysDictType, SysDictData } from '@/types'

const router = useRouter()
const { sys_normal_disable } = useDict('sys_normal_disable')

const typeList = ref<SysDictType[]>([])
const typeLoading = ref(false)
const typeTotal = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, dictType: '' })

const dataList = ref<SysDictData[]>([])
const dataLoading = ref(false)
const currentDictType = ref('')
const currentDictName = ref('')

const getTypeList = async () => {
  typeLoading.value = true
  try {
    const res = await listType(queryParams)
    typeList.value = res.rows
    typeTotal.value = res.total
  } finally {
    typeLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNum = 1
  getTypeList()
}

const handleTypeClick = async (row: SysDictType) => {
  currentDictType.value = row.dictType ?? ''
  currentDictName.value = row.dictName ?? ''
  dataLoading.value = true
  try {
    const res = await listData({ dictType: currentDictType.value, pageNum: 1, pageSize: 200 })
    dataList.value = res.rows
  } finally {
    dataLoading.value = false
  }
}

const goDictManage = () => {
  router.push('/system/dict')
}

getTypeList()
</script>
