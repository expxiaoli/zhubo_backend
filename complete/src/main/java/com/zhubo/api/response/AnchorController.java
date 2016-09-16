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
    public AnchorResponse searchZhuboByAliasId(@RequestParam(value="room_id") Long roomId, @RequestParam(value="platform_id") Integer platformId) {
        Query query = session.createQuery("from Anchor where anchor_alias_id = :room_id and platform_id = :platform_id");
        query.setParameter("room_id", roomId);
        query.setParameter("platform_id", platformId);
        List<Anchor> anchors = query.list();
        if(anchors.size() > 0) {
            Anchor anchor = anchors.get(0);
            return new AnchorResponse(anchor.getAnchorId(), anchor.getAnchorName(), anchor.getType(), anchor.getArea());
        } else {
            return new AnchorResponse(0, null, null, null);
        }
    }
}