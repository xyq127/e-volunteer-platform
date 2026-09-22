package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.CheckIn;
import com.evolunteer.entity.CheckinCodeView;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class CheckInServiceImpl implements CheckInService {

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

    @Override
    public IPage<CheckIn> pageCheckinRecords(Integer activityNum, String state, String source,
                                             Integer pageNum, Integer pageSize) {
        Page<CheckIn> page = PageSupport.of(pageNum, pageSize);
        IPage<CheckIn> result = checkInMapper.selectPageCheckinByActivityNum(page, activityNum,
                PageSupport.normalizeKeyword(state), PageSupport.normalizeKeyword(source));
        decorate(result.getRecords());
        return result;
    }

    @Override
    public List<CheckIn> listCheckinRecords(Integer activityNum) {
        List<CheckIn> records = checkInMapper.selectCheckinListByActivityNum(activityNum);
        decorate(records);
        return records;
    }

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

    @Override
    public IPage<CheckIn> pageManualCheckins(String state, String keyword, Integer pageNum, Integer pageSize) {
        Page<CheckIn> page = PageSupport.of(pageNum, pageSize);
        IPage<CheckIn> result = checkInMapper.selectPageManualCheckins(page,
                PageSupport.normalizeKeyword(state), PageSupport.normalizeKeyword(keyword));
        decorate(result.getRecords());
        return result;
    }

    @Override
    public IPage<CheckIn> pageAnomalies(String keyword, Integer pageNum, Integer pageSize) {
        Page<CheckIn> page = PageSupport.of(pageNum, pageSize);
        IPage<CheckIn> result = checkInMapper.selectPageAnomalies(page, PageSupport.normalizeKeyword(keyword));
        decorate(result.getRecords());
        return result;
    }

    @Override
    public Map<Object, Object> scanAnomalies() {
        Map<Object, Object> map = new HashMap<>();
        checkInMapper.service_anomaly_scan(map);
        Object flagged = map.get("flaggedCount");
        int flaggedCount = flagged == null ? 0 : ((Number) flagged).intValue();
        if (flaggedCount > 0) {
            log.warn("异常时长巡检标记 {} 条服务记录，等待平台管理员裁定", flaggedCount);
        }
        return map;
    }

    @Override
    public Map<Object, Object> reviewByAdmin(String loginId, Integer checkinNum, Integer isPass,
                                             String remark) {

        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", loginId);
        map.put("checkinNum", checkinNum);
        map.put("isPass", isPass);
        map.put("remark", remark);
        checkInMapper.admin_check_checkin(map);

        if (isSuccess(map)) {
            log.info("平台管理员裁定服务时长，签到编号：{}，结果：{}", checkinNum, map.get("msg"));
            notifyVolunteerOfReview(checkinNum, (String) map.get("msg"));
        }
        return map;
    }

    @Override
    public Map<Object, Object> issueConfirmSheet(String loginId, Integer checkinNum, String objectName,
                                                 String objectPhone) {
        Map<Object, Object> map = new HashMap<>();
        map.put("loginId", loginId);
        map.put("checkinNum", checkinNum);
        map.put("objectName", objectName);
        map.put("objectPhone", objectPhone);
        checkInMapper.organization_issue_confirm_sheet(map);
        if (isSuccess(map)) {
            log.info("志愿者组织生成服务确认单，签到编号：{}，服务对象：{}", checkinNum, objectName);
        }
        return map;
    }

    @Override
    public Map<Object, Object> objectConfirm(String confirmCode, String objectPhone, Integer resultValue,
                                             String objectRemark) {
        Map<Object, Object> map = new HashMap<>();
        map.put("confirmCode", confirmCode);
        map.put("objectPhone", objectPhone);
        map.put("resultValue", resultValue);
        map.put("objectRemark", objectRemark);
        checkInMapper.service_object_confirm(map);
        return map;
    }

    @Override
    public Map<Object, Object> settleActivity(Integer activityNum) {
        Map<Object, Object> map = new HashMap<>();
        map.put("activityNum", activityNum);
        checkInMapper.organization_settle_activity(map);
        return map;
    }

    @Override
    public CheckinCodeView currentCheckinCode(Integer activityNum) {
        CheckinCodeView codeInfo = activityMapper.selectCheckinCodeInfo(activityNum);
        if (codeInfo != null && (codeInfo.getSecretGenerated() == null || codeInfo.getSecretGenerated() == 0)) {

            refreshCheckinCode(activityNum);
            codeInfo = activityMapper.selectCheckinCodeInfo(activityNum);
        }
        return codeInfo;
    }

    @Override
    public String refreshCheckinCode(Integer activityNum) {
        List<Activity> activities = activityMapper.selectByActivityNum(activityNum);
        if (activities.isEmpty()) {
            return null;
        }
        activityMapper.updateActivityCheckinSecret(randomCheckinSecret(), activityNum);
        CheckinCodeView codeInfo = activityMapper.selectCheckinCodeInfo(activityNum);
        return codeInfo == null ? null : codeInfo.getCheckinCode();
    }

    private String randomCheckinSecret() {
        return (UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "")).substring(0, 32).toUpperCase();
    }

    private void decorate(List<CheckIn> records) {
        for (CheckIn checkIn : records) {
            checkIn.setCheckinSourceText(sourceText(checkIn.getCheckinSource()));
            checkIn.setCheckinObjectconfirmText(objectConfirmText(checkIn.getCheckinObjectconfirm()));
            checkIn.setCheckinTimecheckText(timecheckText(checkIn.getCheckinTimecheck()));
            checkIn.setCheckinObjectphone(maskPhone(checkIn.getCheckinObjectphone()));
        }
    }

    private String objectConfirmText(String objectConfirm) {
        if ("1".equals(objectConfirm)) {
            return "服务对象已确认";
        }
        if ("2".equals(objectConfirm)) {
            return "服务对象已否认";
        }
        return "待服务对象确认";
    }

    private String timecheckText(String timecheck) {
        if ("1".equals(timecheck)) {
            return "已确认";
        }
        if ("2".equals(timecheck)) {
            return "已驳回";
        }
        if ("3".equals(timecheck)) {
            return "已冲销";
        }
        return "待复核";
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

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

    private String sourceText(String source) {
        return "2".equals(source) ? "组织补录" : "平台签到";
    }

    private boolean isSuccess(Map<Object, Object> map) {
        Object ok = map.get("ok");
        return ok != null && ((Number) ok).intValue() == 1;
    }
}
