package org.luckyjourney.entity.vo;

import lombok.Data;
import org.luckyjourney.entity.user.User;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-25 15:40
 */
@Data
public class UserVO{

    private String nickName;

    private Boolean sex;

    private String description;

    private Long follow;

    private Long fans;
}
