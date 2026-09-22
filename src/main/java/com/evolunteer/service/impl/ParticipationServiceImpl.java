package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.Organization;
import com.evolunteer.entity.Participation;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.entity.VolunteerParticipationView;
import com.evolunteer.mapper.ActivityMapper;
import com.evolunteer.mapper.OrganizationMapper;
import com.evolunteer.mapper.ParticipationMapper;
import com.evolunteer.mapper.VolunteerMapper;
import com.evolunteer.service.NotificationService;
import com.evolunteer.service.ParticipationService;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名业务实现类：志愿者报名、撤回报名与参加确认，志愿者组织审核与移除报名，
 * 以及参加确认到期自动释放名额，均通过数据库存储过程完成，保证名额校验、候补递补与信用分约束在同一事务内生效；
 * 关键结果同时写入站内通知，便于志愿者与志愿者组织及时获知。
 */
@Slf4j
@Service
public class ParticipationServiceImpl extends ServiceImpl<ParticipationMapper, Participation> implements ParticipationService {

    /**
     * 报名审核状态：待审核
     */
    private static final String APPLY_STATE_PENDING = "0";

    /**
     * 报名审核状态：已通过
     */
    private static final String APPLY_STATE_APPROVED = "1";

    /**
     * 报名审核状态：未通过
     */
    private static final String APPLY_STATE_REJECTED = "2";

    /**
     * 报名审核状态：候补
     */
    private static final String APPLY_STATE_WAITING = "3";

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private NotificationService notificationService;

    /**
     * 查询指定活动下已报名的志愿者列表
     *
     * @param activityNum 活动编号
     * @return 报名记录列表，已关联志愿者基本信息
     */
    @Override
    public List<Participation> getVolunteerList(Integer activityNum) {
        return baseMapper.selectParticipateWithVolunteerByActivityNum(activityNum);
    }

    /**
     * 按活动分页查询报名志愿者名单
     *
     * @param activityNum 活动编号
     * @param keyword     志愿者编号、姓名或手机号关键字，可为空
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @return 报名记录分页结果
     */
    @Override
    public IPage<Participation> pageVolunteerList(Integer activityNum, String keyword,
                                                  Integer pageNum, Integer pageSize) {
        Page<Participation> page = PageSupport.of(pageNum, pageSize);
        IPage<Participation> result = baseMapper.selectPageParticipateWithVolunteerByActivityNum(
                page, activityNum, PageSupport.normalizeKeyword(keyword));
        for (Participation participation : result.getRecords()) {
            participation.setParticipateConfirmstateText(confirmStateText(participation.getParticipateConfirmstate()));
        }
        return result;
    }

    /**
     * 查询指定活动已通过报名的志愿者登录账号
     *
     * @param activityNum 活动编号
     * @return 志愿者登录账号列表
     */
    @Override
    public List<String> approvedVolunteerAccounts(Integer activityNum) {
        return baseMapper.selectApprovedVolunteerAccounts(activityNum);
    }

    /**
     * 查询志愿者的报名记录，并按当前状态标记可签到、可签退、可撤回与可确认
     *
     * @param volunteerNum 志愿者编号
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @return 报名记录视图分页结果
     */
    @Override
    public IPage<VolunteerParticipationView> pageVolunteerParticipations(Integer volunteerNum,
                                                                        Integer pageNum, Integer pageSize) {
        Page<VolunteerParticipationView> page = PageSupport.of(pageNum, pageSize);
        IPage<VolunteerParticipationView> result = baseMapper.selectPageVolunteerParticipations(page, volunteerNum);

        for (VolunteerParticipationView participation : result.getRecords()) {
            fillOperationFlags(participation);
        }
        return result;
    }

