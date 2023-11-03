package org.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.entity.user.Follow;
import org.luckyjourney.entity.vo.BasePage;
import org.luckyjourney.entity.vo.FollowVO;
import org.luckyjourney.mapper.FollowMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.service.user.FollowService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-10-25
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    @Override
    public int getFollowCount(Long userId) {
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId));
    }

    @Override
    public int getFansCount(Long userId) {
        return count(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId));
    }

    @Override
    public Collection<Long> getFollow(Long userId, BasePage basePage) {
        final List<Follow> list = list(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId).orderByDesc(Follow::getGmtCreated));
        final List<Long> followIds = list.stream().skip((basePage.getPage() - 1) * basePage.getLimit()).limit(basePage.getLimit()).map(Follow::getFollowId).collect(Collectors.toList());
        return followIds;
    }

    @Override
    public Collection<Long> getFollow(Long userId) {
        final List<Long> ids = list(new LambdaQueryWrapper<Follow>().eq(Follow::getUserId, userId).select(Follow::getFollowId)).stream().map(Follow::getFollowId).collect(Collectors.toList());
        return ids;
    }

    @Override
    public Collection<Long> getFans(Long userId, BasePage basePage) {
        final List<Follow> list = list(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId, userId).orderByDesc(Follow::getGmtCreated));
        final List<Long> followIds = list.stream().skip((basePage.getPage() - 1) * basePage.getLimit()).limit(basePage.getLimit()).map(Follow::getUserId).collect(Collectors.toList());
        return followIds;
    }

    @Override
    public Boolean follows(Long followsId, Long userId) {

        if (followsId.equals(userId)){
            throw new IllegalArgumentException("你不能关注自己");
        }

        // 直接保存(唯一索引),保存失败则删除
        final Follow follow = new Follow();
        follow.setFollowId(followsId);
        follow.setUserId(userId);
        try {
            save(follow);
        }catch (Exception e){
            // 删除
            remove(new LambdaQueryWrapper<Follow>().eq(Follow::getFollowId,followsId).eq(Follow::getUserId,userId));
            return false;
        }

        return true;
    }
}
