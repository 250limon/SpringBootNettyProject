package csu.limon.chat.service.impl;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.service.ChatRoomService;
import csu.limon.chat.util.JSONUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    // 单例实例

    /*
   TODO:
    键：String（房间 ID，如 "1"）
    值：ConcurrentHashMap<Channel, String>，表示该房间内的用户映射：
        键：Channel（Netty 的通道对象，代表一个 WebSocket 连接）。
        值：String（用户名，如 "user1"）。
    示例：rooms.get("1") 返回 {channel1=user1, channel2=1}，表示房间 1 有用户 user1 和 1 */
    private final ConcurrentHashMap<String, ConcurrentHashMap<Channel, String>> rooms = new ConcurrentHashMap<>();//存储所有聊天室的状态

    //记录每个通道当前所在的房间
    /*TODO:
       键：Channel（WebSocket 连接）。
       值：String（房间 ID）。
       示例：userRooms.get(channel1) 返回 "1"，表示 channel1 在房间 1
     */
    public final ConcurrentHashMap<Channel, String> userRooms = new ConcurrentHashMap<>();
    private int createdRoomCount = 0; // 用于跟踪创建的房间数量
    private final ConcurrentHashMap<String,String> roomIdToName=new ConcurrentHashMap<>();

    //业务方法
    @Override
    public void joinRoom(String roomId, Channel channel, String username) {
        System.out.println("Before joining room " + roomId + ", current state: " + rooms.get(roomId));
        //获取某一个房间的
        ConcurrentHashMap<Channel, String> room = rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());

        // 检查通道是否已在房间中
        if (room.containsKey(channel)) {
            System.out.println("Channel " + channel + " already in room " + roomId + " as user: " + room.get(channel));
            if (!room.get(channel).equals(username)) {
                System.out.println("Updating username for channel " + channel + " from " + room.get(channel) + " to " + username);
                room.put(channel, username);
            }
        } else {
            room.put(channel, username);
            userRooms.put(channel, roomId);
            System.out.println("User " + username + " joined room: " + roomId + ", Channel: " + channel);
        }

        System.out.println("After joining room " + roomId + ", current users: " + room.values());
        printRoomUserCount("1");
        printAllRoomsState();
    }
    @Override
    public void leaveRoom(Channel channel) {
        String roomId = userRooms.remove(channel);
        if (roomId != null) {
            ConcurrentHashMap<Channel, String> room = rooms.get(roomId);
            if (room != null) {
                String username = room.remove(channel);
                System.out.println("User " + username + " left room: " + roomId + ", Channel: " + channel);
                System.out.println("Current users in room " + roomId + ": " + (room.isEmpty() ? "none" : room.values()));
                if (room.isEmpty()) {
                    rooms.remove(roomId);
                    System.out.println("Room " + roomId + " removed (empty)");
                }
                printRoomUserCount("1");
                printAllRoomsState();
            }
        }
    }

    public void broadcastMessage(String roomId, Message msg) {
        ConcurrentHashMap<Channel, String> room = rooms.get(roomId);
        if (room != null) {
            System.out.println("Broadcasting message to room " + roomId + " with " + room.size() + " users: " + msg);
            for (Channel ch : room.keySet()) {
                if (ch.isActive()) {
                    try {
                        String json = JSONUtil.toJsonString(msg);
                        ch.writeAndFlush(new TextWebSocketFrame(json));
                        System.out.println("Message sent to channel: " + ch + ", user: " + room.get(ch));
                    } catch (Exception e) {
                        System.err.println("Failed to send message to channel " + ch + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Skipping inactive channel: " + ch);
                    leaveRoom(ch); // 移除不活跃的通道
                }
            }
            printRoomUserCount("1");
            printAllRoomsState();
        } else {
            System.out.println("No users in room " + roomId + " to broadcast message");
        }
    }
    @Override
    public void sendUserList(String roomId) throws Exception{
        Set<String> users = getUsersInRoom(roomId);
        String userListJson = JSONUtil.toJsonString(users);
        Message userListMsg = new Message(MessageType.USER_LIST, null, roomId, userListJson);
        broadcastMessage(roomId, userListMsg);
        System.out.println("Sent user list for room " + roomId + ": " + users);
    }
    @Override
    public void handleJoinRoom(ChannelHandlerContext ctx, Message msg, String userId)throws Exception {
        String roomId = msg.getReceiver();
        if (roomId == null) {
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Invalid room ID"))
            ));
            System.out.println("Rejected JOIN_ROOM: roomId is null");
            ctx.close();
            return;
        }

        // 检查是否已在目标房间
        String currentRoom = getUserRoom(ctx.channel());
        if (currentRoom != null && currentRoom.equals(roomId)) {
            System.out.println("User " + userId + " already in room: " + roomId + ", ignoring JOIN_ROOM");
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.SUCCESS, null, null, "Already in room: " + roomId))
            ));
            return;
        }

        // 离开当前房间（如果有）
        if (currentRoom != null) {
            leaveRoom(ctx.channel());
            System.out.println("User " + userId + " left previous room: " + currentRoom);
        }

        joinRoom(roomId, ctx.channel(), userId);
        ctx.writeAndFlush(new TextWebSocketFrame(
                JSONUtil.toJsonString(new Message(MessageType.SUCCESS, null, null, "Joined room: " + roomId))
        ));
        sendUserList(roomId);
        System.out.println("User " + userId + " joined room: " + roomId + ", Channel: " + ctx.channel());
    }
    @Override
    public void handleLeaveRoom(ChannelHandlerContext ctx, Message msg, String userId)throws Exception {
        String roomId = msg.getReceiver();
        leaveRoom(ctx.channel());
        ctx.writeAndFlush(new TextWebSocketFrame(
                JSONUtil.toJsonString(new Message(MessageType.SUCCESS, null, null, "Left room: " + roomId))
        ));
        sendUserList(roomId);
        System.out.println("User " + userId + " left room: " + roomId);
    }
    @Override
    public void creatRoom(ChannelHandlerContext ctx, Message msg) throws Exception {
        // 获取房间号
        String roomName = msg.getContent();
        // 检查房间是否已经存在
        boolean roomExists = userRooms.values().stream().anyMatch(r -> r.equals(roomName));
        if (roomExists) {
            // 如果房间已经存在，发送错误响应
            Message response = new Message();
            response.setType(MessageType.ERROR);
            response.setContent("房间" + roomName + "已经存在啦，请改个名字吧！ ");
            System.out.println("房间" + roomName + "已经存在啦，请改个名字吧！ ");
            ctx.writeAndFlush(response);
            return;
        }

        // 检查当前用户是否已经在一个房间中
        Channel channel = ctx.channel();
        if (userRooms.containsKey(channel)) {
            // 如果用户已经在某个房间中，发送错误响应
            Message response = new Message();
            response.setType(MessageType.ERROR);
            response.setContent("你已经进入了房间 " + userRooms.get(channel)+"，不能再创建新房间啦！: ");
            System.out.println("你已经进入了房间 " + userRooms.get(channel)+"，不能再创建新房间啦！: ");
            ctx.writeAndFlush(response);
            return;
        }

        // 将当前用户与房间关联
        userRooms.put(channel, roomName);
        this.createdRoomCount++;
        String roomId = String.valueOf(this.createdRoomCount);
        roomIdToName.put(roomId,roomName);
        // 创建房间
        // 创建响应消息
        Message response = new Message();
        response.setReceiver(roomId);
        System.out.println("房间" + roomName + "创建成功，房间号为：" + roomId);
        String userid= (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
        handleJoinRoom(ctx, response, userid);

    }
    @Override
    public void roomList(ChannelHandlerContext ctx, Message msg) throws Exception {
        // 获取所有房间ID
        List<String> roomIds = new ArrayList<>(roomIdToName.keySet());
        // 创建响应消息
        Message response = new Message();
        response.setType(MessageType.ROOM_LIST);
        //按房间数量新建一个RoomListResponse数组
        RoomListResponse[] roomListResponses = new RoomListResponse[roomIds.size()];

        for (int i = 0; i < roomIds.size(); i++) {
            String roomId = roomIds.get(i);
            // 创建一个RoomListResponse对象
            RoomListResponse roomListResponse = new RoomListResponse();
            roomListResponse.setRoomId(roomId);
            // 获取房间名
            String roomName = roomIdToName.get(roomId);
            roomListResponse.setRoomName(roomName);
            // 获取房间人数
            int userNumber = getUsersInRoom(roomId).size();
            roomListResponse.setUserNumber(userNumber);
            // 将RoomListResponse对象添加到数组中
            roomListResponses[i] = roomListResponse;
            System.out.println("房间" + roomName + "的房间号为：" + roomId + "，人数为：" + userNumber);
        }
        response.setContent(JSONUtil.toJsonString(roomListResponses)); // 将房间ID用逗号分隔成一个字符串
        // 将响应发送回客户端
        ctx.writeAndFlush(response);

    }
    //模块方法
    public Set<String> getUsersInRoom(String roomId) {
        ConcurrentHashMap<Channel, String> room = rooms.get(roomId);
        return room != null ? room.values().stream().collect(Collectors.toSet()) : Set.of();
    }
    @Override
    public String getUserRoom(Channel channel) {
        return userRooms.get(channel);
    }
    private void printRoomUserCount(String roomId) {
        ConcurrentHashMap<Channel, String> room = rooms.get(roomId);
        int userCount = (room != null) ? room.size() : 0;
        System.out.println("Current user count in room " + roomId + ": " + userCount);
    }
    private void printAllRoomsState() {
        System.out.println("Current state of all rooms: " + rooms);
    }

    class RoomListResponse{
        private String roomId;
        private String roomName;
        private int userNumber;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getRoomName() {
            return roomName;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public int getUserNumber() {
            return userNumber;
        }

        public void setUserNumber(int userNumber) {
            this.userNumber = userNumber;
        }

    }

}