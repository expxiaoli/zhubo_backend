package com.zhubo.task;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.google.common.collect.Lists;
import com.zhubo.entity.Anchor;
import com.zhubo.entity.AnchorMetricByMinutes;
import com.zhubo.global.ResourceManager;

public class ParseQixiuRoomPageTask {
    private File file;
    private static Integer platformId = 1;
    private final ResourceManager resourceManager;
    private static final String curPlatform = "奇秀";
    private static final String curClass = "主播房间";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ParseQixiuRoomPageTask(String filePath, ResourceManager resourceManager) {
        file = new File(filePath);
        this.resourceManager = resourceManager;
    }
    
    public void run() throws JDOMException, IOException, ParseException {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(file);

        Element root = document.getRootElement();
        Element dataElement = root.getChild("Body", Namespace.getNamespace("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/"))
            .getChild("PostDocument", Namespace.getNamespace("Spider", "urn:http://service.sina.com.cn/spider"))
            .getChild("document");
        
        String pagePlatform = dataElement.getChild("platform").getValue();
        String pageClass = dataElement.getChild("class").getValue();
        String dataStr = dataElement.getChild("date").getValue();
        Date pageDate = sdf.parse(dataStr);
        if(pagePlatform.equals(curPlatform) && pageClass.equals(curClass)) {
            Element allContItemElement = dataElement.getChild("cont_items");
            parseAndStoreMetric(allContItemElement, pageDate);
        }
        
    }
    
    private void parseAndStoreMetric(Element root, Date pageDate) {
        List<Element> itemElements = root.getChildren();
        Long anchorAliasId = null;
        String anchorName = null;
        List<Metric> metrics = Lists.newArrayList();
        for(Element itemElement : itemElements) {
            if(itemElement.getChild("cont_item_name") != null) {
                String itemName = itemElement.getChildText("cont_item_name");
                String itemBody = itemElement.getChildText("cont_item_body");
                if(itemName.equals("房间号")) {
                    anchorAliasId = Long.valueOf(itemBody);
                } else if(itemName.equals("昵称")) {
                    anchorName = itemBody;
                } else {
                    metrics.add(new Metric(itemName, Integer.valueOf(itemBody)));
                }
            }
        }
        
        
        Anchor anchor = getAnchorOrNewOne(resourceManager, platformId, anchorAliasId, anchorName);
        for(Metric metric : metrics) {
            AnchorMetricByMinutes metricByMinutes = new AnchorMetricByMinutes(anchor.getAnchorId(), metric.type, metric.value, pageDate);
            resourceManager.getDatabaseSession().save(metricByMinutes);
        }
        resourceManager.commit();
        System.out.println("parse and store metric done");
    }
    
    public Anchor getAnchorOrNewOne(ResourceManager rm, Integer platformId, Long anchorAliasId, String anchorName) {
        Anchor anchor = getAnchor(rm, platformId, anchorAliasId);
        if(anchor == null) {
            System.out.println(String.format("platform_id %d, anchor_alias_id %d is not existed", platformId, anchorAliasId));
            Anchor newAnchor = new Anchor(platformId, anchorAliasId, anchorName);
            rm.getDatabaseSession().save(newAnchor);
            rm.commit();
            System.out.println(String.format("insert platform_id %d, anchor_alias_id %d to anchor table done", platformId, anchorAliasId));
            anchor = getAnchor(rm, platformId, anchorAliasId);
        }
        return anchor;
    }
    
    public Anchor getAnchor(ResourceManager rm, Integer platformId, Long anchorAliasId) {
        Session session = rm.getDatabaseSession();
        Query query = session.createQuery("from Anchor where platform_id = :platform_id and anchor_alias_id = :anchor_alias_id");
        query.setParameter("platform_id", platformId);
        query.setParameter("anchor_alias_id", anchorAliasId);
        List<Anchor> anchors = query.list();
        if(anchors.isEmpty()) {
            return null;
        } else {
            return anchors.get(0);
        }
    }
    
    public static class Metric {
        public String type;
        public Integer value;
        public Metric(String type, Integer value) {
            this.type = type;
            this.value = value;
        }
    }
    
    public static void main(String[] args) throws JDOMException, IOException, ParseException {
        ParseQixiuRoomPageTask task = new ParseQixiuRoomPageTask(
                "/Users/xiao.li/coding/zhubo/sample_data/room_page", ResourceManager.generateResourceManager());
        task.run();
    }
    

}