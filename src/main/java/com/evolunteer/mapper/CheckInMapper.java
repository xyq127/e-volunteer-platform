package com.evolunteer.mapper;

import com.evolunteer.entity.CheckIn;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CheckInMapper extends BaseMapper<CheckIn> {

    void volunteer_checkin(Map<Object, Object> map);

    void volunteer_checkout(Map<Object, Object> map);

    void organization_check_checkin(Map<Object, Object> map);

    void organization_record_service_hours(Map<Object, Object> map);

    void admin_check_checkin(Map<Object, Object> map);

    void organization_issue_confirm_sheet(Map<Object, Object> map);

    void service_object_confirm(Map<Object, Object> map);

    void service_anomaly_scan(Map<Object, Object> map);

    IPage<CheckIn> selectPageAnomalies(Page<CheckIn> page, @Param("keyword") String keyword);

    void organization_settle_activity(Map<Object, Object> map);

    IPage<CheckIn> selectPageCheckinByActivityNum(Page<CheckIn> page,
                                                  @Param("activityNum") Integer activityNum,
                                                  @Param("state") String state,
                                                  @Param("source") String source);

    List<CheckIn> selectCheckinListByActivityNum(@Param("activityNum") Integer activityNum);

    IPage<CheckIn> selectPageManualCheckins(Page<CheckIn> page,
                                            @Param("state") String state,
                                            @Param("keyword") String keyword);
}
