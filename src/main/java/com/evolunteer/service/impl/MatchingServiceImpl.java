package com.evolunteer.service.impl;

import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ActivityMatch;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.mapper.MatchingMapper;
import com.evolunteer.service.MatchingService;
import com.evolunteer.service.VolunteerService;
import com.evolunteer.utils.MatchCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 供需匹配业务实现类：从数据库读取活动技能要求、活动地点距离与组织历史参与次数，
 * 结合志愿者技能标签与其已报名活动的时间段，计算匹配度并按匹配度降序返回推荐活动。
 */
@Service
public class MatchingServiceImpl implements MatchingService {

    @Autowired
    private MatchingMapper matchingMapper;

    @Autowired
    private VolunteerService volunteerService;

    /**
     * 查询志愿者可报名的志愿活动，并附加匹配度与匹配理由，按匹配度降序排列
     *
     * @param volunteerNum 志愿者编号
     * @return 匹配活动列表
     */
    @Override
    public List<ActivityMatch> recommendActivities(Integer volunteerNum) {

        Volunteer volunteer = volunteerService.getById(volunteerNum);
        if (volunteer == null) {
            return new ArrayList<>();
        }

        List<String> volunteerSkills = volunteerService.listSkillNames(volunteerNum);
        List<Activity> busyActivities = matchingMapper.selectBusyActivities(volunteerNum);
        List<ActivityMatch> matches = matchingMapper.selectActivityMatches(volunteerNum,
                toDouble(volunteer.getVolunteerLatitude()), toDouble(volunteer.getVolunteerLongitude()),
                null, true);

        for (ActivityMatch match : matches) {
            score(match, volunteerSkills, busyActivities);
        }
        matches.sort(Comparator.comparingInt(ActivityMatch::getMatchScore).reversed());
        return matches;
    }

    /**
     * 计算指定活动对指定志愿者的匹配度
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  活动编号
     * @return 匹配度计算结果，活动不存在时返回 null
     */
    @Override
    public MatchCalculator.MatchResult matchForActivity(Integer volunteerNum, Integer activityNum) {

        Volunteer volunteer = volunteerService.getById(volunteerNum);
        if (volunteer == null) {
            return null;
        }

        List<ActivityMatch> matches = matchingMapper.selectActivityMatches(volunteerNum,
                toDouble(volunteer.getVolunteerLatitude()), toDouble(volunteer.getVolunteerLongitude()),
                activityNum, false);
        if (matches.isEmpty()) {
            return null;
        }

        ActivityMatch match = matches.get(0);
        return MatchCalculator.calculate(volunteerService.listSkillNames(volunteerNum),
                splitSkillNames(match.getActivitySkillNames()),
                match.getDistanceMeters(),
                conflictingActivityNames(match, matchingMapper.selectBusyActivities(volunteerNum)),
                match.getOrganizationHistoryCount());
    }

    /**
     * 计算匹配活动的匹配度与命中技能，写回匹配视图对象
     */
    private void score(ActivityMatch match, List<String> volunteerSkills, List<Activity> busyActivities) {

        MatchCalculator.MatchResult result = MatchCalculator.calculate(volunteerSkills,
                splitSkillNames(match.getActivitySkillNames()),
                match.getDistanceMeters(),
                conflictingActivityNames(match, busyActivities),
                match.getOrganizationHistoryCount());

        match.setMatchScore(result.getScore());
        match.setMatchedSkills(result.getMatchedSkills());
        match.setReasons(result.getReasons());
    }

    /**
     * 找出与匹配活动时间重叠的已报名活动名称
     */
    private List<String> conflictingActivityNames(ActivityMatch match, List<Activity> busyActivities) {

        List<String> names = new ArrayList<>();
        if (match.getActivityBegintime() == null || match.getActivityEndtime() == null) {
            return names;
        }
        for (Activity busy : busyActivities) {
            if (busy.getActivityNum().equals(match.getActivityNum())
                    || busy.getActivityBegintime() == null || busy.getActivityEndtime() == null) {
                continue;
            }
            boolean overlap = busy.getActivityBegintime().before(match.getActivityEndtime())
                    && match.getActivityBegintime().before(busy.getActivityEndtime());
            if (overlap) {
                names.add(busy.getActivityName());
            }
        }
        return names;
    }

    /**
     * 拆分数据库中聚合的活动技能标签
     */
    private List<String> splitSkillNames(String activitySkillNames) {
        List<String> skills = new ArrayList<>();
        if (activitySkillNames == null || activitySkillNames.trim().isEmpty()) {
            return skills;
        }
        for (String skillName : activitySkillNames.split(",")) {
            if (!skillName.trim().isEmpty()) {
                skills.add(skillName.trim());
            }
        }
        return skills;
    }

    /**
     * 将坐标由高精度小数转为匹配查询使用的浮点数，未设置坐标时返回 null
     */
    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
