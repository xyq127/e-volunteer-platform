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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者组织业务实现类：负责志愿活动的申报入库以及志愿者组织资料的查询与维护。
 */
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

    @Autowired
    ActivityMapper activityMapper;

    /**
     * 调用存储过程写入志愿者组织申报的志愿活动
     *
     * @param map 申报参数，包含组织账号、活动名称、活动时间、活动地点与招募人数等
     * @return 存储过程返回的处理结果信息
     */
    @Override
    public Object upAct(Map<Object, Object> map) {
        activityMapper.organization_insert_activity(map);
        return map.get("msg");
    }

    /**
     * 按登录账号查询志愿者组织资料
     *
     * @param id 志愿者组织登录账号
     * @return 志愿者组织实体，不存在时返回 null
     */
    @Override
    public Organization getOrgInfo(String id) {
        List<Organization> organizations = baseMapper.selectByOrganizationId(id);
        return organizations.isEmpty() ? null : organizations.get(0);
    }

    /**
     * 按登录账号查询志愿者组织编号
     *
     * @param id 志愿者组织登录账号
     * @return 志愿者组织编号，不存在时返回 null
     */
    @Override
    public Integer getOrganizationNum(String id) {
        Organization organization = getOrgInfo(id);
        return organization == null ? null : organization.getOrganizationNum();
    }

    /**
     * 修改志愿者组织的名称与简介
     *
     * @param id           志愿者组织登录账号
     * @param organization 待更新的组织名称与组织简介
     * @return 修改成功返回 true
     */
    @Override
    public Boolean changeInfo(String id, Organization organization) {
        return baseMapper.updateOrganizationNameAndOrganizationIntroductionByOrganizationId(
                organization.getOrganizationName(), organization.getOrganizationIntroduction(), id) > 0;
    }
}
