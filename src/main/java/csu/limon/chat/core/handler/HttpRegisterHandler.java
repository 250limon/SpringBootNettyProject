package csu.limon.chat.core.handler;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.pojo.User;
import csu.limon.chat.service.UserService;
import csu.limon.chat.util.JSONUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
@ChannelHandler.Sharable
@Component
public class HttpRegisterHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Autowired
    private UserService userService;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (request.uri().equals("/ws")) {
            ctx.fireChannelRead(request.retain());
            return;
        }


        // 处理 CORS 预检请求（OPTIONS）
        if (request.method().equals(HttpMethod.OPTIONS)) {
            sendOptionsResponse(ctx);
            return;
        }

        if (!request.uri().equals("/register") || !request.method().equals(HttpMethod.POST)) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "无效的请求");
            return;
        }

        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
        User user = new User();

        try {
            for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                    Attribute attribute = (Attribute) data;
                    if (attribute.getName().equals("username")) {
                        user.setName(attribute.getValue());
                    } else if (attribute.getName().equals("password")) {
                        user.setPassword(attribute.getValue());
                    }
                } else if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.FileUpload) {
                    FileUpload fileUpload = (FileUpload) data;
                    if (fileUpload.getName().equals("image") && fileUpload.isCompleted()) {
                        // 限制文件大小（例如 5MB）
                        if (fileUpload.length() > 5 * 1024 * 1024) {
                            sendError(ctx, HttpResponseStatus.BAD_REQUEST, "图片文件过大，最大支持5MB");
                            return;
                        }
                        String fileType=fileUpload.getFilename().substring(fileUpload.getFilename().lastIndexOf(".")+1);
                        String fileName = System.currentTimeMillis() + "." +fileType;
                        // 保存到 resources/images 目录
                        Path uploadDir = Paths.get("src/main/resources/static/images");
                        if (!Files.exists(uploadDir)) {
                            Files.createDirectories(uploadDir);
                        }
                        Path filePath = uploadDir.resolve(fileName);
                        fileUpload.renameTo(filePath.toFile());
                        user.setImage(fileName);
                    }
                }
            }

            if (user.getName() == null || user.getPassword() == null) {
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, "用户名和密码不能为空");
                return;
            }

            // 保存用户信息到数据库
            try {
                userService.register(user.getName(), user.getPassword(), user.getImage());
            } catch (RuntimeException e) {
                sendError(ctx, HttpResponseStatus.BAD_REQUEST, e.getMessage());
                return;
            }

            sendResponse(ctx, HttpResponseStatus.OK, JSONUtil.toJsonString(
                    new Message(MessageType.SUCCESS, null, null, "Register successful")));
        } catch (IOException e) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "图片上传失败: " + e.getMessage());
        } finally {
            decoder.destroy();
        }
    }

    private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String content) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        // 添加 CORS 头
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:63342");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        ctx.writeAndFlush(response);
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status, String message) throws Exception {
        String json = JSONUtil.toJsonString(new Message(MessageType.ERROR, null, null, message));
        sendResponse(ctx, status, json);
    }

    private void sendOptionsResponse(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:63342");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, OPTIONS");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.headers().set(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE, "86400");
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.err.println("HttpRegisterHandler error: " + cause.getMessage());
        try {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, "服务器错误: " + cause.getMessage());
        } catch (Exception e) {
            ctx.close();
        }
    }
}