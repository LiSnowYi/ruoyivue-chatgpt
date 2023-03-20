package com.ruoyi.common.core.domain.model;

/**
 * wx聊天gpt身体
 *
 * @author lijiarui
 * @date 2023/03/18
 */
public class WxChatGptBody {

    /**
     * 微信开放id
     */
    private String openId;

    /**
     * 聊天文本
     */
    private String chatTxt;


    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getChatTxt() {
        return chatTxt;
    }

    public void setChatTxt(String chatTxt) {
        this.chatTxt = chatTxt;
    }
}
