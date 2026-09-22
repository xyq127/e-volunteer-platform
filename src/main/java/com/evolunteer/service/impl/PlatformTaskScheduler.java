package com.evolunteer.service.impl;

import com.evolunteer.entity.Admin;
import com.evolunteer.mapper.AdminMapper;
import com.evolunteer.service.ActivityService;
import com.evolunteer.service.CheckInService;
import com.evolunteer.service.NotificationService;
import com.evolunteer.service.ParticipationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class PlatformTaskScheduler {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ParticipationService participationService;

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AdminMapper adminMapper;

    @Scheduled(fixedDelayString = "${evolunteer.schedule.interval-ms:60000}", initialDelay = 10000)
    public void refreshActivityStateAndParticipateConfirm() {
        try {
            Map<Object, Object> stateResult = activityService.refreshActivityStates();
            Map<Object, Object> expireResult = participationService.releaseUnconfirmedParticipations();
            Map<Object, Object> anomalyResult = checkInService.scanAnomalies();

            int finished = toInt(stateResult.get("finishedCount"));
            int released = toInt(expireResult.get("releasedCount"));
            int flagged = toInt(anomalyResult.get("flaggedCount"));
            if (finished > 0 || released > 0 || flagged > 0) {
                log.info("定时任务处理完成，活动自动结束 {} 个，未确认报名释放 {} 条，异常时长标记 {} 条",
                        finished, released, flagged);
            }
            if (flagged > 0) {
                notifyAdminsOfAnomaly(flagged);
            }
        } catch (RuntimeException e) {
            log.error("平台定时任务执行失败", e);
        }
    }

    private void notifyAdminsOfAnomaly(int flaggedCount) {
        for (Admin admin : adminMapper.selectList(null)) {
            notificationService.send(admin.getAdminId(), "ROLE_ADMIN", "异常服务时长待裁定",
                    "巡检发现 " + flaggedCount + " 条服务记录命中时长异常规则，请前往「补录时长复核」页裁定。");
        }
    }

    private int toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }
}
