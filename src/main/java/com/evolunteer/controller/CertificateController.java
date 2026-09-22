package com.evolunteer.controller;

import com.evolunteer.entity.ApiResponse;
import com.evolunteer.entity.ServiceCertificate;
import com.evolunteer.service.CertificateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping(value = "/certificate")
public class CertificateController {

    @Autowired
    CertificateService certificateService;

    @RequestMapping(value = "/verify", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse verify(@RequestParam(value = "volunteerId") String volunteerId,
                              @RequestParam(value = "code") String code) {

        ServiceCertificate certificate = certificateService.verifyCertificate(volunteerId, code);
        if (certificate == null) {
            log.warn("志愿服务证明校验失败，未找到志愿者：{}", volunteerId);
            return ApiResponse.fail("未找到该志愿者编号对应的服务记录");
        }
        if (!certificate.isValid()) {
            log.warn("志愿服务证明校验码不正确，志愿者编号：{}", volunteerId);
            return ApiResponse.fail("校验码不正确，该证明可能被篡改或不存在")
                    .add("verification", certificate);
        }
        log.info("志愿服务证明校验通过，志愿者编号：{}", volunteerId);
        return ApiResponse.success("校验通过").add("verification", certificate);
    }
}
