package com.evolunteer.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.evolunteer.entity.Organization;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
}




