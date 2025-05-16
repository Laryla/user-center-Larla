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
import com.larly.usercenter.model.vo.TeamExitVo;
import com.larly.usercenter.model.vo.TeamJoinVo;
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
import org.springframework.util.CollectionUtils;


import javax.servlet.http.HttpServletRequest;
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
        if(status == null){
            status = TeamStausEnum.PUBLIC.getKey();
        }
        if(status != null && (status >3 || status < -1) ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
//          如果加密.必选有密码,密码<=32
        if(TeamStausEnum.Encryption.equals(TeamStausEnum.getTextByKey(status))){
            if(StringUtils.isBlank(password) && password.length() >= 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不满足要求");
            }
        }
//          超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if(expireTime == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请输入过期时间");
        }
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
        Long teamId = teamQuery.getId();
        if (teamQuery != null){
//            根据队伍id查询
            if(teamId != null && teamId <= 0){
                queryWrapper.eq("id",teamId);
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
//            默认查询公开
            if(status == null){
                queryWrapper.eq("status",TeamStausEnum.PUBLIC.getText());
                key = TeamStausEnum.PUBLIC.getText();
            }
            if(!userService.isAdmin(userLogin) && !key.equals(TeamStausEnum.PUBLIC) ){
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
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team,teamUserVo);
//            获取创建队伍的用户信息
            Long userId = team.getUserId();
            User user= userService.getById(userId);
//        查询当前队伍人数
            Long teamHasUser = userTeamService.count(new QueryWrapper<UserTeam>().eq("team_id", team.getId()));
            teamUserVo.setNum(teamHasUser);
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

    @Override
    public Boolean joinTeam(TeamJoinVo teamJoinVo, User userLogin) {
        if(teamJoinVo == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userLogin == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
//       用户最多加入5个
        QueryWrapper<UserTeam> userJoinNumWrapper = new QueryWrapper<>();
        long userId = userLogin.getId();
        if(userId < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        userJoinNumWrapper.eq("user_id",userId);
        long userJoinTeam = userTeamService.count(userJoinNumWrapper);
        if(userJoinTeam > 5 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已经添加5个队伍，不能在添加");
        }
//        只能加入未满的队伍
        long teamId = teamJoinVo.getId();;
        Team teamData = this.getById(teamId);
        Integer maxNum = teamData.getMaxNum();
//       查询该队伍已经有多少人（用户队伍关系表：查询teamId）
        QueryWrapper<UserTeam> teamJoinNumWrapper = new QueryWrapper<>();
        teamJoinNumWrapper.eq("team_id",teamId);
        long teamJoinNum = userTeamService.count(teamJoinNumWrapper);
        if(teamJoinNum >= maxNum){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已满");
        }
//        不能重复加入相同的队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("user_id",userId).eq("team_id",teamId);
        long count = userTeamService.count(userTeamQueryWrapper);
        // 存在了就不能加入了
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能重复加入");
        }
//        禁止加入私密队伍
        Integer status = teamData.getStatus();
        String teamStatus = TeamStausEnum.getTextByKey(status);
        if(TeamStausEnum.PRIVATE.equals(teamStatus) || teamStatus == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私密队伍");
        }
//        如果加入的是加密的队伍，要输入密码。密码正确才能加入
        String password = teamData.getPassword();
        if(TeamStausEnum.Encryption.equals(teamStatus)){
            if(StringUtils.isBlank(password) || password.equals(teamJoinVo.getPassword()) ){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }
//        修改队伍关系表
        QueryWrapper<UserTeam> addUserTeamWrapper = new QueryWrapper<>();
        UserTeam  userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return  userTeamService.save(userTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean exitTeam(TeamExitVo teamExitVo, User userLogin) {
        if(teamExitVo == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userLogin == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long userId = userLogin.getId();
        long teamId = teamExitVo.getId();
//        校验队伍是否存在
        Team team = this.getById(teamId);
        if(team == null ){
            throw new BusinessException(ErrorCode.SERVER_ERROR,"队伍不存在");
        }
//       校验我是否加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("user_id",userId).eq("team_id",teamId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if(count != 1){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"你没有加入该队伍");
        }
//        查询队伍人数
        QueryWrapper<UserTeam> userCountWrapper = new QueryWrapper<>();
        userCountWrapper.eq("team_id",teamId);
        long userInTeamCount = userTeamService.count(userCountWrapper);
        if(userInTeamCount == 1){
//            队伍就一个人，直接解散队伍
//            删除对应关系表数据
            boolean b = userTeamService.removeById(teamId);
            if(b == false){
                throw new BusinessException(ErrorCode.SERVER_ERROR);
            }
            return this.removeById(teamId);
        }else{
//            队伍人数不是一个人
//            如果是队长就权限自动移交给第二进入的人
            Long teamUserId = team.getUserId();
            if(userId == teamUserId){
//                是队长
                // 根据id判断哪个用户先进来 UserTeam表
                QueryWrapper<UserTeam> changeUserTeamWrapper = new QueryWrapper<>();
//                SELECT * FROM user_team WHERE is_delete = 0 AND team_id = 64  ORDER BY id DESC LIMIT 2
                changeUserTeamWrapper.eq("team_id", teamId)
                        .orderByDesc("id")
                        .last("LIMIT 2");
                List<UserTeam> list = userTeamService.list(changeUserTeamWrapper);
                if (CollectionUtils.isEmpty(list)) {
                    throw new BusinessException(ErrorCode.SERVER_ERROR, "队伍无人加入");
                }
//                取【1】
                long changeUserId = list.get(1).getUserId();
                team.setUserId(changeUserId);
//                修改team列表中的userId字段
                if (!this.updateById(team)) {
                    throw new BusinessException(ErrorCode.SERVER_ERROR, "队长权限移交失败");
                }
                QueryWrapper<UserTeam> delectUserTeamWrapper = new QueryWrapper<>();
                delectUserTeamWrapper.eq("user_id",userId);
                return userTeamService.remove(delectUserTeamWrapper);
            }else{
//                不是队长，直接退出
                QueryWrapper<UserTeam> delectUserTeamWrapper = new QueryWrapper<>();
                delectUserTeamWrapper.eq("user_id",userId);
                return userTeamService.remove(delectUserTeamWrapper);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delectTeam(long teamId, User userLogin) {
        if(teamId <= 0 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userLogin == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if(team == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"该队伍不存在");
        }
        long teamUserId = team.getUserId();
        long userId = userLogin.getId();
        if(teamUserId != userId){
            throw new BusinessException(ErrorCode.SERVER_ERROR,"不可以解散别人的队伍");
        }
//        删除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("team_id",teamId);
        if(!userTeamService.remove(userTeamQueryWrapper)){
            throw new BusinessException(ErrorCode.SERVER_ERROR);
        }

        return this.removeById(teamId);
    }
}




