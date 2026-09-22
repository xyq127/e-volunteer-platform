package com.evolunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Organization;

import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿者组织业务接口，基于 MyBatis-Plus 提供志愿者组织数据的通用增删改查能力，
 * 并面向志愿者组织提供志愿活动申报与组织资料维护能力。
 */
public interface OrganizationService extends IService<Organization> {

    /**
     * 申报志愿活动
     *
     * @param map 申报参数，包含组织账号与活动基本信息
     * @return 处理结果信息
     */
    Object upAct(Map<Object, Object> map);

    /**
     * 按登录账号查询志愿者组织资料
     *
     * @param id 志愿者组织登录账号
     * @return 志愿者组织实体，不存在时返回 null
     */
    Organization getOrgInfo(String id);

    /**
     * 按登录账号查询志愿者组织编号
     *
     * @param id 志愿者组织登录账号
     * @return 志愿者组织编号，不存在时返回 null
     */
    Integer getOrganizationNum(String id);

    /**
     * 修改志愿者组织的名称与简介
     *
     * @param id           志愿者组织登录账号
     * @param organization 待更新的组织名称与组织简介
     * @return 修改成功返回 true
     */
    Boolean changeInfo(String id, Organization organization);
}
