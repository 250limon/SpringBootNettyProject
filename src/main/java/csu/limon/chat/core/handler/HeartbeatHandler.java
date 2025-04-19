package csu.limon.chat.core.handler;
import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HeartbeatHandler extends ParentHandler{

    @Override
    protected void process(ChannelHandlerContext ctx, Message msg) throws Exception {
        ctx.writeAndFlush(new Message(MessageType.HEARTBEAT, null, null, "PONG"));
        System.out.println("Received heartbeat from " + ctx.channel() + ", responded with PONG");
    }

    @Override
    void addMessageType(List<MessageType> messageTypes) {
          messageTypes.add(MessageType.HEARTBEAT);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state()) {
                case READER_IDLE:
                    // No data received for 60 seconds
                    System.out.println("No data received from " + ctx.channel() + " for 60 seconds, closing connection");
                    ctx.close();
                    break;
                default:
                    break;
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}


//
//public class HeartbeatHandler extends SimpleChannelInboundHandler<Message> {
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
//        if (msg.getType() == MessageType.HEARTBEAT) {
//            // Respond to heartbeat
//            ctx.writeAndFlush(new Message(MessageType.HEARTBEAT, null, null, "PONG"));
//            System.out.println("Received heartbeat from " + ctx.channel() + ", responded with PONG");
//        } else {
//            // Pass non-heartbeat messages to the next handler
//            ctx.fireChannelRead(msg);
//            System.out.println("Non-heartbeat message passed to next handler: " + msg);
//        }
//    }
//
//    @Override
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if (evt instanceof IdleStateEvent) {
//            IdleStateEvent event = (IdleStateEvent) evt;
//            switch (event.state()) {
//                case READER_IDLE:
//                    // No data received for 60 seconds
//                    System.out.println("No data received from " + ctx.channel() + " for 60 seconds, closing connection");
//                    ctx.close();
//                    break;
//                default:
//                    break;
//            }
//        } else {
//            super.userEventTriggered(ctx, evt);
//        }
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        System.err.println("HeartbeatHandler error: " + cause.getMessage());
//        ctx.close();
//    }
//}
