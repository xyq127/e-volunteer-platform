package com.evolunteer.mapper;

import com.evolunteer.entity.Activity;
import com.evolunteer.entity.ActivityMatch;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchingMapper {

    List<ActivityMatch> selectActivityMatches(@Param("volunteerNum") Integer volunteerNum,
                                              @Param("latitude") Double latitude,
                                              @Param("longitude") Double longitude,
                                              @Param("activityNum") Integer activityNum,
                                              @Param("openOnly") boolean openOnly,
                                              @Param("keyword") String keyword,
                                              @Param("limitCount") Integer limitCount);

    List<Activity> selectBusyActivities(@Param("volunteerNum") Integer volunteerNum);
}
