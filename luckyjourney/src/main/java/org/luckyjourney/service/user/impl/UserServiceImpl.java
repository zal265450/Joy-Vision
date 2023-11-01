package org.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.user.Favorites;
import org.luckyjourney.entity.user.Follow;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.*;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.user.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.user.FavoritesService;
import org.luckyjourney.service.user.FollowService;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.validation.constraints.NotBlank;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Autowired
    private FollowService followService;

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Autowired
    private InterestPushService interestPushService;

    @Autowired
    private FavoritesService favoritesService;

    @Override
    public boolean register(RegisterVO registerVO) throws Exception {

        // 邮箱是否存在
        final int count = count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerVO.getEmail()));
        if (count == 1){
            throw new Exception("邮箱已被注册");
        }
        final String code = registerVO.getCode();
        final Object o = redisCacheUtil.get(RedisConstant.EMAIL_CODE + code);
        if (o == null){
            throw new IllegalArgumentException("验证码为空");
        }
        final User user = new User();
        user.setNickName(UUID.randomUUID().toString().substring(0,10));
        user.setEmail(registerVO.getEmail());
        user.setPassword(registerVO.getPassword());

        save(user);

        // 创建默认收藏夹
        final Favorites favorites = new Favorites();
        favorites.setUserId(user.getId());
        favorites.setName("默认收藏夹");
        favoritesService.save(favorites);

        // 这里如果单独抽出一个用户配置表就好了,但是没有必要再搞个表
        user.setDefaultFavoritesId(favorites.getId());
        updateById(user);
        return true;
    }

    @Override
    public UserVO getInfo(Long userId){

        final User user = getById(userId);
        if (ObjectUtils.isEmpty(user)){
            return new UserVO();
        }
        final UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);

        // 查出关注数量
        final long followCount = followService.getFollowCount(userId);

        // 查出粉丝数量
        final long fansCount = followService.getFansCount(userId);
        userVO.setFollow(followCount);
        userVO.setFans(fansCount);
        return userVO;
    }

    @Override
    public void setModel(ModelVO modelVO) {
        final Long userId = UserHolder.get();
        final User user = getById(userId);
        // 初始化模型
        interestPushService.initUserModel(userId,modelVO.getVideoTypes());
        user.setSex(modelVO.getSex());
        updateById(user);
    }

    @Override
    public List<User> getFollows(Long userId, BasePage basePage) {

        final List<Long> followIds = followService.getFollow(userId, basePage);
        return getUsers(followIds);
    }

    @Override
    public List<User> getFans(Long userId, BasePage basePage) {
        final List<Long> fansIds = followService.getFans(userId, basePage);
        return getUsers(fansIds);
    }

    @Override
    public List<User> list(Collection<Long> userIds) {
        return list(new LambdaQueryWrapper<User>().in(User::getId,userIds).select(User::getId,User::getNickName,User::getSex));
    }


    public List<User> getUsers(List<Long> ids){
        final Map<Long, String> userMap = listByIds(ids).stream().collect(Collectors.toMap(User::getId, User::getNickName));
        List<User> result = new ArrayList<>();
        for (Long followId : ids) {
            final User user = new User();
            user.setId(followId);
            user.setNickName(userMap.get(followId));
            result.add(user);
        }
        return result;
    }
}
