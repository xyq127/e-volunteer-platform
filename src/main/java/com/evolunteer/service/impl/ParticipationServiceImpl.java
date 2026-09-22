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

@Slf4j
@Service
public class ParticipationServiceImpl extends ServiceImpl<ParticipationMapper, Participation> implements ParticipationService {

    private static final String APPLY_STATE_PENDING = "0";

    private static final String APPLY_STATE_APPROVED = "1";

    private static final String APPLY_STATE_REJECTED = "2";

    private static final String APPLY_STATE_WAITING = "3";

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<Participation> getVolunteerList(Integer activityNum) {
        return baseMapper.selectParticipateWithVolunteerByActivityNum(activityNum);
    }

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

    @Override
    public List<String> approvedVolunteerAccounts(Integer activityNum) {
        return baseMapper.selectApprovedVolunteerAccounts(activityNum);
    }

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

    @Override
    public Map<Object, Object> cancelParticipate(Integer participateNum, Integer volunteerNum) {
        Map<Object, Object> map = new HashMap<>();
        map.put("participateNum", participateNum);
        map.put("volunteerNum", volunteerNum);
        baseMapper.volunteer_cancel_participate(map);
        return map;
    }

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

    private void fillOperationFlags(VolunteerParticipationView participation) {

        String applyState = participation.getParticipateApplystate();
        boolean approved = APPLY_STATE_APPROVED.equals(applyState);

        boolean notCheckedIn = participation.getCheckinBegintime() == null;
        boolean pendingCheckout = participation.getCheckinBegintime() != null
                && participation.getCheckinEndtime() == null;

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

    private String confirmStateText(String confirmState) {
        if ("1".equals(confirmState)) {
            return "已确认参加";
        }
        if ("2".equals(confirmState)) {
            return "已放弃参加";
        }
        return "待确认";
    }

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

    private Activity findActivity(Integer activityNum) {
        List<Activity> activities = activityMapper.selectByActivityNum(activityNum);
        return activities.isEmpty() ? null : activities.get(0);
    }

    private String organizationAccountOf(Activity activity) {
        if (activity == null || activity.getOrganizationNum() == null) {
            return null;
        }
        Organization organization = organizationMapper.selectById(activity.getOrganizationNum());
        return organization == null ? null : organization.getOrganizationId();
    }

    private String volunteerAccountOf(Integer volunteerNum) {
        if (volunteerNum == null) {
            return null;
        }
        Volunteer volunteer = volunteerMapper.selectById(volunteerNum);
        return volunteer == null ? null : volunteer.getVolunteerId();
    }

    private boolean isSuccess(Map<Object, Object> map) {
        Object ok = map.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
