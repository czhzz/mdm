<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="6">
        <el-input v-model="processName" placeholder="流程名称（如：客户数据审核）" />
      </el-col>
      <el-col :span="6">
        <el-select v-model="selectedTemplate" placeholder="选择预设模板" @change="applyTemplate" clearable>
          <el-option label="单人审批" value="single" />
          <el-option label="多人会签" value="countersign" />
          <el-option label="逐级审批" value="multi" />
        </el-select>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" icon="Promotion" :loading="deploying" @click="handleDeploy">部署流程</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button icon="Document" @click="showXml = true">查看 XML</el-button>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :span="16">
        <!-- bpmn-js 画布 -->
        <div ref="canvasRef" class="bpmn-canvas" />
      </el-col>
      <el-col :span="8">
        <el-card header="节点属性配置">
          <el-form label-width="90px" size="small">
            <el-form-item label="审批人类型">
              <el-select v-model="nodeConfig.assigneeType" placeholder="请选择">
                <el-option label="角色" value="role" />
                <el-option label="指定人" value="user" />
                <el-option label="提交人上级" value="superior" />
              </el-select>
            </el-form-item>
            <el-form-item label="角色编码" v-if="nodeConfig.assigneeType === 'role'">
              <el-input v-model="nodeConfig.roleCode" placeholder="如 mdm:audit" />
            </el-form-item>
            <el-form-item label="指定用户" v-if="nodeConfig.assigneeType === 'user'">
              <el-input v-model="nodeConfig.userName" placeholder="登录用户名" />
            </el-form-item>
            <el-form-item label="会签规则" v-if="selectedTemplate === 'countersign'">
              <el-select v-model="nodeConfig.signRule" placeholder="请选择">
                <el-option label="全部同意" value="ALL" />
                <el-option label="任一同意" value="ANY" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
        <el-alert type="info" :closable="false" style="margin-top: 12px">
          <p>流程节点说明：</p>
          <p>• 单人审批：提交 → 审批 → 结束</p>
          <p>• 多人会签：提交 → 会签(多审批人) → 结束</p>
          <p>• 逐级审批：提交 → 一级审批 → 二级审批 → 结束</p>
        </el-alert>
      </el-col>
    </el-row>

    <!-- XML 预览 -->
    <el-dialog title="BPMN XML" v-model="showXml" width="800px" append-to-body>
      <el-input type="textarea" :rows="20" v-model="bpmnXml" />
      <template #footer>
        <el-button type="primary" @click="showXml = false">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import { deployProcess } from '@/api/mdm/audit'
import { ElMessage } from 'element-plus'

const canvasRef = ref<HTMLElement>()
const processName = ref('')
const selectedTemplate = ref('')
const showXml = ref(false)
const deploying = ref(false)
const bpmnXml = ref('')
let modeler: BpmnModeler | null = null

const nodeConfig = reactive({
  assigneeType: 'role',
  roleCode: '',
  userName: '',
  signRule: 'ALL'
})

// 预设模板 BPMN
function buildBpmnXml(template: string): string {
  if (template === 'single') {
    return `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:flowable="http://flowable.org/bpmn"
  targetNamespace="http://flowable.org/mdm">
  <process id="singleAudit" name="单人审批" isExecutable="true">
    <startEvent id="start" name="提交" />
    <userTask id="approve" name="审批" flowable:assignee="${nodeConfig.userName || '${assignee}'}" />
    <endEvent id="end" name="结束" />
    <sequenceFlow id="f1" sourceRef="start" targetRef="approve" />
    <sequenceFlow id="f2" sourceRef="approve" targetRef="end" />
  </process>
</definitions>`
  }
  if (template === 'countersign') {
    return `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:flowable="http://flowable.org/bpmn"
  targetNamespace="http://flowable.org/mdm">
  <process id="countersignAudit" name="多人会签" isExecutable="true">
    <startEvent id="start" name="提交" />
    <userTask id="sign" name="会签" flowable:assignee="${nodeConfig.userName || '${assignee}'}" />
    <endEvent id="end" name="结束" />
    <sequenceFlow id="f1" sourceRef="start" targetRef="sign" />
    <sequenceFlow id="f2" sourceRef="sign" targetRef="end" />
  </process>
</definitions>`
  }
  return `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:flowable="http://flowable.org/bpmn"
  targetNamespace="http://flowable.org/mdm">
  <process id="multiAudit" name="逐级审批" isExecutable="true">
    <startEvent id="start" name="提交" />
    <userTask id="approve1" name="一级审批" flowable:assignee="${nodeConfig.userName || '${assignee}'}" />
    <userTask id="approve2" name="二级审批" flowable:assignee="${nodeConfig.userName || '${assignee}'}" />
    <endEvent id="end" name="结束" />
    <sequenceFlow id="f1" sourceRef="start" targetRef="approve1" />
    <sequenceFlow id="f2" sourceRef="approve1" targetRef="approve2" />
    <sequenceFlow id="f3" sourceRef="approve2" targetRef="end" />
  </process>
</definitions>`
}

function applyTemplate(template: string) {
  if (!template) return
  bpmnXml.value = buildBpmnXml(template)
  renderBpmn(bpmnXml.value)
}

async function renderBpmn(xml: string) {
  await nextTick()
  if (!canvasRef.value) return
  modeler = new BpmnModeler({ container: canvasRef.value })
  try {
    await modeler.importXML(xml)
  } catch (err) {
    console.warn('BPMN 渲染失败，使用文本模式', err)
  }
}

function handleDeploy() {
  if (!processName.value) {
    ElMessage.warning('请输入流程名称')
    return
  }
  if (!bpmnXml.value) {
    ElMessage.warning('请选择预设模板或编辑 XML')
    return
  }
  deploying.value = true
  deployProcess(processName.value, bpmnXml.value).then(res => {
    ElMessage.success('流程部署成功，流程 Key: ' + res.data)
    deploying.value = false
  }).catch(() => { deploying.value = false })
}

onMounted(() => {
  // 默认渲染单人审批模板
  bpmnXml.value = buildBpmnXml('single')
  renderBpmn(bpmnXml.value)
})
</script>

<style scoped>
.bpmn-canvas {
  height: 500px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}
</style>