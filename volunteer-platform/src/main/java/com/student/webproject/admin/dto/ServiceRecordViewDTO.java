// volunteer-platform/src/main/java/com/student/webproject/admin/dto/ServiceRecordViewDTO.java
package com.student.webproject.admin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ServiceRecordViewDTO {
    private Long id;
    private Long userId;
    private String studentId; // 新增：学号
    private Long activityId;
    private String realName; // 志愿者真实姓名
    private String activityTitle; // 活动标题
    private BigDecimal serviceHours;
    private String remarks;
    private LocalDateTime recordedAt;
}