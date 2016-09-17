package com.zhubo.task.parsepage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.GenericJDBCException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mysql.jdbc.StringUtils;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.entity.Audience;
import com.zhubo.entity.AudiencePayByMinutes;
import com.zhubo.entity.AudiencePayPeriod;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;
import com.zhubo.helper.ModelHelper;
import com.zhubo.helper.ModelHelper.PayPeriodObject;
import com.zhubo.task.processdata.TimeUnit;

public class ParseQixiuRoomPageTask extends BaseParsePageTask {
    private static Integer platformId = 1;
    private static final String curPlatform = "奇秀";
    private static final String curClass = "主播房间";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ParseQixiuRoomPageTask(String filePath, ResourceManager resourceManager) {
        super(filePath, resourceManager);
    }

    public boolean run() throws JDOMException, IOException, PageFormatException {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(file);

        Element root = document.getRootElement();
        Element dataElement = root
                .getChild(
                        "Body",
                        Namespace.getNamespace("SOAP-ENV",
                                "http://schemas.xmlsoap.org/soap/envelope/"))
                .getChild("PostDocument",
                        Namespace.getNamespace("Spider", "urn:http://service.sina.com.cn/spider"))
                .getChild("document");

        String pageClass = dataElement.getChild("class").getValue();
        if (pageClass.equals(curClass)) {
            try {
                String pagePlatform = dataElement.getChild("platform").getValue();
                String dataStr = dataElement.getChild("date").getValue();
                Date pageDate = sdf.parse(dataStr);
                Element allContItemElement = dataElement.getChild("cont_items");
                parseAndStoreMetric(allContItemElement, pageDate);
                return true;
            } catch (ParseException e) {
                throw new PageFormatException("platform, time or type element is not existed");
            }
        } else {
            return false;
        }

    }

    private void parseAndStoreMetric(Element root, Date pageDate) {
        List<Element> itemElements = root.getChildren();
        Long anchorAliasId = null;
        String anchorName = null;
        List<Metric> metrics = Lists.newArrayList();
        Map<String, Pay> pays = Maps.newHashMap();
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
            } else if (itemElement.getChild("vipnickname") != null) {
                String audienceName = itemElement.getChildText("vipnickname");
                Long audienceAliasId = StringUtils.isNullOrEmpty(itemElement
                        .getChildText("vipuserid")) ? null : Long.valueOf(itemElement
                        .getChildText("vipuserid"));
                Integer money = StringUtils.isNullOrEmpty(itemElement.getChildText("top_money")) ? null
                        : Integer.valueOf(itemElement.getChildText("top_money"));
                pays.put(audienceName, new Pay(audienceAliasId, audienceName, money));

            }
        }

        Anchor anchor = getAnchorOrNew(resourceManager, platformId, anchorAliasId, anchorName);
        for (Metric metric : metrics) {
            if (ModelHelper.getMetric(resourceManager, anchor.getAnchorId(), metric.type, pageDate) == null) {
                AnchorMetricByMinutes metricByMinutes = new AnchorMetricByMinutes(
                        anchor.getAnchorId(), metric.type, metric.value, pageDate);
                resourceManager.getDatabaseSession().save(metricByMinutes);
            }
        }
        for (Pay pay : pays.values()) {
            Audience audience = getAudienceOrNewOrUpdate(resourceManager, platformId,
                    pay.audienceName, pay.audienceAliasId);
            if (pay.money != null) {
                storePayPeriodAndPayMinute(resourceManager, audience.getAudienceId(),
                        anchor.getAnchorId(), platformId, pay.money, pageDate);
            }
        }
        resourceManager.commit();
    }

    private void storePayPeriodAndPayMinute(ResourceManager rm, long audienceId, long anchorId,
            int platformId, int periodMoney, Date ts) {
        Map<Long, Map<Long, PayPeriodObject>> payPeriodCache = rm.getPayPeriodCache();
        Date periodStart = getQixiuPayAggregateDate(ts);
        PayPeriodObject payPeriod = new PayPeriodObject(platformId, periodMoney, periodStart, ts);
        int diffMoney = ModelHelper.getDiffMoneyAndUpdatePayPeriodInCache(payPeriodCache, audienceId, anchorId, payPeriod);
        if(diffMoney != 0) {
            storeMinutePayIfNeeded(rm, audienceId, anchorId, platformId, diffMoney, ts);
        }
    }

    private void storeMinutePayIfNeeded(ResourceManager rm, long audienceId, long anchorId,
            int platformId, int money, Date ts) {
        if (ModelHelper.getPay(rm, audienceId, anchorId, ts) == null) {
            AudiencePayByMinutes payByMinutes = new AudiencePayByMinutes(audienceId, anchorId,
                    platformId, money, ts);
            rm.getDatabaseSession().save(payByMinutes);
        }
    }

    private Date getQixiuPayAggregateDate(Date ts) {
        return GeneralHelper.getAggregateDate(ts, TimeUnit.WEEK);
    }

    public static Audience getAudienceOrNewOrUpdate(ResourceManager rm, int platformId,
            String audienceName, Long audienceAliasId) {
        Audience oldAudience = ModelHelper.getAudience(rm, platformId, audienceName);
        if (oldAudience == null) {
            Audience newAudience = new Audience(platformId, audienceAliasId, audienceName);
            rm.getDatabaseSession().save(newAudience);
            rm.commit();
            return newAudience;
        } else if (audienceAliasId != null && oldAudience.getAudienceAliasId() == null) {
            oldAudience.setAudienceAliasId(audienceAliasId);
            rm.getDatabaseSession().update(oldAudience);
            rm.commit();
            return oldAudience;
        } else {
            return oldAudience;
        }
    }

    public Anchor getAnchorOrNew(ResourceManager rm, Integer platformId, Long anchorAliasId,
            String anchorName) {
        Anchor anchor = ModelHelper.getAnchor(rm, platformId, anchorAliasId);
        if (anchor == null) {
            System.out.println(String.format("platform_id %d, anchor_alias_id %d is not existed",
                    platformId, anchorAliasId));
            Anchor newAnchor = new Anchor(platformId, anchorAliasId, anchorName);
            rm.getDatabaseSession().save(newAnchor);
            rm.commit();
            anchor = ModelHelper.getAnchor(rm, platformId, anchorAliasId);
        }
        return anchor;
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
     * public static void main(String[] args) throws JDOMException, IOException,
     * ParseException { ParseQixiuRoomPageTask task = new
     * ParseQixiuRoomPageTask( "sample_data/room_page",
     * ResourceManager.generateResourceManager()); task.run(); }
     */

}
