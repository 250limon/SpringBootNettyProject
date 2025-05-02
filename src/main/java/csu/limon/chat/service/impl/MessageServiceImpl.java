package csu.limon.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import csu.limon.chat.mapper.FriendMapper;
import csu.limon.chat.pojo.Chatmessage;
import csu.limon.chat.pojo.Friend;
import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.service.ChatmessageService;
import csu.limon.chat.service.MessageService;
import csu.limon.chat.service.UserService;
import csu.limon.chat.util.JSONUtil;
import csu.limon.chat.util.MessageSender;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {
    @Autowired
    private ChatRoomServiceImpl chatRoomService;
    @Autowired
    private ChatmessageService chatmessageService;
    @Autowired
    private UserService userService;
    @Autowired
    private FriendMapper friendMapper;

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
        String senderUsername = msg.getSender();
        String receiverUsername = msg.getReceiver();
        String content = msg.getContent();

        // 1. 校验输入
        if (senderUsername == null || receiverUsername == null || content == null) {
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "发送者、接收者或内容为空"))
            ));
            return;
        }
        if (!isNumeric(senderUsername) || !isNumeric(receiverUsername)) {
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "用户名必须为数字"))
            ));
            return;
        }

        // 2. 存库
        Chatmessage chatmessage = new Chatmessage();
        chatmessage.setSender(Integer.parseInt(senderUsername));
        chatmessage.setReceiver(Integer.parseInt(receiverUsername));
        chatmessage.setContent(content);
        try {
            chatmessageService.insertChatMessage(chatmessage);
        } catch (Exception e) {
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "保存消息到数据库失败"))
            ));
            return;
        }

        // 3. 转发消息给对方（如果在线）
        msg.setType(MessageType.FRIEND_MSG);
        Channel receiverChannel = userService.getChannelUserMap().get(receiverUsername);
        if (receiverChannel != null && receiverChannel.isActive()) {
            receiverChannel.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(msg)
            ));
        } else {
            // 对方不在线
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(
                            MessageType.ERROR,
                            null, null,
                            "对方不在线，消息已保存"
                    ))
            ));
        }
    }


    @Override
    public void friendGroupMessageHandler(ChannelHandlerContext ctx, Message msg) throws Exception {

    }

    @Override
    public void chatHistoryHandler(ChannelHandlerContext ctx, Message msg) throws Exception {

    }


    @Override
    public void getChatHistory(ChannelHandlerContext ctx, Message msg) throws Exception {
        String senderUsername = msg.getSender();
        String friendUsername = msg.getReceiver();
        System.out.println("收到CHAT_HISTORY请求");
        // 验证输入
        if (senderUsername == null || friendUsername == null) {
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "发送者或接收者为空"))
            ));
            System.out.println("拒绝无效字段的聊天记录请求: " + msg);
            return;
        }

        // 验证用户名是否为数字
        if (!isNumeric(senderUsername) || !isNumeric(friendUsername)) {
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "用户名必须为数字"))
            ));
            System.out.println("拒绝非数字用户名的聊天记录请求: sender=" + senderUsername + ", friend=" + friendUsername);
            return;
        }

        // 查询聊天记录
        List<Chatmessage> history;
        try {
            history = chatmessageService.getChatMessage(senderUsername, friendUsername);
        } catch (Exception e) {
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "获取聊天记录失败"))
            ));
            System.out.println("获取聊天记录时错误: " + e.getMessage());
            return;
        }

        // 转换为 Message 对象
        List<Message> messageHistory = history.stream()
                .map(chatmessage -> new Message(
                        MessageType.FRIEND_MSG,
                        String.valueOf(chatmessage.getSender()),
                        String.valueOf(chatmessage.getReceiver()),
                        chatmessage.getContent()
                ))
                .collect(Collectors.toList());

        // 发送聊天记录



        ctx.writeAndFlush(new TextWebSocketFrame(
                JSONUtil.toJsonString(new Message(
                        MessageType.CHAT_HISTORY,
                        senderUsername,
                        friendUsername,
                        JSONUtil.toJsonString(messageHistory)
                ))
        ));
        System.out.println("发送 CHAT_HISTORY 成功: " + JSONUtil.toJsonString(messageHistory));
    }

    @Override
    public void sendFriendGroupMessage(ChannelHandlerContext ctx, Message msg) throws Exception {

        String senderUsername = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
        String content = msg.getContent();
        int user_id=Integer.parseInt(senderUsername);
        List<Friend> friends=friendMapper.selectList(new QueryWrapper<Friend>().eq("user_id",user_id));
        for(int i=0;i<friends.size();i++) {
            int friend_id=friends.get(i).getFriendId();
            String friendUsername =String.valueOf(friend_id);
            Channel friendChannel = userService.getChannelUserMap().get(String.valueOf(friend_id));
            if (friendChannel != null && friendChannel.isActive()) {
                friendChannel.writeAndFlush(new TextWebSocketFrame(
                        JSONUtil.toJsonString(new Message(
                                MessageType.FRIEND_GROUP_MSG,
                                senderUsername,
                                friendUsername,
                                content
                        ))
                ));
            }
        }
    }

    @Override
    public void liveFeedback(ChannelHandlerContext ctx, Message msg) throws Exception {
        String senderUsername = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
        int user_id=Integer.parseInt(senderUsername);
        List<Friend> friends=friendMapper.selectList(new QueryWrapper<Friend>().eq("user_id",user_id));
        List<Integer>friend_ids=new java.util.ArrayList<>();
        for(int i=0;i<friends.size();i++) {
            int friend_id=friends.get(i).getFriendId();
            Channel friendChannel = userService.getChannelUserMap().get(String.valueOf(friend_id));
            if (friendChannel != null && friendChannel.isActive()) {
                friend_ids.add(friend_id);
            }
        }
        MessageSender.response(ctx,new Message(MessageType.LIVE_FEEDBACK,null,null,JSONUtil.toJsonString(friend_ids)));
    }


    // 辅助方法：验证字符串是否为数字
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        return str.matches("\\d+");
    }
}
