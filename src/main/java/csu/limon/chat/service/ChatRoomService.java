package csu.limon.chat.service;

import csu.limon.chat.pojo.Message;
import io.netty.channel.ChannelHandlerContext;

public interface ChatRoomService {
     void handleJoinRoom(ChannelHandlerContext ctx, Message msg, String userId)throws Exception;
     void handleLeaveRoom(ChannelHandlerContext ctx, Message msg, String userId)throws Exception;
}
