package com.ruoyi.framework.web.service;

import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.service.ISysRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.enums.UserStatus;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysUserService;

import javax.annotation.Resource;

/**
 * 用户验证处理
 *
 * @author ruoyi
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;
    
    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private SysRegisterService registerService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private TokenService tokenService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user))
        {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException("登录用户：" + username + " 不存在");
        }
        else if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        else if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }else if(StringUtils.isNull(user.getPassword()))
        {
            log.info("登录用户：{} 微信用户，不可登陆后台。.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 为微信用户，不可登陆后台。");
        }

        passwordService.validate(user);

        return createLoginUser(user);
    }


    /**
     * wx用户loginand注册
     *
     * @param openId 开放id
     * @return {@link Boolean}
     * @throws UsernameNotFoundException 用户名没有发现异常
     */
    public String wxUserLoginandRegister (String openId, String sessionKey) throws UsernameNotFoundException
    {
        String msg = "";
        SysUser user = userService.selectUserByUserName(openId);
        RegisterBody registerBody = new RegisterBody();
        if(StringUtils.isNull(user)){
            //注册
            registerBody.setUsername(openId);
            registerBody.setPassword(sessionKey);
            registerBody.setWxStatus(UserConstants.WX_STATUS_SUCCESS);
            msg = registerService.register(registerBody);
            //角色
            if(StringUtils.isEmpty(msg)){
                SysUser wxUser = userService.selectUserByUserName(openId);
                Long[] userIds = new Long[1];
                userIds[0] = wxUser.getUserId();
                //微信角色
                roleService.insertAuthUsers(UserConstants.WX_ROLEID, userIds);
            }
            return msg;
        }
//        else{
//            LoginUser loginUser = SecurityUtils.getLoginUser();
//
//            //每次登陆保存微信session为密码
//            if (userService.resetUserPwd(user.getUserName(), SecurityUtils.encryptPassword(sessionKey)) > 0)
//            {
//                // 更新缓存用户密码
//                loginUser.getUser().setPassword(SecurityUtils.encryptPassword(sessionKey));
//                tokenService.setLoginUser(loginUser);
//            }
//            userService.updateUser(user);
//        }
        return msg;
    }
    public String wxSessionEdit(String openId, String sessionKey){
        String msg = "";
        SysUser user = userService.selectUserByUserName(openId);
        LoginUser loginUser = SecurityUtils.getLoginUser();
        //每次登陆保存微信session为密码
        if (userService.resetUserPwd(user.getUserName(), SecurityUtils.encryptPassword(sessionKey)) > 0)
        {
            // 更新缓存用户密码
            loginUser.getUser().setPassword(SecurityUtils.encryptPassword(sessionKey));
            tokenService.setLoginUser(loginUser);
        }
        userService.updateUser(user);
        return msg;
    }



    public UserDetails createLoginUser(SysUser user)
    {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
