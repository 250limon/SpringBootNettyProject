package csu.limon.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import csu.limon.chat.core.handler.AuthHandler;
import csu.limon.chat.core.handler.FriendHandler;
import csu.limon.chat.mapper.ApplyMapper;
import csu.limon.chat.mapper.UserMapper;
import csu.limon.chat.pojo.*;
import csu.limon.chat.service.FriendService;
import csu.limon.chat.mapper.FriendMapper;
import csu.limon.chat.service.UserService;
import csu.limon.chat.util.JSONUtil;
import csu.limon.chat.util.MessageSender;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService{

    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ApplyMapper applyMapper;
    @Override
    public boolean isFriendExist(String sender) {
        if (sender == null || sender.trim().isEmpty()) {
            return false;
        }
        try {
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void addFriend(ChannelHandlerContext ctx,Message msg) throws Exception {
        String existingUser = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
        Friend friend = friendMapper.selectOne(new QueryWrapper<Friend>()
                .eq("user_id", existingUser)
                .eq("friend_id", msg.getReceiver()));
        if (friend != null) {
            System.out.println("好友已添加");
            MessageSender.response(ctx,
                    new Message(MessageType.ERROR, null, null, "好友已添加"));
            return;
        }
        Apply apply = new Apply();
        apply.setApplicant(Integer.valueOf(existingUser));
        apply.setRespondent(Integer.valueOf(msg.getReceiver()));
        applyMapper.insert(apply);
        MessageSender.response(ctx,
                new Message(MessageType.SUCCESS, null, null, "好友申请已发送"));
    }

    @Override
    public void deleteFriend(ChannelHandlerContext ctx, Message msg) throws Exception {

    }

    @Override
    public void searchFriend(ChannelHandlerContext ctx, Message msg) throws Exception {

            User user = userMapper.selectById(Integer.parseInt(msg.getContent()));
            List<User>users=new ArrayList<>();
            users.add(user);
            // 将用户列表转为JSON字符串发送
            MessageSender.response(ctx,
                    new Message(MessageType.SEARCH_FRIEND, null, null, JSONUtil.toJsonString(users)));

    }

    @Override
    public void friendList(ChannelHandlerContext ctx, Message msg) throws Exception {

    }

    @Override
    public void findApplyList(ChannelHandlerContext ctx, Message msg) throws Exception {

    }

    @Override
    public void receiveApply(ChannelHandlerContext ctx, Message msg) throws Exception {

    }

    @Override
    public void rejectApply(ChannelHandlerContext ctx, Message msg) throws Exception {

    }

}




