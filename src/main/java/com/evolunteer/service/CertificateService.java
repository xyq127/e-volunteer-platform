package com.evolunteer.service;

import com.evolunteer.entity.ServiceCertificate;

public interface CertificateService {

    ServiceCertificate issueCertificate(Integer volunteerNum);

    ServiceCertificate verifyCertificate(String volunteerId, String verifyCode);
}
