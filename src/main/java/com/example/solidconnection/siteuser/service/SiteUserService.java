package com.example.solidconnection.siteuser.service;

import static com.example.solidconnection.common.exception.ErrorCode.ALREADY_BLOCKED_BY_CURRENT_USER;
import static com.example.solidconnection.common.exception.ErrorCode.BLOCK_USER_NOT_FOUND;
import static com.example.solidconnection.common.exception.ErrorCode.CANNOT_BLOCK_YOURSELF;
import static com.example.solidconnection.common.exception.ErrorCode.USER_NOT_FOUND;

import com.example.solidconnection.common.dto.SliceResponse;
import com.example.solidconnection.common.exception.CustomException;
import com.example.solidconnection.siteuser.domain.UserBlock;
import com.example.solidconnection.siteuser.dto.NicknameExistsResponse;
import com.example.solidconnection.siteuser.dto.UserBlockResponse;
import com.example.solidconnection.siteuser.repository.SiteUserRepository;
import com.example.solidconnection.siteuser.repository.UserBlockRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class SiteUserService {

    private final SiteUserRepository siteUserRepository;
    private final UserBlockRepository userBlockRepository;

    public NicknameExistsResponse checkNicknameExists(String nickname) {
        boolean exists = siteUserRepository.existsByNickname(nickname);
        return NicknameExistsResponse.from(exists);
    }

    @Transactional(readOnly = true)
    public SliceResponse<UserBlockResponse> getBlockedUsers(long siteUserId, Pageable pageable) {
        Slice<UserBlockResponse> slice = userBlockRepository.findBlockedUsersWithNickname(siteUserId, pageable);

        List<UserBlockResponse> content = slice.getContent();
        return SliceResponse.of(content, slice);
    }

    @Transactional
    public void blockUser(long blockerId, long blockedId) {
        validateBlockUser(blockerId, blockedId);
        UserBlock userBlock = new UserBlock(blockerId, blockedId);
        userBlockRepository.save(userBlock);
    }

    private void validateBlockUser(long blockerId, long blockedId) {
        if (Objects.equals(blockerId, blockedId)) {
            throw new CustomException(CANNOT_BLOCK_YOURSELF);
        }
        if (!siteUserRepository.existsById(blockedId)) {
            throw new CustomException(USER_NOT_FOUND);
        }
        if (userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw new CustomException(ALREADY_BLOCKED_BY_CURRENT_USER);
        }
    }

    @Transactional
    public void cancelUserBlock(long blockerId, long blockedId) {
        if (!siteUserRepository.existsById(blockedId)) {
            throw new CustomException(USER_NOT_FOUND);
        }
        UserBlock userBlock = userBlockRepository.findByBlockerIdAndBlockedId(blockerId, blockedId)
                .orElseThrow(() -> new CustomException(BLOCK_USER_NOT_FOUND));
        userBlockRepository.delete(userBlock);
    }
}
