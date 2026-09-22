package com.evolunteer.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class VolunteerProfileView implements Serializable {

    private String volunteerId;

    private String volunteerName;

    private String volunteerTel;

    private String volunteerGender;

    private Date volunteerBirth;

    private Integer volunteerCredit;

    private Integer volunteerNoshow;

    private Double volunteerTotalDuration;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private List<String> skills;

    private String starLevel;

    private String nextLevelName;

    private Double nextLevelHours;

    private Double hoursToNextLevel;

    private static final long serialVersionUID = 1L;
}
