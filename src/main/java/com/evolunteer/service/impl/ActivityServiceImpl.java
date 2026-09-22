package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evolunteer.entity.Activity;
import com.evolunteer.mapper.ActivityMapper;
import com.evolunteer.service.ActivityService;
import com.evolunteer.service.NotificationService;
import com.evolunteer.service.ParticipationService;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ParticipationService participationService;

    @Override
    public IPage<Activity> pageActivityByOrganization(Integer organizationNum, String state, String keyword,
                                                      Integer pageNum, Integer pageSize) {
        Page<Activity> page = PageSupport.of(pageNum, pageSize);
        return baseMapper.selectPageByOrganizationAndState(page, organizationNum,
                state, PageSupport.normalizeKeyword(keyword));
    }

    @Override
    public IPage<Activity> pageActivityByState(String state, String keyword, Integer pageNum, Integer pageSize) {
        Page<Activity> page = PageSupport.of(pageNum, pageSize);
        return baseMapper.selectPageByState(page, state, PageSupport.normalizeKeyword(keyword));
    }

    @Override
    public List<Activity> getActivityByNumAndState(Integer num, String state) {
        return baseMapper.selectAllByOrganizationNumAndActivityStateAndActivityIsdeleted(num, state, 0);
    }

    @Override
    public List<Activity> getActivityByState(String state) {
        return baseMapper.selectAllByActivityStateAndActivityIsdeleted(state, 0);
    }

    @Override
    public List<Activity> activityInfo(Integer activityNum) {
        return baseMapper.selectByActivityNum(activityNum);
    }

    @Override
    public Boolean searchActivityByName(String name) {
        return baseMapper.selectAllByActivityName(name).isEmpty();
    }

    @Override
    public Object passActByAdminIdWithStatue(Map<Object, Object> map) {
        baseMapper.admin_check_activity(map);
        return map.get("msg");
    }

    @Override
    public Map<Object, Object> changeActivityState(String loginId, Integer activityNum, String targetState) {

        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", loginId);
        map.put("activityNum", activityNum);
        map.put("targetState", targetState);
        baseMapper.organization_change_activity_state(map);

        if (isSuccess(map)) {
            notifyParticipants(activityNum, targetState);
        }
        return map;
    }

    @Override
    public Map<Object, Object> refreshActivityStates() {

        Map<Object, Object> map = new HashMap<>();
        baseMapper.activity_state_refresh(map);

        Integer startedCount = toInt(map.get("startedCount"));
        Integer finishedCount = toInt(map.get("finishedCount"));
        if (startedCount > 0 || finishedCount > 0) {
            log.info("活动状态自动流转完成，自动开始 {} 个，自动结束 {} 个", startedCount, finishedCount);
        }
        return map;
    }

    @Override
    public Object reviseAct(Map<Object, Object> map) {
        baseMapper.organization_revise_activity(map);
        return map.get("msg");
    }

    @Override
    public Activity selectAct(String actName) {
        List<Activity> activities = baseMapper.selectByActivityName(actName);
        return activities.isEmpty() ? null : activities.get(0);
    }

    @Override
    public Boolean deleteAct(Integer num) {
        return baseMapper.updateActivityIsdeletedByActivityNum(1, num) > 0;
    }

    private void notifyParticipants(Integer activityNum, String targetState) {

        List<Activity> activities = baseMapper.selectByActivityNum(activityNum);
        if (activities.isEmpty()) {
            return;
        }
        Activity activity = activities.get(0);
        List<String> accounts = participationService.approvedVolunteerAccounts(activityNum);
        if (accounts.isEmpty()) {
            return;
        }
        if ("2".equals(targetState)) {
            notificationService.send(accounts, "ROLE_VOLUNTEER", "活动已开始：" + activity.getActivityName(),
                    "活动已开始，请在活动现场使用签到码完成签到，活动结束后签退。");
        } else {
            notificationService.send(accounts, "ROLE_VOLUNTEER", "活动已结束：" + activity.getActivityName(),
                    "活动已结束，请确认已完成签退；服务时长将由志愿者组织复核后计入累计服务时长。");
        }
    }

    private boolean isSuccess(Map<Object, Object> map) {
        Object ok = map.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }

    private Integer toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }
}
