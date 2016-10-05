package com.zhubo.task.parsepage.task;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mysql.jdbc.StringUtils;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AnchorIncomeByMinutes;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.entity.Audience;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.entity.AudiencePayPeriod;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.DatabaseCache;
import com.zhubo.global.DatabaseCache.AnchorObject;
import com.zhubo.global.DatabaseCache.PayPeriodObject;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;
import com.zhubo.helper.ModelHelper;
import com.zhubo.task.processdata.TimeUnit;

public class ParseRoomPageTask extends BaseParsePageTask {
    private Integer income;
    private boolean needCommit;

    public ParseRoomPageTask(String filePath, ResourceManager resourceManager, int platformId) {
        super(filePath, resourceManager, platformId);
        income = 0;
        needCommit = false;
    }

    public boolean run() throws JDOMException, IOException, PageFormatException {
        SAXBuilder builder = new SAXBuilder();
        Document document = null;
        Element root = null;
        Element dataElement = null;
        long start = System.currentTimeMillis();
        
        try {
            document = builder.build(file);

            root = document.getRootElement();
            dataElement = root
                    .getChild(
                            "Body",
                            Namespace.getNamespace("SOAP-ENV",
                                    "http://schemas.xmlsoap.org/soap/envelope/"))
                    .getChild(
                            "PostDocument",
                            Namespace.getNamespace("Spider",
                                    "urn:http://service.sina.com.cn/spider")).getChild("document");
        } catch (JDOMException e) {
            e.printStackTrace();
            throw new PageFormatException("JDOMException happens");
        } catch (IOException e) {
            e.printStackTrace();
            throw new PageFormatException("IOException happens");
        }
        try {
            String pageClass = dataElement.getChild("class").getValue();
            String pagePlatform = dataElement.getChild("platform").getValue();
            String dataStr = dataElement.getChild("date").getValue();
            Date pageDate = GeneralHelper.parseWithMultipleFormats(dataStr);
            Element allContItemElement = dataElement.getChild("cont_items");
            long end = System.currentTimeMillis();            
            System.out.println("*** ParseRoomPageTask parse xml: " + (end-start));
           
            parseAndStoreMetric(allContItemElement, pageDate);
            long end2 = System.currentTimeMillis();
            System.out.println("************** ParseRoomPageTask: " + (end2-start) + "\n\n");
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            throw new PageFormatException("platform, time or type element is not existed");
        }

    }

    private void parseAndStoreMetric(Element root, Date pageDate) {
        List<Element> itemElements = root.getChildren();
        Long anchorAliasId = null;
        String anchorName = null;
        List<Metric> metrics = Lists.newArrayList();
        Map<String, Pay> pays = Maps.newHashMap();
        long step1 = System.currentTimeMillis();
        for (Element itemElement : itemElements) {
            if (itemElement.getChild("cont_item_name") != null) {
                String itemName = itemElement.getChildText("cont_item_name");
                String itemBody = itemElement.getChildText("cont_item_body");
                if (itemName.equals("房间号")) {
                    anchorAliasId = Long.valueOf(itemBody);
                } else if (itemName.equals("昵称")) {
                    anchorName = itemBody;
                } else {
                    metrics.add(new Metric(itemName, Integer.valueOf(itemBody)));
                }
            } else if (itemElement.getChild("top_name") != null) {
                String audienceName = itemElement.getChildText("top_name");
                Long audienceAliasId = StringUtils.isNullOrEmpty(itemElement
                        .getChildText("vipuserid")) ? null : Long.valueOf(itemElement
                        .getChildText("vipuserid"));
                Integer money = StringUtils.isNullOrEmpty(itemElement.getChildText("top_money")) ? null
                        : Integer.valueOf(itemElement.getChildText("top_money"));
                pays.put(audienceName, new Pay(audienceAliasId, audienceName, money));

            }
        }
        long step2 = System.currentTimeMillis();
        System.out.println("*** ParseRoomPageTask parseAndStoreMetric parse xml more: " + (step2-step1));

        Long anchorId = getAnchorIdOrNew(resourceManager, platformId, anchorAliasId, anchorName, pageDate);
        long step3 = System.currentTimeMillis();
        System.out.println("*** ParseRoomPageTask parseAndStoreMetric getAnchorOrNew: " + (step3-step2));
        
        for (Metric metric : metrics) {
            if (!resourceManager.getDatabaseCache().existInMetricByMinutes(anchorId,
                    metric.type, pageDate)) {
                AnchorMetricByMinutes metricByMinutes = new AnchorMetricByMinutes(
                        anchorId, platformId, metric.type, metric.value, pageDate);
                resourceManager.getDatabaseSession().save(metricByMinutes);
                needCommit = true;
            }
        }
        long step4 = System.currentTimeMillis();
        System.out.println("*** ParseRoomPageTask parseAndStoreMetric saveMetricIfNeeded: " + (step4-step3));

        for (Pay pay : pays.values()) {
            long step11 = System.currentTimeMillis();
            Long audienceId = getAudienceIdOrNewOrUpdate(resourceManager, platformId,
                    pay.audienceName, pay.audienceAliasId);
            long step12 = System.currentTimeMillis();
            System.out.println("* ParseRoomPageTask parseAndStoreMetric search audience one: " + (step12 - step11));
            if (pay.money != null) {
                storePayPeriodAndPayMinute(resourceManager, audienceId, anchorId,
                        platformId, pay.money, pageDate);
                long step13 = System.currentTimeMillis();
                System.out.println("* ParseRoomPageTask parseAndStoreMetric storePayPeriodAndPayMinute one: " + (step13 - step12));
            }
        }
        long step5 = System.currentTimeMillis();
        System.out.println("*** ParseRoomPageTask parseAndStoreMetric savePayPeriodAndPayByMinutes total: " + (step5-step4));

        if (income > 0) {
            storeAnchorIncomeIfNeeded(resourceManager, anchorId, platformId, income,
                    pageDate);
        }
        long step6 = System.currentTimeMillis();
        System.out.println("*** ParseRoomPageTask parseAndStoreMetric saveIncome: " + (step6-step5));
        
        if(needCommit) {
            resourceManager.commit();
        } else {
            System.out.println("old page, ignore commit");
        }
        
        long step7 = System.currentTimeMillis();
        System.out.println("*** ParseRoomPageTask parseAndStoreMetric commit: " + (step7-step6));
    }

