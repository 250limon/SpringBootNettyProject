package csu.limon.chat.core.handler;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.service.impl.MessageServiceImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageHandler extends ParentHandler {
    @Autowired
    private MessageServiceImpl messageService;



    public MessageHandler() {
        super();
    }

    @Override
    protected void process(ChannelHandlerContext ctx, Message msg) throws Exception {
        // 检查用户是否已认证
        String username = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
         if(msg.getType()==MessageType.ROOM_MSG)
       {
           System.out.println("-------------Received message------------" );
           messageService.groupMessageHandle(ctx,msg);
       } else if (msg.getType()==MessageType.FRIEND_MSG) {
             messageService.friendMessageHandler(ctx,msg);
         } else if (msg.getType()==MessageType.FRIEND_GROUP_MSG) {
             messageService.friendMessageHandler(ctx, msg);
         }
    }

    @Override
    void addMessageType(List<MessageType> messageTypes) {
        messageTypes.add(MessageType.ROOM_MSG);
        messageTypes.add(MessageType.FRIEND_MSG);
        messageTypes.add(MessageType.FRIEND_GROUP_MSG);
    }


}



//
//@ChannelHandler.Sharable
//public class MessageHandler extends SimpleChannelInboundHandler<Message> {
//    private final ChatRoomService chatRoomService = ChatRoomService.getInstance();
//
//    @Override
//    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
//        System.out.println("Received message: " + msg);
//        // 忽略 LOGIN 消息
//        if (msg.getType() == MessageType.LOGIN) {
//            System.out.println("Ignoring LOGIN message in MessageHandler");
//            return;
//        }
//
//        // 检查用户是否已认证
//        String username = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
//        if (username == null) {
//            ctx.writeAndFlush(new TextWebSocketFrame(
//                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Not authenticated"))
//            ));
//            System.out.println("Rejected message from unauthenticated user: " + msg);
//            ctx.close();
//            return;
//        }
//
//        // 验证消息的 sender 是否与认证用户一致
//        if (msg.getType() == MessageType.MSG && !username.equals(msg.getSender())) {
//            ctx.writeAndFlush(new TextWebSocketFrame(
//                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Sender mismatch: expected " + username))
//            ));
//            System.out.println("Rejected MSG with sender mismatch: expected " + username + ", got " + msg.getSender());
//            return;
//        }
//
//        switch (msg.getType()) {
//            case JOIN_ROOM:
//                handleJoinRoom(ctx, msg, username);
//                break;
//            case LEAVE_ROOM:
//                handleLeaveRoom(ctx, msg, username);
//                break;
//            case MSG:
//                if (msg.getRoomId() != null) {
//                    // 验证用户是否在房间中
//                    String currentRoom = chatRoomService.getUserRoom(ctx.channel());
//                    if (currentRoom != null && currentRoom.equals(msg.getRoomId())) {
//                        chatRoomService.broadcastMessage(msg.getRoomId(), msg);
//                    } else {
//                        ctx.writeAndFlush(new TextWebSocketFrame(
//                                JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Not in room: " + msg.getRoomId()))
//                        ));
//                        System.out.println("Rejected MSG from user not in room: " + msg);
//                    }
//                } else {
//                    ctx.writeAndFlush(new TextWebSocketFrame(
//                            JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Room ID required"))
//                    ));
//                    System.out.println("Rejected MSG with null roomId: " + msg);
//                }
//                break;
//            default:
//                System.out.println("Unhandled message type: " + msg.getType());
//        }
//    }
//
//    private void handleJoinRoom(ChannelHandlerContext ctx, Message msg, String username)throws Exception {
//        String roomId = msg.getRoomId();
//        if (roomId == null) {
//            ctx.writeAndFlush(new TextWebSocketFrame(
//                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Invalid room ID"))
//            ));
//            System.out.println("Rejected JOIN_ROOM: roomId is null");
//            ctx.close();
//            return;
//        }
//
//        // 检查是否已在目标房间
//        String currentRoom = chatRoomService.getUserRoom(ctx.channel());
//        if (currentRoom != null && currentRoom.equals(roomId)) {
//            System.out.println("User " + username + " already in room: " + roomId + ", ignoring JOIN_ROOM");
//            ctx.writeAndFlush(new TextWebSocketFrame(
//                    JSONUtil.toJsonString(new Message(MessageType.SUCCESS, null, null, "Already in room: " + roomId))
//            ));
//            return;
//        }
//
//        // 离开当前房间（如果有）
//        if (currentRoom != null) {
//            chatRoomService.leaveRoom(ctx.channel());
//            System.out.println("User " + username + " left previous room: " + currentRoom);
//        }
//
//        chatRoomService.joinRoom(roomId, ctx.channel(), username);
//        ctx.writeAndFlush(new TextWebSocketFrame(
//                JSONUtil.toJsonString(new Message(MessageType.SUCCESS, null, null, "Joined room: " + roomId))
//        ));
//        sendUserList(roomId);
//        System.out.println("User " + username + " joined room: " + roomId + ", Channel: " + ctx.channel());
//    }
//
//    private void handleLeaveRoom(ChannelHandlerContext ctx, Message msg, String username)throws Exception {
//        String roomId = msg.getRoomId();
//        chatRoomService.leaveRoom(ctx.channel());
//        ctx.writeAndFlush(new TextWebSocketFrame(
//                JSONUtil.toJsonString(new Message(MessageType.SUCCESS, null, null, "Left room: " + roomId))
//        ));
//        sendUserList(roomId);
//        System.out.println("User " + username + " left room: " + roomId);
//    }
//
//    private void sendUserList(String roomId) throws Exception{
//        Set<String> users = chatRoomService.getUsersInRoom(roomId);
//        String userListJson = JSONUtil.toJsonString(users);
//        Message userListMsg = new Message(MessageType.USER_LIST, null, roomId, userListJson);
//        chatRoomService.broadcastMessage(roomId, userListMsg);
//        System.out.println("Sent user list for room " + roomId + ": " + users);
//    }
//
//    @Override
//    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        String username = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
//        String roomId = chatRoomService.getUserRoom(ctx.channel());
//        chatRoomService.leaveRoom(ctx.channel());
//        if (roomId != null) {
//            sendUserList(roomId);
//        }
//        System.out.println("User " + username + " disconnected from room: " + roomId);
//        super.channelInactive(ctx);
//    }
//
//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        System.err.println("MessageHandler error: " + cause.getMessage());
//        cause.printStackTrace();
//        ctx.close();
//    }
//}