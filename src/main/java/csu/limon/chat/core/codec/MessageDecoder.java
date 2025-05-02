package csu.limon.chat.core.codec;


import csu.limon.chat.pojo.Message;
import csu.limon.chat.util.JSONUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    private static final int HEADER_SIZE = 10; // 2 + 1 + 1 + 2 + 4
    private static final short MAGIC_NUMBER = (short) 0xCAFE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 检查消息头是否完整
        System.out.println("MessageDecoder decode");
        if (in.readableBytes() < HEADER_SIZE) {
            return;
        }

        in.markReaderIndex();

        // 读取消息头
        short magic = in.readShort();
        if (magic != MAGIC_NUMBER) {
            ctx.writeAndFlush(new RuntimeException("Invalid magic number: " + magic));
            ctx.close();
            return;
        }

        byte version = in.readByte();
        byte type = in.readByte();
        short status = in.readShort();
        int length = in.readInt();

        // 检查消息体是否完整
        if (in.readableBytes() < length) {
            in.resetReaderIndex(); // 半包，重置读取位置
            return;
        }

        // 读取消息体
        byte[] body = new byte[length];
        in.readBytes(body);

        try {
            // JSON 反序列化
            Message message = JSONUtil.parse(body, Message.class);
            out.add(message); // 传递给后续处理器
        } catch (Exception e) {
            System.err.println("Failed to parse message body: " + e.getMessage());
            // 发送错误响应（通过 MessageEncoder 处理）
            Message errorMsg = new Message();

            errorMsg.setContent("Invalid message format");
            ctx.writeAndFlush(errorMsg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("MessageDecoder error: " + cause.getMessage());
        ctx.close();
    }
}
