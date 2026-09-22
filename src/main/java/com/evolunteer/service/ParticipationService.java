package com.evolunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Participation;
import com.evolunteer.entity.VolunteerParticipationView;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名业务接口，基于 MyBatis-Plus 提供活动报名数据的通用增删改查能力，
 * 并提供志愿者报名与撤回报名、志愿者组织报名审核以及本人报名记录查询能力。
 */
public interface ParticipationService extends IService<Participation> {

    /**
     * 查询指定活动下已报名的志愿者列表
     *
     * @param activityNum 活动编号
     * @return 报名记录列表，已关联志愿者基本信息
     */
    List<Participation> getVolunteerList(Integer activityNum);

    /**
     * 查询志愿者的报名记录，并按当前时间与审核状态标记可签到、可签退与可撤回
     *
     * @param volunteerNum 志愿者编号
     * @return 报名记录视图列表
     */
    List<VolunteerParticipationView> listVolunteerParticipations(Integer volunteerNum);

    /**
     * 志愿者报名志愿活动：校验信用分、报名截止时间与名额，名额已满时进入候补队列
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @return 处理结果，包含提示信息 msg
     */
    Map<Object, Object> applyActivity(Integer volunteerNum, Integer activityNum);

    /**
     * 志愿者撤回报名：已通过的报名释放名额并自动递补候补队列中的第一位志愿者
     *
     * @param participateNum 报名编号
     * @param volunteerNum   志愿者编号
     * @return 处理结果，包含提示信息 msg
     */
    Map<Object, Object> cancelParticipate(Integer participateNum, Integer volunteerNum);

    /**
     * 写入志愿者组织对报名申请的审核结果
     *
     * @param map 审核参数，包含报名编号与审核结果
     * @return 处理结果信息
     */
    Object volCheckRecruit(Map<Object, Object> map);
}
