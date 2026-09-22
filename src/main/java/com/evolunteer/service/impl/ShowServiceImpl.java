package com.evolunteer.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.evolunteer.entity.PortalShowView;
import com.evolunteer.entity.ShowLikeRecord;
import com.evolunteer.entity.VolunteerShow;
import com.evolunteer.entity.VolunteerShowPicture;
import com.evolunteer.enums.AuditActionEnum;
import com.evolunteer.mapper.PortalMapper;
import com.evolunteer.mapper.ShowLikeRecordMapper;
import com.evolunteer.mapper.VolunteerShowMapper;
import com.evolunteer.mapper.VolunteerShowPictureMapper;
import com.evolunteer.service.AuditLogService;
import com.evolunteer.service.FileStorageService;
import com.evolunteer.service.ShowService;
import com.evolunteer.utils.PageSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class ShowServiceImpl implements ShowService {

    @Autowired
    private VolunteerShowMapper volunteerShowMapper;

    private static final String PICTURE_ID_PREFIX = "pic_";

    private static final String BUSINESS_ID_FORMAT = "%s%05d";

    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    @Autowired
    private VolunteerShowPictureMapper volunteerShowPictureMapper;

    @Autowired
    private ShowLikeRecordMapper showLikeRecordMapper;

    @Autowired
    private PortalMapper portalMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public Map<Object, Object> publish(Integer volunteerNum, Integer activityNum, String detail) {

        Map<Object, Object> parameter = new HashMap<>();
        parameter.put("volunteerNum", volunteerNum);
        parameter.put("activityNum", activityNum == null || activityNum <= 0 ? null : activityNum);
        parameter.put("detail", detail);
        volunteerShowMapper.volunteer_publish_show(parameter);

        Map<Object, Object> result = new HashMap<>();
        boolean ok = isOk(parameter.get("ok"));
        String msg = (String) parameter.get("msg");
        Integer showNum = toInteger(parameter.get("showNum"));
        if (showNum != null) {
            result.put("showNum", showNum);
        }
        log.info("志愿者发布志愿秀，志愿者编号：{}，活动编号：{}，结果：{}", volunteerNum, activityNum, msg);
        return ok ? success(result, msg) : fail(result, msg);
    }

    @Override
    @Transactional
    public Map<Object, Object> uploadPicture(Integer volunteerNum, Integer showNum, MultipartFile file) {

        Map<Object, Object> result = new HashMap<>();
        VolunteerShow show = showNum == null ? null : volunteerShowMapper.selectShowByNum(showNum);
        if (show == null) {
            return fail(result, "该志愿秀不存在或已被删除");
        }
        if (!show.getVolunteerNum().equals(volunteerNum)) {
            return fail(result, "只能为自己的志愿秀上传图片");
        }

        String storedName = fileStorageService.store(file);
        try {
            String originalName = file.getOriginalFilename();
            VolunteerShowPicture picture = new VolunteerShowPicture();
            picture.setShowNum(showNum);
            picture.setPictureName(originalName == null || originalName.trim().isEmpty()
                    ? storedName : originalName.trim());
            picture.setPictureRoute(fileStorageService.routeOf(storedName));
            picture.setPictureUniquename(storedName);
            picture.setPictureIsdeleted(0);
            volunteerShowPictureMapper.insert(picture);

            Integer newNum = picture.getPictureNum();
            String pictureId = String.format(BUSINESS_ID_FORMAT, PICTURE_ID_PREFIX, newNum);
            VolunteerShowPicture idUpdate = new VolunteerShowPicture();
            idUpdate.setPictureNum(newNum);
            idUpdate.setPictureId(pictureId);
            volunteerShowPictureMapper.updateById(idUpdate);

            log.info("志愿秀图片已上传，志愿秀编号：{}，图片编号：{}，存储名：{}", showNum, newNum, storedName);
            return success(result, "图片已上传");
        } catch (RuntimeException e) {

            fileStorageService.delete(storedName);
            log.error("志愿秀图片写入失败，志愿秀编号：{}，存储名：{}", showNum, storedName, e);
            throw e;
        }
    }

    @Override
    public IPage<PortalShowView> pageByVolunteer(Integer volunteerNum, Integer pageNum, Integer pageSize) {
        Page<PortalShowView> page = PageSupport.of(pageNum, pageSize);
        return volunteerShowMapper.selectPageByVolunteer(page, volunteerNum);
    }

    @Override
    public IPage<PortalShowView> pagePortal(Integer pageNum, Integer pageSize, String keyword, Integer volunteerNum) {
        Page<PortalShowView> page = PageSupport.of(pageNum, pageSize);
        IPage<PortalShowView> result = portalMapper.selectShowPage(page, PageSupport.normalizeKeyword(keyword));
        markLiked(result.getRecords(), volunteerNum);
        return result;
    }

    @Override
    @Transactional
    public PortalShowView detail(Integer showNum, Integer volunteerNum) {

        PortalShowView show = showNum == null ? null : portalMapper.selectShowByNum(showNum);
        if (show == null) {
            return null;
        }
        show.setPictureRoutes(portalMapper.selectPictureRoutes(showNum));
        markLiked(Collections.singletonList(show), volunteerNum);

        volunteerShowMapper.increaseBrowse(showNum);
        show.setShowBrowse(show.getShowBrowse() == null ? 1 : show.getShowBrowse() + 1);
        return show;
    }

    @Override
    @Transactional
    public Map<Object, Object> like(Integer volunteerNum, Integer showNum) {

        Map<Object, Object> result = new HashMap<>();
        VolunteerShow show = showNum == null ? null : volunteerShowMapper.selectShowByNum(showNum);
        if (show == null) {
            return fail(result, "该志愿秀不存在或已被删除");
        }

        ShowLikeRecord existing = selectLikeRecord(showNum, volunteerNum);
        boolean liked;
        if (existing == null) {
            ShowLikeRecord record = new ShowLikeRecord();
            record.setShowNum(showNum);
            record.setVolunteerNum(volunteerNum);
            try {
                showLikeRecordMapper.insert(record);
                volunteerShowMapper.increaseLike(showNum);
            } catch (DuplicateKeyException e) {

                log.warn("志愿者重复提交点赞已忽略，志愿秀编号：{}，志愿者编号：{}", showNum, volunteerNum);
            }
            liked = true;
        } else {
            showLikeRecordMapper.deleteById(existing.getLikeNum());

            volunteerShowMapper.decreaseLike(showNum);
            liked = false;
        }

        volunteerShowMapper.refreshLikeCount(showNum);
        VolunteerShow latest = volunteerShowMapper.selectShowByNum(showNum);
        Integer likeCount = latest == null || latest.getShowLike() == null ? 0 : latest.getShowLike();

        result.put("liked", liked);
        result.put("likeCount", likeCount);
        log.info("志愿者{}志愿秀，志愿秀编号：{}，志愿者编号：{}，最新点赞次数：{}",
                liked ? "点赞" : "取消点赞", showNum, volunteerNum, likeCount);
        return success(result, liked ? "点赞成功" : "已取消点赞");
    }

    @Override
    public IPage<PortalShowView> pageAdmin(Integer pageNum, Integer pageSize, String keyword) {
        Page<PortalShowView> page = PageSupport.of(pageNum, pageSize);
        return portalMapper.selectShowPage(page, PageSupport.normalizeKeyword(keyword));
    }

    @Override
    @Transactional
    public Map<Object, Object> deleteShow(Integer showNum, String operator, String ip) {

        Map<Object, Object> result = new HashMap<>();
        VolunteerShow show = showNum == null ? null : volunteerShowMapper.selectShowByNum(showNum);
        if (show == null) {
            return fail(result, "该志愿秀不存在或已被删除");
        }
        if (volunteerShowMapper.logicDeleteByShowNum(showNum) <= 0) {
            return fail(result, "志愿秀删除失败，请稍后重试");
        }

        auditLogService.record(operator, ADMIN_ROLE, AuditActionEnum.SHOW_DELETE,
                "志愿秀编号：" + showNum, "删除志愿秀，分享内容：" + show.getShowDetail(), ip);
        log.info("平台管理员删除志愿秀，志愿秀编号：{}，操作账号：{}", showNum, operator);
        return success(result, "志愿秀已删除");
    }

    private ShowLikeRecord selectLikeRecord(Integer showNum, Integer volunteerNum) {
        List<ShowLikeRecord> records = showLikeRecordMapper.selectList(
                Wrappers.<ShowLikeRecord>lambdaQuery()
                        .eq(ShowLikeRecord::getShowNum, showNum)
                        .eq(ShowLikeRecord::getVolunteerNum, volunteerNum));
        return records.isEmpty() ? null : records.get(0);
    }

    private void markLiked(List<PortalShowView> shows, Integer volunteerNum) {

        if (shows == null || shows.isEmpty()) {
            return;
        }
        if (volunteerNum == null) {
            for (PortalShowView show : shows) {
                show.setLiked(false);
            }
            return;
        }

        List<Integer> showNums = new ArrayList<>();
        for (PortalShowView show : shows) {
            showNums.add(show.getShowNum());
        }
        Set<Integer> likedShowNums = new HashSet<>(portalMapper.selectLikedShowNums(volunteerNum, showNums));
        for (PortalShowView show : shows) {
            show.setLiked(likedShowNums.contains(show.getShowNum()));
        }
    }

    private boolean isOk(Object ok) {
        return ok instanceof Number && ((Number) ok).intValue() == 1;
    }

    private Integer toInteger(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
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
