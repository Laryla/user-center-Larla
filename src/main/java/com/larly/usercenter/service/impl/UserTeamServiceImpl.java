package com.larly.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.larly.usercenter.model.domain.UserTeam;
import com.larly.usercenter.service.UserTeamService;
import com.larly.usercenter.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author 许颢达
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2025-05-13 13:45:35
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




