package csu.limon.chat.core.handler;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.util.JSONUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;

import java.util.Map;

@ChannelHandler.Sharable
@Component
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {

        String text = frame.text();//获取json字符串
        System.out.println("Received WebSocket text frame: " + text);
        try {
            Message message = JSONUtil.parse(text.getBytes(), Message.class);
            System.out.println("Parsed WebSocket message: " + message);
            ctx.fireChannelRead(message); // 传递给后续处理器
        } catch (Exception e) {
            System.err.println("Failed to parse WebSocket message: " + e.getMessage());
            ctx.writeAndFlush(new TextWebSocketFrame(
                JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Invalid message format"))
            ));
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("WebSocketFrameHandler error: " + cause.getMessage());
        ctx.close();
    }
}