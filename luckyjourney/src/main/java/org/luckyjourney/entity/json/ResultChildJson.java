package org.luckyjourney.entity.json;

import lombok.Data;
import lombok.ToString;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 02:28
 */
@Data
@ToString
public class ResultChildJson {
    String suggestion;
    ScenesJson scenes;
}
