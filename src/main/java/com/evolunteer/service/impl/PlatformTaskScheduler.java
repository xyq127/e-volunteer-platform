package com.evolunteer.service.impl;

import com.evolunteer.service.ActivityService;
import com.evolunteer.service.ParticipationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 平台定时任务：按活动时间自动流转活动状态，并释放活动开始前仍未确认参加的报名名额。
 * 两个任务都只做幂等的批量更新与状态释放，重复执行不会产生重复数据处理。
 */
@Slf4j
@Component
public class PlatformTaskScheduler {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ParticipationService participationService;

    /**
     * 活动状态自动流转与未确认报名释放，默认每分钟执行一次
     */
    @Scheduled(fixedDelayString = "${evolunteer.schedule.interval-ms:60000}", initialDelay = 10000)
    public void refreshActivityStateAndParticipateConfirm() {
        try {
            Map<Object, Object> stateResult = activityService.refreshActivityStates();
            Map<Object, Object> expireResult = participationService.releaseUnconfirmedParticipations();

            int finished = toInt(stateResult.get("finishedCount"));
            int released = toInt(expireResult.get("releasedCount"));
            if (finished > 0 || released > 0) {
                log.info("定时任务处理完成，活动自动结束 {} 个，未确认报名释放 {} 条", finished, released);
            }
        } catch (RuntimeException e) {
            log.error("平台定时任务执行失败", e);
        }
    }

    /**
     * 把存储过程的数值出参转为整数
     */
    private int toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }
}
