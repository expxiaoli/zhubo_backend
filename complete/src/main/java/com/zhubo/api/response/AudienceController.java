package com.zhubo.api.response;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhubo.entity.Audience;
import com.zhubo.global.ResourceManager;

@RestController
public class AudienceController {
    
    @RequestMapping("/search_audience")
    public AudienceResponse searchAudienceByAliasId(@RequestParam(value="alias_id") Long aliasId, @RequestParam(value="platform_id") Integer platformId) {
        Session session = ResourceManager.generateResourceManager().getNewDatabaseSession();
        Query query = session.createQuery("from Audience where audience_alias_id = :alias_id and platform_id = :platform_id");
        query.setParameter("alias_id", aliasId);
        query.setParameter("platform_id", platformId);
        List<Audience> audiences = query.list();
        AudienceResponse response;
        if(audiences.size() > 0) {
            Audience audience = audiences.get(0);  
            response = new AudienceResponse(audience.getAudienceId(), audience.getAudienceAliasId(), audience.getAudienceName());
        } else {
            response = new AudienceResponse(0, null, null);
        }
        ResourceManager.generateResourceManager().closeSessionAndTransaction();
        return response;
    }
}
