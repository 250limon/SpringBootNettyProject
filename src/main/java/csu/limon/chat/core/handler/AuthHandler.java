package csu.limon.chat.core.handler;

import csu.limon.chat.mapper.UserMapper;
import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.service.UserService;
import csu.limon.chat.service.impl.UserServiceImpl;
import csu.limon.chat.util.JSONUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class AuthHandler extends ParentHandler {
    @Autowired
    private UserService userService;

    public AuthHandler() {
        super();
    }


    @Override
    protected void process(ChannelHandlerContext ctx, Message msg) throws Exception {

        if(msg.getType()==MessageType.LOGIN)
        {
            userService.login(ctx,msg);
        }


    }

    @Override
    void addMessageType(List<MessageType> messageTypes) {
        messageTypes.add(MessageType.LOGIN);

    }



    @Data
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

