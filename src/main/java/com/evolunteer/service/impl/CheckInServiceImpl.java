package com.evolunteer.service.impl;

import com.evolunteer.entity.Activity;
import com.evolunteer.entity.CheckIn;
import com.evolunteer.mapper.ActivityMapper;
import com.evolunteer.mapper.CheckInMapper;
import com.evolunteer.service.CheckInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 可信签到业务实现类：签到签退、服务时长复核与活动结算均通过数据库存储过程完成，
 * 现场签到码由平台随机生成，志愿者组织可在活动现场公布。
 */
@Service
public class CheckInServiceImpl implements CheckInService {

    /**
     * 现场签到码使用的字符集合，去掉了容易混淆的 0、1、I、O
     */
    private static final char[] CODE_CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    /**
     * 现场签到码长度
     */
    private static final int CODE_LENGTH = 6;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private ActivityMapper activityMapper;

    /**
     * 志愿者签到
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @param code         现场签到码
     * @param latitude     签到地点纬度，未获取到定位时传 null
     * @param longitude    签到地点经度，未获取到定位时传 null
     * @return 处理结果，包含提示信息 msg 与轨迹标记 flag
     */
    @Override
    public Map<Object, Object> checkin(Integer volunteerNum, Integer activityNum, String code,
                                       Double latitude, Double longitude) {
        Map<Object, Object> map = new HashMap<>();
        map.put("volunteerNum", volunteerNum);
        map.put("activityNum", activityNum);
        map.put("code", code);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        checkInMapper.volunteer_checkin(map);
        return map;
    }

    /**
     * 志愿者签退，按签到签退时间自动核算服务时长
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @param latitude     签退地点纬度，未获取到定位时传 null
     * @param longitude    签退地点经度，未获取到定位时传 null
     * @return 处理结果，包含提示信息 msg 与核算出的服务时长 duration
     */
    @Override
    public Map<Object, Object> checkout(Integer volunteerNum, Integer activityNum,
                                        Double latitude, Double longitude) {
        Map<Object, Object> map = new HashMap<>();
        map.put("volunteerNum", volunteerNum);
        map.put("activityNum", activityNum);
        map.put("latitude", latitude);
        map.put("longitude", longitude);
        checkInMapper.volunteer_checkout(map);
        return map;
    }

    /**
     * 按活动查询签到记录，供志愿者组织复核服务时长
     *
     * @param activityNum 活动编号
     * @return 签到记录列表，已关联志愿者基本信息
     */
    @Override
    public List<CheckIn> listCheckinRecords(Integer activityNum) {
        return checkInMapper.selectCheckinListByActivityNum(activityNum);
    }

    /**
     * 志愿者组织复核服务时长
     *
     * @param checkinNum 签到编号
     * @param isPass     复核结果（1 确认时长、2 驳回）
     * @param remark     复核意见
     * @return 处理结果，包含提示信息 msg
     */
    @Override
    public Map<Object, Object> reviewCheckin(Integer checkinNum, Integer isPass, String remark) {
        Map<Object, Object> map = new HashMap<>();
        map.put("checkinNum", checkinNum);
        map.put("isPass", isPass);
        map.put("remark", remark);
        checkInMapper.organization_check_checkin(map);
        return map;
    }

    /**
     * 活动结算：对已通过报名但未签退的志愿者记录爽约并扣减信用分
     *
     * @param activityNum 活动编号
     * @return 处理结果，包含提示信息 msg
     */
    @Override
    public Map<Object, Object> settleActivity(Integer activityNum) {
        Map<Object, Object> map = new HashMap<>();
        map.put("activityNum", activityNum);
        checkInMapper.organization_settle_activity(map);
        return map;
    }

    /**
     * 查询活动的现场签到码
     *
     * @param activityNum 活动编号
     * @return 现场签到码，活动不存在或尚未生成时返回 null
     */
    @Override
    public String getCheckinCode(Integer activityNum) {
        List<Activity> activities = activityMapper.selectByActivityNum(activityNum);
        return activities.isEmpty() ? null : activities.get(0).getActivityCheckinCode();
    }

    /**
     * 生成或重新生成活动的现场签到码
     *
     * @param activityNum 活动编号
     * @return 生成后的现场签到码，活动不存在时返回 null
     */
    @Override
    public String refreshCheckinCode(Integer activityNum) {
        List<Activity> activities = activityMapper.selectByActivityNum(activityNum);
        if (activities.isEmpty()) {
            return null;
        }
        String checkinCode = randomCheckinCode();
        activityMapper.updateActivityCheckinCode(checkinCode, activityNum);
        return checkinCode;
    }

    /**
     * 生成随机现场签到码
     */
    private String randomCheckinCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder checkinCode = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            checkinCode.append(CODE_CHARACTERS[random.nextInt(CODE_CHARACTERS.length)]);
        }
        return checkinCode.toString();
    }
}
