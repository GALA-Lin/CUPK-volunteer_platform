<template>
  <div class="record-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>后台 - 志愿时长管理</span>
          <div>
            <el-button :icon="Upload" @click="importDialogVisible = true" style="margin-right: 10px;">从Excel导入</el-button>
            <el-button type="primary" :icon="Plus" @click="handleOpenDialog()">登记新时长</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="realName" label="志愿者姓名" width="150" />
        <el-table-column prop="activityTitle" label="所属活动" />
        <el-table-column prop="serviceHours" label="服务时长(小时)" width="150" />
        <el-table-column prop="remarks" label="备注" show-overflow-tooltip />
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="handleOpenDialog(scope.row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
          background
          :layout="'total, sizes, prev, pager, next'"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
          style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
      <el-form ref="recordFormRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="form.userId" :min="1" placeholder="请输入用户ID" style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="活动ID" prop="activityId">
          <el-input-number v-model="form.activityId" :min="1" placeholder="请输入活动ID" style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="服务时长" prop="serviceHours">
          <el-input-number v-model="form.serviceHours" :precision="2" :step="0.5" :min="0" style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input type="textarea" v-model="form.remarks" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="从Excel导入时长" width="500px">
      <el-form label-width="100px">
        <el-form-item label="关联活动ID" required>
          <el-input-number v-model="importActivityId" :min="1" placeholder="请输入要关联的活动ID" style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="选择文件" required>
          <el-upload
              ref="uploadRef"
              :limit="1"
              :auto-upload="false"
              :on-exceed="handleExceed"
              :on-change="handleFileChange"
              accept=".xlsx, .xls"
          >
            <template #trigger>
              <el-button type="primary">选择Excel文件</el-button>
            </template>
            <el-button :icon="Download" link type="success" @click.stop="handleDownloadTemplate" style="margin-left: 10px;">
              下载模板
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传.xlsx或.xls文件，格式如下：<br>
                - 第一列：**学生学号** (必填)<br>
                - 第二列：**服务时长(小时)** (必填)<br>
                - 第三列：**备注** (选填)<br>
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="success" @click="handleSubmitImport" :loading="importLoading">开始导入</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import {ElMessage, ElMessageBox, ElPagination} from 'element-plus';
import { getServiceRecords, createServiceRecord, updateServiceRecord, deleteServiceRecord, importServiceRecords } from '@/services/serviceRecordAdminApi.js';
import { downloadImportTemplate } from '@/services/serviceRecordAdminApi.js'; // 【修改】导入新API
import { Plus, Upload, Download } from '@element-plus/icons-vue'; // 【修改】导入新图标

// --- 表格与分页数据 ---
const tableData = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(true);
const handleSizeChange = (newSize) => {
  pageSize.value = newSize;
  currentPage.value = 1;
  fetchRecords();
};
const handleDownloadTemplate = async () => {
  try {
    const blob = await downloadImportTemplate();
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `时长导入模板.xlsx`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(link.href);
  } catch (error) {
    ElMessage.error("模板下载失败");
  }
};

// --- 编辑/创建对话框 ---
const dialogVisible = ref(false);
const recordFormRef = ref(null);
const getInitialForm = () => ({ id: null, userId: null, activityId: null, serviceHours: 1.0, remarks: '' });
const form = reactive(getInitialForm());
const rules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  activityId: [{ required: true, message: '请输入活动ID', trigger: 'blur' }],
  serviceHours: [{ required: true, message: '请输入服务时长', trigger: 'blur' }],
};
const dialogTitle = computed(() => (form.id ? '编辑时长记录' : '登记新时长'));

// --- 导入功能相关状态 ---
const importDialogVisible = ref(false);
const importLoading = ref(false);
const importActivityId = ref(null);
const uploadRef = ref();
const fileToUpload = ref(null);

// --- 方法 ---

const fetchRecords = async () => {
  loading.value = true;
  try {
    const data = await getServiceRecords(currentPage.value, pageSize.value);
    tableData.value = data.records;
    total.value = data.total;
  } catch (error) { ElMessage.error('获取时长列表失败'); }
  finally { loading.value = false; }
};

const handlePageChange = (page) => {
  currentPage.value = page;
  fetchRecords();
};

const resetForm = () => {
  Object.assign(form, getInitialForm());
  recordFormRef.value?.clearValidate();
};

const handleOpenDialog = (row) => {
  resetForm();
  if (row?.id) {
    const { realName, activityTitle, ...coreData } = row;
    Object.assign(form, coreData);
  }
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!recordFormRef.value) return;
  await recordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const action = form.id ? updateServiceRecord(form.id, form) : createServiceRecord(form);
        await action;
        ElMessage.success(form.id ? '更新成功' : '创建成功');
        dialogVisible.value = false;
        await fetchRecords();
      } catch (error) { ElMessage.error(error.message || '操作失败'); }
    }
  });
};

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条时长记录吗？用户的总时长也会相应扣除。', '警告', {
    confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning',
  }).then(async () => {
    try {
      await deleteServiceRecord(id);
      ElMessage.success('删除成功');
      await fetchRecords();
    } catch (error) { ElMessage.error(error.message || '删除失败'); }
  });
};

const handleFileChange = (file) => {
  fileToUpload.value = file.raw;
};

const handleExceed = (files) => {
  uploadRef.value.clearFiles();
  const file = files[0];
  file.uid = Date.now();
  uploadRef.value.handleStart(file);
  fileToUpload.value = file.raw;
};

const handleSubmitImport = async () => {
  if (!importActivityId.value) { return ElMessage.error('请输入要关联的活动ID'); }
  if (!fileToUpload.value) { return ElMessage.error('请选择要上传的Excel文件'); }

  importLoading.value = true;
  try {
    const resultMessage = await importServiceRecords(fileToUpload.value, importActivityId.value);
    ElMessageBox.alert(resultMessage, '导入结果', {
      confirmButtonText: '好的',
      callback: () => {
        // 不管成功或失败，都刷新列表
        fetchRecords();
      },
    });
    importDialogVisible.value = false;
  } catch (error) {
    // axios拦截器已经处理了错误弹窗，这里不需要额外操作
  } finally {
    importLoading.value = false;
  }
};

onMounted(() => {
  fetchRecords();
});
</script>

<style scoped>
.record-management {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.el-upload__tip {
  line-height: 1.5;
  color: #909399;
  font-size: 12px;
}
</style>