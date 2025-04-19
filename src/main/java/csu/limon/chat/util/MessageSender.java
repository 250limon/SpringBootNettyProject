package csu.limon.chat.util;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class MessageSender {

   public static void response(ChannelHandlerContext ctx, Message msg) throws Exception{
        ctx.writeAndFlush(new TextWebSocketFrame(
                JSONUtil.toJsonString(msg)
        ));
    }
}
