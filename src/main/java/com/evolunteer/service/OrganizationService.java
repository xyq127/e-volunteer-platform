package com.evolunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Organization;

import java.util.Map;

public interface OrganizationService extends IService<Organization> {

    Object upAct(Map<Object, Object> map);

    Organization getOrgInfo(String id);

    Integer getOrganizationNum(String id);

    Boolean changeInfo(String id, Organization organization);
}
