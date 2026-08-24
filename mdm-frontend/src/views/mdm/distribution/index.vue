<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab1 应用凭证 -->
      <el-tab-pane label="应用凭证" name="app">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAddApp">新增应用</el-button>
          </el-col>
        </el-row>
        <el-table v-loading="appLoading" :data="appList" border>
          <el-table-column label="应用名称" prop="appName" min-width="140" />
          <el-table-column label="AppID" prop="appid" min-width="220">
            <template #default="scope">
              <el-tag type="info">{{ scope.row.appid }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="scope">
              <el-switch :model-value="scope.row.enabled === '1'" @change="(v: boolean | string | number) => handleAppEnabled(scope.row, v)" />
            </template>
          </el-table-column>
          <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
          <el-table-column label="创建时间" prop="createTime" width="160" align="center" />
          <el-table-column label="操作" width="220" align="center">
            <template #default="scope">
              <el-button link type="warning" icon="Key" @click="handleResetSecret(scope.row)">重置密钥</el-button>
              <el-button link type="primary" icon="Edit" @click="handleEditApp(scope.row)">编辑</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDeleteApp(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="appTotal > 0"
          :total="appTotal"
          v-model:page="appQuery.pageNum"
          v-model:limit="appQuery.pageSize"
          @pagination="getAppList"
        />
      </el-tab-pane>

      <!-- Tab2 分发配置 -->
      <el-tab-pane label="分发配置" name="config">
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAddDist">新增配置</el-button>
          </el-col>
        </el-row>
        <el-table v-loading="distLoading" :data="distList" border>
          <el-table-column label="订阅应用" prop="appName" min-width="130" />
          <el-table-column label="数据对象" prop="objectName" min-width="130" />
          <el-table-column label="触发时机" width="120" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.triggerType === 'IMMEDIATE' ? 'success' : 'info'">
                {{ scope.row.triggerType === 'IMMEDIATE' ? '变更即推' : '手动' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="分发方式" width="90" align="center">
            <template #default="scope">
              <el-tag :type="(scope.row.channel || 'HTTP') === 'MQ' ? 'warning' : 'primary'" size="small">
                {{ (scope.row.channel || 'HTTP') === 'MQ' ? 'MQ队列' : 'HTTP' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="回调地址/队列" prop="endpointUrl" min-width="220" show-overflow-tooltip>
            <template #default="scope">
              <span v-if="scope.row.channel === 'MQ'">{{ scope.row.queueName || 'mdm.dist.<对象编码>' }}</span>
              <span v-else>{{ scope.row.endpointUrl }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.enabled === '1' ? 'success' : 'info'">
                {{ scope.row.enabled === '1' ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleEditDist(scope.row)">编辑</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDeleteDist(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="distTotal > 0"
          :total="distTotal"
          v-model:page="distQuery.pageNum"
          v-model:limit="distQuery.pageSize"
          @pagination="getDistList"
        />
      </el-tab-pane>

      <!-- Tab3 分发记录 -->
      <el-tab-pane label="分发记录" name="record">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="recordQuery.status" placeholder="全部" clearable style="width: 130px" @change="handleRecordQuery">
              <el-option label="待发送" value="0" />
              <el-option label="成功" value="1" />
              <el-option label="失败" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleRecordQuery">搜索</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="recordLoading" :data="recordList" border>
          <el-table-column label="订阅应用" prop="appName" min-width="120" />
          <el-table-column label="对象" prop="objectCode" width="120" align="center" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.actionType === 'INSERT' ? 'success' : 'primary'">
                {{ scope.row.actionType === 'INSERT' ? '新增' : '修改' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="数据ID" prop="dataId" width="80" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="scope">
              <el-tag :type="recordStatusTag(scope.row.status!)">
                {{ recordStatusLabel(scope.row.status!) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发送时间" prop="sendTime" width="160" align="center" />
          <el-table-column label="成功时间" prop="successTime" width="160" align="center" />
          <el-table-column label="失败原因" prop="errorMsg" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="90" align="center">
            <template #default="scope">
              <el-button v-if="scope.row.status === '2'" link type="primary" icon="Refresh" @click="handleRetry(scope.row)">重推</el-button>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="recordTotal > 0"
          :total="recordTotal"
          v-model:page="recordQuery.pageNum"
          v-model:limit="recordQuery.pageSize"
          @pagination="getRecordList"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 应用凭证表单对话框 -->
    <el-dialog v-model="appOpen" :title="appTitle" width="460px" append-to-body>
      <el-form ref="appRef" :model="appForm" :rules="appRules" label-width="90px">
        <el-form-item label="应用名称" prop="appName">
          <el-input v-model="appForm.appName" placeholder="订阅方系统名称" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="appForm.remark" type="textarea" :rows="2" placeholder="订阅用途说明" />
        </el-form-item>
      </el-form>
      <el-alert
        v-if="appCredential"
        type="success"
        :closable="false"
        title="创建成功，请立即保存以下凭据（密钥不再显示）"
        style="margin-top: 8px"
      >
        <div style="margin-top: 6px">
          <div>AppID：<b>{{ appCredential.appid }}</b></div>
          <div>Secret：<b>{{ appCredential.secret }}</b></div>
        </div>
      </el-alert>
      <template #footer>
        <el-button type="primary" @click="submitAppForm">确 定</el-button>
        <el-button @click="appOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 分发配置表单对话框 -->
    <el-dialog v-model="distOpen" :title="distTitle" width="520px" append-to-body>
      <el-form ref="distRef" :model="distForm" :rules="distRules" label-width="100px">
        <el-form-item label="订阅应用" prop="appId">
          <el-select v-model="distForm.appId" placeholder="选择订阅应用" style="width: 100%">
            <el-option v-for="app in appList" :key="app.appId!" :label="app.appName" :value="app.appId" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据对象" prop="objectId">
          <el-select v-model="distForm.objectId" placeholder="选择数据对象" style="width: 100%">
            <el-option
              v-for="obj in objectOptions"
              :key="obj.objectId!"
              :label="obj.objectName + '（' + obj.objectCode + '）'"
              :value="obj.objectId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="触发时机" prop="triggerType">
          <el-radio-group v-model="distForm.triggerType">
            <el-radio value="IMMEDIATE">变更即推</el-radio>
            <el-radio value="MANUAL">手动重推</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分发方式" prop="channel">
          <el-radio-group v-model="distForm.channel">
            <el-radio value="HTTP">HTTP 回调</el-radio>
            <el-radio value="MQ">RabbitMQ 队列</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="distForm.channel === 'MQ'" label="队列名称" prop="queueName">
          <el-input v-model="distForm.queueName" placeholder="默认 mdm.dist.<对象编码>，可自定义" />
        </el-form-item>
        <el-form-item v-if="distForm.channel !== 'MQ'" label="回调地址" prop="endpointUrl">
          <el-input v-model="distForm.endpointUrl" placeholder="如 http://erp.example.com/mdm/push" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="distForm.enabled" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitDistForm">确 定</el-button>
        <el-button @click="distOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { FormInstance } from "element-plus";
import type { MdmApp, MdmDistribution, MdmDistributionRecord, MdmObject } from "@/types";
import { listObject } from "@/api/mdm/model";
import {
  listApp, addApp, editApp, delApp, resetSecret,
  listDist, addDist, editDist, delDist,
  listRecord, retryRecord,
} from "@/api/mdm/distribution";

const activeTab = ref("app");
const objectOptions = ref<MdmObject[]>([]);

// ===== 应用凭证 =====
const appList = ref<MdmApp[]>([]);
const appLoading = ref(false);
const appTotal = ref(0);
const appQuery = ref({ pageNum: 1, pageSize: 10 });
const appOpen = ref(false);
const appTitle = ref("");
const appForm = ref<MdmApp>({ enabled: "1" });
const appRef = ref<FormInstance>();
const appCredential = ref<MdmApp | null>(null);
const appRules = {
  appName: [{ required: true, message: "应用名称不能为空", trigger: "blur" }],
};

const getAppList = async () => {
  appLoading.value = true;
  try {
    const res = await listApp(appQuery.value);
    appList.value = res.rows;
    appTotal.value = res.total;
  } finally {
    appLoading.value = false;
  }
};

const handleAddApp = () => {
  appForm.value = { enabled: "1" };
  appCredential.value = null;
  appTitle.value = "新增应用";
  appOpen.value = true;
};

const handleEditApp = (row: MdmApp) => {
  appForm.value = { ...row };
  appCredential.value = null;
  appTitle.value = "编辑应用";
  appOpen.value = true;
};

const submitAppForm = () => {
  appRef.value?.validate(async (valid) => {
    if (!valid) return;
    if (appForm.value.appId != null) {
      await editApp(appForm.value);
      ElMessage.success("保存成功");
    } else {
      const res = await addApp(appForm.value);
      appCredential.value = { appid: res.data?.appid, secret: res.data?.secret };
      ElMessage.success("应用创建成功，请在弹窗中保存凭据");
    }
    appOpen.value = false;
    getAppList();
  });
};

const handleAppEnabled = async (row: MdmApp, v: boolean | string | number) => {
  await editApp({ appId: row.appId, enabled: v ? "1" : "0" });
  ElMessage.success(v ? "已启用" : "已停用");
  getAppList();
};

const handleResetSecret = (row: MdmApp) => {
  ElMessageBox.confirm("重置后原密钥立即失效、仅展示一次，确认重置？", "系统提示", { type: "warning" })
    .then(async () => {
      const res = await resetSecret(row.appId!);
      ElMessageBox.alert(`应用「${row.appName}」新 Secret：\n${res.data}`, "重置成功", { confirmButtonText: "我已保存" });
      getAppList();
    })
    .catch(() => {});
};

const handleDeleteApp = (row: MdmApp) => {
  ElMessageBox.confirm(`确定删除应用「${row.appName}」及其分发配置吗？`, "系统提示", { type: "warning" })
    .then(async () => {
      await delApp(row.appId!);
      ElMessage.success("删除成功");
      getAppList();
    })
    .catch(() => {});
};

// ===== 分发配置 =====
const distList = ref<MdmDistribution[]>([]);
const distLoading = ref(false);
const distTotal = ref(0);
const distQuery = ref({ pageNum: 1, pageSize: 10 });
const distOpen = ref(false);
const distTitle = ref("");
const distForm = ref<MdmDistribution>({ triggerType: "IMMEDIATE", enabled: "1", channel: "HTTP" });
const distRef = ref<FormInstance>();
const distRules = {
  appId: [{ required: true, message: "请选择订阅应用", trigger: "change" }],
  objectId: [{ required: true, message: "请选择数据对象", trigger: "change" }],
  endpointUrl: [{ required: true, message: "回调地址不能为空", trigger: "blur" }],
};

const getDistList = async () => {
  distLoading.value = true;
  try {
    const res = await listDist(distQuery.value);
    distList.value = res.rows;
    distTotal.value = res.total;
  } finally {
    distLoading.value = false;
  }
};

const handleAddDist = () => {
  distForm.value = { triggerType: "IMMEDIATE", enabled: "1", channel: "HTTP" };
  distTitle.value = "新增分发配置";
  distOpen.value = true;
};

const handleEditDist = (row: MdmDistribution) => {
  distForm.value = { ...row };
  distTitle.value = "编辑分发配置";
  distOpen.value = true;
};

const submitDistForm = () => {
  distRef.value?.validate(async (valid) => {
    if (!valid) return;
    if (distForm.value.distId != null) {
      await editDist(distForm.value);
    } else {
      await addDist(distForm.value);
    }
    ElMessage.success("保存成功");
    distOpen.value = false;
    getDistList();
  });
};

const handleDeleteDist = (row: MdmDistribution) => {
  ElMessageBox.confirm("确定删除该分发配置吗？", "系统提示", { type: "warning" })
    .then(async () => {
      await delDist(row.distId!);
      ElMessage.success("删除成功");
      getDistList();
    })
    .catch(() => {});
};

// ===== 分发记录 =====
const recordList = ref<MdmDistributionRecord[]>([]);
const recordLoading = ref(false);
const recordTotal = ref(0);
const recordQuery = ref({ pageNum: 1, pageSize: 10 });

const recordStatusLabel = (s: string) =>
  s === "1" ? "成功" : s === "2" ? "失败" : "待发送";

const recordStatusTag = (s: string) =>
  s === "1" ? "success" : s === "2" ? "danger" : "warning";

const getRecordList = async () => {
  recordLoading.value = true;
  try {
    const res = await listRecord(recordQuery.value);
    recordList.value = res.rows;
    recordTotal.value = res.total;
  } finally {
    recordLoading.value = false;
  }
};

const handleRecordQuery = () => {
  recordQuery.value.pageNum = 1;
  getRecordList();
};

const handleRetry = (row: MdmDistributionRecord) => {
  ElMessageBox.confirm("确定重推该记录吗？", "系统提示", { type: "warning" })
    .then(async () => {
      await retryRecord(row.recordId!);
      ElMessage.success("已重推");
      getRecordList();
    })
    .catch(() => {});
};

onMounted(async () => {
  getAppList();
  getDistList();
  getRecordList();
  const res = await listObject({ status: "1", pageSize: 100 });
  objectOptions.value = res.rows;
});
</script>