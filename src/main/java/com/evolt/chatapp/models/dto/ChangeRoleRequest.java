// models/dto/ChangeRoleRequest.java
package com.evolt.chatapp.models.dto;

public class ChangeRoleRequest {
    private String role; // "MEMBER" or "ADMIN" (OWNER isn't assignable)
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}