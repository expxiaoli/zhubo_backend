package com.zhubo.task.parsepage.task;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.google.common.collect.Sets;
import com.mysql.jdbc.StringUtils;
import com.zhubo.entity.Anchor;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.DatabaseCache.AnchorObject;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;
import com.zhubo.helper.ModelHelper;

public class ParsePlatformPageWithDirectInfoTask extends BaseParsePageTask {
    public ParsePlatformPageWithDirectInfoTask(String filePath, Set<Long> invalidAliasIds,
            ResourceManager resourceManager, int platformId) {
        super(filePath, invalidAliasIds, resourceManager, platformId);
    }

    public boolean run() throws JDOMException, IOException, PageFormatException, ParseException {
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
        if (dataElement.getChild("platform") != null
                && (dataElement.getChild("time") != null || dataElement.getChild("date") != null)
                && dataElement.getChild("type") != null) {
            String pagePlatform = dataElement.getChild("platform").getValue();
            Element pageTimeElement = dataElement.getChild("time");
            Element pageDateElement = dataElement.getChild("date");
            String pageTime = pageTimeElement != null ? pageTimeElement.getValue()
                    : pageDateElement.getValue();
            Date pageDate = GeneralHelper.parseWithMultipleFormats(pageTime);
            String pageType = dataElement.getChild("type").getValue();
            Element allContItemElement = dataElement.getChild("cont_items");
            if (allContItemElement == null) {
                throw new PageFormatException("cont_items is not existed");
            }
            parseAndStoreAnchorContent(allContItemElement, pageType, pageDate);
            return true;
        } else {
            throw new PageFormatException("platform, time or type element is not existed");
        }

    }

    public void parseAndStoreAnchorContent(Element root, String pageType, Date pageDate) {
        List<Element> itemElements = root.getChildren();
        boolean needCommit = false;
        for (Element itemElement : itemElements) {
            String roomNumberText = itemElement.getChildText("room_number");
            if (StringUtils.isNullOrEmpty(roomNumberText)) {
                continue;
            }
            Long roomNumber = Long.valueOf(roomNumberText);
            if (invalidAliasIds.contains(roomNumber)) {
                System.out.println("-_-> invalid alias id " + roomNumber + ", ignore this page");
                continue;
            }
            String nickName = itemElement.getChildText("nickname");
            String area = itemElement.getChildText("area");
            AnchorObject anchorInCache = resourceManager.getDatabaseCache()
                    .getAnchorObjectFromCache(roomNumber);
            if (anchorInCache == null) {
                Anchor anchor = new Anchor(platformId, roomNumber, nickName, pageDate);
                anchor.setArea(area);
                anchor.setType(pageType);
                resourceManager.getDatabaseSession().save(anchor);
                resourceManager.getDatabaseCache().setAnchorObjectInCache(
                        anchor.getAnchorAliasId(),
                        new AnchorObject(anchor.getAnchorId(), anchor.getArea(), anchor.getType()));
                needCommit = true;
            } else if (area != null && (anchorInCache.area == null || !area.equals(anchorInCache.area))
                    || (pageType != null && (anchorInCache.type == null || !pageType.equals(anchorInCache.type)))) {
                Anchor anchor = (Anchor) resourceManager.getDatabaseSession().load(Anchor.class,
                        anchorInCache.anchorId);
                anchor.setArea(area);
                anchor.setType(pageType);
                resourceManager.getDatabaseSession().update(anchor);
                resourceManager.getDatabaseCache().setAnchorObjectInCache(
                        anchor.getAnchorAliasId(),
                        new AnchorObject(anchor.getAnchorId(), area, pageType));
                needCommit = true;
            }
        }
        if(needCommit) {
            resourceManager.commit();
        }
    }
    /*
     * public static void main(String[] args) throws JDOMException, IOException,
     * PageFormatException, ParseException { ParsePlatformPageTask task = new
     * ParsePlatformPageTask(
     * "/Users/xiao.li/coding/zhubo_data/v6/20161020/平台-奇秀广场-20161020142809-20161020142811790"
     * , Sets.newHashSet(), ResourceManager.generateResourceManager(), 1);
     * task.run(); }
     */
}
