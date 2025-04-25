package csu.limon.chat.service;

import csu.limon.chat.pojo.Apply;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 18632
* @description 针对表【apply】的数据库操作Service
* @createDate 2025-04-20 13:12:07
*/
public interface ApplyService extends IService<Apply> {
    void insertApply(Apply apply);
}
