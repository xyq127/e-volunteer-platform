package com.evolunteer.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class PortalShowView implements Serializable {

    private Integer showNum;

    private String showId;

    private String showDetail;

    private Date showSharetime;

    private Integer volunteerNum;

    private String volunteerName;

    private Integer activityNum;

    private String activityName;

    private Integer showBrowse;

    private Integer pictureCount;

    private Integer showLike;

    private String pictureRoute;

    private List<String> pictureRoutes;

    private Boolean liked;

    private static final long serialVersionUID = 1L;
}
