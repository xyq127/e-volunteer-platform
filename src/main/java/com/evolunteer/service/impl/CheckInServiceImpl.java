package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.CheckIn;
import com.evolunteer.entity.Participation;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.mapper.ActivityMapper;
import com.evolunteer.mapper.CheckInMapper;
import com.evolunteer.mapper.ParticipationMapper;
import com.evolunteer.mapper.VolunteerMapper;
import com.evolunteer.service.CheckInService;
import com.evolunteer.service.NotificationService;
import com.evolunteer.utils.DateTimeParser;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 可信服务记录业务实现类：签到签退、服务时长复核、服务时长补录与补录复核、活动结算均通过数据库存储过程完成；
 * 现场签到码由平台随机生成，志愿者组织可在活动现场公布。
 */
@Slf4j
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

    @Autowired
    private ParticipationMapper participationMapper;

    @Autowired
    private VolunteerMapper volunteerMapper;

    @Autowired
    private NotificationService notificationService;

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
     * @return 处理结果，包含提示信息 msg、服务时长 duration 与处理标记 ok
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
     * 按活动分页查询服务记录，供志愿者组织复核
     *
     * @param activityNum 活动编号
     * @param state       复核状态，0 待复核、1 已确认、2 已驳回，为空时查询全部
     * @param source      记录来源，1 平台签到、2 组织补录，为空时查询全部
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @return 服务记录分页结果
     */
    @Override
    public IPage<CheckIn> pageCheckinRecords(Integer activityNum, String state, String source,
                                             Integer pageNum, Integer pageSize) {
        Page<CheckIn> page = PageSupport.of(pageNum, pageSize);
        IPage<CheckIn> result = checkInMapper.selectPageCheckinByActivityNum(page, activityNum,
                PageSupport.normalizeKeyword(state), PageSupport.normalizeKeyword(source));
        for (CheckIn checkIn : result.getRecords()) {
            checkIn.setCheckinSourceText(sourceText(checkIn.getCheckinSource()));
        }
        return result;
    }

    /**
     * 按活动查询全部服务记录，供导出使用
     *
     * @param activityNum 活动编号
     * @return 服务记录列表，已关联志愿者基本信息
     */
    @Override
    public List<CheckIn> listCheckinRecords(Integer activityNum) {
        List<CheckIn> records = checkInMapper.selectCheckinListByActivityNum(activityNum);
        for (CheckIn checkIn : records) {
            checkIn.setCheckinSourceText(sourceText(checkIn.getCheckinSource()));
        }
        return records;
    }

    /**
     * 志愿者组织复核服务时长
     *
     * @param checkinNum 签到编号
     * @param isPass     复核结果（1 确认时长、2 驳回）
     * @param remark     复核意见
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    @Override
    public Map<Object, Object> reviewCheckin(Integer checkinNum, Integer isPass, String remark) {

        Map<Object, Object> map = new HashMap<>();
        map.put("checkinNum", checkinNum);
        map.put("isPass", isPass);
        map.put("remark", remark);
        checkInMapper.organization_check_checkin(map);

        if (isSuccess(map)) {
            notifyVolunteerOfReview(checkinNum, (String) map.get("msg"));
        }
        return map;
    }

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
    @Override
    public Map<Object, Object> recordServiceHours(String loginId, Integer participateNum, String beginTime,
                                                  String endTime, String remark) {

        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", loginId);
        map.put("participateNum", participateNum);

        Date begin;
        Date end;
        try {
            begin = DateTimeParser.parse(beginTime);
            end = DateTimeParser.parse(endTime);
        } catch (IllegalArgumentException e) {
            map.put("ok", 0);
            map.put("msg", "服务时间格式不正确，请按年-月-日 时:分填写");
            return map;
        }
        map.put("beginTime", begin);
        map.put("endTime", end);
        map.put("remark", remark);
        checkInMapper.organization_record_service_hours(map);

        if (isSuccess(map)) {
            log.info("志愿者组织补录服务时长，报名编号：{}，结果：{}", participateNum, map.get("msg"));
        }
        return map;
    }

    /**
     * 平台管理员分页查询补录记录
     *
     * @param state    复核状态，0 待复核、1 已确认、2 已驳回，为空时查询全部
     * @param keyword  志愿者姓名、志愿者编号或活动名称关键字，可为空
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 补录记录分页结果
     */
    @Override
    public IPage<CheckIn> pageManualCheckins(String state, String keyword, Integer pageNum, Integer pageSize) {
        Page<CheckIn> page = PageSupport.of(pageNum, pageSize);
        IPage<CheckIn> result = checkInMapper.selectPageManualCheckins(page,
                PageSupport.normalizeKeyword(state), PageSupport.normalizeKeyword(keyword));
        for (CheckIn checkIn : result.getRecords()) {
            checkIn.setCheckinSourceText(sourceText(checkIn.getCheckinSource()));
        }
        return result;
    }

    /**
     * 平台管理员复核补录记录
     *
     * @param loginId    平台管理员登录账号
     * @param checkinNum 签到编号
     * @param isPass     复核结果（1 确认时长、2 驳回）
     * @param remark     复核意见
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    @Override
    public Map<Object, Object> reviewManualCheckin(String loginId, Integer checkinNum, Integer isPass,
                                                   String remark) {

        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", loginId);
        map.put("checkinNum", checkinNum);
        map.put("isPass", isPass);
        map.put("remark", remark);
        checkInMapper.admin_check_manual_checkin(map);

        if (isSuccess(map)) {
            log.info("平台管理员复核补录服务时长，签到编号：{}，结果：{}", checkinNum, map.get("msg"));
            notifyVolunteerOfReview(checkinNum, (String) map.get("msg"));
        }
        return map;
    }

    /**
     * 活动结算：对已通过报名但未签退的志愿者记录爽约并扣减信用分
     *
     * @param activityNum 活动编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
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

    /**
     * 复核结果写入后通知志愿者
     */
    private void notifyVolunteerOfReview(Integer checkinNum, String msg) {

        CheckIn checkIn = checkInMapper.selectById(checkinNum);
        if (checkIn == null) {
            return;
        }
        Participation participation = participationMapper.selectById(checkIn.getParticipateNum());
        if (participation == null) {
            return;
        }
        Volunteer volunteer = volunteerMapper.selectById(participation.getVolunteerNum());
        if (volunteer == null) {
            return;
        }
        List<Activity> activities = activityMapper.selectByActivityNum(participation.getActivityNum());
        String activityName = activities.isEmpty() ? "志愿活动" : activities.get(0).getActivityName();
        notificationService.send(volunteer.getVolunteerId(), "ROLE_VOLUNTEER",
                "服务时长复核结果：" + activityName, msg);
    }

    /**
     * 记录来源文案
     */
    private String sourceText(String source) {
        return "2".equals(source) ? "组织补录" : "平台签到";
    }

    /**
     * 判断存储过程返回的处理结果是否成功
     */
    private boolean isSuccess(Map<Object, Object> map) {
        Object ok = map.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
