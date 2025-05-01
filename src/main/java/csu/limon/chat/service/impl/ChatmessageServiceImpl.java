package csu.limon.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import csu.limon.chat.pojo.Chatmessage;
import csu.limon.chat.service.ChatmessageService;
import csu.limon.chat.mapper.ChatmessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 18632
* @description 针对表【chatmessage】的数据库操作Service实现
* @createDate 2025-04-20 13:11:56
*/
@Service
public class ChatmessageServiceImpl extends ServiceImpl<ChatmessageMapper, Chatmessage>
    implements ChatmessageService{
    @Autowired
    private ChatmessageMapper chatmessageMapper;

    @Override
    public List<Chatmessage> getChatMessage(String user, String friend) {

        LambdaQueryWrapper<Chatmessage> queryWrapper = new LambdaQueryWrapper<Chatmessage>()
        .eq(Chatmessage::getSender, user)
        .eq(Chatmessage::getReceiver, friend).or()
                .eq(Chatmessage::getSender, friend)
                .eq(Chatmessage::getReceiver, user);
        List<Chatmessage> chatmessages = chatmessageMapper.selectList(queryWrapper);
        return chatmessages;
    }

    @Override
    public void insertChatMessage(Chatmessage chatmessage) {
           chatmessageMapper.insert(chatmessage);
    }
}