    /**
     * 志愿者报名志愿活动：校验信用分、报名截止时间与名额，名额已满时进入候补队列
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    @Override
    public Map<Object, Object> applyActivity(Integer volunteerNum, Integer activityNum) {

        Map<Object, Object> map = new HashMap<>();
        map.put("volunteerNum", volunteerNum);
        map.put("activityNum", activityNum);
        baseMapper.volunteer_apply_activity(map);

        if (isSuccess(map)) {
            Activity activity = findActivity(activityNum);
            if (activity != null) {
                notificationService.send(organizationAccountOf(activity), "ROLE_ORGANIZATION",
                        "新的活动报名：" + activity.getActivityName(),
                        "有志愿者提交了报名申请，请及时在「未开始的活动」中审核报名。");
            }
        }
        return map;
    }

    /**
     * 志愿者撤回报名：已通过的报名释放名额并自动递补候补队列中的第一位志愿者
     *
     * @param participateNum 报名编号
     * @param volunteerNum   志愿者编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    @Override
    public Map<Object, Object> cancelParticipate(Integer participateNum, Integer volunteerNum) {
        Map<Object, Object> map = new HashMap<>();
        map.put("participateNum", participateNum);
        map.put("volunteerNum", volunteerNum);
        baseMapper.volunteer_cancel_participate(map);
        return map;
    }

    /**
     * 志愿者确认参加或放弃参加，放弃时释放名额并自动递补候补志愿者
     *
     * @param participateNum 报名编号
     * @param volunteerNum   志愿者编号
     * @param confirmState   确认状态（1 确认参加、2 放弃参加）
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    @Override
    public Map<Object, Object> confirmParticipate(Integer participateNum, Integer volunteerNum,
                                                  Integer confirmState) {

        Map<Object, Object> map = new HashMap<>();
        map.put("participateNum", participateNum);
        map.put("volunteerNum", volunteerNum);
        map.put("confirmState", confirmState);
        baseMapper.volunteer_confirm_participate(map);

        if (isSuccess(map)) {
            notifyConfirmResult(participateNum, confirmState, (String) map.get("msg"));
        }
        return map;
    }

    /**
     * 志愿者组织移除本组织活动名单中的报名
     *
     * @param loginId        志愿者组织登录账号
     * @param participateNum 报名编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    @Override
    public Map<Object, Object> removeParticipate(String loginId, Integer participateNum) {

        Participation participation = baseMapper.selectById(participateNum);
        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", loginId);
        map.put("participateNum", participateNum);
        baseMapper.organization_remove_participate(map);

        if (isSuccess(map) && participation != null) {
            Activity activity = findActivity(participation.getActivityNum());
            notificationService.send(volunteerAccountOf(participation.getVolunteerNum()), "ROLE_VOLUNTEER",
                    "报名已被移除" + (activity == null ? "" : "：" + activity.getActivityName()),
                    (String) map.get("msg"));
        }
        return map;
    }

    /**
     * 释放未按期确认参加的报名名额，并记录释放数量
     *
     * @return 处理结果，包含释放数量 releasedCount
     */
    @Override
    public Map<Object, Object> releaseUnconfirmedParticipations() {

        Map<Object, Object> map = new HashMap<>();
        baseMapper.participate_confirm_expire(map);
        Object releasedCount = map.get("releasedCount");
        int released = releasedCount == null ? 0 : ((Number) releasedCount).intValue();
        if (released > 0) {
            log.info("参加确认到期自动释放报名 {} 条", released);
        }
        return map;
    }

    /**
     * 写入志愿者组织对报名申请的审核结果，并通知志愿者审核结果
     *
     * @param map 审核参数，包含报名编号与审核结果
     * @return 存储过程返回的处理结果信息
     */
    @Override
    public Object volCheckRecruit(Map<Object, Object> map) {

        Integer participateNum = (Integer) map.get("participateNum");
        Object isPass = map.get("isPass");
        Participation participation = baseMapper.selectById(participateNum);

        baseMapper.organization_check_volunteer(map);

        if (participation != null) {
            String msg = (String) map.get("msg");
            Activity activity = findActivity(participation.getActivityNum());
            String activityName = activity == null ? "志愿活动" : activity.getActivityName();
            boolean approved = isPass != null && ((Number) isPass).intValue() == 1
                    && msg != null && msg.contains("已通过");
            if (approved) {
                notificationService.send(volunteerAccountOf(participation.getVolunteerNum()), "ROLE_VOLUNTEER",
                        "报名申请已通过：" + activityName,
                        "请在活动开始前于「我的报名」中确认参加，未按期确认将自动释放名额。");
            } else if (msg != null && msg.contains("未通过")) {
                notificationService.send(volunteerAccountOf(participation.getVolunteerNum()), "ROLE_VOLUNTEER",
                        "报名申请未通过：" + activityName, "很抱歉，本次报名未通过志愿者组织审核。");
            }
        }
        return map.get("msg");
    }

