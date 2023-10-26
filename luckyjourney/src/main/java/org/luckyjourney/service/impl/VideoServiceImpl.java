package org.luckyjourney.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.luckyjourney.constant.VideoOpenConstant;
import org.luckyjourney.entity.Video;
import org.luckyjourney.entity.vo.PageBean;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.VideoMapper;
import org.luckyjourney.service.VideoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Override
    public R postVideo(Video video) {
        // 设置发布者
        if (!VideoOpenConstant.OPEN.equals(video.getOpen()) && !VideoOpenConstant.PRIVATE.equals(video.getOpen())) {
            throw new IllegalArgumentException("open参数错误！");
        }
        video.setUserId(UserHolder.get());
        if (this.save(video)) {
            return R.ok().data(video.getId());
        }
        return R.error();
    }

    @Override
    public PageBean<Video> getVideoList(Long typeId, BasePage basePage) {
        LambdaQueryWrapper<Video> wrapper = new QueryWrapper<Video>().lambda();
        if (typeId != null) {
            wrapper.eq(Video::getTypeId, typeId);
        }
        return videoListPageQuery(basePage, wrapper);
    }

    private PageBean<Video> videoListPageQuery(BasePage basePage, LambdaQueryWrapper<Video> wrapper) {
        Page<Video> page = new Page<>(basePage.getPage(), basePage.getLimit());
        IPage<Video> videoPage = videoMapper.selectPage(page, wrapper);
        PageBean<Video> pageBean = new PageBean<>();
        List<Video> videoList = videoPage.getRecords();
        //符合条件的视频总数
        pageBean.setTotalCount(videoPage.getTotal());
        //符合条件的视频总页数
        pageBean.setTotalPage((pageBean.getTotalCount() + basePage.getLimit() - 1) / basePage.getLimit());
        //重新设置当前页数的真实视频数量
        basePage.setLimit((long) videoList.size());
        pageBean.setCurPageInfo(basePage);
        //当前页的视频实体
        pageBean.setList(videoList);
        return pageBean;
    }

}
