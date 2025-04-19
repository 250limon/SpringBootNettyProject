package csu.limon.chat.core.codec;


import csu.limon.chat.pojo.Message;
import csu.limon.chat.util.JSONUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MessageDecoder extends ByteToMessageDecoder {
    private static final int HEADER_SIZE = 8;
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < HEADER_SIZE) return;
        in.markReaderIndex();
        int magic = in.readInt();
        if (magic != MAGIC_NUMBER) {
            ctx.close();
            System.err.println("Invalid magic number: " + magic);
            return;
        }
        byte type = in.readByte();
        int length = in.readInt();
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }
        byte[] body = new byte[length];
        in.readBytes(body);
        Message msg = JSONUtil.parse(body, Message.class);
        System.out.println("Decoded message: " + msg);
        out.add(msg);
    }
}
