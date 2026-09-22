package com.evolunteer.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class CheckinCodeView implements Serializable {

    private Integer activityNum;

    private String checkinCode;

    private Long expiresInSeconds;

    private Integer secretGenerated;

    private static final long serialVersionUID = 1L;
}
