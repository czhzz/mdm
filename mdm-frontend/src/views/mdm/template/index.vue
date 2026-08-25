<template>
  <div class="app-container">
    <el-row :gutter="16">
      <el-col :span="8" v-for="tpl in templates" :key="tpl.code">
        <el-card shadow="hover" style="margin-bottom: 16px; cursor: pointer" @click="previewTemplate(tpl)">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold">{{ tpl.name }}</span>
              <el-tag type="info">{{ tpl.objectCode }}</el-tag>
            </div>
          </template>
          <p style="color: #909399; margin-bottom: 12px">{{ tpl.description }}</p>
          <div style="color: #606266; font-size: 13px">
            <span>属性数：{{ tpl.attributes?.length || 0 }}</span>
            <span style="margin-left: 16px">编码方案：{{ tpl.codeRule ? '已配置' : '未配置' }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 预览弹窗 -->
    <el-dialog :title="'模板预览：' + preview.name" v-model="previewOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模板编码">{{ preview.code }}</el-descriptions-item>
        <el-descriptions-item label="对象编码">{{ preview.objectCode }}</el-descriptions-item>
        <el-descriptions-item label="模板名称">{{ preview.name }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{ preview.description }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>属性列表</el-divider>
      <el-table :data="preview.attributes || []" size="small">
        <el-table-column label="属性编码" prop="attrCode" />
        <el-table-column label="属性名称" prop="attrName" />
        <el-table-column label="数据类型" prop="dataType" width="80" />
        <el-table-column label="必填" width="60">
          <template #default="scope">
            <span>{{ scope.row.requiredFlag === 'Y' ? '是' : '否' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="唯一" width="60">
          <template #default="scope">
            <span>{{ scope.row.uniqueFlag === 'Y' ? '是' : '否' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-divider v-if="preview.codeRule">编码方案</el-divider>
      <el-descriptions v-if="preview.codeRule" :column="2" border size="small">
        <el-descriptions-item label="规则名称">{{ preview.codeRule.ruleName }}</el-descriptions-item>
        <el-descriptions-item label="重置周期">{{ preview.codeRule.resetType }}</el-descriptions-item>
        <el-descriptions-item label="编码字段">{{ preview.codeRule.codeField }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="previewOpen = false">关 闭</el-button>
        <el-button type="primary" @click="handleCreate(preview.code)" :loading="creating">一键创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { listTemplates, previewTemplate as fetchTemplatePreview, createFromTemplate } from '@/api/mdm/template'
import type { MdmTemplate } from '@/api/mdm/template'
import { ElMessage, ElMessageBox } from 'element-plus'

const templates = ref<MdmTemplate[]>([])
const previewOpen = ref(false)
const creating = ref(false)
const preview = reactive<MdmTemplate>({
  code: '', name: '', description: '', icon: '', objectCode: '', objectName: '', attributes: []
})

function loadTemplates() {
  listTemplates().then(res => {
    templates.value = res.data || []
  })
}

function previewTemplate(tpl: MdmTemplate) {
  // 本地函数与 API 同名，调用 API 时用别名 fetchTemplatePreview，避免遮蔽递归
  fetchTemplatePreview(tpl.code).then(res => {
    Object.assign(preview, res.data)
    previewOpen.value = true
  })
}

function handleCreate(code: string) {
  ElMessageBox.confirm('确认从该模板创建对象？创建后可在模型管理中修改任何配置。', '提示', { type: 'info' }).then(() => {
    creating.value = true
    createFromTemplate(code).then(() => {
      ElMessage.success('创建成功！请在模型管理中查看并发布对象')
      previewOpen.value = false
      creating.value = false
    }).catch(() => { creating.value = false })
  })
}

loadTemplates()
</script>