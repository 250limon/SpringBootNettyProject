package csu.limon.chat.service;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import io.netty.channel.ChannelHandlerContext;

/**
* @author 18632
* @description 针对表【user】的数据库操作Service
* @createDate 2025-04-19 14:06:48
*/
public interface UserService extends IService<User> {
     boolean isAccountExist(String sender);
     boolean isPasswordRight(String sender,String password);
     String getUserNameById(String userId);


     void login(ChannelHandlerContext ctx,Message msg) throws Exception;
     void register(ChannelHandlerContext ctx,Message msg) throws Exception;
}
