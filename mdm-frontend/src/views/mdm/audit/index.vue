<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- Tab1 审核配置 -->
      <el-tab-pane label="审核配置" name="config">
        <el-form :inline="true">
          <el-form-item label="数据对象">
            <el-select
              v-model="configObjectId"
              placeholder="请选择数据对象"
              style="width: 260px"
              @change="loadFlowConfig"
            >
              <el-option
                v-for="obj in objectOptions"
                :key="obj.objectId!"
                :label="obj.objectName + '（' + obj.objectCode + '）'"
                :value="obj.objectId"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <el-form
          v-if="configObjectId != null"
          :model="flowForm"
          label-width="120px"
          style="max-width: 480px"
        >
          <el-form-item label="启用审核流程">
            <el-switch v-model="flowForm.enabled" active-value="1" inactive-value="0" />
            <el-alert
              v-if="flowForm.enabled === '1'"
              type="info"
              :closable="false"
              show-icon
              title="启用后，该对象的新增/修改将自动转入待审核，审核通过后生效"
              style="margin-top: 6px"
            />
          </el-form-item>
          <el-form-item v-if="flowForm.enabled === '1'" label="审核角色">
            <el-select v-model="flowForm.auditRole" placeholder="选择可参与审核的角色" clearable style="width: 100%">
              <el-option
                v-for="role in roleOptions"
                :key="role.roleId"
                :label="role.roleName"
                :value="role.roleKey"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveFlowConfig">保存配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- Tab2 审核任务（待办） -->
      <el-tab-pane label="审核任务" name="task">
        <el-form :inline="true" class="mb8">
          <el-form-item label="状态">
            <el-select v-model="taskQuery.status" placeholder="全部" clearable style="width: 130px" @change="handleTaskQuery">
              <el-option label="待审核" value="0" />
              <el-option label="已通过" value="1" />
              <el-option label="已驳回" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleTaskQuery">搜索</el-button>
          </el-form-item>
        </el-form>
        <el-table v-loading="taskLoading" :data="taskList" border>
          <el-table-column label="数据对象" min-width="160">
            <template #default="scope">{{ objectName(scope.row.objectId) }}</template>
          </el-table-column>
          <el-table-column label="操作类型" width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.actionType === 'INSERT' ? 'success' : 'primary'">
                {{ scope.row.actionType === 'INSERT' ? '新增' : '修改' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="提交人" prop="submitBy" width="100" align="center" />
          <el-table-column label="提交时间" prop="createTime" width="160" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="scope">
              <el-tag :type="taskStatusTag(scope.row.status!)">
                {{ taskStatusLabel(scope.row.status!) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="审核人" prop="auditBy" width="100" align="center" />
          <el-table-column label="驳回原因" min-width="120" show-overflow-tooltip>
            <template #default="scope">{{ scope.row.rejectReason || '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
              <template v-if="scope.row.status === '0'">
                <el-button link type="success" icon="Check" @click="handleApprove(scope.row)">通过</el-button>
                <el-button link type="danger" icon="Close" @click="handleReject(scope.row)">驳回</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <pagination
          v-show="taskTotal > 0"
          :total="taskTotal"
          v-model:page="taskQuery.pageNum"
          v-model:limit="taskQuery.pageSize"
          @pagination="getTaskList"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 审核详情：变更前后快照对比 -->
    <el-dialog v-model="detailOpen" title="审核详情" width="760px" append-to-body>
      <el-descriptions :column="2" border size="small" style="margin-bottom: 12px">
        <el-descriptions-item label="数据对象">{{ objectName(detailTask?.objectId) }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="detailTask?.actionType === 'INSERT' ? 'success' : 'primary'">
            {{ detailTask?.actionType === 'INSERT' ? '新增' : '修改' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交人">{{ detailTask?.submitBy }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detailTask?.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="diffRows" border size="small">
        <el-table-column label="字段" min-width="140" />
        <el-table-column label="变更前" min-width="150" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.before }}</template>
        </el-table-column>
        <el-table-column label="变更后" min-width="150" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.after }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectOpen" title="驳回审核" width="420px" append-to-body>
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="3"
        placeholder="请输入驳回原因（必填）"
      />
      <template #footer>
        <el-button type="primary" @click="submitReject">确 定</el-button>
        <el-button @click="rejectOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { MdmObject, MdmAuditFlow, MdmAuditTask, MdmAttribute } from "@/types";
import { listObject, getObjectMeta } from "@/api/mdm/model";
import { listRole } from "@/api/system/role";
import type { SysRole } from "@/types";
import { getAuditFlow, saveAuditFlow, listAuditTask, approveAudit, rejectAudit } from "@/api/mdm/audit";

const activeTab = ref("config");

// ===== 对象与角色选项 =====
const objectOptions = ref<MdmObject[]>([]);
const roleOptions = ref<SysRole[]>([]);
/** objectId -> 属性元数据（用于快照字段名翻译） */
const attrMap = new Map<number, MdmAttribute[]>();

const loadObjects = async () => {
  const res = await listObject({ status: "1", pageSize: 100 });
  objectOptions.value = res.rows;
};

const loadRoles = async () => {
  const res = await listRole({ pageNum: 1, pageSize: 100 });
  roleOptions.value = res.rows;
};

const objectName = (id?: number) =>
  objectOptions.value.find((o) => o.objectId === id)?.objectName ?? `对象#${id}`;

/** 懒加载对象属性元数据并缓存 */
const loadAttrs = async (objectId?: number) => {
  if (!objectId || attrMap.has(objectId)) return;
  const res = await getObjectMeta(objectId);
  attrMap.set(objectId, res.data?.attributes ?? []);
};

// ===== 审核配置 =====
const configObjectId = ref<number>();
const flowForm = ref<MdmAuditFlow>({ enabled: "0" });

const loadFlowConfig = async (id: number) => {
  flowForm.value = (await getAuditFlow(id)).data ?? { objectId: id, enabled: "0" };
};

const saveFlowConfig = async () => {
  await saveAuditFlow({ objectId: configObjectId.value, ...flowForm.value });
  ElMessage.success("审核流程配置已保存");
};

// ===== 审核任务 =====
const taskList = ref<MdmAuditTask[]>([]);
const taskLoading = ref(false);
const taskTotal = ref(0);
const taskQuery = ref({ pageNum: 1, pageSize: 10, status: "0" });

const taskStatusLabel = (s: string) =>
  s === "1" ? "已通过" : s === "2" ? "已驳回" : "待审核";

const taskStatusTag = (s: string) =>
  s === "1" ? "success" : s === "2" ? "danger" : "warning";

const getTaskList = async () => {
  taskLoading.value = true;
  try {
    const res = await listAuditTask(taskQuery.value);
    taskList.value = res.rows;
    taskTotal.value = res.total;
  } finally {
    taskLoading.value = false;
  }
};

const handleTaskQuery = () => {
  taskQuery.value.pageNum = 1;
  getTaskList();
};

const handleApprove = (row: MdmAuditTask) => {
  ElMessageBox.confirm("审核通过后变更将立即生效，确认通过？", "系统提示", { type: "warning" })
    .then(async () => {
      await approveAudit(row.taskId!);
      ElMessage.success("已通过");
      getTaskList();
    })
    .catch(() => {});
};

const rejectOpen = ref(false);
const rejectReason = ref("");
let rejectingTask: MdmAuditTask | null = null;

const handleReject = (row: MdmAuditTask) => {
  rejectingTask = row;
  rejectReason.value = "";
  rejectOpen.value = true;
};

const submitReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning("请填写驳回原因");
    return;
  }
  await rejectAudit(rejectingTask!.taskId!, rejectReason.value.trim());
  ElMessage.success("已驳回");
  rejectOpen.value = false;
  getTaskList();
};

// ===== 审核详情（前后快照对比）=====
const detailOpen = ref(false);
const detailTask = ref<MdmAuditTask>();

/** 忽略基础列，仅展示业务属性变更 */
const BASE_COLUMNS = new Set([
  "id", "object_code", "status", "version", "create_by", "create_time", "update_by", "update_time", "remark",
]);

const diffRows = computed(() => {
  const task = detailTask.value;
  if (!task) return [];
  const before = task.beforeData ? JSON.parse(task.beforeData) : {};
  const after = task.afterData ? JSON.parse(task.afterData) : {};
  const attrs = attrMap.get(task.objectId ?? -1) ?? [];
  const keys = Array.from(new Set([...Object.keys(before), ...Object.keys(after)]))
    .filter((k) => !BASE_COLUMNS.has(k));
  return keys.map((k) => {
    const attr = attrs.find((a) => a.attrCode === k);
    const v = (o: Record<string, any>) => (o[k] == null || o[k] === "" ? "-" : String(o[k]));
    return {
      label: attr ? `${attr.attrName}（${k}）` : k,
      before: task.actionType === "UPDATE" ? v(before) : "-",
      after: v(after),
    };
  });
});

const handleView = async (row: MdmAuditTask) => {
  detailTask.value = row;
  await loadAttrs(row.objectId);
  detailOpen.value = true;
};

loadObjects();
loadRoles();
</script>