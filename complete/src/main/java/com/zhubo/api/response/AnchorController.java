package com.zhubo.api.response;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhubo.entity.Anchor;
import com.zhubo.global.ResourceManager;

@RestController
public class AnchorController {

    private final Session session = ResourceManager.generateResourceManager().getDatabaseSession();

    @RequestMapping("/anchor")
    public AnchorResponse getInfoById(@RequestParam(value="id") Long anchorId) {
        Anchor anchor =  (Anchor) session.load(Anchor.class, anchorId);
        return new AnchorResponse(anchorId, anchor.getAnchorName(), anchor.getType(), anchor.getArea());
    }
    
    @RequestMapping("/search_anchor")
    public AnchorResponse searchInfoByName(@RequestParam(value="key") String key) {
        Query query = session.createQuery("from Anchor where anchor_name like :name");
        query.setParameter("name", "%" + key + "%");
        List<Anchor> anchors = query.list();
        if(anchors.size() > 0) {
            Anchor anchor = anchors.get(0);
            return new AnchorResponse(anchor.getAnchorId(), anchor.getAnchorName(), anchor.getType(), anchor.getArea());
        } else {
            return new AnchorResponse(0, null, null, null);
        }
    }
}