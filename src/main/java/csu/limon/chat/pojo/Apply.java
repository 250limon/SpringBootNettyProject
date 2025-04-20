package csu.limon.chat.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * 
 * @TableName apply
 */
@TableName(value ="apply")
public class Apply implements Serializable {
    /**
     * 
     */
    private Integer respondent;

    /**
     * 
     */
    private Integer applicant;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public Integer getRespondent() {
        return respondent;
    }

    /**
     * 
     */
    public void setRespondent(Integer respondent) {
        this.respondent = respondent;
    }

    /**
     * 
     */
    public Integer getApplicant() {
        return applicant;
    }

    /**
     * 
     */
    public void setApplicant(Integer applicant) {
        this.applicant = applicant;
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
        Apply other = (Apply) that;
        return (this.getRespondent() == null ? other.getRespondent() == null : this.getRespondent().equals(other.getRespondent()))
            && (this.getApplicant() == null ? other.getApplicant() == null : this.getApplicant().equals(other.getApplicant()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRespondent() == null) ? 0 : getRespondent().hashCode());
        result = prime * result + ((getApplicant() == null) ? 0 : getApplicant().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", respondent=").append(respondent);
        sb.append(", applicant=").append(applicant);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}