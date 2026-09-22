package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.Organization;
import com.evolunteer.entity.Participation;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.mapper.ActivityMapper;
import com.evolunteer.mapper.OrganizationMapper;
import com.evolunteer.mapper.ParticipationMapper;
import com.evolunteer.mapper.UserAccountMapper;
import com.evolunteer.service.ExportService;
import com.evolunteer.utils.CsvExporter;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ExportServiceImpl implements ExportService {

    private static final Integer ACCOUNT_ENABLED = 1;

    private static final String APPLY_STATE_APPROVED = "1";

    private static final String ACTIVITY_STATE_AUDIT = "0";

    private static final String ACTIVITY_STATE_NOT_START = "1";

    private static final String ACTIVITY_STATE_CARRY = "2";

    private static final String ACTIVITY_STATE_END = "3";

    private static final String ACTIVITY_STATE_NOT_AUDIT = "4";

    private static final String ACTIVITY_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private static final List<String> VOLUNTEER_HEADERS = Arrays.asList(
            "志愿者编号", "姓名", "手机号", "信用分", "累计服务时长(小时)", "爽约次数", "账号状态");

    private static final List<String> ACTIVITY_HEADERS = Arrays.asList(
            "活动编号", "活动名称", "申报组织", "开始时间", "结束时间", "活动地点", "需求人数", "已通过人数", "活动状态");

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ParticipationMapper participationMapper;

    @Override
    public String exportVolunteers(String keyword) {

        List<Volunteer> volunteers = userAccountMapper.selectVolunteerAccountList(PageSupport.normalizeKeyword(keyword));
        List<List<String>> rows = new ArrayList<>(volunteers.size());
        for (Volunteer volunteer : volunteers) {
            rows.add(Arrays.asList(
                    text(volunteer.getVolunteerId()),
                    text(volunteer.getVolunteerName()),
                    text(volunteer.getVolunteerTel()),
                    text(volunteer.getVolunteerCredit()),
                    text(volunteer.getVolunteerTotalduration()),
                    text(volunteer.getVolunteerNoshow()),
                    accountStateText(volunteer.getAccountEnabled())));
        }
        log.info("导出志愿者名册，关键字：{}，导出条数：{}", keyword, rows.size());
        return CsvExporter.toCsv(VOLUNTEER_HEADERS, rows);
    }

    @Override
    public String exportActivities(String keyword) {

        String normalizedKeyword = PageSupport.normalizeKeyword(keyword);
        QueryWrapper<Activity> wrapper = new QueryWrapper<>();
        wrapper.eq("activity_isdeleted", 0);
        if (normalizedKeyword != null) {
            wrapper.and(condition -> condition.like("activity_name", normalizedKeyword)
                    .or().like("activity_id", normalizedKeyword));
        }
        wrapper.orderByDesc("activity_begintime").orderByDesc("activity_num");
        List<Activity> activities = activityMapper.selectList(wrapper);

        Map<Integer, String> organizationNames = organizationNames();
        Map<Integer, Integer> approvedCounts = approvedCountByActivity();
        SimpleDateFormat timeFormat = new SimpleDateFormat(ACTIVITY_TIME_PATTERN);

        List<List<String>> rows = new ArrayList<>(activities.size());
        for (Activity activity : activities) {
            rows.add(Arrays.asList(
                    text(activity.getActivityId()),
                    text(activity.getActivityName()),
                    text(organizationNames.get(activity.getOrganizationNum())),
                    timeText(timeFormat, activity.getActivityBegintime()),
                    timeText(timeFormat, activity.getActivityEndtime()),
                    text(activity.getActivityLocation()),
                    text(activity.getActivityNeedpeople()),
                    text(approvedCounts.getOrDefault(activity.getActivityNum(), 0)),
                    activityStateText(activity.getActivityState())));
        }
        log.info("导出活动清单，关键字：{}，导出条数：{}", keyword, rows.size());
        return CsvExporter.toCsv(ACTIVITY_HEADERS, rows);
    }

    private Map<Integer, String> organizationNames() {
        Map<Integer, String> organizationNames = new HashMap<>();
        for (Organization organization : organizationMapper.selectAllOrganizationNumAndName()) {
            organizationNames.put(organization.getOrganizationNum(), organization.getOrganizationName());
        }
        return organizationNames;
    }

    private Map<Integer, Integer> approvedCountByActivity() {
        QueryWrapper<Participation> wrapper = new QueryWrapper<>();
        wrapper.select("activity_num as activitynum", "count(*) as approvedcount")
                .eq("participate_applystate", APPLY_STATE_APPROVED)
                .groupBy("activity_num");

        Map<Integer, Integer> approvedCounts = new HashMap<>();
        for (Map<String, Object> row : participationMapper.selectMaps(wrapper)) {
            Integer activityNum = numberValue(row, "activitynum");
            Integer approvedCount = numberValue(row, "approvedcount");
            if (activityNum != null && approvedCount != null) {
                approvedCounts.put(activityNum, approvedCount);
            }
        }
        return approvedCounts;
    }

    private Integer numberValue(Map<String, Object> row, String column) {
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (column.equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number) {
                return ((Number) entry.getValue()).intValue();
            }
        }
        return null;
    }

    private String accountStateText(Integer accountEnabled) {
        if (accountEnabled == null) {
            return "";
        }
        return ACCOUNT_ENABLED.equals(accountEnabled) ? "启用" : "停用";
    }

    private String activityStateText(String activityState) {
        if (activityState == null || activityState.trim().isEmpty()) {
            return "";
        }
        switch (activityState) {
            case ACTIVITY_STATE_AUDIT:
                return "审核中";
            case ACTIVITY_STATE_NOT_START:
                return "未开始";
            case ACTIVITY_STATE_CARRY:
                return "进行中";
            case ACTIVITY_STATE_END:
                return "已结束";
            case ACTIVITY_STATE_NOT_AUDIT:
                return "审核未通过";
            default:
                return activityState;
        }
    }

    private String timeText(SimpleDateFormat timeFormat, Date time) {
        return time == null ? "" : timeFormat.format(time);
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
    }
}
