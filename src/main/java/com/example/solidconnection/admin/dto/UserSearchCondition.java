package com.example.solidconnection.admin.dto;

import com.example.solidconnection.siteuser.domain.Role;

public record UserSearchCondition(
	Role role,
	String keyword
) {
    
}
