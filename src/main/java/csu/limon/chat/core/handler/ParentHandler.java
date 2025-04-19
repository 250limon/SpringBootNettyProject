package csu.limon.chat.core.handler;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@ChannelHandler.Sharable
abstract class ParentHandler extends SimpleChannelInboundHandler<Message> {

    protected List<MessageType> messageTypes;//在处理器中进行处理的消息类型
    protected ParentHandler()
    {
        messageTypes = new ArrayList<>();
        addMessageType(messageTypes);
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if(isPass(msg))
            pass(ctx, msg);
        else
            process(ctx, msg);
    }

    private boolean isPass(Message message) {

        for(MessageType messageType : messageTypes)
        {
            if(messageType.equals(message.getType()))
                return false;
        }
        return true;
    }

   abstract protected void process(ChannelHandlerContext ctx, Message msg) throws Exception;

    private void pass(ChannelHandlerContext channelHandlerContext, Message message){
        channelHandlerContext.fireChannelRead(message);
    }
    abstract void addMessageType(List<MessageType> messageTypes);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println(" error: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }




}
