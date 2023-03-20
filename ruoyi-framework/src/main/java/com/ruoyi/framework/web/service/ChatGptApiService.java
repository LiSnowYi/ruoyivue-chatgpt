package com.ruoyi.framework.web.service;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.ChatGptConstants;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天gpt api服务
 *
 * @author lijiarui
 * @date 2023/03/18
 */
@Component
public class ChatGptApiService {


    /**
     * chatgpt 发送消息
     *
     * @param txt 内容
     * @return {@link String}
     */
    public JSONObject chat(String txt,String userName) {
        Map<String, Object> requestUrlParam = new HashMap<>();
        //版本
        requestUrlParam.put("model", "gpt-3.5-turbo");
        List<Map<String, String>> dataList = new ArrayList<>();
        dataList.add(new HashMap<String, String>(){{
            put("role", "user");
            put("content", txt);
        }});
        requestUrlParam.put("messages", dataList);
        //发送post请求读取调用微信接口获取openid用户唯一标识
        String body = HttpRequest.post(ChatGptConstants.CHATGPT_ENDPOINT)
                .header("Authorization", "Bearer " +ChatGptConstants.CHATGPT_KEY)
                .header("Content-Type", "application/json")
                .body(JSONUtil.toJsonStr(requestUrlParam))
                .execute()
                .body();

        return JSONObject.parseObject(body);
    }


}
