package org.luckyjourney.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-31 01:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotVideo implements Comparable<HotVideo>, Serializable {

    private static final long serialVersionUID = 1L;



    Double hot;

    Long videoId;

    String title;

    @Override
    public int compareTo(HotVideo o) {
        return (int)o.getHot().doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HotVideo hotVideo = (HotVideo) o;
        return Objects.equals(videoId, hotVideo.videoId) &&
                Objects.equals(title, hotVideo.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoId, title);
    }
}
