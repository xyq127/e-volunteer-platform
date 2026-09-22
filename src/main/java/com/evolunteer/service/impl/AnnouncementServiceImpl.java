package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.evolunteer.entity.PolicyAnnouncement;
import com.evolunteer.entity.PolicyFile;
import com.evolunteer.entity.PortalAnnouncementView;
import com.evolunteer.mapper.PolicyAnnouncementMapper;
import com.evolunteer.mapper.PolicyFileMapper;
import com.evolunteer.service.AnnouncementService;
import com.evolunteer.service.FileStorageService;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AnnouncementServiceImpl extends ServiceImpl<PolicyAnnouncementMapper, PolicyAnnouncement>
        implements AnnouncementService {

    private static final String ANNOUNCEMENT_ID_PREFIX = "pol_";

    private static final String POLICY_FILE_ID_PREFIX = "pfile_";

    private static final String BUSINESS_ID_FORMAT = "%s%05d";

    @Autowired
    private PolicyFileMapper policyFileMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public IPage<PortalAnnouncementView> page(Integer pageNum, Integer pageSize, String keyword) {
        Page<PortalAnnouncementView> page = PageSupport.of(pageNum, pageSize);
        return baseMapper.selectPageWithFileCount(page, PageSupport.normalizeKeyword(keyword));
    }

    @Override
    @Transactional
    public Map<Object, Object> save(Integer policyannouncementNum, String name, String detail, String adminId) {

        Map<Object, Object> result = new HashMap<>();
        if (name == null || name.trim().isEmpty()) {
            return fail(result, "请填写公告标题");
        }
        if (detail == null || detail.trim().isEmpty()) {
            return fail(result, "请填写公告内容");
        }

        if (policyannouncementNum != null) {
            if (getById(policyannouncementNum) == null) {
                return fail(result, "该通知公告不存在或已被删除");
            }
            PolicyAnnouncement update = new PolicyAnnouncement();
            update.setPolicyannouncementNum(policyannouncementNum);
            update.setPolicyannouncementName(name.trim());
            update.setPolicyannouncementDetail(detail.trim());
            if (baseMapper.updateById(update) <= 0) {
                return fail(result, "公告保存失败，请稍后重试");
            }
            result.put("policyannouncementNum", policyannouncementNum);
            log.info("平台管理员修改通知公告，公告编号：{}", policyannouncementNum);
            return success(result, "公告已保存");
        }

        Integer adminNum = baseMapper.selectAdminNumByAdminId(adminId);
        if (adminNum == null) {
            return fail(result, "未找到平台管理员信息，请重新登录后再试");
        }

        PolicyAnnouncement announcement = new PolicyAnnouncement();
        announcement.setPolicyannouncementName(name.trim());
        announcement.setPolicyannouncementDetail(detail.trim());
        announcement.setAdminNum(adminNum);
        announcement.setPolicyannouncementIsdeleted(0);
        if (baseMapper.insert(announcement) <= 0) {
            return fail(result, "公告保存失败，请稍后重试");
        }

        Integer newNum = announcement.getPolicyannouncementNum();
        String announcementId = businessId(ANNOUNCEMENT_ID_PREFIX, newNum);
        PolicyAnnouncement idUpdate = new PolicyAnnouncement();
        idUpdate.setPolicyannouncementNum(newNum);
        idUpdate.setPolicyannouncementId(announcementId);
        baseMapper.updateById(idUpdate);

        result.put("policyannouncementNum", newNum);
        log.info("平台管理员发布通知公告，公告编号：{}，公告业务编号：{}", newNum, announcementId);
        return success(result, "公告已发布");
    }

    @Override
    @Transactional
    public Map<Object, Object> delete(Integer policyannouncementNum) {

        Map<Object, Object> result = new HashMap<>();
        if (policyannouncementNum == null || getById(policyannouncementNum) == null) {
            return fail(result, "该通知公告不存在或已被删除");
        }

        List<PolicyFile> policyFiles = policyFileMapper.selectByAnnouncementNum(policyannouncementNum);
        for (PolicyFile policyFile : policyFiles) {
            policyFileMapper.deleteById(policyFile.getPolicyfileNum());
            fileStorageService.delete(policyFile.getPolicyfileUniquename());
        }
        removeById(policyannouncementNum);
        log.info("平台管理员删除通知公告，公告编号：{}，同时清理附件数量：{}", policyannouncementNum, policyFiles.size());
        return success(result, "公告已删除");
    }

    @Override
    @Transactional
    public PolicyFile uploadFile(Integer policyannouncementNum, MultipartFile file) {

        if (policyannouncementNum == null || getById(policyannouncementNum) == null) {
            throw new IllegalArgumentException("该通知公告不存在或已被删除");
        }

        String storedName = fileStorageService.store(file);
        try {
            String originalName = file.getOriginalFilename();
            PolicyFile policyFile = new PolicyFile();
            policyFile.setPolicyannouncementNum(policyannouncementNum);
            policyFile.setPolicyfileName(originalName == null || originalName.trim().isEmpty()
                    ? storedName : originalName.trim());
            policyFile.setPolicyfileRoute(fileStorageService.routeOf(storedName));
            policyFile.setPolicyfileUniquename(storedName);
            policyFile.setPolicyfileIsdeleted("0");
            policyFileMapper.insert(policyFile);

            Integer newNum = policyFile.getPolicyfileNum();
            String policyFileId = businessId(POLICY_FILE_ID_PREFIX, newNum);
            PolicyFile idUpdate = new PolicyFile();
            idUpdate.setPolicyfileNum(newNum);
            idUpdate.setPolicyfileId(policyFileId);
            policyFileMapper.updateById(idUpdate);

            policyFile.setPolicyfileId(policyFileId);
            log.info("公告附件已上传，公告编号：{}，附件编号：{}，存储名：{}",
                    policyannouncementNum, newNum, storedName);
            return policyFile;
        } catch (RuntimeException e) {

            fileStorageService.delete(storedName);
            log.error("公告附件写入失败，公告编号：{}，存储名：{}", policyannouncementNum, storedName, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public boolean deleteFile(Integer policyfileNum) {

        PolicyFile policyFile = policyfileNum == null ? null : policyFileMapper.selectById(policyfileNum);
        if (policyFile == null) {
            return false;
        }
        policyFileMapper.deleteById(policyfileNum);
        fileStorageService.delete(policyFile.getPolicyfileUniquename());
        log.info("公告附件已删除，附件编号：{}，存储名：{}", policyfileNum, policyFile.getPolicyfileUniquename());
        return true;
    }

    @Override
    public List<PolicyFile> listFiles(Integer policyannouncementNum) {
        return policyFileMapper.selectByAnnouncementNum(policyannouncementNum);
    }

    private String businessId(String prefix, Integer num) {
        return String.format(BUSINESS_ID_FORMAT, prefix, num);
    }

    private Map<Object, Object> fail(Map<Object, Object> result, String msg) {
        result.put("ok", 0);
        result.put("msg", msg);
        return result;
    }

    private Map<Object, Object> success(Map<Object, Object> result, String msg) {
        result.put("ok", 1);
        result.put("msg", msg);
        return result;
    }
}
