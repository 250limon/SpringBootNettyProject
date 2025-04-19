package csu.limon.chat.core.codec;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.util.JSONUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;

import java.util.List;
@ChannelHandler.Sharable
@Component
public class MessageEncoder extends MessageToMessageEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        String json = JSONUtil.toJsonString(msg);
        TextWebSocketFrame frame = new TextWebSocketFrame(json);
        out.add(frame);
        System.out.println("Encoded message to WebSocket frame: " + json);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("MessageEncoder error: " + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }
}
