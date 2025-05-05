package csu.limon.chat.core.handler;
import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.util.JSONUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;

@ChannelHandler.Sharable
@Component
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    // 添加内存池分配器
    private static final PooledByteBufAllocator ALLOCATOR = PooledByteBufAllocator.DEFAULT;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) throws Exception {
        String text = frame.text();
        System.out.println("Received WebSocket text frame: " + text);
        try {
            Message message = JSONUtil.parse(text.getBytes(), Message.class);
            System.out.println("Parsed WebSocket message: " + message);
            ctx.fireChannelRead(message); // 传递给后续处理器
        } catch (Exception e) {
            System.err.println("Failed to parse WebSocket message: " + e.getMessage());
            // 使用内存池分配 ByteBuf
            String json = JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Invalid message format"));
            ByteBuf buf = ALLOCATOR.buffer(json.length());
            buf.writeBytes(json.getBytes());
            ctx.writeAndFlush(new TextWebSocketFrame(buf));
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