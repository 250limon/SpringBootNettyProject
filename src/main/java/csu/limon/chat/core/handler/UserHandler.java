package csu.limon.chat.core.handler;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.service.UserService;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserHandler extends ParentHandler{
    public UserHandler() {
        super();
    }
    @Autowired
    private UserService userService;
    @Override
    protected void process(ChannelHandlerContext ctx, Message msg) throws Exception {
        if(msg.getType()==MessageType.GET_USER)
        {
            userService.getUser(ctx,msg);
        }
    }

    @Override
    void addMessageType(List<MessageType> messageTypes) {
        messageTypes.add(MessageType.GET_USER);
    }
}