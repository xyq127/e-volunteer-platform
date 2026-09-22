package com.evolunteer.service;

import com.evolunteer.entity.CheckIn;

import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 可信签到业务接口：志愿者现场签到签退由数据库存储过程校验签到码、签到时间窗口与地理围栏后写入轨迹并核算时长，
 * 志愿者组织复核服务时长后时长计入志愿者累计服务时长，活动结束后结算考勤并记录爽约。
 */
public interface CheckInService {

    /**
     * 志愿者签到
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @param code         现场签到码
     * @param latitude     签到地点纬度，未获取到定位时传 null
     * @param longitude    签到地点经度，未获取到定位时传 null
     * @return 处理结果，包含提示信息 msg 与轨迹标记 flag（1 正常、2 异常待复核）
     */
    Map<Object, Object> checkin(Integer volunteerNum, Integer activityNum, String code,
                                Double latitude, Double longitude);

    /**
     * 志愿者签退，按签到签退时间自动核算服务时长
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @param latitude     签退地点纬度，未获取到定位时传 null
     * @param longitude    签退地点经度，未获取到定位时传 null
     * @return 处理结果，包含提示信息 msg 与核算出的服务时长 duration（小时）
     */
    Map<Object, Object> checkout(Integer volunteerNum, Integer activityNum, Double latitude, Double longitude);

    /**
     * 按活动查询签到记录，供志愿者组织复核服务时长
     *
     * @param activityNum 活动编号
     * @return 签到记录列表，已关联志愿者基本信息
     */
    List<CheckIn> listCheckinRecords(Integer activityNum);

    /**
     * 志愿者组织复核服务时长
     *
     * @param checkinNum 签到编号
     * @param isPass     复核结果（1 确认时长、2 驳回）
     * @param remark     复核意见
     * @return 处理结果，包含提示信息 msg
     */
    Map<Object, Object> reviewCheckin(Integer checkinNum, Integer isPass, String remark);

    /**
     * 活动结算：对已通过报名但未签退的志愿者记录爽约并扣减信用分
     *
     * @param activityNum 活动编号
     * @return 处理结果，包含提示信息 msg
     */
    Map<Object, Object> settleActivity(Integer activityNum);

    /**
     * 查询活动的现场签到码
     *
     * @param activityNum 活动编号
     * @return 现场签到码，尚未生成时返回 null
     */
    String getCheckinCode(Integer activityNum);

    /**
     * 生成或重新生成活动的现场签到码
     *
     * @param activityNum 活动编号
     * @return 生成后的现场签到码，活动不存在时返回 null
     */
    String refreshCheckinCode(Integer activityNum);
}
