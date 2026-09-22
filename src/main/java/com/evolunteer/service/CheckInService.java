package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.CheckIn;

import java.util.List;

import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 可信服务记录业务接口：志愿者现场签到签退由数据库存储过程校验签到码、签到时间窗口与地理围栏后写入轨迹并核算时长；
 * 志愿者组织复核平台签到记录、为线下服务补录时长，补录记录由平台管理员复核，
 * 复核通过的时长计入志愿者累计服务时长；活动结束后结算考勤并记录爽约。
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
     * @return 处理结果，包含提示信息 msg、轨迹标记 flag 与处理标记 ok
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
     * @return 处理结果，包含提示信息 msg、服务时长 duration 与处理标记 ok
     */
    Map<Object, Object> checkout(Integer volunteerNum, Integer activityNum, Double latitude, Double longitude);

    /**
     * 按活动分页查询服务记录，供志愿者组织复核
     *
     * @param activityNum 活动编号
     * @param state       复核状态，0 待复核、1 已确认、2 已驳回，为空时查询全部
     * @param source      记录来源，1 平台签到、2 组织补录，为空时查询全部
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @return 服务记录分页结果
     */
    IPage<CheckIn> pageCheckinRecords(Integer activityNum, String state, String source,
                                      Integer pageNum, Integer pageSize);

    /**
     * 按活动查询全部服务记录，供导出使用
     *
     * @param activityNum 活动编号
     * @return 服务记录列表，已关联志愿者基本信息
     */
    List<CheckIn> listCheckinRecords(Integer activityNum);

    /**
     * 志愿者组织复核平台签到记录的服务时长
     *
     * @param checkinNum 签到编号
     * @param isPass     复核结果（1 确认时长、2 驳回）
     * @param remark     复核意见
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> reviewCheckin(Integer checkinNum, Integer isPass, String remark);

    /**
     * 志愿者组织为线下服务补录服务时长，补录记录提交平台管理员复核
     *
     * @param loginId        志愿者组织登录账号
     * @param participateNum 报名编号
     * @param beginTime      服务开始时间
     * @param endTime        服务结束时间
     * @param remark         补录说明
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> recordServiceHours(String loginId, Integer participateNum, String beginTime,
                                           String endTime, String remark);

    /**
     * 平台管理员分页查询待复核的补录记录
     *
     * @param state    复核状态，0 待复核、1 已确认、2 已驳回，为空时查询全部
     * @param keyword  志愿者姓名、志愿者编号或活动名称关键字，可为空
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 补录记录分页结果
     */
    IPage<CheckIn> pageManualCheckins(String state, String keyword, Integer pageNum, Integer pageSize);

    /**
     * 平台管理员复核补录记录
     *
     * @param loginId    平台管理员登录账号
     * @param checkinNum 签到编号
     * @param isPass     复核结果（1 确认时长、2 驳回）
     * @param remark     复核意见
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> reviewManualCheckin(String loginId, Integer checkinNum, Integer isPass, String remark);

    /**
     * 活动结算：对已通过报名但未签退的志愿者记录爽约并扣减信用分
     *
     * @param activityNum 活动编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
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