    /**
     * 填充报名记录的状态文案与可执行操作标记
     */
    private void fillOperationFlags(VolunteerParticipationView participation) {

        String applyState = participation.getParticipateApplystate();
        boolean approved = APPLY_STATE_APPROVED.equals(applyState);
        // 一次报名只对应一组签到签退记录，已签到过的报名不再提供签到入口
        boolean notCheckedIn = participation.getCheckinBegintime() == null;
        boolean pendingCheckout = participation.getCheckinBegintime() != null
                && participation.getCheckinEndtime() == null;
        // 已结束的活动不再提供签到入口，具体的签到时间窗口由签到存储过程统一校验
        boolean activityOpen = ("1".equals(participation.getActivityState())
                || "2".equals(participation.getActivityState()))
                && participation.getActivityEndtime() != null
                && participation.getActivityEndtime().after(new Date());
        boolean cancelableState = APPLY_STATE_PENDING.equals(applyState)
                || APPLY_STATE_APPROVED.equals(applyState)
                || APPLY_STATE_WAITING.equals(applyState);

        participation.setParticipateApplystateText(applyStateText(applyState));
        participation.setParticipateConfirmstateText(confirmStateText(participation.getParticipateConfirmstate()));
        participation.setCanCheckin(approved && notCheckedIn && activityOpen);
        participation.setCanCheckout(pendingCheckout);
        participation.setCanCancel(notCheckedIn && cancelableState);
        participation.setCanConfirm(approved
                && !"1".equals(participation.getParticipateConfirmstate())
                && activityOpen);
    }

    /**
     * 将报名审核状态转为页面展示文案
     */
    private String applyStateText(String applyState) {
        if (APPLY_STATE_APPROVED.equals(applyState)) {
            return "已通过";
        }
        if (APPLY_STATE_REJECTED.equals(applyState)) {
            return "未通过";
        }
        if (APPLY_STATE_WAITING.equals(applyState)) {
            return "候补";
        }
        return "待审核";
    }

    /**
     * 将参加确认状态转为页面展示文案
     */
    private String confirmStateText(String confirmState) {
        if ("1".equals(confirmState)) {
            return "已确认参加";
        }
        if ("2".equals(confirmState)) {
            return "已放弃参加";
        }
        return "待确认";
    }

    /**
     * 向志愿者组织通知志愿者的参加确认结果
     */
    private void notifyConfirmResult(Integer participateNum, Integer confirmState, String msg) {

        Participation participation = baseMapper.selectById(participateNum);
        if (participation == null) {
            return;
        }
        Activity activity = findActivity(participation.getActivityNum());
        if (activity == null) {
            return;
        }
        String title = Integer.valueOf(2).equals(confirmState) ? "志愿者放弃参加：" : "志愿者已确认参加：";
        notificationService.send(organizationAccountOf(activity), "ROLE_ORGANIZATION",
                title + activity.getActivityName(), msg);
    }

    /**
     * 查询活动信息
     */
    private Activity findActivity(Integer activityNum) {
        List<Activity> activities = activityMapper.selectByActivityNum(activityNum);
        return activities.isEmpty() ? null : activities.get(0);
    }

    /**
     * 查询活动所属志愿者组织的登录账号
     */
    private String organizationAccountOf(Activity activity) {
        if (activity == null || activity.getOrganizationNum() == null) {
            return null;
        }
        Organization organization = organizationMapper.selectById(activity.getOrganizationNum());
        return organization == null ? null : organization.getOrganizationId();
    }

    /**
     * 查询志愿者编号对应的登录账号
     */
    private String volunteerAccountOf(Integer volunteerNum) {
        if (volunteerNum == null) {
            return null;
        }
        Volunteer volunteer = volunteerMapper.selectById(volunteerNum);
        return volunteer == null ? null : volunteer.getVolunteerId();
    }

    /**
     * 判断存储过程返回的处理结果是否成功
     */
    private boolean isSuccess(Map<Object, Object> map) {
        Object ok = map.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
