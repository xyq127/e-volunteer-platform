package com.evolunteer.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
public class VolunteerParticipationView implements Serializable {

    private Integer participateNum;

    private Integer activityNum;

    private String activityId;

    private String activityName;

    private String activityState;

    private Date activityBegintime;

    private Date activityEndtime;

    private String activityLocation;

    private Date participateApplytime;

    private String participateApplystate;

    private Double participateDuration;

    private String participateTimecheck;

    private String participateConfirmstate;

    private Date participateConfirmtime;

    private Date checkinBegintime;

    private Date checkinEndtime;

    private Double checkinDuration;

    private Integer checkinDistance;

    private String checkinFlag;

    private String checkinTimecheck;

    private String checkinRemark;

    private String checkinSource;

    private String participateApplystateText;

    private String participateConfirmstateText;

    private boolean canCheckin;

    private boolean canCheckout;

    private boolean canCancel;

    private boolean canConfirm;

    private static final long serialVersionUID = 1L;
}
