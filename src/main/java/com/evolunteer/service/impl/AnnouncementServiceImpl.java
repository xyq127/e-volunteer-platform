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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 通知公告业务实现类：平台管理员发布与删除通知公告、上传与删除公告附件均在本类完成，
 * 公告业务编号按“前缀 + 五位自增编号”生成，附件删除时同时清理附件记录与物理文件，
 * 保证附件记录与服务器文件目录始终一致。
 */
@Slf4j
@Service
public class AnnouncementServiceImpl extends ServiceImpl<PolicyAnnouncementMapper, PolicyAnnouncement>
        implements AnnouncementService {

    /**
     * 公告业务编号前缀
     */
    private static final String ANNOUNCEMENT_ID_PREFIX = "pol_";

    /**
     * 附件业务编号前缀
     */
    private static final String POLICY_FILE_ID_PREFIX = "pfile_";

    /**
     * 业务编号格式：前缀 + 五位自增编号
     */
    private static final String BUSINESS_ID_FORMAT = "%s%05d";

    @Autowired
    private PolicyFileMapper policyFileMapper;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 分页查询通知公告，每条公告附带附件数量
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  关键字，按公告标题与公告内容模糊匹配，可为空
     * @return 通知公告分页结果
     */
    @Override
    public IPage<PortalAnnouncementView> page(Integer pageNum, Integer pageSize, String keyword) {
        Page<PortalAnnouncementView> page = PageSupport.of(pageNum, pageSize);
        return baseMapper.selectPageWithFileCount(page, PageSupport.normalizeKeyword(keyword));
    }

    /**
     * 保存通知公告：编号为空时新增并生成公告业务编号，否则更新公告标题与内容
     *
     * @param policyannouncementNum 公告编号，新增时传 null
     * @param name                  公告标题
     * @param detail                公告内容
     * @param adminId               发布公告的管理员登录账号
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与公告编号 policyannouncementNum
     */
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

        // 公告业务编号依赖自增主键，插入成功后按主键回写，保证编号唯一且与主键对应
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

    /**
     * 逻辑删除通知公告，并同步清理公告附件记录与物理文件
     *
     * @param policyannouncementNum 公告编号
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
    @Override
    @Transactional
    public Map<Object, Object> delete(Integer policyannouncementNum) {

        Map<Object, Object> result = new HashMap<>();
        if (policyannouncementNum == null || getById(policyannouncementNum) == null) {
            return fail(result, "该通知公告不存在或已被删除");
        }

        // 公告被删除后附件不再可用，附件记录与物理文件一并清理，避免残留占用存储
        List<PolicyFile> policyFiles = policyFileMapper.selectByAnnouncementNum(policyannouncementNum);
        for (PolicyFile policyFile : policyFiles) {
            policyFileMapper.deleteById(policyFile.getPolicyfileNum());
            fileStorageService.delete(policyFile.getPolicyfileUniquename());
        }
        removeById(policyannouncementNum);
        log.info("平台管理员删除通知公告，公告编号：{}，同时清理附件数量：{}", policyannouncementNum, policyFiles.size());
        return success(result, "公告已删除");
    }

    /**
     * 上传公告附件：文件保存到服务器文件目录并写入公告附件表
     *
     * @param policyannouncementNum 公告编号
     * @param file                  上传文件
     * @return 已保存的公告附件信息
     */
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

            // 附件业务编号依赖自增主键，插入成功后按主键回写
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
            // 写库失败时回滚事务，同时清理已经落盘的文件，保证记录与文件一致
            fileStorageService.delete(storedName);
            log.error("公告附件写入失败，公告编号：{}，存储名：{}", policyannouncementNum, storedName, e);
            throw e;
        }
    }

    /**
     * 删除公告附件：同时逻辑删除附件记录并删除服务器上的物理文件
     *
     * @param policyfileNum 附件编号
     * @return 附件不存在时返回 false，删除成功返回 true
     */
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

    /**
     * 按公告查询附件列表
     *
     * @param policyannouncementNum 公告编号
     * @return 附件列表
     */
    @Override
    public List<PolicyFile> listFiles(Integer policyannouncementNum) {
        return policyFileMapper.selectByAnnouncementNum(policyannouncementNum);
    }

    /**
     * 按“前缀 + 五位自增编号”生成业务编号
     */
    private String businessId(String prefix, Integer num) {
        return String.format(BUSINESS_ID_FORMAT, prefix, num);
    }

    /**
     * 组装处理失败的返回结果
     */
    private Map<Object, Object> fail(Map<Object, Object> result, String msg) {
        result.put("ok", 0);
        result.put("msg", msg);
        return result;
    }

    /**
     * 组装处理成功的返回结果
     */
    private Map<Object, Object> success(Map<Object, Object> result, String msg) {
        result.put("ok", 1);
        result.put("msg", msg);
        return result;
    }
}
