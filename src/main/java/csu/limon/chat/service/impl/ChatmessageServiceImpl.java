package csu.limon.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import csu.limon.chat.pojo.Chatmessage;
import csu.limon.chat.service.ChatmessageService;
import csu.limon.chat.mapper.ChatmessageMapper;
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

    @Override
    public List<Chatmessage> getChatMessage(String user, String friend) {
        return null;
    }

    @Override
    public void insertChatMessage(Chatmessage chatmessage) {

    }
}




