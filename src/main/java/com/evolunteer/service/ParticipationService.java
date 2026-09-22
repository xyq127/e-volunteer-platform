package com.evolunteer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.evolunteer.entity.Participation;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 活动报名业务接口，基于 MyBatis-Plus 提供活动报名数据的通用增删改查能力，
 * 并面向志愿者组织提供报名志愿者查询与报名申请审核能力。
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
     * 写入志愿者组织对报名申请的审核结果
     *
     * @param map 审核参数，包含报名编号与审核结果
     * @return 处理结果信息
     */
    Object volCheckRecruit(Map<Object, Object> map);
}
