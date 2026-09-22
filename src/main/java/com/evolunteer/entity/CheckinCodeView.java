package com.evolunteer.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 现场签到码视图对象：现场签到码由活动签到密钥与时间窗口派生，每 120 秒轮换一次，
 * 本对象给出当前生效的签到码与剩余有效秒数，供志愿者组织在活动现场公布，不属于任何数据表。
 */
@Data
public class CheckinCodeView implements Serializable {

    /** 活动编号 */
    private Integer activityNum;

    /** 当前生效的现场签到码，尚未生成签到密钥时为空 */
    private String checkinCode;

    /** 当前签到码的剩余有效秒数 */
    private Long expiresInSeconds;

    /** 是否已生成签到密钥，0 表示尚未生成 */
    private Integer secretGenerated;

    private static final long serialVersionUID = 1L;
}
