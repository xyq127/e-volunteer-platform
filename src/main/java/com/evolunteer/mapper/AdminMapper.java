package com.evolunteer.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.evolunteer.entity.Admin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台管理员表 admin 的数据访问接口。
 *
 * @Entity com.evolunteer.entity.Admin
 */
@Repository
public interface AdminMapper extends BaseMapper<Admin> {

    List<Admin> selectByAdminName(@Param("adminName") String adminName);

    List<Admin> selectByAdminId(@Param("adminId") String adminId);

}




