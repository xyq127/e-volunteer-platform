package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.AuditLog;
import com.evolunteer.enums.AuditActionEnum;

public interface AuditLogService {

    void record(String operator, String role, AuditActionEnum action, String target, String detail, String ip);

    IPage<AuditLog> page(String action, String operator, Integer pageNum, Integer pageSize);
}
