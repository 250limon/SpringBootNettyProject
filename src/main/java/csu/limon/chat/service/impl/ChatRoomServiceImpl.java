package csu.limon.chat.service.impl;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.pojo.User;
import csu.limon.chat.service.ChatRoomService;
import csu.limon.chat.util.JSONUtil;
import csu.limon.chat.util.MessageSender;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    private final ConcurrentHashMap<String, ConcurrentHashMap<Channel, String>> rooms = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Channel, String> userRooms = new ConcurrentHashMap<>();
    private int createdRoomCount = 0;
    private final ConcurrentHashMap<String, String> roomIdToName = new ConcurrentHashMap<>();

    @Autowired
    private UserServiceImpl userService;

    @Override
    public void joinRoom(String roomId, Channel channel, String username) {
        System.out.println("Before joining room " + roomId + ", current state: " + rooms.get(roomId));
        ConcurrentHashMap<Channel, String> room = rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>());

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
        printRoomUserCount(roomId);
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
                    roomIdToName.remove(roomId); // Ensure room is removed from name mapping
                    System.out.println("Room " + roomId + " destroyed (empty)");
                }
                printRoomUserCount(roomId);
                printAllRoomsState();
            }
        }
    }

    public void broadcastMessage(String roomId, Message msg) throws Exception {
        ConcurrentHashMap<Channel, String> room = rooms.get(roomId);

        if (room == null) {
            System.out.println("Room " + roomId + " does not exist or has been destroyed");
            return;
        }
        User user = userService.getUserById(msg.getSender());

            RoomMessageResponse response = new RoomMessageResponse();
            response.setContent(msg.getContent());
            response.setSender(user);
        msg.setContent(JSONUtil.toJsonString(response));
        System.out.println("Broadcasting message to room " + roomId + " with " + room.size() + " users: " + msg);
        for (Channel ch : room.keySet()) {
            if (ch.isActive()) {
                try {
                    MessageSender.response(ch.pipeline().lastContext(), msg);
                    System.out.println("Message sent to channel: " + ch + ", user: " + room.get(ch));
                } catch (Exception e) {
                    System.err.println("Failed to send message to channel " + ch + ": " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Skipping inactive channel: " + ch);
                leaveRoom(ch);
            }
        }
        printRoomUserCount(roomId);
        printAllRoomsState();
    }

    @Override
    public void sendUserList(String roomId) throws Exception {
        ConcurrentHashMap<Channel, String> room = rooms.get(roomId);
        if (room == null) {
            System.out.println("Room " + roomId + " does not exist or has been destroyed, skipping user list send");
            return;
        }
        Set<String> users = getUsersInRoom(roomId);
        String userListJson = JSONUtil.toJsonString(users);
        Message userListMsg = new Message(MessageType.USER_LIST, null, roomId, userListJson);
        broadcastMessage(roomId, userListMsg);
        System.out.println("Sent user list for room " + roomId + ": " + users);
    }

    @Override
    public void handleJoinRoom(ChannelHandlerContext ctx, Message msg, String userId) throws Exception {
        String roomId = msg.getReceiver();
        if (roomId == null) {
            MessageSender.response(ctx,
                    new Message(MessageType.ERROR, null, null, "Invalid room ID"));
            System.out.println("Rejected JOIN_ROOM: roomId is null");
            ctx.close();
            return;
        }

        // Check if room exists (prevent joining destroyed rooms)
        if (!rooms.containsKey(roomId) && !roomIdToName.containsKey(roomId)) {
            MessageSender.response(ctx,
                    new Message(MessageType.ERROR, null, null, "Room " + roomId + " does not exist or has been destroyed"));
            System.out.println("Rejected JOIN_ROOM: room " + roomId + " does not exist");
            return;
        }

        String currentRoom = getUserRoom(ctx.channel());
        if (currentRoom != null && currentRoom.equals(roomId)) {
            System.out.println("User " + userId + " already in room: " + roomId + ", ignoring JOIN_ROOM");
            MessageSender.response(ctx,
                    new Message(MessageType.SUCCESS, null, null, "Already in room: " + roomId));
            return;
        }

        if (currentRoom != null) {
            leaveRoom(ctx.channel());
            System.out.println("User " + userId + " left previous room: " + currentRoom);
        }

        joinRoom(roomId, ctx.channel(), userId);
        MessageSender.response(ctx,
                new Message(MessageType.SUCCESS, null, null, "Joined room: " + roomId));
        //sendUserList(roomId);
        System.out.println("User " + userId + " joined room: " + roomId + ", Channel: " + ctx.channel());
    }


    @Override
    public void handleLeaveRoom(ChannelHandlerContext ctx, Message msg, String userId) throws Exception {
        String roomId = msg.getReceiver();
        leaveRoom(ctx.channel());
        MessageSender.response(ctx,
                new Message(MessageType.SUCCESS, null, null, "Left room: " + roomId));
        sendUserList(roomId);
        System.out.println("User " + userId + " left room: " + roomId);
    }

    @Override
    public void creatRoom(ChannelHandlerContext ctx, Message msg) throws Exception {
        String roomName = msg.getContent();
        boolean roomExists = roomIdToName.containsValue(roomName); // Check room name in roomIdToName
        if (roomExists) {
            MessageSender.response(ctx,
                    new Message(MessageType.ERROR, null, null, "房间 " + roomName + " 已经存在啦，请改个名字吧！"));
            System.out.println("房间 " + roomName + " 已经存在啦，请改个名字吧！");
            return;
        }

        Channel channel = ctx.channel();
        if (userRooms.containsKey(channel)) {
            MessageSender.response(ctx,
                    new Message(MessageType.ERROR, null, null, "你已经进入了房间 " + userRooms.get(channel) + "，不能再创建新房间啦！"));
            System.out.println("你已经进入了房间 " + userRooms.get(channel) + "，不能再创建新房间啦！");
            return;
        }

        userRooms.put(channel, roomName);
        this.createdRoomCount++;
        String roomId = String.valueOf(this.createdRoomCount);
        roomIdToName.put(roomId, roomName);
        Message response = new Message();
        response.setReceiver(roomId);
        System.out.println("房间 " + roomName + " 创建成功，房间号为：" + roomId);
        String userid = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
        handleJoinRoom(ctx, response, userid);
    }

    @Override
    public void roomList(ChannelHandlerContext ctx, Message msg) throws Exception {
        // Only include rooms with active users
        List<String> roomIds = new ArrayList<>(rooms.keySet()); // Use rooms.keySet() to ensure only active rooms
        Message response = new Message();
        response.setType(MessageType.ROOM_LIST);
        RoomListResponse[] roomListResponses = new RoomListResponse[roomIds.size()];
        for (int i = 0; i < roomIds.size(); i++) {
            String roomId = roomIds.get(i);
            ConcurrentHashMap<Channel, String> room = rooms.get(roomId);
            if (room == null || room.isEmpty()) {
                continue; // Skip destroyed or empty rooms
            }
            RoomListResponse roomListResponse = new RoomListResponse();
            roomListResponse.setRoomId(roomId);
            String roomName = roomIdToName.get(roomId);
            roomListResponse.setRoomName(roomName);
            int userNumber = room.size();
            roomListResponse.setUserNumber(userNumber);
            roomListResponses[i] = roomListResponse;
            System.out.println("房间 " + roomName + " 的房间号为：" + roomId + "，人数为：" + userNumber);
        }
        response.setContent(JSONUtil.toJsonString(roomListResponses));
        MessageSender.response(ctx, response);
    }

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

    class RoomListResponse {
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
    class RoomMessageResponse{
        private String content;
        private User sender;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public User getSender() {
            return sender;
        }

        public void setSender(User sender) {
            this.sender = sender;
        }
    }
}