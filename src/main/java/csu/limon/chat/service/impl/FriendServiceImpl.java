package csu.limon.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
        List<User>users=new ArrayList<>();
            //判断是否为数字
            if(msg.getContent().matches("\\d+")){//正则表达式，判断是否为数字
                User user = userMapper.selectById(Integer.parseInt(msg.getContent()));
                users.add(user);
                if(user==null){
                  List<User>  userList= userMapper.selectList(new QueryWrapper<User>()
                            .like("name", msg.getContent()));

                    users.addAll(userList);
                }

            }
            else{
                List<User> userList= userMapper.selectList(new QueryWrapper<User>()
                        .like("name", msg.getContent()));
                users.addAll(userList);
            }




            // 将用户列表转为JSON字符串发送
            MessageSender.response(ctx,
                    new Message(MessageType.SEARCH_FRIEND, null, null, JSONUtil.toJsonString(users)));

    }

    @Override
    public void friendList(ChannelHandlerContext ctx, Message msg) throws Exception {
           String sender = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
           List<Friend> friends = friendMapper.selectList(new LambdaQueryWrapper<Friend>()
                   .eq(Friend::getUserId, sender));

           List<User> userList = new ArrayList<>();
           for (Friend friend : friends) {
               User user = userMapper.selectById(friend.getFriendId());
               userList.add(user);
           }
           // 将用户列表转为JSON字符串发送
           MessageSender.response(ctx,
                   new Message(MessageType.FRIEND_LIST, null, null, JSONUtil.toJsonString(userList)));



    }

    @Override
    public void findApplyList(ChannelHandlerContext ctx, Message msg) throws Exception {
        String existingUser = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
        System.out.println("当前用户名："+existingUser);
        List<Apply> applyList = applyMapper.selectList(new QueryWrapper<Apply>()
                .eq("respondent", existingUser));
        MessageSender.response(ctx,new Message(MessageType.APPLY_LIST,null,null,JSONUtil.toJsonString(applyList)));
    }

    @Override
    public void receiveApply(ChannelHandlerContext ctx, Message msg) throws Exception {
        String existingUser = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
        List<Apply> applyList =applyMapper.selectList(new QueryWrapper<Apply>()
                .eq("respondent", existingUser));
        Apply apply =applyList.get(0);
        Friend friend1 = new Friend();
        friend1.setUserId(Integer.valueOf(existingUser));
        friend1.setFriendId(apply.getApplicant());
        friendMapper.insert(friend1);
        Friend friend2 = new Friend();
        friend2.setUserId(apply.getApplicant());
        friend2.setFriendId(Integer.valueOf(existingUser));
        friendMapper.insert(friend2);
        System.out.println("我的名字："+existingUser+"需添加好友名字："+apply.getApplicant());
        applyMapper.delete(new QueryWrapper<Apply>()
                .eq("respondent",existingUser)
                .eq("applicant",apply.getApplicant()));
        MessageSender.response(ctx, new Message(MessageType.SUCCESS, null, null, "同意添加好友"));
    }

    @Override
    public void rejectApply(ChannelHandlerContext ctx, Message msg) throws Exception {
        String existingUser = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
        List<Apply> applyList =applyMapper.selectList(new QueryWrapper<Apply>()
                .eq("respondent", existingUser));
        Apply apply =applyList.get(0);
        applyMapper.delete(new QueryWrapper<Apply>()
                .eq("respondent",existingUser)
                .eq("applicant",apply.getApplicant()));
        MessageSender.response(ctx, new Message(MessageType.SUCCESS, null, null, "不同意好友"));
    }

}




