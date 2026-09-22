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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿服务证明业务实现类：以志愿者已核定累计服务时长与已核定服务记录数生成证明，
 * 校验码由平台密钥对上述数据计算 HMAC 摘要得到，数据被篡改时重新计算的校验码无法匹配。
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    /**
     * 证明签发日期格式
     */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @Autowired
    private VolunteerService volunteerService;

    @Autowired
    private ParticipationMapper participationMapper;

    /**
     * 平台证明密钥，部署环境可通过环境变量 EVOLUNTEER_CERT_SECRET 覆盖
     */
    @Value("${evolunteer.certificate.secret}")
    private String certificateSecret;

    /**
     * 生成志愿者的志愿服务证明
     *
     * @param volunteerNum 志愿者编号
     * @return 志愿服务证明，志愿者不存在时返回 null
     */
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

    /**
     * 核验志愿服务证明：按志愿者当前累计服务数据重新计算校验码并与提交的校验码比对
     *
     * @param volunteerId 志愿者业务编号
     * @param verifyCode  证明校验码
     * @return 核验结果证明，校验码不匹配时校验标记为不通过
     */
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

    /**
     * 组装志愿服务证明：累计时长与记录数决定校验码，姓名对外展示时做脱敏处理
     */
    private ServiceCertificate buildCertificate(Volunteer volunteer, List<ServiceRecord> records) {

        double totalDuration = volunteer.getVolunteerTotalduration() == null ? 0 : volunteer.getVolunteerTotalduration();
        int activityCount = records.size();
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

    /**
     * 姓名脱敏：保留姓氏，其余字符以星号代替
     */
    private String maskName(String volunteerName) {
        if (volunteerName == null || volunteerName.isEmpty()) {
            return volunteerName;
        }
        return volunteerName.charAt(0) + "*".repeat(volunteerName.length() - 1);
    }
}
