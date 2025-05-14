package com.larly.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.larly.usercenter.common.ErrorCode;
import com.larly.usercenter.contact.TeamStausEnum;
import com.larly.usercenter.exception.BusinessException;
import com.larly.usercenter.model.domain.Team;
import com.larly.usercenter.model.domain.User;
import com.larly.usercenter.model.domain.UserTeam;
import com.larly.usercenter.model.dto.TeamQuery;
import com.larly.usercenter.model.response.UserResult;
import com.larly.usercenter.model.vo.TeamUpdateVo;
import com.larly.usercenter.model.vo.TeamUserVo;
import com.larly.usercenter.service.TeamService;
import com.larly.usercenter.mapper.TeamMapper;
import com.larly.usercenter.service.UserService;
import com.larly.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;

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

    @Autowired
    private UserService userService;

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

    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, User userLogin) {
//        todo 校验查询
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null){
//            根据队伍id查询
            Long id = teamQuery.getId();
            if(id != null && id <= 0){
                queryWrapper.eq("id",id);
            }
//            根据标题
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
//            根据描述
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                queryWrapper.like("description",description);
            }
//            根据最大人数
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0){
                queryWrapper.eq("max_num",maxNum);
            }
//            根据用户ID
            Long userId = teamQuery.getUserId();
            if(userId != null && userId > 0 ){
                queryWrapper.eq("user_id",userId);
            }
//            根据状态
            Integer status = teamQuery.getStatus();
            String key = TeamStausEnum.getTextByKey(status);
//            默认查询公开和加密的队伍
            if(status == null){
                queryWrapper.and(i -> i.eq("status",TeamStausEnum.PUBLIC.getKey()).or().eq("status",TeamStausEnum.Encryption.getKey()));
            }
            if(userService.isAdmin(userLogin) || key.equals(TeamStausEnum.PUBLIC) ){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            queryWrapper.eq("status",key);
        }
//        不展示已过期的队伍信息（必须）
//        如果没有指定就默认全部
//        sql :  expire_time > now() or expire_time is null
        queryWrapper.and(i -> i.gt("expire_time",new Date()).or().isNull("expire_time"));

//        查询
        List<Team> teamList = this.list(queryWrapper);
        if(teamList.isEmpty()){
            return new ArrayList<>();
        }
//        关联查询(查询创建人信息)
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        for (Team team : teamList) {
            TeamUserVo  teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team,teamUserVo);
//            获取创建队伍的用户信息
            Long userId = team.getUserId();
            User user= userService.getById(userId);
            if(user != null){
                UserResult userResult = new UserResult();
                BeanUtils.copyProperties(user,userResult);
                teamUserVo.setCreateUser(userResult);
            }
            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;
    }

    @Override
    public Boolean updateTeam(TeamUpdateVo teamUpdateVo, User userLogin) {
        if(teamUpdateVo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userLogin == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Long id = teamUpdateVo.getId();
//        新老数据只有id不变可以拿到数据
        Team oldteam = this.getById(id);
        if(oldteam == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"数据不存在");
        }
        if(!userService.isAdmin(userLogin) && oldteam.getUserId() != userLogin.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
//        如果传入的值和原来的一样就不能修改
        if (StringUtils.equals(oldteam.getName(), teamUpdateVo.getName()) &&
                StringUtils.equals(oldteam.getDescription(), teamUpdateVo.getDescription()) &&
                Objects.equals(oldteam.getStatus(), teamUpdateVo.getStatus()) &&
                Objects.equals(oldteam.getExpireTime(), teamUpdateVo.getExpireTime())) {
            // 所有字段都一样，无需更新
            return true;
        }
        Integer status = teamUpdateVo.getStatus();
//        如果队伍是加密,用户必须传入密码
        if(status != null && status == TeamStausEnum.Encryption.getKey()){
            if(StringUtils.isBlank(teamUpdateVo.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密队伍必须传入密码");
            }
        }

        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateVo,updateTeam);
        boolean update = this.updateById(updateTeam);
        return update;
    }
}




