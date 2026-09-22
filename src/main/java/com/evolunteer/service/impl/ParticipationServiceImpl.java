package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evolunteer.entity.Participation;
import com.evolunteer.entity.VolunteerParticipationView;
import com.evolunteer.mapper.ParticipationMapper;
import com.evolunteer.service.ParticipationService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名业务实现类：志愿者报名与撤回报名、志愿者组织审核报名均通过数据库存储过程完成，
 * 保证名额校验、候补递补与信用分约束在同一事务内生效。
 */
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
     * 查询志愿者的报名记录，并按当前状态标记可签到、可签退与可撤回
     *
     * @param volunteerNum 志愿者编号
     * @return 报名记录视图列表
     */
    @Override
    public List<VolunteerParticipationView> listVolunteerParticipations(Integer volunteerNum) {

        List<VolunteerParticipationView> participations = baseMapper.selectVolunteerParticipations(volunteerNum);

        for (VolunteerParticipationView participation : participations) {
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

            participation.setParticipateApplystateText(applyStateText(applyState));
            participation.setCanCheckin(approved && notCheckedIn && activityOpen);
            participation.setCanCheckout(pendingCheckout);
            // 已签到的报名不能撤回，避免删除已产生服务轨迹的记录
            boolean cancelableState = APPLY_STATE_PENDING.equals(applyState)
                    || APPLY_STATE_APPROVED.equals(applyState)
                    || APPLY_STATE_WAITING.equals(applyState);
            participation.setCanCancel(notCheckedIn && cancelableState);
        }
        return participations;
    }

    /**
     * 志愿者报名志愿活动：校验信用分、报名截止时间与名额，名额已满时进入候补队列
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @return 处理结果，包含提示信息 msg
     */
    @Override
    public Map<Object, Object> applyActivity(Integer volunteerNum, Integer activityNum) {
        Map<Object, Object> map = new HashMap<>();
        map.put("volunteerNum", volunteerNum);
        map.put("activityNum", activityNum);
        baseMapper.volunteer_apply_activity(map);
        return map;
    }

    /**
     * 志愿者撤回报名：已通过的报名释放名额并自动递补候补队列中的第一位志愿者
     *
     * @param participateNum 报名编号
     * @param volunteerNum   志愿者编号
     * @return 处理结果，包含提示信息 msg
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
     * 调用存储过程写入志愿者组织对报名申请的审核结果
     *
     * @param map 审核参数，包含报名编号与审核结果
     * @return 存储过程返回的处理结果信息
     */
    @Override
    public Object volCheckRecruit(Map<Object, Object> map) {
        baseMapper.organization_check_volunteer(map);
        return map.get("msg");
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
}
