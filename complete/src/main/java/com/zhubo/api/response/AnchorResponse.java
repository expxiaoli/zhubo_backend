package com.zhubo.api.response;

public class AnchorResponse {
    private final long anchorId;
    private final String anchorName;
    private final String type;
    private final String area;
   
    public AnchorResponse(long anchorId, String anchorName, String type, String area) {
        this.anchorId = anchorId;
        this.anchorName = anchorName;
        this.type = type;
        this.area = area;
    }
   
    public long getAnchorId() {
        return anchorId;
    }
   
    public String getAnchorName() {
        return anchorName;
    }
    
    public String getType() {
        return type;
    }
    
    public String getArea() {
        return area;
    }
}