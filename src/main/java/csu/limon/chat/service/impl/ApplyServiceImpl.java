package csu.limon.chat.service.impl;

import com.baomidou.mybatisplus.core.assist.ISqlRunner;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import csu.limon.chat.pojo.Apply;
import csu.limon.chat.service.ApplyService;
import csu.limon.chat.mapper.ApplyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author luoru
 * @description 针对表【apply】的数据库操作Service实现
 * @createDate 2025-04-22 00:00:33
 */
@Service
public class ApplyServiceImpl extends ServiceImpl<ApplyMapper, Apply> implements ApplyService{
    @Autowired
    private ApplyMapper applyMapper;
    @Override
    public void insertApply(Apply apply) {
        if(apply == null || apply.getApplicant() == null || apply.getRespondent() == null) {
            throw new IllegalArgumentException("申请参数不完整");
        }

        Apply existing = applyMapper.selectOne(new QueryWrapper<Apply>()
                .eq("applicant", apply.getApplicant())
                .eq("respondent", apply.getRespondent())
                .eq("status", 0));

        if(existing != null) {
            throw new RuntimeException("已存在待处理的好友申请");
        }

//        apply.setStatus(0); // 0表示待处理
//        apply.setApplyTime(new Date());

        int result = applyMapper.insert(apply);

        if(result <= 0) {
            throw new RuntimeException("好友申请插入失败");
        }
    }

}




