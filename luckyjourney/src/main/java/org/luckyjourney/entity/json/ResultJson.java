package org.luckyjourney.entity.json;

import lombok.Data;
import lombok.ToString;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 02:14
 */
@Data
@ToString
public class ResultJson {
    Integer code;
    String message;
    ResultChildJson result;
}


