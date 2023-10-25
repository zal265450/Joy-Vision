package org.luckyjourney.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import org.luckyjourney.entity.user.User;
import org.luckyjourney.entity.vo.RegisterVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
public interface UserService extends IService<User> {

    boolean register(RegisterVO registerVO) throws Exception;
}
