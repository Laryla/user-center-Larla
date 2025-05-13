package com.larly.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.larly.usercenter.common.ErrorCode;
import com.larly.usercenter.contact.TeamStausEnum;
import com.larly.usercenter.exception.BusinessException;
import com.larly.usercenter.model.domain.Team;
import com.larly.usercenter.model.domain.User;
import com.larly.usercenter.model.domain.UserTeam;
import com.larly.usercenter.service.TeamService;
import com.larly.usercenter.mapper.TeamMapper;
import com.larly.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.OpenOption;
import java.util.Date;
import java.util.Optional;

/**
* @author 许颢达
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2025-05-13 13:45:03
*/
@Service
//因为涉及到多表查询,要设置事务保证数据的统一和完整
@Transactional(rollbackFor = Exception.class)
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{

    @Autowired
    private UserTeamService userTeamService;

    @Override
    public Long addTeam(Team team, User userLogin) {
//        1.请求参数是否为空
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        2.是否登录,未登录不允许创建
        if(userLogin == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
//        3.校验信息
//          队伍人数<1 && <=20
//        这个Integer是包装类可能为null,Optional.ofNullable判断一下,给默认值
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum < 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
//          队伍标题 <= 20
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length() >= 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题不满足要求");
        }
//          描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length() >= 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"描述不满足要求,请输入小于512字");
        }
//          status是否公开,默认是0(公开)
        Integer status = team.getStatus();
        String password = team.getPassword();
//          如果加密.必选有密码,密码<=32
        if(TeamStausEnum.Encryption.equals(TeamStausEnum.getTextByKey(status))){
            if(StringUtils.isBlank(password) && password.length() >= 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"面膜不满足要求");
            }
        }
//          超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
//        获取当前时间
        Date currentData = new Date();
        if(currentData.after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请正确设置超时时间");
        }
//          每个用户最多创建5个队伍
        Long userId = userLogin.getId();
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        long count = this.count(queryWrapper);
        if(count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"当前你已经无法创建队伍,你当前已创建5个");
        }
//        4.插入队伍=>队伍表
//        保存哪个用户的队伍
        team.setUserId(userId);
        if(!this.save(team)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"插入失败");
        }
        Long teamId = team.getId();
//        5.插入用户=>关联用户队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setTeamId(teamId);
        userTeam.setUserId(userId);

        if(!userTeamService.save(userTeam)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"插入失败");
        }

//        返回队伍id
        return teamId;
    }
}




