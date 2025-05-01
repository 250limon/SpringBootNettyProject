package csu.limon.chat.service;

import csu.limon.chat.pojo.Apply;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 18632
* @description 针对表【apply】的数据库操作Service
* @createDate 2025-04-20 13:12:07
*/
public interface ApplyService extends IService<Apply> {
    void insertApply(Apply apply);
    void deleteApply(Apply apply);
    List<Apply> getApplyByUserId(String userId);
    boolean caninsertApply(String applicant,String respondent);
}
