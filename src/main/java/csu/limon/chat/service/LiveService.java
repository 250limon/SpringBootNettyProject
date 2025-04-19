package csu.limon.chat.service;

import csu.limon.chat.pojo.Message;
import io.netty.channel.ChannelHandlerContext;

public interface LiveService {
    public void broadCast(ChannelHandlerContext ctx, Message msg) throws Exception;
    public void feedBack(ChannelHandlerContext ctx, Message msg) throws Exception;
}
