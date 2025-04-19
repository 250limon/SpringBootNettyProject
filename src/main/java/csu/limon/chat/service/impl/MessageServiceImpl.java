package csu.limon.chat.service.impl;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.service.MessageService;
import csu.limon.chat.util.JSONUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private ChatRoomServiceImpl chatRoomService;

    //TODO:业务方法
    @Override
    public void groupMessageHandle(ChannelHandlerContext ctx, Message msg) throws Exception{
        if (msg.getReceiver() != null) {
            // 验证用户是否在房间中
            String currentRoom = chatRoomService.getUserRoom(ctx.channel());
            if (currentRoom != null && currentRoom.equals(msg.getReceiver())) {
                chatRoomService.broadcastMessage(msg.getReceiver(), msg);
            } else {
                ctx.writeAndFlush(new TextWebSocketFrame(
                        JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Not in room: " + msg.getReceiver()))
                ));
                System.out.println("Rejected MSG from user not in room: " + msg);
            }
        } else {
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Room ID required"))
            ));
            System.out.println("Rejected MSG with null roomId: " + msg);
        }
    }

    @Override
    public void friendMessageHandler(ChannelHandlerContext ctx, Message msg) throws Exception {

    }

    @Override
    public void friendGroupMessageHandler(ChannelHandlerContext ctx, Message msg) throws Exception {

    }
}
