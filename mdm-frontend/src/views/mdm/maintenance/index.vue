<template>
  <div class="app-container">
    <!-- 对象选择 -->
    <el-form :inline="true">
      <el-form-item label="数据对象">
        <el-select
          v-model="objectCode"
          placeholder="请选择数据对象"
          style="width: 260px"
          @change="handleObjectChange"
        >
          <el-option
            v-for="obj in objectOptions"
            :key="obj.objectId"
            :label="obj.objectName + '（' + obj.objectCode + '）'"
            :value="obj.objectCode"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="currentMeta.object?.objectCode">
        <el-tag type="info"
          >{{ currentMeta.object.objectCode }} · v{{
            currentMeta.object.version
          }}</el-tag
        >
      </el-form-item>
    </el-form>

    <template v-if="attributes.length">
      <!-- 动态查询区 -->
      <el-form
        v-show="showSearch"
        ref="queryRef"
        :model="queryParams"
        :inline="true"
        label-width="80px"
      >
        <el-form-item
          v-for="attr in attributes"
          :key="attr.attrCode"
          :label="attr.attrName"
          :prop="attr.attrCode"
        >
          <el-select
            v-if="attr.sourceType === 'enum'"
            v-model="queryParams[attr.attrCode]"
            placeholder="请选择"
            clearable
            style="width: 160px"
          >
            <el-option
              v-for="opt in enumOptions(attr)"
              :key="opt"
              :label="opt"
              :value="opt"
            />
          </el-select>
          <el-date-picker
            v-else-if="attr.dataType === 'date'"
            v-model="queryParams[attr.attrCode]"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择"
            style="width: 160px"
          />
          <el-date-picker
            v-else-if="attr.dataType === 'datetime'"
            v-model="queryParams[attr.attrCode]"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择"
            style="width: 160px"
          />
          <el-input
            v-else
            v-model="queryParams[attr.attrCode]"
            :placeholder="'请输入' + attr.attrName"
            clearable
            style="width: 160px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery"
            >搜索</el-button
          >
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd"
            >新增</el-button
          >
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete()"
            >删除</el-button
          >
        </el-col>
        <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
      </el-row>

      <!-- 动态表格 -->
      <el-table
        v-loading="loading"
        :data="dataList"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column
          v-for="attr in attributes"
          :key="attr.attrCode"
          :label="attr.attrName"
          :prop="attr.attrCode"
          align="center"
          :show-overflow-tooltip="true"
        />
        <el-table-column label="状态" align="center" width="90">
          <template #default="scope">
            <dict-tag :options="dataStatusOptions" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="200">
          <template #default="scope">
            <el-button
              link
              type="primary"
              icon="Edit"
              @click="handleEdit(scope.row)"
              >编辑</el-button
            >
            <el-button
              v-if="scope.row.status !== '1'"
              link
              type="success"
              icon="CircleCheck"
              @click="handleActivate(scope.row)"
              >生效</el-button
            >
            <el-button
              v-if="scope.row.status === '1'"
              link
              type="warning"
              icon="VideoPause"
              @click="handleStop(scope.row)"
              >停用</el-button
            >
            <el-button
              link
              type="danger"
              icon="Delete"
              @click="handleDelete(scope.row)"
              >删除</el-button
            >
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
    </template>
    <el-empty v-else description="请先选择已发布的数据对象" />

    <!-- 新增/编辑：动态表单 -->
    <el-dialog v-model="open" :title="title" width="600px" append-to-body>
      <el-form
        ref="dataRef"
        :model="form"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item
          v-for="attr in attributes"
          :key="attr.attrCode"
          :label="attr.attrName"
          :prop="attr.attrCode"
        >
          <el-select
            v-if="attr.sourceType === 'enum'"
            v-model="form[attr.attrCode]"
            placeholder="请选择"
            style="width: 100%"
          >
            <el-option
              v-for="opt in enumOptions(attr)"
              :key="opt"
              :label="opt"
              :value="opt"
            />
          </el-select>
          <el-date-picker
            v-else-if="attr.dataType === 'date'"
            v-model="form[attr.attrCode]"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择"
            style="width: 100%"
          />
          <el-date-picker
            v-else-if="attr.dataType === 'datetime'"
            v-model="form[attr.attrCode]"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择"
            style="width: 100%"
          />
          <el-input-number
            v-else-if="attr.dataType === 'number'"
            v-model="form[attr.attrCode]"
            :controls="false"
            :min="attr.minValue ? Number(attr.minValue) : undefined"
            :max="attr.maxValue ? Number(attr.maxValue) : undefined"
            style="width: 100%"
          />
          <el-switch
            v-else-if="attr.dataType === 'boolean'"
            v-model="form[attr.attrCode]"
            active-value="Y"
            inactive-value="N"
          />
          <el-input
            v-else
            v-model="form[attr.attrCode]"
            :placeholder="'请输入' + attr.attrName"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import type { FormInstance } from "element-plus";
