package org.luckyjourney.entity.json;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-29 14:00
 */
@Data
@ToString
public class TypeJson {
    String suggestion;
    List<CutsJson> cuts;
}
