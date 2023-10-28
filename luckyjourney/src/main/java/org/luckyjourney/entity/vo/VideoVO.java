package org.luckyjourney.entity.vo;

import lombok.Data;
import org.luckyjourney.entity.Video;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-27 15:39
 */
@Data
public class VideoVO extends Video {

    // 浏览量
    private Long PV;

    // 点赞量
    private Long stars;

    // 分享量
    private Long shares;
}
