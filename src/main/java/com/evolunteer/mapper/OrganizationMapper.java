package com.evolunteer.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.evolunteer.entity.Organization;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者组织表 organization 的数据访问接口。
 *
 * @Entity com.evolunteer.entity.Organization
 */
@Repository
public interface OrganizationMapper extends BaseMapper<Organization> {

    List<Organization> selectByOrganizationName(@Param("organizationName") String organizationName);

    List<Organization> selectByOrganizationId(@Param("organizationId") String organizationId);

    int updateOrganizationNameAndOrganizationIntroductionByOrganizationId(@Param("organizationName") String organizationName, @Param("organizationIntroduction") String organizationIntroduction, @Param("organizationId") String organizationId);

    /**
     * 平台管理员分页查询志愿者组织名单：关联统一用户表取登录账号状态，并统计组织已申报的活动数量，
     * 已被停用的组织同样列出，便于平台管理员查看账号状态或重新启用
     *
     * @param page    分页对象
     * @param keyword 组织登录账号或组织名称关键字，可为空
     * @return 志愿者组织分页结果
     */
    IPage<Organization> selectAccountPage(Page<Organization> page, @Param("keyword") String keyword);

    /**
     * 查询全部志愿者组织的编号与名称，含已被停用的组织，供活动清单导出回填申报组织名称
     *
     * @return 志愿者组织列表
     */
    List<Organization> selectAllOrganizationNumAndName();
}




