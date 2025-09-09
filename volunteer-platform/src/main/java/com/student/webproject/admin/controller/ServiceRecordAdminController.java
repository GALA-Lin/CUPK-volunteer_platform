// volunteer-platform/src/main/java/com/student/webproject/admin/controller/ServiceRecordAdminController.java

package com.student.webproject.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.student.webproject.admin.dto.ServiceRecordCreateDTO;
import com.student.webproject.admin.dto.ServiceRecordViewDTO;
import com.student.webproject.admin.service.ServiceRecordAdminService;
import com.student.webproject.common.response.Result;
import com.student.webproject.user.Entity.ServiceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/admin/service-records")
public class ServiceRecordAdminController {

    @Autowired
    private ServiceRecordAdminService serviceRecordAdminService;

    /**
     * 创建服务记录
     *
     * @param dto 服务记录创建DTO
     * @return 包含创建服务记录结果的Result对象
     */
    @PostMapping
    public Result<ServiceRecord> createServiceRecord(@RequestBody ServiceRecordCreateDTO dto) {
        return serviceRecordAdminService.createServiceRecord(dto);
    }


    /**
     * 更新服务时长记录的API端点
     * @param id 要更新的记录ID，从URL路径中获取
     * @param dto 包含更新数据的请求体
     * @return 更新后的服务记录
     */
    @PutMapping("/{id}")
    public Result<ServiceRecord> updateServiceRecord(@PathVariable Long id, @RequestBody ServiceRecordCreateDTO dto) {
        return serviceRecordAdminService.updateServiceRecord(id, dto);
    }

    /**
     * 删除服务时长记录的API端点
     * @param id 要删除的记录ID，从URL路径中获取
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteServiceRecord(@PathVariable Long id) {
        return serviceRecordAdminService.deleteServiceRecord(id);
    }
    /**
     * 获取服务时长记录列表的API端点
     */
    @GetMapping
    public Result<IPage<ServiceRecordViewDTO>> listServiceRecords(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long pageSize
    ) {
        return serviceRecordAdminService.listServiceRecords(page, pageSize);
    }
    /**
     * 处理Excel文件上传的API端点
     * @param file 上传的文件
     * @param activityId 表单中附带的活动ID
     * @return 处理结果
     * @throws IOException 如果文件读取过程中失败
     */
    @PostMapping("/import")
    public Result<String> importFromExcel(@RequestParam("file") MultipartFile file, @RequestParam("activityId") Long activityId) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return Result.error(400, "上传失败，文件为空。");
        }
        return serviceRecordAdminService.importServiceRecordsFromExcel(file, activityId);
    }
    /**
     * 提供Excel模板下载的API端点
     */
    @GetMapping("/template")
    public ResponseEntity<InputStreamResource> downloadTemplate() throws IOException {
        String fileName = "时长导入模板.xlsx";
        ByteArrayInputStream bis = serviceRecordAdminService.downloadExcelTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(bis));
    }
}