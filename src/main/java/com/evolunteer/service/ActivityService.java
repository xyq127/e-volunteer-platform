package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Activity;

import java.util.List;
import java.util.Map;

public interface ActivityService extends IService<Activity> {

    IPage<Activity> pageActivityByOrganization(Integer organizationNum, String state, String keyword,
                                               Integer pageNum, Integer pageSize);

    IPage<Activity> pageActivityByState(String state, String keyword, Integer pageNum, Integer pageSize);

    List<Activity> getActivityByNumAndState(Integer num, String state);

    List<Activity> getActivityByState(String state);

    Boolean deleteAct(Integer num);

    List<Activity> activityInfo(Integer activityNum);

    Boolean searchActivityByName(String name);

    Object passActByAdminIdWithStatue(Map<Object, Object> map);

    Map<Object, Object> changeActivityState(String loginId, Integer activityNum, String targetState);

    Map<Object, Object> refreshActivityStates();

    Object reviseAct(Map<Object, Object> map);

    Activity selectAct(String actName);
}
