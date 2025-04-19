package csu.limon.chat.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import csu.limon.chat.pojo.Friend;
import csu.limon.chat.service.FriendService;
import csu.limon.chat.mapper.FriendMapper;
import org.springframework.stereotype.Service;

/**
* @author 18632
* @description 针对表【friend】的数据库操作Service实现
* @createDate 2025-04-19 14:09:57
*/
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend>
    implements FriendService{

}




