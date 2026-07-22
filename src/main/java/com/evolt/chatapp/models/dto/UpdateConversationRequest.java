// models/dto/UpdateConversationRequest.java
package com.evolt.chatapp.models.dto;

public class UpdateConversationRequest {
    private String name;
    private String imageUrl;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}