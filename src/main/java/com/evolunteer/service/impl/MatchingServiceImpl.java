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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 供需匹配业务实现类：从数据库读取活动技能要求、活动地点距离与组织历史参与次数，
 * 结合志愿者技能标签与其已报名活动的时间段，计算匹配度并按匹配度降序返回推荐活动。
 * <p>
 * 匹配度由本类的纯函数计算器打分，无法交给数据库排序，因此分页时先取候选集（上限见
 * {@link #MATCH_CANDIDATE_LIMIT}）再在内存中排序分页；候选集上限保证单次请求的排序开销可控，
 * 返回的总条数以候选集为准，页面分页控件因此始终与排序结果一致。
 */
@Service
public class MatchingServiceImpl implements MatchingService {

    /**
     * 参与匹配度排序的候选活动数量上限
     */
    private static final int MATCH_CANDIDATE_LIMIT = 200;

    @Autowired
    private MatchingMapper matchingMapper;

    @Autowired
    private VolunteerService volunteerService;

    /**
     * 分页查询志愿者可报名的志愿活动并计算匹配度，按匹配度降序返回
     *
     * @param volunteerNum 志愿者编号
     * @param keyword      活动名称或活动地点关键字，可为空
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @return 匹配活动分页结果
     */
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
     * 构造空的分页结果
     */
    private IPage<ActivityMatch> emptyPage(Page<ActivityMatch> page) {
        Page<ActivityMatch> result = new Page<>(page.getCurrent(), page.getSize(), 0);
        result.setRecords(new ArrayList<>());
        return result;
    }

    /**
     * 将坐标由高精度小数转为匹配查询使用的浮点数，未设置坐标时返回 null
     */
    private Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
