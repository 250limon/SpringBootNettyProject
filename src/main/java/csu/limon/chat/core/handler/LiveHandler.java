package csu.limon.chat.core.handler;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.service.MessageService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class LiveHandler extends ParentHandler{

    @Autowired
    private MessageService messageService;
    public LiveHandler() {
        super();
    }

    @Override
    protected void process(ChannelHandlerContext ctx, Message msg) throws Exception {
           if(msg.getType()==MessageType.LIVE_FEEDBACK)
           {
               messageService.liveFeedback(ctx,msg);
           }
    }

    @Override
    void addMessageType(List<MessageType> messageTypes) {
         messageTypes.add(MessageType.LIVE_FEEDBACK);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Message msg=new Message();
        msg.setType(MessageType.FRIEND_GROUP_MSG);
        msg.setContent("我已登出");
        messageService.sendFriendGroupMessage(ctx,msg);
        super.channelInactive(ctx);
    }
}
