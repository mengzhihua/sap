package com.sap.fi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sap.common.BizException;
import com.sap.fi.entity.PostingPeriod;
import com.sap.fi.mapper.PostingPeriodMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class PostingPeriodService {
    private final PostingPeriodMapper periods;
    public PostingPeriodService(PostingPeriodMapper periods) { this.periods = periods; }
    public List<PostingPeriod> list() { return periods.selectList(new LambdaQueryWrapper<PostingPeriod>().orderByDesc(PostingPeriod::getFiscalYear).orderByAsc(PostingPeriod::getBukrs)); }
    public PostingPeriod save(PostingPeriod p) {
        validate(p);
        if (p.getId() == null) periods.insert(p); else periods.updateById(p);
        return p;
    }
    public void delete(long id) { periods.deleteById(id); }
    public void assertOpen(String bukrs, LocalDate date) {
        List<PostingPeriod> configured = periods.selectList(new LambdaQueryWrapper<PostingPeriod>().eq(PostingPeriod::getBukrs, bukrs).eq(PostingPeriod::getFiscalYear, date.getYear()));
        if (configured.isEmpty()) return;
        int month = date.getMonthValue();
        if (configured.stream().noneMatch(p -> Boolean.TRUE.equals(p.getOpen()) && month >= p.getFromPeriod() && month <= p.getToPeriod()))
            throw new BizException(String.format("公司代码 %s 的 %d 年 %02d 期间已关闭（OB52）", bukrs, date.getYear(), month));
    }
    private void validate(PostingPeriod p) {
        if (p.getBukrs() == null || p.getBukrs().trim().isEmpty()) throw new BizException("公司代码不能为空");
        if (p.getFiscalYear() == null || p.getFiscalYear() < 2000 || p.getFiscalYear() > 9999) throw new BizException("会计年度不正确");
        if (p.getFromPeriod() == null || p.getToPeriod() == null || p.getFromPeriod() < 1 || p.getToPeriod() > 12 || p.getFromPeriod() > p.getToPeriod()) throw new BizException("期间范围必须为 1 至 12，且起始期间不能晚于结束期间");
        if (p.getOpen() == null) p.setOpen(true);
    }
}
