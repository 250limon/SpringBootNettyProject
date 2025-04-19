package csu.limon.chat.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import csu.limon.chat.core.handler.AuthHandler;
import csu.limon.chat.mapper.UserMapper;
import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.pojo.User;
import csu.limon.chat.service.UserService;
import csu.limon.chat.util.JSONUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private  UserMapper userMapper;

    @Override
    public boolean isAccountExist(String sender) {
        // 实现数据库查询逻辑
        return false;
    }

    @Override
    public boolean isPasswordRight(String sender, String password) {
        // 实现数据库查询逻辑
        return false;
    }

    @Override
    public String getUserNameById(String userId) {
        return null;
    }

    @Override
    public void login(ChannelHandlerContext ctx, Message msg) throws Exception {
        AuthHandler.LoginRequest loginRequest = JSONUtil.parse(msg.getContent().getBytes(), AuthHandler.LoginRequest.class);
        System.out.println("Parsed login request: " + loginRequest);

        String existingUser = (String) ctx.channel().attr(AttributeKey.valueOf("user")).get();
        if (existingUser != null) {
            System.out.println("Channel " + ctx.channel() + " already authenticated as: " + existingUser);
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Channel already authenticated"))
            ));
            ctx.close();
            return;
        }

        if (isValidCredentials(loginRequest)) {
            ctx.channel().attr(AttributeKey.valueOf("user")).set(loginRequest.getUsername());
            System.out.println("User authenticated: " + loginRequest.getUsername() + ", Channel: " + ctx.channel());
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.SUCCESS, null, null, "Login successful"))
            ));
        } else {
            ctx.writeAndFlush(new TextWebSocketFrame(
                    JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, "Invalid credentials"))
            ));
            ctx.close();
        }
    }

    private boolean isValidCredentials(AuthHandler.LoginRequest loginRequest) {
        // 替换为数据库查询
        return (loginRequest.getUsername().equals("1") && loginRequest.getPassword().equals("2")) ||
                (loginRequest.getUsername().equals("user1") && loginRequest.getPassword().equals("password1"));
    }

    @Override
    public void register(ChannelHandlerContext ctx, Message msg) {
        // 实现注册逻辑
    }
}




