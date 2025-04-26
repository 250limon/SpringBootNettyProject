package csu.limon.chat.core.handler;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.service.MessageService;
import csu.limon.chat.service.impl.MessageServiceImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageHandler extends ParentHandler {
    @Autowired
    private MessageService messageService;



    public MessageHandler() {
        super();
    }

    @Override
    protected void process(ChannelHandlerContext ctx, Message msg) throws Exception {
        // 检查用户是否已认证
        String username = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
         if(msg.getType()==MessageType.ROOM_MSG)
       {
           System.out.println("-------------Received message------------" );
           messageService.groupMessageHandle(ctx,msg);
       } else if (msg.getType()==MessageType.FRIEND_MSG) {
             messageService.friendMessageHandler(ctx,msg);
         } else if (msg.getType()==MessageType.FRIEND_GROUP_MSG) {
             messageService.friendMessageHandler(ctx, msg);
         }
         else if (msg.getType()==MessageType.CHAT_HISTORY) {
             messageService.getChatHistory(ctx,msg);
         }
    }

    @Override
    void addMessageType(List<MessageType> messageTypes) {
        messageTypes.add(MessageType.ROOM_MSG);
        messageTypes.add(MessageType.FRIEND_MSG);
        messageTypes.add(MessageType.FRIEND_GROUP_MSG);
        messageTypes.add(MessageType.CHAT_HISTORY);
    }


}
