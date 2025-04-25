package csu.limon.chat.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import csu.limon.chat.core.handler.AuthHandler;
import csu.limon.chat.mapper.UserMapper;
import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.pojo.User;
import csu.limon.chat.service.UserService;
import csu.limon.chat.util.JSONUtil;
import csu.limon.chat.util.MessageSender;
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
        User user=userMapper.selectById(Integer.parseInt(sender));
        if(user!=null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPasswordRight(String sender, String password) {
        User user=userMapper.selectById(Integer.parseInt(sender));
        if(user.getPassword().equals(password)) {
            return true;
        }
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
        isValidCredentials(loginRequest,ctx);
    }

    @Override
    public void register(String username, String password, String image) throws Exception {
        User user=new User();
        user.setName(username);
        user.setPassword(password);
        user.setImage(image);
        userMapper.insert(user);
    }

    private void isValidCredentials(AuthHandler.LoginRequest loginRequest,ChannelHandlerContext ctx) {
        if(loginRequest.getUsername()!=null && loginRequest.getPassword()!=null) {
            if(!isAccountExist(loginRequest.getUsername())){
                try {
                    System.out.println("账户不存在!");
                    MessageSender.response(ctx,new Message(MessageType.ERROR,null,null,"账户不存在!"));
                    ctx.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                if(!isPasswordRight(loginRequest.getUsername(),loginRequest.getPassword())){
                    try {
                        System.out.println("密码不正确!");
                        MessageSender.response(ctx,new Message(MessageType.ERROR,null,null,"密码不正确!"));
                        ctx.close();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                else {
                    try {
                        System.out.println("Login successful");
                        ctx.channel().attr(AttributeKey.valueOf("user")).set(loginRequest.getUsername());
                        MessageSender.response(ctx,new Message(MessageType.SUCCESS,null,null,"Login successful"));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

    }




    @Override
    public void getUser(ChannelHandlerContext ctx, Message msg) throws Exception {
        String userId=msg.getContent();
        User user=userMapper.selectById(Integer.parseInt(userId));
        String Juser=JSONUtil.toJsonString(user);
        String Username = user.getName();
        String Password = user.getPassword();
        String Image = user.getImage();
        MessageSender.response(ctx,new Message(MessageType.GET_USER,null,null,Juser));
    }

    class RegisterRequest {
        private String Username;
        private String Password;
        private String Image;

        public String getUsername() {
            return Username;
        }

        public void setUsername(String username) {
            Username = username;
        }

        public String getPassword() {
            return Password;
        }

        public void setPassword(String password) {
            Password = password;
        }

        public String getImage() {
            return Image;
        }

        public void setImage(String image) {
            Image = image;
        }
    }
}




