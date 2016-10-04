package com.zhubo.api.response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhubo.api.response.RankResponse.RankItem;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AnchorIncomeByDays;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.ModelHelper;

@RestController
public class RankController {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private final int rankNumber = 10;
    
    @RequestMapping("/anchor_income_day_rank")
    public RankResponse getAnchorIncomeDayRank(@RequestParam(value = "platform_id") int platformId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AnchorIncomeByDays where platform_id = :platform_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("platform_id", platformId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AnchorIncomeByDays> records = query.list();
        Map<Long, Integer> incomeMapping = Maps.newHashMap();
        for(AnchorIncomeByDays record : records) {
            Integer oldIncome = incomeMapping.get(record.getAnchorId());
            if(oldIncome == null) {
                incomeMapping.put(record.getAnchorId(), 0);
                oldIncome = incomeMapping.put(record.getAnchorId(), 0);
            }            
            incomeMapping.put(record.getAnchorId(), oldIncome + record.getMoney());
        }
        
        List<RankItem> rankItems = Lists.newArrayList();
        for(Long anchorId : incomeMapping.keySet()) {
            rankItems.add(new RankItem(anchorId, null, null, null, incomeMapping.get(anchorId)));
        }
        Collections.sort(rankItems, new RankComparator());
        List<RankItem> finalRankItems = Lists.newArrayList();
        int index = 0;
        while(index < rankNumber && index < rankItems.size()) {
            RankItem item = rankItems.get(index);
            item.setRank(index + 1);
            Anchor anchor = ModelHelper.getAnchor(ResourceManager.generateResourceManager(), item.getId());           
            if(anchor != null) {
                item.setName(anchor.getAnchorName());
                item.setAliasID(anchor.getAnchorAliasId());
            }
            finalRankItems.add(item);
            index++;
        }
        return new RankResponse(finalRankItems);
    }
    
    private class RankComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            RankItem i1 = (RankItem) o1;
            RankItem i2 = (RankItem) o2;
            return i2.getValue() - i1.getValue();
        }
        
    }
}
