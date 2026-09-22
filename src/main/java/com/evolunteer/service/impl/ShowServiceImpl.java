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

/**
 * E志愿志愿者服务平台 V1.0
 * <p>
 * 志愿秀业务实现类：志愿者发布志愿秀通过数据库存储过程完成，关联活动时由存储过程校验报名关系；
 * 图片上传先落盘再写库，写库失败会清理已落盘文件；点赞按志愿者去重，同一志愿者对同一志愿秀
 * 只保留一条点赞记录，点赞次数与点赞记录行数保持一致且不小于 0。
 */
@Slf4j
@Service
public class ShowServiceImpl implements ShowService {

    @Autowired
    private VolunteerShowMapper volunteerShowMapper;

    /**
     * 图片业务编号前缀
     */
    private static final String PICTURE_ID_PREFIX = "pic_";

    /**
     * 业务编号格式：前缀 + 五位自增编号
     */
    private static final String BUSINESS_ID_FORMAT = "%s%05d";

    /**
     * 平台管理员角色编码，用于记录志愿秀删除的操作审计
     */
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

    /**
     * 志愿者发布志愿秀，关联活动时只能是本人已通过报名的活动
     *
     * @param volunteerNum 志愿者编号
     * @param activityNum  关联活动编号，不关联活动时传 null
     * @param detail       分享内容
     * @return 处理结果，包含提示信息 msg、处理标记 ok 与志愿秀编号 showNum
     */
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

    /**
     * 为本人发布的志愿秀上传图片
     *
     * @param volunteerNum 志愿者编号
     * @param showNum      志愿秀编号
     * @param file         上传图片
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
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

            // 图片业务编号依赖自增主键，插入成功后按主键回写
            Integer newNum = picture.getPictureNum();
            String pictureId = String.format(BUSINESS_ID_FORMAT, PICTURE_ID_PREFIX, newNum);
            VolunteerShowPicture idUpdate = new VolunteerShowPicture();
            idUpdate.setPictureNum(newNum);
            idUpdate.setPictureId(pictureId);
            volunteerShowPictureMapper.updateById(idUpdate);

            log.info("志愿秀图片已上传，志愿秀编号：{}，图片编号：{}，存储名：{}", showNum, newNum, storedName);
            return success(result, "图片已上传");
        } catch (RuntimeException e) {
            // 写库失败时回滚事务，同时清理已经落盘的文件，保证记录与文件一致
            fileStorageService.delete(storedName);
            log.error("志愿秀图片写入失败，志愿秀编号：{}，存储名：{}", showNum, storedName, e);
            throw e;
        }
    }

    /**
     * 分页查询志愿者本人发布的志愿秀
     *
     * @param volunteerNum 志愿者编号
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @return 志愿秀分页结果
     */
    @Override
    public IPage<PortalShowView> pageByVolunteer(Integer volunteerNum, Integer pageNum, Integer pageSize) {
        Page<PortalShowView> page = PageSupport.of(pageNum, pageSize);
        return volunteerShowMapper.selectPageByVolunteer(page, volunteerNum);
    }

    /**
     * 分页查询门户志愿秀，附带分享志愿者姓名、关联活动名称与首图路径
     *
     * @param pageNum      页码
     * @param pageSize     每页条数
     * @param keyword      关键字，按分享内容与分享志愿者姓名模糊匹配，可为空
     * @param volunteerNum 当前访问志愿者编号，未登录时传 null，仅用于标记是否已点赞
     * @return 志愿秀分页结果
     */
    @Override
    public IPage<PortalShowView> pagePortal(Integer pageNum, Integer pageSize, String keyword, Integer volunteerNum) {
        Page<PortalShowView> page = PageSupport.of(pageNum, pageSize);
        IPage<PortalShowView> result = portalMapper.selectShowPage(page, PageSupport.normalizeKeyword(keyword));
        markLiked(result.getRecords(), volunteerNum);
        return result;
    }

