package csu.limon.chat.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * 
 * @TableName chatmessage
 */
@TableName(value ="chatmessage")
public class Chatmessage implements Serializable {
    /**
     * 
     */
    private Integer sender;

    /**
     * 
     */
    private Integer receiver;

    /**
     * 
     */
    private String content;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public Integer getSender() {
        return sender;
    }

    /**
     * 
     */
    public void setSender(Integer sender) {
        this.sender = sender;
    }

    /**
     * 
     */
    public Integer getReceiver() {
        return receiver;
    }

    /**
     * 
     */
    public void setReceiver(Integer receiver) {
        this.receiver = receiver;
    }

    /**
     * 
     */
    public String getContent() {
        return content;
    }

    /**
     * 
     */
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Chatmessage other = (Chatmessage) that;
        return (this.getSender() == null ? other.getSender() == null : this.getSender().equals(other.getSender()))
            && (this.getReceiver() == null ? other.getReceiver() == null : this.getReceiver().equals(other.getReceiver()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSender() == null) ? 0 : getSender().hashCode());
        result = prime * result + ((getReceiver() == null) ? 0 : getReceiver().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", sender=").append(sender);
        sb.append(", receiver=").append(receiver);
        sb.append(", content=").append(content);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}