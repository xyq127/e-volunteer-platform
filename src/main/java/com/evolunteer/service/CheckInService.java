package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.CheckIn;
import com.evolunteer.entity.CheckinCodeView;

import java.util.List;

import java.util.Map;

public interface CheckInService {

    Map<Object, Object> checkin(Integer volunteerNum, Integer activityNum, String code,
                                Double latitude, Double longitude);

    Map<Object, Object> checkout(Integer volunteerNum, Integer activityNum, Double latitude, Double longitude);

    IPage<CheckIn> pageCheckinRecords(Integer activityNum, String state, String source,
                                      Integer pageNum, Integer pageSize);

    List<CheckIn> listCheckinRecords(Integer activityNum);

    Map<Object, Object> reviewCheckin(Integer checkinNum, Integer isPass, String remark);

    Map<Object, Object> recordServiceHours(String loginId, Integer participateNum, String beginTime,
                                           String endTime, String remark);

    IPage<CheckIn> pageManualCheckins(String state, String keyword, Integer pageNum, Integer pageSize);

    Map<Object, Object> reviewByAdmin(String loginId, Integer checkinNum, Integer isPass, String remark);

    IPage<CheckIn> pageAnomalies(String keyword, Integer pageNum, Integer pageSize);

    Map<Object, Object> scanAnomalies();

    Map<Object, Object> issueConfirmSheet(String loginId, Integer checkinNum, String objectName, String objectPhone);

    Map<Object, Object> objectConfirm(String confirmCode, String objectPhone, Integer resultValue, String objectRemark);

    Map<Object, Object> settleActivity(Integer activityNum);

    CheckinCodeView currentCheckinCode(Integer activityNum);

    String refreshCheckinCode(Integer activityNum);
}
