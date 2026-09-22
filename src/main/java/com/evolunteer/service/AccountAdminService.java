package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.Organization;
import com.evolunteer.entity.Volunteer;

import java.util.Map;

public interface AccountAdminService {

    IPage<Organization> pageOrganizations(String keyword, Integer page, Integer size);

    IPage<Volunteer> pageVolunteers(String keyword, Integer page, Integer size);

    Map<Object, Object> createOrganization(String loginId, String organizationId, String organizationName,
                                           String password, String establishDate, String introduction);

    Map<Object, Object> setEnabled(String loginId, String accountId, Integer enabled);

    Map<Object, Object> resetPassword(String loginId, String accountId);
}
