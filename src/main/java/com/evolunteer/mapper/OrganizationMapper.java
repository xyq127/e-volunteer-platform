package com.evolunteer.mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.evolunteer.entity.Organization;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationMapper extends BaseMapper<Organization> {

    List<Organization> selectByOrganizationName(@Param("organizationName") String organizationName);

    List<Organization> selectByOrganizationId(@Param("organizationId") String organizationId);

    int updateOrganizationNameAndOrganizationIntroductionByOrganizationId(@Param("organizationName") String organizationName, @Param("organizationIntroduction") String organizationIntroduction, @Param("organizationId") String organizationId);

    IPage<Organization> selectAccountPage(Page<Organization> page, @Param("keyword") String keyword);

    List<Organization> selectAllOrganizationNumAndName();
}
