package com.evolunteer.service.impl;

import com.evolunteer.entity.CheckIn;
import com.evolunteer.entity.Participation;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.service.CheckInService;
import com.evolunteer.service.OrgExportService;
import com.evolunteer.service.ParticipationService;
import com.evolunteer.utils.CsvExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class OrgExportServiceImpl implements OrgExportService {

    private static final String APPLY_STATE_PENDING = "0";

    private static final String APPLY_STATE_APPROVED = "1";

    private static final String APPLY_STATE_REJECTED = "2";

    private static final String APPLY_STATE_WAITING = "3";

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private CheckInService checkInService;

    @Override
    public String exportParticipants(Integer activityNum) {

        List<String> headers = Arrays.asList("志愿者编号", "姓名", "性别", "联系电话",
                "报名状态", "参加确认", "服务时长(小时)", "时长复核状态", "爽约");
        List<List<String>> rows = new ArrayList<>();

        for (Participation participation : participationService.getVolunteerList(activityNum)) {
            Volunteer volunteer = participation.getVolunteer();
            rows.add(Arrays.asList(
                    volunteer == null ? "" : volunteer.getVolunteerId(),
                    volunteer == null ? "" : volunteer.getVolunteerName(),
                    volunteer == null ? "" : volunteer.getVolunteerGender(),
                    volunteer == null ? "" : volunteer.getVolunteerTel(),
                    applyStateText(participation.getParticipateApplystate()),
                    confirmStateText(participation.getParticipateConfirmstate()),
                    durationText(participation.getParticipateDuration()),
                    timecheckText(participation.getParticipateTimecheck()),
                    Integer.valueOf(1).equals(participation.getParticipateNoshow()) ? "爽约" : "正常"));
        }
        return CsvExporter.toCsv(headers, rows);
    }

    @Override
    public String exportServiceHours(Integer activityNum) {

        List<String> headers = Arrays.asList("志愿者编号", "姓名", "联系电话", "服务开始时间",
                "服务结束时间", "服务时长(小时)", "记录来源", "复核状态", "复核意见");
        List<List<String>> rows = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_PATTERN);
        for (CheckIn checkIn : checkInService.listCheckinRecords(activityNum)) {
            rows.add(Arrays.asList(
                    nullToEmpty(checkIn.getVolunteerId()),
                    nullToEmpty(checkIn.getVolunteerName()),
                    nullToEmpty(checkIn.getVolunteerTel()),
                    formatTime(checkIn.getCheckinBegintime(), format),
                    formatTime(checkIn.getCheckinEndtime(), format),
                    durationText(checkIn.getCheckinDuration()),
                    "2".equals(checkIn.getCheckinSource()) ? "组织补录" : "平台签到",
                    timecheckText(checkIn.getCheckinTimecheck()),
                    nullToEmpty(checkIn.getCheckinRemark())));
        }
        return CsvExporter.toCsv(headers, rows);
    }

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

    private String confirmStateText(String confirmState) {
        if ("1".equals(confirmState)) {
            return "已确认参加";
        }
        if ("2".equals(confirmState)) {
            return "已放弃参加";
        }
        return "待确认";
    }

    private String timecheckText(String timecheck) {
        if ("1".equals(timecheck)) {
            return "已确认";
        }
        if ("2".equals(timecheck)) {
            return "已驳回";
        }
        return "待复核";
    }

    private String durationText(Double duration) {
        return duration == null ? "" : String.format("%.1f", duration);
    }

    private String formatTime(Date time, SimpleDateFormat format) {
        return time == null ? "" : format.format(time);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
