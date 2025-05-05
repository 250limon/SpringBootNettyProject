package csu.limon.chat.util;

import csu.limon.chat.pojo.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class MessageSender {
    private static final PooledByteBufAllocator ALLOCATOR = PooledByteBufAllocator.DEFAULT;

    public static void response(ChannelHandlerContext ctx, Message msg) {
        try {
            String json = JSONUtil.toJsonString(msg);
            ByteBuf buf = ALLOCATOR.buffer(json.length());
            buf.writeBytes(json.getBytes());
            ctx.writeAndFlush(new TextWebSocketFrame(buf));
        } catch (Exception e) {
            System.err.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
