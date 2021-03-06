package com.web.site.chat;

/**
 * 创建聊天结果POJO
 *
 * @author Egan
 * @date 2018/9/17 9:33
 **/
public class CreateResult {

    private final ChatSession chatSession;
    private final ChatMessage chatMessage;

    public CreateResult(ChatSession chatSession, ChatMessage chatMessage) {
        this.chatSession = chatSession;
        this.chatMessage = chatMessage;
    }

    public ChatSession getChatSession() {
        return chatSession;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }
}
