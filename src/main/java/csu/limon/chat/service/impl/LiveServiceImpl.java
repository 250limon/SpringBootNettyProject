package csu.limon.chat.service.impl;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.service.LiveService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;

@Service
public class LiveServiceImpl implements LiveService {

    @Override
    public void broadCast(ChannelHandlerContext ctx, Message msg) throws Exception {

    }

    @Override
    public void feedBack(ChannelHandlerContext ctx, Message msg) throws Exception {

    }
}
