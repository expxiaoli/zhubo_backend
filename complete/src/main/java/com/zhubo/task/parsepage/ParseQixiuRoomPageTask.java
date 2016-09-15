package com.zhubo.task.parsepage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.exception.GenericJDBCException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.google.common.collect.Lists;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.ModelHelper;

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
        Element dataElement = root.getChild("Body", Namespace.getNamespace("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/"))
            .getChild("PostDocument", Namespace.getNamespace("Spider", "urn:http://service.sina.com.cn/spider"))
            .getChild("document");
        
        String pageClass = dataElement.getChild("class").getValue();
        if(pageClass.equals(curClass)) {
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
            }
        }

        Anchor anchor = getAnchorOrNewOne(resourceManager, platformId, anchorAliasId, anchorName);
        for (Metric metric : metrics) {
            AnchorMetricByMinutes metricByMinutes = new AnchorMetricByMinutes(anchor.getAnchorId(),
                    metric.type, metric.value, pageDate);
            resourceManager.getDatabaseSession().save(metricByMinutes);
        }
        resourceManager.commit();
    }

    public Anchor getAnchorOrNewOne(ResourceManager rm, Integer platformId, Long anchorAliasId,
            String anchorName) {
        Anchor anchor = ModelHelper.getAnchor(rm, platformId, anchorAliasId);
        if (anchor == null) {
            System.out.println(String.format("platform_id %d, anchor_alias_id %d is not existed",
                    platformId, anchorAliasId));
            Anchor newAnchor = new Anchor(platformId, anchorAliasId, anchorName);
            rm.getDatabaseSession().save(newAnchor);
            rm.commit();
            System.out.println(String.format(
                    "insert platform_id %d, anchor_alias_id %d to anchor table done", platformId,
                    anchorAliasId));
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
    /*
     * public static void main(String[] args) throws JDOMException, IOException,
     * ParseException { ParseQixiuRoomPageTask task = new
     * ParseQixiuRoomPageTask( "sample_data/room_page",
     * ResourceManager.generateResourceManager()); task.run(); }
     */

}
