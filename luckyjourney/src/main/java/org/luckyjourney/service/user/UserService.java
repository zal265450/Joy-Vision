package org.luckyjourney.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.*;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
public interface UserService extends IService<User> {

    /**
     * 注册
     * @param registerVO
     * @return
     * @throws Exception
     */
    boolean register(RegisterVO registerVO) throws Exception;

    /**
     * 获取用户信息:
     * 1.用户基本信息
     * 2.关注数量
     * 3.粉丝数量
     * @param userId 用户id
     * @return
     */
    UserVO getInfo(Long userId);

    /**
     * 填写用户模型
     * @param modelVO
     */
    void setModel(ModelVO modelVO);

    /**
     * 获取关注
     * @param userId
     * @param basePage
     * @return
     */
    List<User> getFollows(Long userId, BasePage basePage);

    /**
     * 获取粉丝
     * @param userId
     * @param basePage
     * @return
     */
    List<User> getFans(Long userId, BasePage basePage);
}
