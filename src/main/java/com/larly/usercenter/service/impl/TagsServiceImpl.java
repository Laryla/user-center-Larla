package com.larly.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.larly.usercenter.model.domain.Tags;
import com.larly.usercenter.service.TagsService;
import com.larly.usercenter.mapper.TagsMapper;
import org.springframework.stereotype.Service;

/**
* @author 许颢达
* @description 针对表【tags(标签)】的数据库操作Service实现
* @createDate 2025-05-08 17:41:10
*/
@Service
public class TagsServiceImpl extends ServiceImpl<TagsMapper, Tags>
    implements TagsService{

}




