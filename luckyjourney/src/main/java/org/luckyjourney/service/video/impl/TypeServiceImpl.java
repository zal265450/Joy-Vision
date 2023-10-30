package org.luckyjourney.service.video.impl;

import org.luckyjourney.entity.video.Type;
import org.luckyjourney.mapper.video.TypeMapper;
import org.luckyjourney.service.video.TypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author xhy
 * @since 2023-10-24
 */
@Service
public class TypeServiceImpl extends ServiceImpl<TypeMapper, Type> implements TypeService {

}
