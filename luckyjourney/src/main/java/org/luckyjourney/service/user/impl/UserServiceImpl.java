package org.luckyjourney.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.RegisterVO;
import org.luckyjourney.mapper.user.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.luckyjourney.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
}
