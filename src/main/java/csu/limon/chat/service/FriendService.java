package csu.limon.chat.service;

import csu.limon.chat.pojo.Friend;
import com.baomidou.mybatisplus.extension.service.IService;
import csu.limon.chat.pojo.Message;
import io.netty.channel.ChannelHandlerContext;

/**
* @author 18632
* @description 针对表【friend】的数据库操作Service
* @createDate 2025-04-19 14:09:57
*/
public interface FriendService extends IService<Friend> {
     boolean isFriendExist(String sender);
     void addFriend(ChannelHandlerContext ctx,Message msg) throws Exception;
     void deleteFriend(ChannelHandlerContext ctx, Message msg) throws Exception;
     void searchFriend(ChannelHandlerContext ctx, Message msg) throws Exception;
     void friendList(ChannelHandlerContext ctx, Message msg) throws Exception;
     void findApplyList(ChannelHandlerContext ctx, Message msg) throws Exception;
     void receiveApply(ChannelHandlerContext ctx, Message msg) throws Exception;
     void rejectApply(ChannelHandlerContext ctx, Message msg) throws Exception;
}