    private void storePayPeriodAndPayMinute(ResourceManager rm, long audienceId, long anchorId,
            int platformId, int periodMoney, Date ts) {
        Date periodStart = getQixiuPayAggregateDate(ts);
        PayPeriodObject payPeriod = new PayPeriodObject(platformId, periodMoney, periodStart, ts);
        Integer diffMoney = resourceManager.getDatabaseCache()
                .getDiffMoneyAndUpdateLatestPayPeriodInCache(audienceId, anchorId, payPeriod);
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
            int platformId, int money, Date ts, Date periodStart) {
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

    public Long getAudienceIdOrNewOrUpdate(ResourceManager rm, int platformId,
            String audienceName, Long audienceAliasId) {
        DatabaseCache dbCache = rm.getDatabaseCache();
        Long audienceIdInCache = dbCache.getIdFromAudienceAliasIdOrAudienceName(platformId,
                audienceAliasId, audienceName);
        if (audienceIdInCache != null) {
            return audienceIdInCache;
        }

        Audience oldAudience = ModelHelper.getAudience(rm, platformId, audienceAliasId, audienceName);
        if (oldAudience == null) {
            Audience newAudience = new Audience(platformId, audienceAliasId, audienceName);
            rm.getDatabaseSession().save(newAudience);
            needCommit = true;
            return newAudience.getAudienceId();
        } else if (audienceName != null && 
                (oldAudience.getAudienceName() == null || !audienceName.equals(oldAudience.getAudienceName()))) {
            oldAudience.setAudienceName(audienceName);
            rm.getDatabaseSession().update(oldAudience);
            needCommit = true;
            return oldAudience.getAudienceId();
        } else if(audienceAliasId != null &&
                (oldAudience.getAudienceAliasId() == null || !audienceAliasId.equals(oldAudience.getAudienceAliasId()))){
            oldAudience.setAudienceAliasId(audienceAliasId);
            rm.getDatabaseSession().update(oldAudience);
            needCommit = true;
            return oldAudience.getAudienceId();
        } else {
            return oldAudience.getAudienceId();
        }
    }

    public Long getAnchorIdOrNew(ResourceManager rm, Integer platformId, Long anchorAliasId,
            String anchorName, Date pageDate) {
        AnchorObject cacheAnchorObject = rm.getDatabaseCache().getAnchorObjectFromCache(anchorAliasId);
        if(cacheAnchorObject != null) {
            return cacheAnchorObject.anchorId;
        }
        Anchor anchor = ModelHelper.getAnchor(rm, platformId, anchorAliasId);
        if (anchor == null) {
            System.out.println(String.format("platform_id %d, anchor_alias_id %d is not existed",
                    platformId, anchorAliasId));
            anchor = new Anchor(platformId, anchorAliasId, anchorName, pageDate);
            rm.getDatabaseSession().save(anchor);
            rm.commit();
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
        public Integer money;

        public Pay(Long audienceAliasId, String audienceName, Integer money) {
            this.audienceAliasId = audienceAliasId;
            this.audienceName = audienceName;
            this.money = money;
        }
    }
/*
    public static void main(String[] args) throws JDOMException, IOException, ParseException, PageFormatException {

        ParseRoomPageTask task = new ParseRoomPageTask("/Users/xiao.li/coding/zhubo_data/0929",
                ResourceManager.generateResourceManager(), 1);
        task.run();
    }
*/
}
