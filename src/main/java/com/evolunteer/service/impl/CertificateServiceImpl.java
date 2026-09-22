package com.evolunteer.service.impl;

import com.evolunteer.entity.ServiceCertificate;
import com.evolunteer.entity.ServiceRecord;
import com.evolunteer.entity.Volunteer;
import com.evolunteer.enums.VolunteerStarEnum;
import com.evolunteer.mapper.ParticipationMapper;
import com.evolunteer.service.CertificateService;
import com.evolunteer.service.VolunteerService;
import com.evolunteer.utils.CertificateCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class CertificateServiceImpl implements CertificateService {

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private ParticipationMapper participationMapper;

    @Value("${evolunteer.certificate.secret}")
    private String certificateSecret;

    @Override
    public ServiceCertificate issueCertificate(Integer volunteerNum) {

        Volunteer volunteer = volunteerService.getById(volunteerNum);
        if (volunteer == null) {
            return null;
        }
        List<ServiceRecord> records = participationMapper.selectApprovedServiceRecords(volunteerNum);
        ServiceCertificate certificate = buildCertificate(volunteer, records);
        certificate.setValid(true);
        return certificate;
    }

    @Override
    public ServiceCertificate verifyCertificate(String volunteerId, String verifyCode) {

        Volunteer volunteer = volunteerService.getByLoginId(volunteerId);
        if (volunteer == null) {
            return null;
        }

        List<ServiceRecord> records = participationMapper.selectApprovedServiceRecords(volunteer.getVolunteerNum());
        ServiceCertificate certificate = buildCertificate(volunteer, records);
        certificate.setValid(verifyCode != null && certificate.getVerifyCode().equalsIgnoreCase(verifyCode.trim()));
        return certificate;
    }

    private ServiceCertificate buildCertificate(Volunteer volunteer, List<ServiceRecord> records) {

        double totalDuration = volunteer.getVolunteerTotalduration() == null ? 0 : volunteer.getVolunteerTotalduration();
        int activityCount = participationMapper.countApprovedActivities(volunteer.getVolunteerNum());
        VolunteerStarEnum star = VolunteerStarEnum.of(totalDuration);

        ServiceCertificate certificate = new ServiceCertificate();
        certificate.setVolunteerId(volunteer.getVolunteerId());
        certificate.setVolunteerName(maskName(volunteer.getVolunteerName()));
        certificate.setTotalDuration(totalDuration);
        certificate.setActivityCount(activityCount);
        certificate.setStarLevel(star.getStarName());
        certificate.setNextLevelName(star.getNextStarName());
        certificate.setHoursToNextLevel(VolunteerStarEnum.hoursToNextStar(totalDuration));
        certificate.setIssueDate(new SimpleDateFormat(DATE_PATTERN).format(new Date()));
        certificate.setVerifyCode(CertificateCodeUtil.code(certificateSecret, volunteer.getVolunteerId(),
                totalDuration, activityCount));
        certificate.setRecords(records);
        return certificate;
    }

    private String maskName(String volunteerName) {
        if (volunteerName == null || volunteerName.isEmpty()) {
            return volunteerName;
        }
        return volunteerName.charAt(0) + "*".repeat(volunteerName.length() - 1);
    }
}
