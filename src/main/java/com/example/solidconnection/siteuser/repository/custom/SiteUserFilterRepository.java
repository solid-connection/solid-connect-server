package com.example.solidconnection.siteuser.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.solidconnection.admin.dto.RestrictedUserInfoDetailResponse;
import com.example.solidconnection.admin.dto.RestrictedUserSearchCondition;
import com.example.solidconnection.admin.dto.RestrictedUserSearchResponse;
import com.example.solidconnection.admin.dto.UserInfoDetailResponse;
import com.example.solidconnection.admin.dto.UserSearchCondition;
import com.example.solidconnection.admin.dto.UserSearchResponse;

public interface SiteUserFilterRepository {

	Page<UserSearchResponse> searchAllUsers(UserSearchCondition searchCondition, Pageable pageable);
	Page<RestrictedUserSearchResponse> searchRestrictedUsers(RestrictedUserSearchCondition searchCondition, Pageable pageable);
	UserInfoDetailResponse getUserInfoDetailByUserId(long userId);
	RestrictedUserInfoDetailResponse getRestrictedUserInfoDetail(long userId);
}
