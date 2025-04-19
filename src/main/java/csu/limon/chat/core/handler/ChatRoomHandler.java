package csu.limon.chat.core.handler;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.service.ChatRoomService;
import csu.limon.chat.service.impl.ChatRoomServiceImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatRoomHandler extends ParentHandler {
    @Autowired
    private ChatRoomService chatRoomService;

    public ChatRoomHandler() {
        super();

    }

    @Override
    protected void process(ChannelHandlerContext ctx, Message msg) throws Exception {
        String username = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
            if(msg.getType()==MessageType.JOIN_ROOM)
            {
                chatRoomService.handleJoinRoom(ctx,msg,username);
            }
            else if(msg.getType()==MessageType.LEAVE_ROOM){
                chatRoomService.handleLeaveRoom(ctx,msg,username);
            } else if (msg.getType() == MessageType.CREAT_ROOM) {
                chatRoomService.creatRoom(ctx,msg);
            } else if (msg.getType()==MessageType.ROOM_LIST) {
                chatRoomService.roomList(ctx,msg);
            }
    }
    @Override
    void addMessageType(List<MessageType> messageTypes) {
        messageTypes.add(MessageType.JOIN_ROOM);
        messageTypes.add(MessageType.LEAVE_ROOM);
    }
    private void sendUserList(String roomId) throws Exception{
       chatRoomService.sendUserList(roomId);
    }
    @Override //通道断开连接时候的回调函数
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String username = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
        String roomId = chatRoomService.getUserRoom(ctx.channel());
        chatRoomService.leaveRoom(ctx.channel());
        if (roomId != null) {
            sendUserList(roomId);
        }
        System.out.println("User " + username + " disconnected from room: " + roomId);
        super.channelInactive(ctx);
    }
}
