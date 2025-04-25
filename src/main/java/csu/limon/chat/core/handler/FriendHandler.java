package csu.limon.chat.core.handler;

import csu.limon.chat.pojo.Message;
import csu.limon.chat.pojo.MessageType;
import csu.limon.chat.service.FriendService;
import csu.limon.chat.service.MessageService;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class FriendHandler extends ParentHandler{
    public FriendHandler() {
        super();
    }
    @Autowired
    MessageService messageService;
    @Autowired
    FriendService friendService;

    @Override
    protected void process(ChannelHandlerContext ctx, Message msg) throws Exception {
        if(msg.getType()==MessageType.ADD_FRIEND){
            friendService.addFriend(ctx,msg);
        }
        else if(msg.getType()==MessageType.DELETE_FRIEND){
            friendService.deleteFriend(ctx,msg);
        }
        else if(msg.getType()==MessageType.FRIEND_LIST){
            friendService.friendList(ctx,msg);
        }
        else if(msg.getType()==MessageType.SEARCH_FRIEND){
            friendService.searchFriend(ctx,msg);
        }
        else if(msg.getType()==MessageType.APPLY_LIST){
            friendService.findApplyList(ctx,msg);
        }
        else if(msg.getType()==MessageType.RECEIVE_APPLY){
            friendService.receiveApply(ctx,msg);
        }
        else if(msg.getType()==MessageType.REJECT_APPLY){
            friendService.rejectApply(ctx,msg);
        }
    }

    @Override
    void addMessageType(List<MessageType> messageTypes) {
        messageTypes.add(MessageType.ADD_FRIEND);
        messageTypes.add(MessageType.DELETE_FRIEND);
        messageTypes.add(MessageType.FRIEND_LIST);
        messageTypes.add(MessageType.APPLY_LIST);
        messageTypes.add(MessageType.RECEIVE_APPLY);
        messageTypes.add(MessageType.REJECT_APPLY);
        messageTypes.add(MessageType.SEARCH_FRIEND);
    }
}
