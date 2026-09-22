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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿活动业务实现类：封装志愿活动的分状态分页查询、平台审核结果回写、活动状态流转与自动流转、
 * 审核未通过后的重新申报以及活动逻辑删除等数据库操作，并在状态流转后向参与者发送站内通知。
 */
@Slf4j
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ParticipationService participationService;

    /**
     * 按志愿者组织编号与活动状态分页查询活动
     *
     * @param organizationNum 志愿者组织编号
     * @param state           活动状态
     * @param keyword         活动名称或活动编号关键字，可为空
     * @param pageNum         页码
     * @param pageSize        每页条数
     * @return 活动分页结果
     */
    @Override
    public IPage<Activity> pageActivityByOrganization(Integer organizationNum, String state, String keyword,
                                                      Integer pageNum, Integer pageSize) {
        Page<Activity> page = PageSupport.of(pageNum, pageSize);
        return baseMapper.selectPageByOrganizationAndState(page, organizationNum,
                state, PageSupport.normalizeKeyword(keyword));
    }

    /**
     * 按活动状态分页查询平台内活动，供平台管理员审核使用
     *
     * @param state    活动状态
     * @param keyword  活动名称或活动编号关键字，可为空
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 活动分页结果
     */
    @Override
    public IPage<Activity> pageActivityByState(String state, String keyword, Integer pageNum, Integer pageSize) {
        Page<Activity> page = PageSupport.of(pageNum, pageSize);
        return baseMapper.selectPageByState(page, state, PageSupport.normalizeKeyword(keyword));
    }

    /**
     * 按志愿者组织编号与活动状态查询活动列表
     *
     * @param num   志愿者组织编号
     * @param state 活动状态
     * @return 活动列表
     */
    @Override
    public List<Activity> getActivityByNumAndState(Integer num, String state) {
        return baseMapper.selectAllByOrganizationNumAndActivityStateAndActivityIsdeleted(num, state, 0);
    }

    /**
     * 按活动状态查询平台内全部活动，供平台管理员审核使用
     *
     * @param state 活动状态
     * @return 活动列表
     */
    @Override
    public List<Activity> getActivityByState(String state) {
        return baseMapper.selectAllByActivityStateAndActivityIsdeleted(state, 0);
    }

    /**
     * 查询指定活动的详情
     *
     * @param activityNum 活动编号
     * @return 活动详情列表
     */
    @Override
    public List<Activity> activityInfo(Integer activityNum) {
        return baseMapper.selectByActivityNum(activityNum);
    }

    /**
     * 校验活动名称是否可用，供志愿者组织申报活动时查重
     *
     * @param name 活动名称
     * @return 名称未被占用返回 true
     */
    @Override
    public Boolean searchActivityByName(String name) {
        return baseMapper.selectAllByActivityName(name).isEmpty();
    }

    /**
     * 调用存储过程写入平台管理员的活动审核结果
     *
     * @param map 审核参数，包含管理员账号、活动编号、审核结果、结构化原因与审核意见
     * @return 存储过程返回的处理结果信息
     */
    @Override
    public Object passActByAdminIdWithStatue(Map<Object, Object> map) {
        baseMapper.admin_check_activity(map);
        return map.get("msg");
    }

    /**
     * 志愿者组织人工流转活动状态，状态变化后通知该活动已通过报名的志愿者
     *
     * @param loginId     志愿者组织登录账号
     * @param activityNum 活动编号
     * @param targetState 目标状态（2 进行中、3 已结束）
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
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

    /**
     * 按活动时间自动流转活动状态，供定时任务调用；自动开始与自动结束时同样通知参与者
     *
     * @return 处理结果，包含开始数量 startedCount 与结束数量 finishedCount
     */
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

    /**
     * 调用存储过程将审核未通过的活动修改后重新提交审核
     *
     * @param map 重新申报参数，包含组织账号、活动编号、活动基本信息、技能标签与活动坐标
     * @return 存储过程返回的处理结果信息
     */
    @Override
    public Object reviseAct(Map<Object, Object> map) {
        baseMapper.organization_revise_activity(map);
        return map.get("msg");
    }

    /**
     * 按活动名称精确查询活动
     *
     * @param actName 活动名称
     * @return 匹配的活动，不存在时返回 null
     */
    @Override
    public Activity selectAct(String actName) {
        List<Activity> activities = baseMapper.selectByActivityName(actName);
        return activities.isEmpty() ? null : activities.get(0);
    }

    /**
     * 逻辑删除活动
     *
     * @param num 活动编号
     * @return 删除成功返回 true
     */
    @Override
    public Boolean deleteAct(Integer num) {
        return baseMapper.updateActivityIsdeletedByActivityNum(1, num) > 0;
    }

    /**
     * 活动开始或结束时向已通过报名的志愿者发送站内通知
     */
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

    /**
     * 判断存储过程返回的处理结果是否成功
     */
    private boolean isSuccess(Map<Object, Object> map) {
        Object ok = map.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }

    /**
     * 把存储过程的数值出参转为整数
     */
    private Integer toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }
}
