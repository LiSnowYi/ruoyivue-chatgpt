package com.ruoyi.web.controller.chatgpt;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.ChatGptConstants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.WxChatGptBody;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.ChatGptApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/chatgpt")
public class ChatGptController extends BaseController {

    @Autowired
    private ChatGptApiService chatGptApiService;

    @Autowired
    private RedisCache redisCache;


    /**
     * 得到chatgpt文本
     *
     * @param wxChatGptBody wx聊天gpt身体
     * @return {@link AjaxResult}
     */
    @GetMapping("/getChatgptText")
    public AjaxResult getChatgptText( WxChatGptBody wxChatGptBody)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if(StringUtils.isEmpty(wxChatGptBody.getChatTxt())){
            return AjaxResult.error("请输入聊天内容");
        }

        //对接chatgpt接口
        String key = CacheConstants.WX_CHATGPT_COUNT_KEY + user.getUserName();
        //每日次数
        Integer frequencyDay = redisCache.getCacheObject(key);
        if(frequencyDay == null){
            frequencyDay = 0;
        }

        JSONObject json = chatGptApiService.chat(wxChatGptBody.getChatTxt(),user.getUserName());
        if(ObjectUtil.isNotEmpty(json.get("choices"))){
            redisCache.setCacheObject(key, ++frequencyDay, ChatGptConstants.FREQUENCY_DAY, TimeUnit.DAYS);
            JSONArray choices = json.getJSONArray("choices");
            JSONObject result = (JSONObject) choices.get(0);
            System.out.println(AjaxResult.success(result.getJSONObject("message")));
            return AjaxResult.success(result.getJSONObject("message"));

        }
        return AjaxResult.error("获取chatgpt信息失败");
    }

}
