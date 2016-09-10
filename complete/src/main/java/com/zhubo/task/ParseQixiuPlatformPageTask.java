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
import com.zhubo.global.ResourceManager;

public class ParseQixiuPlatformPageTask {
	private File file;
	private static Integer platformId = 1;
	private final ResourceManager resourceManager;
	private static final String curPlatform = "平台";
	private static final String curClass = "奇秀广场";
	       

	public ParseQixiuPlatformPageTask(String filePath, ResourceManager resourceManager) {
		file = new File(filePath);
		this.resourceManager = resourceManager;
	}

	public void run() throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		Document document = builder.build(file);

		Element root = document.getRootElement();
		Element dataElement = root.getChild("Body", Namespace.getNamespace("SOAP-ENV", "http://schemas.xmlsoap.org/soap/envelope/"))
			.getChild("PostDocument", Namespace.getNamespace("Spider", "urn:http://service.sina.com.cn/spider"))
		    .getChild("document");
		String pagePlatform = dataElement.getChild("platform").getValue();
		String pageClass = dataElement.getChild("class").getValue();
		String pageTime =  dataElement.getChild("time").getValue();
		String pageType = dataElement.getChild("type").getValue();			
		if(pagePlatform.equals(curPlatform) && pageClass.equals("奇秀广场")) {
			Element allContItemElement = dataElement.getChild("cont_items");
			parseAndStoreAnchorContent(allContItemElement, pageType);
		}
		System.out.println("all done");
	}
	
	public void parseAndStoreAnchorContent(Element root, String pageType) {
		List<Element> itemElements = root.getChildren();
		int count = 0;
		for(Element itemElement : itemElements) {
			Long roomNumber = Long.valueOf(itemElement.getChildText("room_number"));
			String nickName = itemElement.getChildText("nickname");
			String area = itemElement.getChildText("area");
			Anchor anchor = new Anchor(platformId, roomNumber, nickName);
			anchor.setArea(area);
			anchor.setType(pageType);
			resourceManager.getDatabaseSession().save(anchor);
			count++;
			if(count % 10 == 0) {
				System.out.println(String.format("store %d anchors", count));
				resourceManager.commit();
			}
		}
		System.out.println(String.format("store %d anchors done", count));
	}

	public static void main(String[] args) throws JDOMException, IOException{
		ParseQixiuPlatformPageTask task = new ParseQixiuPlatformPageTask(
				"/Users/xiao.li/coding/zhubo/sample_data/platform_page", ResourceManager.generateResourceManager());
		task.run();
	}
}