    /**
     * 查询志愿秀详情并累加浏览次数
     *
     * @param showNum      志愿秀编号
     * @param volunteerNum 当前访问志愿者编号，未登录时传 null，仅用于标记是否已点赞
     * @return 志愿秀详情，志愿秀不存在或已删除时返回 null
     */
    @Override
    @Transactional
    public PortalShowView detail(Integer showNum, Integer volunteerNum) {

        PortalShowView show = showNum == null ? null : portalMapper.selectShowByNum(showNum);
        if (show == null) {
            return null;
        }
        show.setPictureRoutes(portalMapper.selectPictureRoutes(showNum));
        markLiked(Collections.singletonList(show), volunteerNum);

        // 浏览次数在查询详情时累加，返回给页面的次数与数据库保持一致
        volunteerShowMapper.increaseBrowse(showNum);
        show.setShowBrowse(show.getShowBrowse() == null ? 1 : show.getShowBrowse() + 1);
        return show;
    }

    /**
     * 志愿者点赞或取消点赞：已点赞则取消，未点赞则点赞，同一志愿者对同一志愿秀只计一次
     *
     * @param volunteerNum 志愿者编号
     * @param showNum      志愿秀编号
     * @return 处理结果，包含提示信息 msg、处理标记 ok、是否已点赞 liked 与最新点赞次数 likeCount
     */
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
                // 同一志愿者并发提交时由点赞记录唯一索引兜底，重复提交按已点赞处理
                log.warn("志愿者重复提交点赞已忽略，志愿秀编号：{}，志愿者编号：{}", showNum, volunteerNum);
            }
            liked = true;
        } else {
            showLikeRecordMapper.deleteById(existing.getLikeNum());
            // 取消点赞时递减点赞次数，递减条件限制次数大于 0，避免出现负数
            volunteerShowMapper.decreaseLike(showNum);
            liked = false;
        }

        // 以点赞记录行数重新核算点赞次数，保证点赞次数与点赞记录始终一致且不小于 0
        volunteerShowMapper.refreshLikeCount(showNum);
        VolunteerShow latest = volunteerShowMapper.selectShowByNum(showNum);
        Integer likeCount = latest == null || latest.getShowLike() == null ? 0 : latest.getShowLike();

        result.put("liked", liked);
        result.put("likeCount", likeCount);
        log.info("志愿者{}志愿秀，志愿秀编号：{}，志愿者编号：{}，最新点赞次数：{}",
                liked ? "点赞" : "取消点赞", showNum, volunteerNum, likeCount);
        return success(result, liked ? "点赞成功" : "已取消点赞");
    }

    /**
     * 平台管理员分页查询志愿秀
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param keyword  关键字，按分享内容与分享志愿者姓名模糊匹配，可为空
     * @return 志愿秀分页结果
     */
    @Override
    public IPage<PortalShowView> pageAdmin(Integer pageNum, Integer pageSize, String keyword) {
        Page<PortalShowView> page = PageSupport.of(pageNum, pageSize);
        return portalMapper.selectShowPage(page, PageSupport.normalizeKeyword(keyword));
    }

    /**
     * 平台管理员逻辑删除志愿秀并记录操作审计
     *
     * @param showNum  志愿秀编号
     * @param operator 操作管理员登录账号
     * @param ip       操作来源 IP
     * @return 处理结果，包含提示信息 msg 与处理标记 ok
     */
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

    /**
     * 查询志愿者对指定志愿秀的点赞记录
     */
    private ShowLikeRecord selectLikeRecord(Integer showNum, Integer volunteerNum) {
        List<ShowLikeRecord> records = showLikeRecordMapper.selectList(
                Wrappers.<ShowLikeRecord>lambdaQuery()
                        .eq(ShowLikeRecord::getShowNum, showNum)
                        .eq(ShowLikeRecord::getVolunteerNum, volunteerNum));
        return records.isEmpty() ? null : records.get(0);
    }

    /**
     * 标记志愿秀列表中当前访问志愿者是否已点赞，未登录时统一标记为未点赞
     */
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

    /**
     * 判断存储过程返回的处理结果是否成功
     */
    private boolean isOk(Object ok) {
        return ok instanceof Number && ((Number) ok).intValue() == 1;
    }

    /**
     * 取存储过程输出参数中的编号，非数字时返回 null
     */
    private Integer toInteger(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
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
