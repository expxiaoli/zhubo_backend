package com.zhubo.task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import com.zhubo.entity.Anchor;
import com.zhubo.expcetion.PageFormatException;
import com.zhubo.global.ResourceManager;
import com.zhubo.helper.ModelHelper;

public class ParseQixiuPlatformPageTask extends BaseParsePageTask{
    public ParseQixiuPlatformPageTask(String filePath, ResourceManager resourceManager) {
        super(filePath, resourceManager);
    }

    private static Integer platformId = 1;
    private static final String curPlatform = "平台";
    private static final String curClass = "奇秀广场";

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
        if (pageClass.equals("奇秀广场")) {
            if (dataElement.getChild("platform") != null && dataElement.getChild("time") != null
                    && dataElement.getChild("type") != null) {
                String pagePlatform = dataElement.getChild("platform").getValue();
                String pageTime = dataElement.getChild("time").getValue();
                String pageType = dataElement.getChild("type").getValue();
                Element allContItemElement = dataElement.getChild("cont_items");
                parseAndStoreAnchorContent(allContItemElement, pageType);
                return true;
            } else {
                throw new PageFormatException("platform, time or type element is not existed");
            }
        } else {
            return false;
        }

    }

    public void parseAndStoreAnchorContent(Element root, String pageType) {
        List<Element> itemElements = root.getChildren();
        int count = 0;
        for (Element itemElement : itemElements) {
            Long roomNumber = Long.valueOf(itemElement.getChildText("room_number"));
            String nickName = itemElement.getChildText("nickname");
            String area = itemElement.getChildText("area");
            Anchor oldAnchor = ModelHelper.getAnchor(resourceManager, platformId, roomNumber);
            if (oldAnchor == null) {
                Anchor anchor = new Anchor(platformId, roomNumber, nickName);
                anchor.setArea(area);
                anchor.setType(pageType);
                resourceManager.getDatabaseSession().save(anchor);
            }
            count++;
            if (count % 10 == 0) {
                resourceManager.commit();
            }
        }
    }
/*
    public static void main(String[] args) throws JDOMException, IOException, PageFormatException {
        ParseQixiuPlatformPageTask task = new ParseQixiuPlatformPageTask(
                "sample_data/platform_page", ResourceManager.generateResourceManager());
        task.run();
    }
*/
}
