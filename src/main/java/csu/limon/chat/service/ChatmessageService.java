package csu.limon.chat.service;

import csu.limon.chat.pojo.Chatmessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 18632
* @description 针对表【chatmessage】的数据库操作Service
* @createDate 2025-04-20 13:11:56
*/
public interface ChatmessageService extends IService<Chatmessage> {
      List<Chatmessage> getChatMessage(String user,String friend);
      void insertChatMessage(Chatmessage chatmessage);
}
