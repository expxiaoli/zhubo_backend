package com.zhubo.task.parsepage.task;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.springframework.util.NumberUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mysql.jdbc.StringUtils;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AnchorIncomeByMinutes;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.entity.AnchorRoundIncomeByMinutes;
import com.zhubo.entity.Audience;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.entity.AudiencePayPeriod;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.DatabaseCache;
import com.zhubo.global.DatabaseCache.AnchorObject;
import com.zhubo.global.DatabaseCache.PayPeriodObject;
import com.zhubo.global.DatabaseCache.TopAudiencePayForOneAnchor;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;
import com.zhubo.helper.ModelHelper;
import com.zhubo.task.processdata.TimeUnit;

public class ParseAppRoomPageWithTopAudienceIdentifyTask extends BaseParsePageTask {
    private Integer income;
    private boolean needCommit;

    public ParseAppRoomPageWithTopAudienceIdentifyTask(String filePath, Set<Long> invalidAliasIds,
            ResourceManager resourceManager, int platformId) {
        super(filePath, invalidAliasIds, resourceManager, platformId);
        income = 0;
        needCommit = false;
    }

    public boolean run() throws JDOMException, IOException, PageFormatException {
        InputStreamReader input = new InputStreamReader(new FileInputStream(file));
        BufferedReader bufferedReader = new BufferedReader(input);
        String line = bufferedReader.readLine();
        try {
            JSONObject json = JSONObject.parseObject(line);
            parseAndStoreMetric(json);
            return true;
        }catch(com.alibaba.fastjson.JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void parseAndStoreMetric(JSONObject json) {
        Long dateTs = json.getLong("timestamp");
        Date pageDate = new Date(dateTs * 1000);
        Integer flowers = json.getInteger("fllowers");
        Long anchorAliasId = json.getLong("roomnum");
        String anchorName = json.getString("nick");
        String viplist = json.getString("viplist");
        List<Metric> metrics = Lists.newArrayList();
        if(flowers != null) {
            metrics.add(new Metric("星光", flowers));
        }
      
        Map<Long, Pay> pays = Maps.newHashMap();
        if(viplist != null) {
            JSONArray vips = JSONObject.parseArray(viplist);  
            for (int i = 0; i < vips.size(); i++) {
                JSONObject vip = (JSONObject) vips.get(i);
                String audienceName = null;
                Long audienceAliasId = null;
                if(vip.containsKey("user")) {
                    audienceName = vip.getJSONObject("user").getString("nick");
                    audienceAliasId = vip.getJSONObject("user").getLong("id");
                } else {
                    audienceName = vip.getString("nick");
                    audienceAliasId = vip.getLong("id");
                }
                Long money = vip.getLong("contribution");
                if(audienceAliasId != null && audienceName != null && money != null) {
                    pays.put(audienceAliasId, new Pay(audienceAliasId, audienceName, money));
                }
            }
        }
        if(anchorAliasId == null || anchorAliasId.equals(0L)) {
            System.out.println("-_-> anchor alias id is null or 0, ignore this page");
            return;
        }
        
        if (invalidAliasIds.contains(anchorAliasId)) {
            System.out.println("-_-> invalid alias id " + anchorAliasId + ", ignore this page");
            return;
        }
        
        Long anchorId = getAnchorIdOrNew(resourceManager, platformId, anchorAliasId, anchorName,
                pageDate);
        if(hasProcessed(anchorId, pageDate)) {
            System.out.println("-_-> old top pay ts is newer or same, ignore this page");
            return;
        }

        for (Metric metric : metrics) {
            if (!resourceManager.getDatabaseCache().existInMetricByMinutes(anchorId, metric.type,
                    pageDate)) {
                AnchorMetricByMinutes metricByMinutes = new AnchorMetricByMinutes(anchorId,
                        platformId, metric.type, metric.value, pageDate);
                resourceManager.getDatabaseSession().save(metricByMinutes);
                needCommit = true;
            }
        }

        if(pays.size() > 0) {
            boolean isOldRound = isOldRound(pays, anchorId, pageDate);

            if(!isOldRound) {
                resourceManager.getDatabaseCache().setLatestRoundStart(anchorId, pageDate);
                resourceManager.getDatabaseCache().setPayPeriodInCacheToZeroForOneAnchor(anchorId, new Date(pageDate.getTime() - 1000));
            }
            for (Pay pay : pays.values()) {
                Long audienceId = getAudienceIdOrNewOrUpdate(resourceManager, platformId,
                    pay.audienceName, pay.audienceAliasId);
                if (pay.money != null) {
                    storePayPeriodAndPayMinute(resourceManager, audienceId, anchorId, platformId,
                        isOldRound, pay.money, pageDate);
                }
            }            
            if (income > 0) {
                storeAnchorIncomeIfNeeded(resourceManager, anchorId, platformId, income, pageDate);
            }
            updateTopAudiencePayForOneAnchor(anchorId, pays, pageDate);
        }
        
        if (needCommit) {
            resourceManager.commit();
        } else {
            System.out.println("old page, ignore commit");
        }
    }
    
    private void updateTopAudiencePayForOneAnchor(long anchorId, Map<Long, Pay> pays, Date pageDate) {
        Pay maxPay = null;
        for(Pay pay : pays.values()) {
            if(maxPay == null || maxPay.money < pay.money) {
                maxPay = pay;
            }
        }
        long audienceId = resourceManager.getDatabaseCache().getIdFromAudienceAliasId(platformId, maxPay.audienceAliasId);
        resourceManager.getDatabaseCache().setTopAudiencePayForOneAnchor(anchorId, audienceId, maxPay.money, pageDate);
    }

    private boolean isOldRound(Map<Long, Pay> pays, long anchorId, Date pageDate) {
        TopAudiencePayForOneAnchor oldTopAudiencePay = resourceManager.getDatabaseCache().getTopAudiencePayForOneAnchor(anchorId);
        if(oldTopAudiencePay != null) {
            for(long audienceAliasId : pays.keySet()) {
                Long audienceId = resourceManager.getDatabaseCache().getIdFromAudienceAliasId(platformId, audienceAliasId);
                if(audienceId != null && audienceId.equals(oldTopAudiencePay.audienceId) && 
                        pageDate.compareTo(oldTopAudiencePay.recordEffectiveTime) > 0 && 
                        pays.get(audienceAliasId).money >= oldTopAudiencePay.money) {
                    return true;          
                }
            }
        }
        return false;
    }
    
    private boolean hasProcessed(long anchorId, Date pageDate) {
        TopAudiencePayForOneAnchor oldTopAudiencePay = resourceManager.getDatabaseCache().getTopAudiencePayForOneAnchor(anchorId);
        return oldTopAudiencePay != null && pageDate.compareTo(oldTopAudiencePay.recordEffectiveTime) <= 0;
    }

    private void storePayPeriodAndPayMinute(ResourceManager rm, long audienceId, long anchorId,
            int platformId, boolean isOldRound, long periodMoney, Date ts) {
        Date periodStart = getQixiuPayAggregateDate(ts);
        PayPeriodObject payPeriod = new PayPeriodObject(platformId, periodMoney, periodStart, ts);
        Integer diffMoney = resourceManager.getDatabaseCache()
                .getDiffMoneyAndUpdateLatestPayPeriodInCacheWithTopAudienceIdentify(audienceId, anchorId, isOldRound,
                        payPeriod);
        if (diffMoney != null && diffMoney != 0) {
            income += diffMoney;
            storeMinutePayIfNeeded(rm, audienceId, anchorId, platformId, diffMoney, ts);
            storePayPeriodIfNeeded(rm, audienceId, anchorId, platformId, periodMoney, ts,
                    periodStart);

        } else if (diffMoney == null) {
            storePayPeriodIfNeeded(rm, audienceId, anchorId, platformId, periodMoney, ts,
                    periodStart);
        }
    }

    private void storeMinutePayIfNeeded(ResourceManager rm, long audienceId, long anchorId,
            int platformId, int money, Date ts) {
        if (!rm.getDatabaseCache().existInPayByMinutes(audienceId, anchorId, ts)) {
            AudiencePayByMinutes payByMinutes = new AudiencePayByMinutes(audienceId, anchorId,
                    platformId, money, ts);
            rm.getDatabaseSession().save(payByMinutes);
            needCommit = true;
        } else {
            System.out.println(String.format(
                    "exist in minute pay for platform_id %d, anchor_id %d, audience_id %d, ignore",
                    platformId, anchorId, audienceId));
        }
    }

    private void storePayPeriodIfNeeded(ResourceManager rm, long audienceId, long anchorId,
            int platformId, long money, Date ts, Date periodStart) {
        if (!rm.getDatabaseCache().existInPayPeriod(audienceId, anchorId, ts)) {
            AudiencePayPeriod payPeriod = new AudiencePayPeriod(audienceId, anchorId, platformId,
                    money, ts, periodStart);
            rm.getDatabaseSession().save(payPeriod);
            needCommit = true;
        } else {
            System.out.println(String.format(
                    "exist in minute pay for platform_id %d, anchor_id %d, audience_id %d, ignore",
                    platformId, anchorId, audienceId));
        }
    }

    private void storeAnchorIncomeIfNeeded(ResourceManager rm, long anchorId, int platformId,
            int income, Date pageDate) {
        if (!rm.getDatabaseCache().existInAnchorIncomeByMinutes(anchorId, pageDate)) {
            AnchorIncomeByMinutes incomeByMinutes = new AnchorIncomeByMinutes(anchorId, platformId,
                    income, pageDate);
            rm.getDatabaseSession().save(incomeByMinutes);
            needCommit = true;
        } else {
            System.out.println(String.format(
                    "exist in minute pay for platform_id %d, anchor_id %d, ignore", platformId,
                    anchorId));
        }
    }

    private Date getQixiuPayAggregateDate(Date ts) {
        return GeneralHelper.getAggregateDate(ts, TimeUnit.WEEK);
    }

    public Long getAudienceIdOrNewOrUpdate(ResourceManager rm, int platformId, String audienceName,
            Long audienceAliasId) {
        DatabaseCache dbCache = rm.getDatabaseCache();
        Long oldAudienceIdFromAliasId = dbCache.getIdFromAudienceAliasId(platformId,
                audienceAliasId);
        if (oldAudienceIdFromAliasId == null) {
            Audience newAudience = new Audience(platformId, audienceAliasId, audienceName);
            rm.getDatabaseSession().save(newAudience);
            needCommit = true;
            rm.getDatabaseCache().setAudienceMapper(audienceAliasId, audienceName,
                    newAudience.getAudienceId());
            oldAudienceIdFromAliasId = newAudience.getAudienceId();
        }

        Long oldAudienceIdFromName = dbCache.getIdFromAudienceName(platformId, audienceName);
        if (oldAudienceIdFromName == null) {
            Audience oldAudience = ModelHelper.getAudience(rm, platformId, audienceAliasId, null);
            oldAudience.setAudienceName(audienceName);
            oldAudience.setLastUpdated(new Date());
            rm.getDatabaseSession().update(oldAudience);
            needCommit = true;
            rm.getDatabaseCache()
                    .setAudienceMapper(null, audienceName, oldAudience.getAudienceId());
        }
        return oldAudienceIdFromAliasId;
    }

    public Long getAnchorIdOrNew(ResourceManager rm, Integer platformId, Long anchorAliasId,
            String anchorName, Date pageDate) {
        AnchorObject cacheAnchorObject = rm.getDatabaseCache().getAnchorObjectFromCache(
                anchorAliasId);
        if (cacheAnchorObject != null) {
            return cacheAnchorObject.anchorId;
        }
        Anchor anchor = ModelHelper.getAnchor(rm, platformId, anchorAliasId);
        if (anchor == null) {
            System.out.println(String.format("platform_id %d, anchor_alias_id %d is not existed",
                    platformId, anchorAliasId));
            anchor = new Anchor(platformId, anchorAliasId, anchorName, pageDate);
            rm.getDatabaseSession().save(anchor);
            rm.commit();
            resourceManager.getDatabaseCache().setAnchorObjectInCache(anchor.getAnchorAliasId(),
                    new AnchorObject(anchor.getAnchorId(), anchor.getArea(), anchor.getType()));
        }
        return anchor.getAnchorId();
    }

    public static class Metric {
        public String type;
        public Integer value;

        public Metric(String type, Integer value) {
            this.type = type;
            this.value = value;
        }
    }

    public static class Pay {
        public Long audienceAliasId;
        public String audienceName;
        public Long money;

        public Pay(Long audienceAliasId, String audienceName, Long money) {
            this.audienceAliasId = audienceAliasId;
            this.audienceName = audienceName;
            this.money = money;
        }
    }
    /*
     * public static void main(String[] args) throws JDOMException, IOException,
     * ParseException, PageFormatException {
     * 
     * ParseRoomPageTask task = new
     * ParseRoomPageTask("/Users/xiao.li/coding/zhubo_data/0929",
     * ResourceManager.generateResourceManager(), 1); task.run(); }
     */
}
