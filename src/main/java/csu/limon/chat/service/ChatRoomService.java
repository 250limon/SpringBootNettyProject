package csu.limon.chat.service;

import csu.limon.chat.pojo.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public interface ChatRoomService {
     void handleJoinRoom(ChannelHandlerContext ctx, Message msg, String userId)throws Exception;
     void handleLeaveRoom(ChannelHandlerContext ctx, Message msg, String userId)throws Exception;
     void creatRoom(ChannelHandlerContext ctx, Message msg)throws Exception;
     void roomList(ChannelHandlerContext ctx, Message msg)throws Exception;
     void joinRoom(String roomId, Channel channel, String username)throws Exception;
     void leaveRoom(Channel channel) throws Exception;
     void sendUserList(String roomId) throws Exception;
     String getUserRoom(Channel channel) throws Exception;
}
