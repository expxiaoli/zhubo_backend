package com.zhubo.api.response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.global.ResourceManager;

@RestController
public class AudienceTotalPayController {
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    
    @RequestMapping("/audience_total_pay_minute")
    public AudienceTotalPayResponse getMetricMinute(@RequestParam(value = "audience_id") Long audienceId,
            @RequestParam(value = "start") String start, @RequestParam(value = "end") String end) throws ParseException {
        Date startDate = sdf.parse(start);
        Date endDate = sdf.parse(end);
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session
                .createQuery("from AudiencePayByMinutes where audience_id = :audience_id "
                        + "and record_effective_time >= :start_date and record_effective_time < :end_date");
        query.setParameter("audience_id", audienceId);
        query.setParameter("start_date", startDate);
        query.setParameter("end_date", endDate);
        List<AudiencePayByMinutes> metrics = query.list();
        List<MetricItem> items = Lists.newArrayList();
        for (AudiencePayByMinutes metric : metrics) {
            items.add(new MetricItem(metric.getMoney(), metric.getRecordEffectiveTime()));
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return new AudienceTotalPayResponse(audienceId, items);
    }
}
