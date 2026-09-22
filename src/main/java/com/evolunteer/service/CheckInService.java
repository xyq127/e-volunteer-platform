package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.CheckIn;
import com.evolunteer.entity.CheckinCodeView;

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
     * 平台管理员复核补录记录与命中异常规则的服务记录
     *
     * @param loginId    平台管理员登录账号
     * @param checkinNum 签到编号
     * @param isPass     裁定结果（1 确认计入或维持计入、2 驳回或冲销）
     * @param remark     裁定意见
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> reviewByAdmin(String loginId, Integer checkinNum, Integer isPass, String remark);

    /**
     * 分页查询命中异常规则且尚未裁定的服务记录
     *
     * @param keyword  志愿者姓名、志愿者编号或活动名称关键字，可为空
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 异常记录分页结果
     */
    IPage<CheckIn> pageAnomalies(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 定时巡检：标记单日已计入时长超过上限的服务记录
     *
     * @return 处理结果，包含标记数量 flaggedCount
     */
    Map<Object, Object> scanAnomalies();

    /**
     * 志愿者组织为服务记录生成服务确认单，由服务对象核对手机号后确认
     *
     * @param loginId        志愿者组织登录账号
     * @param checkinNum     签到编号
     * @param objectName     服务对象名称
     * @param objectPhone    服务对象手机号
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与确认码 confirmCode
     */
    Map<Object, Object> issueConfirmSheet(String loginId, Integer checkinNum, String objectName, String objectPhone);

    /**
     * 服务对象确认或否认本次服务（公开接口，无需登录）
     *
     * @param confirmCode  服务确认码
     * @param objectPhone  服务对象手机号
     * @param resultValue  确认结果（1 确认、2 否认）
     * @param objectRemark 确认意见
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> objectConfirm(String confirmCode, String objectPhone, Integer resultValue, String objectRemark);

    /**
     * 活动结算：对已通过报名但未签退的志愿者记录爽约并扣减信用分
     *
     * @param activityNum 活动编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    Map<Object, Object> settleActivity(Integer activityNum);

    /**
     * 查询活动当前生效的轮换签到码与剩余有效秒数
     *
     * @param activityNum 活动编号
     * @return 签到码信息，活动不存在时返回 null
     */
    CheckinCodeView currentCheckinCode(Integer activityNum);

    /**
     * 更换活动的签到密钥以立即重新生成签到码，旧签到码随之失效
     *
     * @param activityNum 活动编号
     * @return 重新生成后的签到码，活动不存在时返回 null
     */
    String refreshCheckinCode(Integer activityNum);
}
