package org.luckyjourney.entity.json;

import lombok.Data;
import lombok.ToString;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 14:05
 */
@Data
@ToString
public class DetailsJson {
    Double score;
    String suggestion;
    String label;
    String group;
}
