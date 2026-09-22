package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evolunteer.entity.Organization;
import com.evolunteer.mapper.ActivityMapper;
import com.evolunteer.mapper.OrganizationMapper;
import com.evolunteer.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

    @Autowired
    ActivityMapper activityMapper;

    @Override
    public Object upAct(Map<Object, Object> map) {
        activityMapper.organization_insert_activity(map);
        return map.get("msg");
    }

    @Override
    public Organization getOrgInfo(String id) {
        List<Organization> organizations = baseMapper.selectByOrganizationId(id);
        return organizations.isEmpty() ? null : organizations.get(0);
    }

    @Override
    public Integer getOrganizationNum(String id) {
        Organization organization = getOrgInfo(id);
        return organization == null ? null : organization.getOrganizationNum();
    }

    @Override
    public Boolean changeInfo(String id, Organization organization) {
        return baseMapper.updateOrganizationNameAndOrganizationIntroductionByOrganizationId(
                organization.getOrganizationName(), organization.getOrganizationIntroduction(), id) > 0;
    }
}
