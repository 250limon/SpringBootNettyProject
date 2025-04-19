package csu.limon.chat.pojo;

// MessageType.java
public enum MessageType {
    //TODO:返回的消息类型
    SUCCESS, ERROR,
    //TODO:接受的消息类型
    //心跳机制
    HEARTBEAT,
    //登陆注册,对应authHandler
    LOGIN,REGISTER,
    //加入房间，离开房间，对应chatRoomHandler
    JOIN_ROOM, LEAVE_ROOM,CREAT_ROOM,ROOM_LIST,
    //对应MessageHandler
    ROOM_MSG,FRIEND_MSG,FRIEND_GROUP_MSG,CHAT_HISTORY,USER_LIST,
    //对应FriendHandler
    ADD_FRIEND,DELETE_FRIEND,FRIEND_LIST,APPLY_LIST,RECEIVE_APPLY,REJECT_APPLY,
    //对应于LiveHandler
    LIVE_BROADCAST_FRIEND,LIVE_FEEDBACK


}
