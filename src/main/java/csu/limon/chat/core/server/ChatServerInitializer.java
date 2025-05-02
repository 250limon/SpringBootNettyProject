package csu.limon.chat.core.server;

import csu.limon.chat.core.codec.MessageEncoder;
import csu.limon.chat.core.handler.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class ChatServerInitializer extends ChannelInitializer<SocketChannel> {
     private final AuthHandler authHandler;
    private final WebSocketFrameHandler webSocketFrameHandler;
    private final MessageEncoder messageEncoder;
    private final HeartbeatHandler heartbeatHandler;
    private final ChatRoomHandler chatRoomHandler;
    private final MessageHandler messageHandler;
    private final LiveHandler liveHandler;
    private final FriendHandler friendHandler;
    private final HttpRegisterHandler httpRegisterHandler;





    @Autowired
    public ChatServerInitializer(
            AuthHandler authHandler,
            WebSocketFrameHandler webSocketFrameHandler,
            MessageEncoder messageEncoder,
            HeartbeatHandler heartbeatHandler,
            ChatRoomHandler chatRoomHandler,
            MessageHandler messageHandler,
            LiveHandler liveHandler,
            FriendHandler friendHandler,
            HttpRegisterHandler httpRegisterHandler) {
        this.authHandler = authHandler;
        this.webSocketFrameHandler = webSocketFrameHandler;
        this.messageEncoder = messageEncoder;
        this.heartbeatHandler = heartbeatHandler;
        this.chatRoomHandler = chatRoomHandler;
        this.messageHandler = messageHandler;
        this.liveHandler = liveHandler;
        this.friendHandler = friendHandler;
        this.httpRegisterHandler = httpRegisterHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(10 * 1024 * 1024))
                .addLast(httpRegisterHandler)
                .addLast(new WebSocketServerProtocolHandler("/ws"))
                .addLast(webSocketFrameHandler)
                .addLast(messageEncoder)
                .addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS))
                .addLast(heartbeatHandler)
                .addLast(authHandler)
                .addLast(chatRoomHandler)
                .addLast(messageHandler)
                .addLast(liveHandler)
                .addLast(friendHandler);

        System.out.println("Initialized pipeline for channel: " + ch);
    }
}