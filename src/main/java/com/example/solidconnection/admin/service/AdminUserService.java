package com.example.solidconnection.admin.service;

import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.solidconnection.admin.dto.RestrictedUserInfoDetailResponse;
import com.example.solidconnection.admin.dto.RestrictedUserSearchCondition;
import com.example.solidconnection.admin.dto.RestrictedUserSearchResponse;
import com.example.solidconnection.admin.dto.UserInfoDetailResponse;
import com.example.solidconnection.admin.dto.UserSearchCondition;
import com.example.solidconnection.admin.dto.UserSearchResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.siteuser.repository.custom.SiteUserFilterRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AdminUserService {

	private final SiteUserRepository siteUserRepository;
	private final SiteUserFilterRepository siteUserFilterRepository;

	@Transactional(readOnly = true)
    public Page<UserSearchResponse> searchAllUsers(UserSearchCondition searchCondition, Pageable pageable) {
        return siteUserFilterRepository.searchAllUsers(searchCondition, pageable);
    }

	@Transactional(readOnly = true)
    public Page<RestrictedUserSearchResponse> searchRestrictedUsers(RestrictedUserSearchCondition searchCondition, Pageable pageable) {
        return siteUserFilterRepository.searchRestrictedUsers(searchCondition, pageable);
    }

	@Transactional(readOnly = true)
    public UserInfoDetailResponse getUserInfoDetail(long userId) {
		SiteUser siteUser = siteUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return siteUserFilterRepository.getUserInfoDetailByUserId(siteUser.getId());
    }

	@Transactional(readOnly = true)
    public RestrictedUserInfoDetailResponse getRestrictedUserInfoDetail(long userId) {
		SiteUser siteUser = siteUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return siteUserFilterRepository.getRestrictedUserInfoDetail(siteUser.getId());
    }
}
