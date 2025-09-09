package com.student.webproject.admin.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceRecordCreateDTO {
    private Long userId;
    private String studentId;
    private Long activityId;
    private BigDecimal serviceHours;
    private String remarks;
}