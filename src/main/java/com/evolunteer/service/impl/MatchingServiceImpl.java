package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ActivityMatch;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.mapper.MatchingMapper;
import com.evolunteer.service.MatchingService;
import com.evolunteer.service.VolunteerService;
import com.evolunteer.utils.MatchCalculator;
import com.evolunteer.utils.PageSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchingServiceImpl implements MatchingService {

    private static final int MATCH_CANDIDATE_LIMIT = 200;

    @Autowired
    private MatchingMapper matchingMapper;

    @Autowired
    private VolunteerService volunteerService;

    @Override
    public IPage<ActivityMatch> pageRecommendActivities(Integer volunteerNum, String keyword,
                                                        Integer pageNum, Integer pageSize) {

        Page<ActivityMatch> page = PageSupport.of(pageNum, pageSize);
        Volunteer volunteer = volunteerService.getById(volunteerNum);
        if (volunteer == null) {
            return emptyPage(page);
        }

        List<String> volunteerSkills = volunteerService.listSkillNames(volunteerNum);
        List<Activity> busyActivities = matchingMapper.selectBusyActivities(volunteerNum);
        List<ActivityMatch> candidates = matchingMapper.selectActivityMatches(volunteerNum,
                toDouble(volunteer.getVolunteerLatitude()), toDouble(volunteer.getVolunteerLongitude()),
                null, true, PageSupport.normalizeKeyword(keyword), MATCH_CANDIDATE_LIMIT);

        for (ActivityMatch match : candidates) {
            score(match, volunteerSkills, busyActivities);
        }
        candidates.sort(Comparator.comparingInt(ActivityMatch::getMatchScore).reversed());

        int total = candidates.size();
        long fromIndex = (page.getCurrent() - 1) * page.getSize();
        List<ActivityMatch> records;
        if (fromIndex >= total) {
            records = new ArrayList<>();
        } else {
            records = new ArrayList<>(candidates.subList((int) fromIndex,
                    (int) Math.min(total, fromIndex + page.getSize())));
        }

        Page<ActivityMatch> result = new Page<>(page.getCurrent(), page.getSize(), total);
        result.setRecords(records);
        return result;
    }

    @Override
    public MatchCalculator.MatchResult matchForActivity(Integer volunteerNum, Integer activityNum) {

        Volunteer volunteer = volunteerService.getById(volunteerNum);
        if (volunteer == null) {
            return null;
        }

        List<ActivityMatch> matches = matchingMapper.selectActivityMatches(volunteerNum,
                toDouble(volunteer.getVolunteerLatitude()), toDouble(volunteer.getVolunteerLongitude()),
                activityNum, false, null, null);
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

    private IPage<ActivityMatch> emptyPage(Page<ActivityMatch> page) {
        Page<ActivityMatch> result = new Page<>(page.getCurrent(), page.getSize(), 0);
        result.setRecords(new ArrayList<>());
        return result;
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
