package csu.limon.chat.core.handler;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class LiveHandler extends ParentHandler{
    public LiveHandler() {
        super();
    }

    @Override
    protected void process(ChannelHandlerContext ctx, Message msg) throws Exception {

    }

    @Override
    void addMessageType(List<MessageType> messageTypes) {

    }
}
