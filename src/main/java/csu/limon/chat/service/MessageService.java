package csu.limon.chat.service;

import csu.limon.chat.pojo.Message;
import io.netty.channel.ChannelHandlerContext;

public interface MessageService {

     void groupMessageHandle(ChannelHandlerContext ctx, Message msg) throws Exception;
     void friendMessageHandler(ChannelHandlerContext ctx,Message msg) throws Exception;
     void friendGroupMessageHandler(ChannelHandlerContext ctx,Message msg) throws Exception;
     void chatHistoryHandler(ChannelHandlerContext ctx, Message msg) throws Exception;
     void userListHandler(ChannelHandlerContext ctx, Message msg) throws Exception;
}
