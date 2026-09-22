package com.evolunteer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.evolunteer.entity.ActivityMatch;
import com.evolunteer.utils.MatchCalculator;

public interface MatchingService {

    IPage<ActivityMatch> pageRecommendActivities(Integer volunteerNum, String keyword,
                                                 Integer pageNum, Integer pageSize);

    MatchCalculator.MatchResult matchForActivity(Integer volunteerNum, Integer activityNum);
}
