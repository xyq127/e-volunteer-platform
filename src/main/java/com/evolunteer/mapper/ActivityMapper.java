package com.evolunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.Activity;
import com.evolunteer.entity.CheckinCodeView;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ActivityMapper extends BaseMapper<Activity> {

    void organization_insert_activity(Map<Object, Object> map);

    void admin_check_activity(Map<Object, Object> map);

    void organization_revise_activity(Map<Object, Object> map);

    void organization_change_activity_state(Map<Object, Object> map);

    void activity_state_refresh(Map<Object, Object> map);

    IPage<Activity> selectPageByOrganizationAndState(Page<Activity> page,
                                                     @Param("organizationNum") Integer organizationNum,
                                                     @Param("activityState") String activityState,
                                                     @Param("keyword") String keyword);

    IPage<Activity> selectPageByState(Page<Activity> page,
                                      @Param("activityState") String activityState,
                                      @Param("keyword") String keyword);

    List<Activity> selectAllByOrganizationNumAndActivityStateAndActivityIsdeleted(
            @Param("organizationNum") Integer organizationNum,
            @Param("activityState") String activityState,
            @Param("activityIsdeleted") Integer activityIsdeleted);

    List<Activity> selectAllByActivityStateAndActivityIsdeleted(
            @Param("activityState") String activityState,
            @Param("activityIsdeleted") Integer activityIsdeleted);

    List<Activity> selectByActivityNum(@Param("activityNum") Integer activityNum);

    List<Activity> selectAllByActivityName(@Param("activityName") String activityName);

    List<Activity> selectByActivityName(@Param("activityName") String activityName);

    int updateActivityCheckinSecret(@Param("activityCheckinSecret") String activityCheckinSecret,
                                    @Param("activityNum") Integer activityNum);

    CheckinCodeView selectCheckinCodeInfo(@Param("activityNum") Integer activityNum);

    int updateActivityIsdeletedByActivityNum(@Param("activityIsdeleted") Integer activityIsdeleted,
                                             @Param("activityNum") Integer activityNum);
}
