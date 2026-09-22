package com.evolunteer.entity;

import java.util.List;
import lombok.Data;

@Data
public class ActivityMatch extends Activity {

    private String activitySkillNames;

    private Integer distanceMeters;

    private int organizationHistoryCount;

    private int matchScore;

    private List<String> matchedSkills;

    private List<String> reasons;
}
