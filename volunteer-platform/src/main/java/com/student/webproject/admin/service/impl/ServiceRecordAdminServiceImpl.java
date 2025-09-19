// volunteer-platform/src/main/java/com/student/webproject/admin/service/impl/ServiceRecordAdminServiceImpl.java

package com.student.webproject.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.student.webproject.admin.dto.ServiceRecordCreateDTO;
import com.student.webproject.admin.dto.ServiceRecordViewDTO;
import com.student.webproject.admin.service.ServiceRecordAdminService;
import com.student.webproject.common.response.Result;
import com.student.webproject.user.Entity.User;
import com.student.webproject.user.Entity.ServiceRecord;
import com.student.webproject.user.mapper.UserMapper;
import com.student.webproject.user.mapper.ServiceRecordMapper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ServiceRecordAdminServiceImpl implements ServiceRecordAdminService {

    @Autowired
    private ServiceRecordMapper serviceRecordMapper;

    @Autowired
    private UserMapper userMapper;

    // 调整 createServiceRecord 方法中的用户查询逻辑
    @Override
    @Transactional
    public Result<ServiceRecord> createServiceRecord(ServiceRecordCreateDTO dto) {
        // 1. 校验并获取用户（支持 userId 或 studentId）
        User user = null;
        if (dto.getUserId() != null) {
            // 优先通过 userId 查询
            user = userMapper.selectById(dto.getUserId());
        } else if (dto.getStudentId() != null && !dto.getStudentId().isEmpty()) {
            // 若未传 userId，通过 studentId 查询
            user = userMapper.selectOne(new QueryWrapper<User>().eq("student_id", dto.getStudentId()));
        }

        // 校验用户是否存在
        if (user == null) {
            String errorMsg = (dto.getUserId() != null)
                    ? "操作失败，ID为 " + dto.getUserId() + " 的用户不存在"
                    : "操作失败，学号为 " + dto.getStudentId() + " 的用户不存在";
            throw new RuntimeException(errorMsg);
        }

        // 2. 校验 userId 和 studentId 是否冲突（若两者都传，需确保对应同一个用户）
        if (dto.getUserId() != null && dto.getStudentId() != null && !dto.getStudentId().isEmpty()) {
            if (!user.getStudentId().equals(dto.getStudentId())) {
                throw new RuntimeException("操作失败，用户ID与学号不匹配（ID对应的学号为：" + user.getStudentId() + "）");
            }
        }

        // 3. 检查记录是否已存在
        QueryWrapper<ServiceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", user.getId()); // 这里必须用用户真实ID，而非传入的可能错误的ID
        queryWrapper.eq("activity_id", dto.getActivityId());
        if (serviceRecordMapper.exists(queryWrapper)) {
            throw new RuntimeException("操作失败：该用户已存在此活动的志愿时长记录，请使用“编辑”功能进行修改。");
        }

        // 4. 创建记录
        ServiceRecord newRecord = new ServiceRecord();
        BeanUtils.copyProperties(dto, newRecord);
        newRecord.setUserId(user.getId()); // 强制使用查询到的真实用户ID，避免传入错误ID
        newRecord.setRecordedAt(LocalDateTime.now());
        newRecord.setRecordedBy(1L);
        newRecord.setRecordMethod("manual");
        serviceRecordMapper.insert(newRecord);

        // 5. 更新用户总时长
        BigDecimal currentTotalHours = user.getTotalServiceHours() == null ? BigDecimal.ZERO : user.getTotalServiceHours();
        BigDecimal newTotalHours = currentTotalHours.add(dto.getServiceHours());
        user.setTotalServiceHours(newTotalHours);
        userMapper.updateById(user);

        return Result.created(newRecord, "时长登记成功，用户总时长已更新");
    }
    @Override
    @Transactional
    public Result<String> importServiceRecordsFromExcel(MultipartFile file, Long activityId) throws IOException {
        List<String> errorMessages = new ArrayList<>();
        int successCount = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // 跳过表头
            if (rows.hasNext()) {
                rows.next();
            }

            int rowNum = 1;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                rowNum++;

                try {
                    String studentId = currentRow.getCell(0).getStringCellValue();
                    double hoursValue = currentRow.getCell(1).getNumericCellValue();
                    Cell remarksCell = currentRow.getCell(2);
                    String remarks = (remarksCell != null) ? remarksCell.getStringCellValue() : "";

                    User user = userMapper.selectOne(new QueryWrapper<User>().eq("student_id", studentId));
                    if (user == null) {
                        throw new RuntimeException("学号 " + studentId + " 对应的用户不存在");
                    }

                    ServiceRecordCreateDTO dto = new ServiceRecordCreateDTO();
                    dto.setUserId(user.getId());
                    dto.setActivityId(activityId);
                    dto.setServiceHours(BigDecimal.valueOf(hoursValue));
                    dto.setRemarks(remarks);

                    createServiceRecord(dto); // 调用已有的创建方法，它包含了所有事务和校验逻辑
                    successCount++;
                } catch (Exception e) {
                    errorMessages.add("第 " + rowNum + " 行处理失败: " + e.getMessage());
                }
            }
        }

        if (errorMessages.isEmpty()) {
            return Result.success("成功导入全部 " + successCount + " 条记录！");
        } else {
            return Result.error(400, "部分记录导入失败。成功 " + successCount + " 条，失败 " + errorMessages.size() + " 条。失败详情: " + String.join("; ", errorMessages));
        }
    }
    /**
     * 更新时长记录的实现（支持userId或studentId）
     */
    @Override
    @Transactional
    public Result<ServiceRecord> updateServiceRecord(Long recordId, ServiceRecordCreateDTO dto) {
        // 1. 校验并获取用户（支持 userId 或 studentId）
        User user = null;
        if (dto.getUserId() != null) {
            // 优先通过 userId 查询
            user = userMapper.selectById(dto.getUserId());
        } else if (dto.getStudentId() != null && !dto.getStudentId().isEmpty()) {
            // 若未传 userId，通过 studentId 查询
            user = userMapper.selectOne(new QueryWrapper<User>().eq("student_id", dto.getStudentId()));
        }

        // 校验用户是否存在
        if (user == null) {
            String errorMsg = (dto.getUserId() != null)
                    ? "操作失败，ID为 " + dto.getUserId() + " 的用户不存在"
                    : "操作失败，学号为 " + dto.getStudentId() + " 的用户不存在";
            throw new RuntimeException(errorMsg);
        }

        // 2. 校验 userId 和 studentId 是否冲突（若两者都传，需确保对应同一个用户）
        if (dto.getUserId() != null && dto.getStudentId() != null && !dto.getStudentId().isEmpty()) {
            if (!user.getStudentId().equals(dto.getStudentId())) {
                throw new RuntimeException("操作失败，用户ID与学号不匹配（ID对应的学号为：" + user.getStudentId() + "）");
            }
        }

        // 3. 通过 userId 和 activityId 查记录（使用查询到的真实用户ID）
        ServiceRecord oldRecord = serviceRecordMapper.selectOne(
                new QueryWrapper<ServiceRecord>()
                        .eq("user_id", user.getId())
                        .eq("activity_id", dto.getActivityId())
        );
        if (oldRecord == null) {
            throw new RuntimeException("更新失败，该用户在此活动中无时长记录");
        }

        // 4. 计算并更新用户总时长
        BigDecimal newTotalHours = calcTotalHoursAfterDiff(dto, user, oldRecord);
        user.setTotalServiceHours(newTotalHours);
        userMapper.updateById(user);

        // 5. 更新时长记录本身（强制使用查询到的真实用户ID）
        BeanUtils.copyProperties(dto, oldRecord);
        oldRecord.setId(recordId); // 确保ID不变
        oldRecord.setUserId(user.getId()); // 覆盖可能传入的错误用户ID
        serviceRecordMapper.updateById(oldRecord);

        return Result.success(oldRecord, "时长记录更新成功");
    }

    private static BigDecimal calcTotalHoursAfterDiff(ServiceRecordCreateDTO dto, User user, ServiceRecord oldRecord) {
        if (user == null) {
            throw new RuntimeException("数据关联错误，找不到记录对应的用户");
        }

        // 计算时长差值，处理可能的null值
        BigDecimal oldHours = oldRecord.getServiceHours() == null ? BigDecimal.ZERO : oldRecord.getServiceHours();
        BigDecimal newHours = dto.getServiceHours() == null ? BigDecimal.ZERO : dto.getServiceHours();
        BigDecimal difference = newHours.subtract(oldHours); // 新时长 - 旧时长

        // 更新用户总时长
        BigDecimal currentTotalHours = user.getTotalServiceHours() == null ? BigDecimal.ZERO : user.getTotalServiceHours();
        return currentTotalHours.add(difference);
    }

    /**
     * 删除时长记录的实现
     */
    @Override
    @Transactional
    public Result<Void> deleteServiceRecord(Long recordId) {
        // 1. 查找要删除的记录
        ServiceRecord recordToDelete = serviceRecordMapper.selectById(recordId);
        if (recordToDelete == null) {
            throw new RuntimeException("删除失败，找不到ID为 " + recordId + " 的时长记录");
        }

        // 2. 查找关联用户
        User user = userMapper.selectById(recordToDelete.getUserId());
        BigDecimal updatedTotalHours = getBigDecimal(user, recordToDelete);

        user.setTotalServiceHours(updatedTotalHours);
        userMapper.updateById(user);

        // 4. 删除这条时长记录
        serviceRecordMapper.deleteById(recordId);

        return Result.success(null, "时长记录删除成功");
    }

    private static BigDecimal getBigDecimal(User user, ServiceRecord recordToDelete) {
        if (user == null) {
            throw new RuntimeException("数据关联错误，找不到记录对应的用户");
        }

        // 3. 计算并更新用户总时长（增加空值处理）
        BigDecimal hoursToDelete = recordToDelete.getServiceHours() == null ? BigDecimal.ZERO : recordToDelete.getServiceHours();
        BigDecimal currentTotalHours = user.getTotalServiceHours() == null ? BigDecimal.ZERO : user.getTotalServiceHours();

        // 确保总时长不会为负数
        BigDecimal updatedTotalHours = currentTotalHours.subtract(hoursToDelete);
        if (updatedTotalHours.compareTo(BigDecimal.ZERO) < 0) {
            updatedTotalHours = BigDecimal.ZERO;
            // 可以考虑这里记录日志，提示总时长出现负数的异常情况
        }
        return updatedTotalHours;
    }

    /**
     * 【新增】获取时长记录列表的实现
     */
    @Override
    public Result<IPage<ServiceRecordViewDTO>> listServiceRecords(Long page, Long pageSize) {
        Page<ServiceRecordViewDTO> pageRequest = new Page<>(page, pageSize);
        IPage<ServiceRecordViewDTO> pageResult = serviceRecordMapper.selectServiceRecordViewPage(pageRequest);
        return Result.success(pageResult, "时长记录查询成功");
    }
    /**
     * 下载模板的业务逻辑实现
     */
    @Override
    public ByteArrayInputStream downloadExcelTemplate() throws IOException {
        String[] headers = {"学生学号", "服务时长(小时)", "备注"};
        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("时长导入模板");
            Row headerRow = sheet.createRow(0);

            // 创建表头
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
             //可以在此添加示例数据，例如：
             Row exampleRow = sheet.createRow(1);
             exampleRow.createCell(0).setCellValue("示例，使用时请删除");
             exampleRow.createCell(1).setCellValue(2.5);
             exampleRow.createCell(2).setCellValue("活动表现优异");

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}