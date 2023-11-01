package org.luckyjourney.mapper.video;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.luckyjourney.entity.video.Video;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@Repository
public interface VideoMapper extends BaseMapper<Video> {

    @Select("SELECT id,share_count,history_count,start_count,favorites_count,gmt_created,title FROM video WHERE id > #{id} and DATEDIFF(gmt_created,NOW())<=0 AND DATEDIFF(gmt_created,NOW())>- #{days} limit #{limit}")
    List<Video> selectNDaysAgeVideo(@Param("id") long id, @Param("days") int days, @Param("limit") int limit);
}
