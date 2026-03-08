package com.example.solidconnection.admin.dto;

import com.example.solidconnection.siteuser.domain.Role;
import com.example.solidconnection.siteuser.domain.UserStatus;

public record RestrictedUserSearchCondition(
	Role role,
	UserStatus userStatus,
	String keyword
) {

}
