package org.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.constant.RedisConstant;
import org.luckyjourney.entity.user.Follow;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.*;
import org.luckyjourney.holder.UserHolder;
import org.luckyjourney.mapper.user.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.service.InterestPushService;
import org.luckyjourney.service.user.FollowService;
import org.luckyjourney.service.user.UserService;
import org.luckyjourney.util.RedisCacheUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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

    @Override
    public boolean register(RegisterVO registerVO) throws Exception {

        // 邮箱是否存在
        final int count = count(new LambdaQueryWrapper<User>().eq(User::getEmail, registerVO.getEmail()));
        if (count == 1){
            throw new Exception("邮箱已被注册");
        }
        // todo 从缓存中对比
        registerVO.getCode();
        final User user = new User();
        user.setNickName(UUID.randomUUID().toString().substring(0,10));
        user.setEmail(registerVO.getEmail());
        user.setPassword(registerVO.getPassword());
        save(user);
        return true;
    }

    @Override
    public UserVO getInfo(Long userId) throws Exception {

        final User user = getById(userId);
        if (ObjectUtils.isEmpty(user)){
            throw new Exception("userId 为空");
        }
        final UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        userVO.setPassword("");

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
