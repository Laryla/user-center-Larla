package com.larly.usercenter.service;

import com.larly.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.larly.usercenter.model.domain.User;
import com.larly.usercenter.model.dto.TeamQuery;
import com.larly.usercenter.model.vo.TeamUpdateVo;
import com.larly.usercenter.model.vo.TeamUserVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 许颢达
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2025-05-13 13:45:03
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param team 队伍信息
     * @param userLogin 验证用户是否登录
     * @return 队伍ID
     */
    Long addTeam(Team team, User userLogin);

    /**
     * 查询队伍信息
     * @param teamQuery
     * @param userLogin
     * @return
     */
    List<TeamUserVo> listTeams (TeamQuery teamQuery, User userLogin);

    /**
     * 更新队伍信息
     * @param teamUpdateVo
     * @param userLogin
     * @return
     */
    Boolean updateTeam(TeamUpdateVo teamUpdateVo, User userLogin);
}
