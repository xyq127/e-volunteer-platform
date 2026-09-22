package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.AuditLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    IPage<AuditLog> selectPageByCondition(Page<AuditLog> page,
                                          @Param("action") String action,
                                          @Param("operator") String operator);
}
