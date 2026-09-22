package com.evolunteer.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class ActivityAnnouncementView implements Serializable {

    private Integer actannouncementNum;

    private Integer activityNum;

    private String actannouncementId;

    private String actannouncementName;

    private String actannouncementDetail;

    private Integer organizationNum;

    private String activityName;

    private static final long serialVersionUID = 1L;
}