import type { MdmObject, MdmAttribute, MdmObjectMeta } from "@/types";
import { listObject, getObjectMeta } from "@/api/mdm/model";
import {
  listData,
  addData,
  editData,
  delData,
  updateDataStatus,
} from "@/api/mdm/data";

const objectOptions = ref<MdmObject[]>([]);
const objectCode = ref("");
const currentMeta = ref<MdmObjectMeta>({ object: {}, attributes: [] });
const attributes = computed(() => currentMeta.value.attributes ?? []);

const dataList = ref<Record<string, any>[]>([]);
const loading = ref(false);
const showSearch = ref(true);
const total = ref(0);
const ids = ref<number[]>([]);
const multiple = ref(true);
const open = ref(false);
const title = ref("");
const queryParams = reactive<Record<string, any>>({ pageNum: 1, pageSize: 10 });
const form = ref<Record<string, any>>({});
const dataRef = ref<FormInstance>();

const dataStatusOptions = [
  { value: "0", label: "草稿", elTagType: "info" },
  { value: "1", label: "已生效", elTagType: "success" },
  { value: "2", label: "已停用", elTagType: "warning" },
];

/** 动态表单校验规则（按必填属性生成） */
const formRules = computed(() => {
  const rules: Record<string, any> = {};
  attributes.value.forEach((attr) => {
    if (attr.requiredFlag === "Y") {
      rules[attr.attrCode] = [
        {
          required: true,
          message: attr.attrName + "不能为空",
          trigger: "blur",
        },
      ];
    }
  });
  return rules;
});

/** 枚举选项 */
const enumOptions = (attr: MdmAttribute) => {
  return attr.enumValues ? attr.enumValues.split(",").filter((v) => v) : [];
};

/** 加载已发布对象列表 */
const loadObjects = async () => {
  const res = await listObject({ status: "1", pageSize: 100 });
  objectOptions.value = res.rows;
};

/** 切换数据对象 */
const handleObjectChange = async (code: string) => {
  const obj = objectOptions.value.find((o) => o.objectCode === code);
  if (!obj) return;
  const res = await getObjectMeta(obj.objectId!);
  currentMeta.value = res.data ?? { object: {}, attributes: [] };
  resetQuery();
};

const getList = async () => {
  if (!objectCode.value) return;
  loading.value = true;
  try {
    const res = await listData(objectCode.value, queryParams);
    dataList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  Object.keys(queryParams).forEach((k) => {
    if (k !== "pageNum" && k !== "pageSize") queryParams[k] = undefined;
  });
  handleQuery();
};

const handleSelectionChange = (selection: Record<string, any>[]) => {
  ids.value = selection.map((item) => item.id);
  multiple.value = !selection.length;
};

const handleAdd = () => {
  form.value = {};
  open.value = true;
  title.value = "新增数据";
};

const handleEdit = (row: Record<string, any>) => {
  form.value = { ...row };
  open.value = true;
  title.value = "编辑数据";
};

const submitForm = () => {
  dataRef.value?.validate(async (valid) => {
    if (!valid) return;
    const payload: Record<string, any> = {};
    attributes.value.forEach((attr) => {
      payload[attr.attrCode] = form.value[attr.attrCode];
    });
    if (form.value.id != null) {
      await editData(objectCode.value, form.value.id, payload);
      ElMessage.success("修改成功");
    } else {
      await addData(objectCode.value, payload);
      ElMessage.success("新增成功");
    }
    open.value = false;
    getList();
  });
};

const cancel = () => {
  open.value = false;
};

const handleActivate = (row: Record<string, any>) => {
  ElMessageBox.confirm("确定使该数据生效吗？", "系统提示", { type: "warning" })
    .then(async () => {
      await updateDataStatus(objectCode.value, row.id, "1");
      ElMessage.success("已生效");
      getList();
    })
    .catch(() => {});
};

const handleStop = (row: Record<string, any>) => {
  ElMessageBox.confirm("确定停用该数据吗？", "系统提示", { type: "warning" })
    .then(async () => {
      await updateDataStatus(objectCode.value, row.id, "2");
      ElMessage.success("已停用");
      getList();
    })
    .catch(() => {});
};

const handleDelete = (row?: Record<string, any>) => {
  const delIds = row ? [row.id] : ids.value;
  ElMessageBox.confirm("确定删除选中的数据吗？", "系统提示", {
    type: "warning",
  })
    .then(async () => {
      await delData(objectCode.value, delIds.join(","));
      ElMessage.success("删除成功");
      getList();
    })
    .catch(() => {});
};

loadObjects();
</script>
