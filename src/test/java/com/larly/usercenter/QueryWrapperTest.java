package com.larly.usercenter;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.larly.usercenter.common.ErrorCode;
import com.larly.usercenter.exception.BusinessException;
import com.larly.usercenter.model.domain.UserTeam;
import com.larly.usercenter.service.UserTeamService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class QueryWrapperTest {

    @Resource
    private UserTeamService userTeamService;

    @Test
    public void Test(){

//        用户已加入该队
//        SELECT COUNT( * ) FROM user_team WHERE is_delete=0 AND (user_id = ? AND team_id = ?)
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("user_id",2).eq("team_id",64);
        long count = userTeamService.count(userTeamQueryWrapper);
        System.out.println("你已经加入该队伍队伍" + count);

//        查询该队伍的人数
//        SELECT COUNT( * ) FROM user_team WHERE is_delete=0 AND (user_id = ? AND team_id = ? AND team_id = ?)
        userTeamQueryWrapper.eq("team_id",64);
        System.out.println(userTeamService.count(userTeamQueryWrapper));
//      不能使用同时个Wrapper会重复

    }

}
