package com.zhubo.task.parsepage.task;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.zhubo.entity.Anchor;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.DatabaseCache.AnchorObject;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.GeneralHelper;
import com.zhubo.helper.ModelHelper;

public class ParsePlatformPageTask extends BaseParsePageTask {
    public ParsePlatformPageTask(String filePath, ResourceManager resourceManager, int platformId) {
        super(filePath, resourceManager, platformId);
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
        if (pageClass.equals("奇秀广场")) {
            if (dataElement.getChild("platform") != null && dataElement.getChild("time") != null
                    && dataElement.getChild("type") != null) {
                String pagePlatform = dataElement.getChild("platform").getValue();
                String pageTime = dataElement.getChild("time").getValue();
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
        } else {
            return false;
        }

    }

    public void parseAndStoreAnchorContent(Element root, String pageType, Date pageDate) {
        List<Element> itemElements = root.getChildren();
        for (Element itemElement : itemElements) {
            Long roomNumber = Long.valueOf(itemElement.getChildText("room_number"));
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
            } else if (anchorInCache.area == null || anchorInCache.type == null) {
                Anchor anchor = (Anchor) resourceManager.getDatabaseSession().load(Anchor.class,
                        anchorInCache.anchorId);
                anchor.setArea(area);
                anchor.setType(pageType);
                resourceManager.getDatabaseSession().update(anchor);
            }
        }
        resourceManager.commit();
    }
    /*
     * public static void main(String[] args) throws JDOMException, IOException,
     * PageFormatException { ParseQixiuPlatformPageTask task = new
     * ParseQixiuPlatformPageTask( "sample_data/platform_page",
     * ResourceManager.generateResourceManager()); task.run(); }
     */
}
