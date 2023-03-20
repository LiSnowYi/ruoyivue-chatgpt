package com.ruoyi.web.controller.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.ChatGptConstants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.constant.WxLoginConstants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.http.HttpUtils;
import com.ruoyi.framework.web.service.UserDetailsServiceImpl;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginBody;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.framework.web.service.SysLoginService;
import com.ruoyi.framework.web.service.SysPermissionService;
import com.ruoyi.system.service.ISysMenuService;

/**
 * 登录验证
 * 
 * @author ruoyi
 */
@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;


    /**
     * 登录方法
     * 
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }


    /**
     * wxlogin
     *
     * @param loginBody 登录身体
     * @return {@link AjaxResult}
     */
    @PostMapping("/wxLogin")
    public AjaxResult wxlogin(@RequestBody LoginBody loginBody)
    {
        String code = loginBody.getCode();
        try
        {
            if (StringUtils.isEmpty(WxLoginConstants.APPID))
            {
                return AjaxResult.error("appId为空");
            }
            // 用户登录凭证（有效期五分钟）
            if (StringUtils.isEmpty(code))
            {
                return AjaxResult.error("登录凭证不能为空");
            }
            String requestUrl = WxLoginConstants.URL;
            Map<String, String> requestUrlParam = new HashMap<>();
            //小程序appId
            requestUrlParam.put("appid", WxLoginConstants.APPID);
            //小程序secret
            requestUrlParam.put("secret", WxLoginConstants.SECRET);
            //小程序端返回的code
            requestUrlParam.put("js_code", code);
            //默认参数
            requestUrlParam.put("grant_type", "authorization_code");
            //发送post请求读取调用微信接口获取openid用户唯一标识
            JSONObject jsonObject = JSON.parseObject(HttpUtils.sendPost(requestUrl, HttpUtils.asUrlParams(requestUrlParam),null));

            String openId = jsonObject.get("openid").toString();
            String sessionKey = jsonObject.get("session_key").toString();
            if (StringUtils.isEmpty(openId))
            {
                return AjaxResult.error("登录失败，无效的登录凭证");
            }
            // 生成令牌
            String token = loginService.wxLogin(openId, UserConstants.WX_PASSWORD);
            return AjaxResult.success().put(Constants.TOKEN, token);
        }
        catch (Exception e)
        {
            String msg = "接口异常";
            if (StringUtils.isNotEmpty(e.getMessage()))
            {
                msg = e.getMessage();
            }
            return AjaxResult.error(msg);
        }
    }

    /**
     * 获取用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     * 
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
