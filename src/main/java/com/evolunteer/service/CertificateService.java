package com.evolunteer.service;

import com.evolunteer.entity.ServiceCertificate;

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿服务证明业务接口：依据志愿者经志愿者组织复核通过的累计服务时长与星级生成带校验码的服务证明，
 * 并为第三方提供按志愿者编号与校验码核验证明的服务，解决志愿服务时长证明难以开具与真伪难辨的问题。
 */
public interface CertificateService {

    /**
     * 生成志愿者的志愿服务证明
     *
     * @param volunteerNum 志愿者编号
     * @return 志愿服务证明，志愿者不存在时返回 null
     */
    ServiceCertificate issueCertificate(Integer volunteerNum);

    /**
     * 核验志愿服务证明
     *
     * @param volunteerId 志愿者业务编号
     * @param verifyCode  证明校验码
     * @return 核验结果证明，校验码不匹配时返回的证明校验标记为不通过
     */
    ServiceCertificate verifyCertificate(String volunteerId, String verifyCode);
}
