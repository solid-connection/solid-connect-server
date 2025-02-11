package com.example.solidconnection.custom.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static com.example.solidconnection.application.service.ApplicationSubmissionService.APPLICATION_UPDATE_COUNT_LIMIT;
import static com.example.solidconnection.siteuser.service.SiteUserService.MIN_DAYS_BETWEEN_NICKNAME_CHANGES;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // apple
    APPLE_AUTHORIZATION_FAILED(HttpStatus.BAD_REQUEST.value(), "애플 인증에 실패했습니다."),
    APPLE_ID_TOKEN_MISSING_EMAIL(HttpStatus.BAD_REQUEST.value(), "애플 idToken 에 이메일이 없습니다."),
    APPLE_ID_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST.value(), "애플 idToken 이 만료되었습니다."),
    INVALID_APPLE_ID_TOKEN(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 애플 idToken 입니다."),
    APPLE_ID_TOKEN_MALFORMED(HttpStatus.BAD_REQUEST.value(), "애플 idToken 의 형식이 잘못되었습니다."),
    APPLE_PUBLIC_KEY_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR.value(), "idToken 를 서명한 애플 공개키를 찾을 수 없습니다"),
    FAILED_TO_READ_APPLE_PRIVATE_KEY(HttpStatus.INTERNAL_SERVER_ERROR.value(), "애플 private key 파일을 읽을 수 없습니다."),
    APPLE_CLIENT_SECRET_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "애플 client secret JWT 생성에 실패했습니다."),

    // kakao
    KAKAO_REDIRECT_URI_MISMATCH(HttpStatus.BAD_REQUEST.value(), "리다이렉트 uri가 잘못되었습니다."),
    INVALID_OR_EXPIRED_KAKAO_AUTH_CODE(HttpStatus.BAD_REQUEST.value(), "사용할 수 없는 카카오 인증 코드입니다. 카카오 인증 코드는 일회용이며, 인증 만료 시간은 10분입니다."),
    KAKAO_ACCESS_TOKEN_FAIL(HttpStatus.NOT_FOUND.value(), "카카오 엑세스 토큰 발급에 실패했습니다"),
    KAKAO_USER_INFO_FAIL(HttpStatus.BAD_REQUEST.value(), "카카오 사용자 정보 조회에 실패했습니다."),
    INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN(HttpStatus.BAD_REQUEST.value(), "우리 서비스에서 발급한 카카오 토큰이 아닙니다"),

    // sign up token
    SIGN_UP_TOKEN_INVALID(HttpStatus.BAD_REQUEST.value(), "유효하지 않은 회원가입 토큰입니다."),
    SIGN_UP_TOKEN_NOT_ISSUED_BY_SERVER(HttpStatus.BAD_REQUEST.value(), "회원가입 토큰이 우리 서버에서 발급되지 않았습니다."),

    // data not found
    UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "존재하지 않는 대학교 지원 정보입니다."),
    UNIVERSITY_INFO_FOR_APPLY_NOT_FOUND_FOR_TERM(HttpStatus.NOT_FOUND.value(), "해당하는 대학교가 이번 모집 기간에 열리지 않았습니다."),
    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "사용자의 대학 지원 정보를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "회원을 찾을 수 없습니다."),
    UNIVERSITY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "대학교를 찾을 수 없습니다."),
    REGION_NOT_FOUND_BY_KOREAN_NAME(HttpStatus.NOT_FOUND.value(), "이름에 해당하는 지역을 찾을 수 없습니다."),
    COUNTRY_NOT_FOUND_BY_KOREAN_NAME(HttpStatus.NOT_FOUND.value(), "이름에 해당하는 국가를 찾을 수 없습니다."),

    // auth
    USER_ALREADY_SIGN_OUT(HttpStatus.UNAUTHORIZED.value(), "로그아웃 되었습니다."),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED.value(), "토큰이 필요한 경로에 빈 토큰으로 요청했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED.value(), "인증이 필요한 접근입니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "액세스 토큰이 만료되었습니다. 재발급 api를 호출해주세요."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "리프레시 토큰이 만료되었습니다. 다시 로그인을 진행해주세요."),

    // s3
    S3_SERVICE_EXCEPTION(HttpStatus.BAD_REQUEST.value(), "S3 서비스 에러 발생"),
    S3_CLIENT_EXCEPTION(HttpStatus.BAD_REQUEST.value(), "S3 클라이언트 에러 발생"),
    FILE_NOT_EXIST(HttpStatus.BAD_REQUEST.value(), "파일이 없습니다."),
    NOT_ALLOWED_FILE_EXTENSIONS(HttpStatus.BAD_REQUEST.value(), "허용되지 않은 확장자입니다."),
    INVALID_FILE_EXTENSIONS(HttpStatus.BAD_REQUEST.value(), "파일 형식이 유효하지 않습니다."),

    // invalid operation
    SCORE_SHOULD_SUBMITTED_FIRST(HttpStatus.BAD_REQUEST.value(), "성적을 먼저 제출해주세요."),
    USER_ALREADY_EXISTED(HttpStatus.CONFLICT.value(), "이미 존재하는 회원입니다."),
    NICKNAME_ALREADY_EXISTED(HttpStatus.CONFLICT.value(), "이미 존재하는 닉네임입니다."),
    INVALID_TEST_TYPE(HttpStatus.BAD_REQUEST.value(), "지원하지 않은 어학 시험 종류입니다."),
    APPLICATION_NOT_APPROVED(HttpStatus.BAD_REQUEST.value(), "성적표가 인증되지 않았습니다."),
    APPLY_UPDATE_LIMIT_EXCEED(HttpStatus.BAD_REQUEST.value(), "지원 정보 수정은 " + APPLICATION_UPDATE_COUNT_LIMIT + "회까지만 가능합니다."),
    CANT_APPLY_FOR_SAME_UNIVERSITY(HttpStatus.BAD_REQUEST.value(), "1, 2, 3지망에 동일한 대학교를 입력할 수 없습니다."),
    CAN_NOT_CHANGE_NICKNAME_YET(HttpStatus.BAD_REQUEST.value(), "마지막 닉네임 변경으로부터 " + MIN_DAYS_BETWEEN_NICKNAME_CHANGES + "일이 지나지 않았습니다."),
    PROFILE_IMAGE_NEEDED(HttpStatus.BAD_REQUEST.value(), "프로필 이미지가 필요합니다."),
    FIRST_CHOICE_REQUIRED(HttpStatus.BAD_REQUEST.value(), "1지망 대학교를 입력해주세요."),
    THIRD_CHOICE_REQUIRES_SECOND(HttpStatus.BAD_REQUEST.value(), "2지망 없이 3지망을 선택할 수 없습니다."),
    DUPLICATE_UNIVERSITY_CHOICE(HttpStatus.BAD_REQUEST.value(), "지망 선택이 중복되었습니다."),

    // community
    INVALID_POST_CATEGORY(HttpStatus.BAD_REQUEST.value(), "잘못된 카테고리명입니다."),
    INVALID_BOARD_CODE(HttpStatus.BAD_REQUEST.value(), "잘못된 게시판 코드입니다."),
    INVALID_POST_ID(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 게시글입니다."),
    INVALID_POST_ACCESS(HttpStatus.BAD_REQUEST.value(), "자신의 게시글만 제어할 수 있습니다."),
    CAN_NOT_DELETE_OR_UPDATE_QUESTION(HttpStatus.BAD_REQUEST.value(), "질문글은 수정이나 삭제할 수 없습니다."),
    CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES(HttpStatus.BAD_REQUEST.value(), "5개 이상의 파일을 업로드할 수 없습니다."),
    INVALID_COMMENT_ID(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 댓글입니다."),
    INVALID_COMMENT_LEVEL(HttpStatus.BAD_REQUEST.value(), "최대 대댓글까지만 작성할 수 있습니다."),
    INVALID_COMMENT_ACCESS(HttpStatus.BAD_REQUEST.value(), "자신의 댓글만 제어할 수 있습니다."),
    CAN_NOT_UPDATE_DEPRECATED_COMMENT(HttpStatus.BAD_REQUEST.value(), "이미 삭제된 댓글을 수정할 수 없습니다."),
    INVALID_POST_LIKE(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 게시글 좋아요입니다."),
    DUPLICATE_POST_LIKE(HttpStatus.BAD_REQUEST.value(), "이미 좋아요한 게시글입니다."),
    ALREADY_LIKED_UNIVERSITY(HttpStatus.BAD_REQUEST.value(), "이미 좋아요한 대학입니다."),
    NOT_LIKED_UNIVERSITY(HttpStatus.BAD_REQUEST.value(), "좋아요하지 않은 대학입니다."),

    // score
    INVALID_GPA_SCORE(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 학점입니다."),
    INVALID_GPA_SCORE_STATUS(HttpStatus.BAD_REQUEST.value(), "학점이 승인되지 않았습니다."),
    INVALID_LANGUAGE_TEST_SCORE(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 어학성적입니다."),
    INVALID_LANGUAGE_TEST_SCORE_STATUS(HttpStatus.BAD_REQUEST.value(), "어학성적이 승인되지 않았습니다."),
    USER_DO_NOT_HAVE_GPA(HttpStatus.BAD_REQUEST.value(), "해당 유저의 학점을 찾을 수 없음"),

    // general
    JSON_PARSING_FAILED(HttpStatus.BAD_REQUEST.value(), "JSON 파싱을 할 수 없습니다."),
    JWT_EXCEPTION(HttpStatus.BAD_REQUEST.value(), "JWT 토큰을 처리할 수 없습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST.value(), "값을 입력할 수 없습니다."),
    NOT_DEFINED_ERROR(HttpStatus.BAD_REQUEST.value(), "에러가 발생했습니다."),
    ;

    private final int code;
    private final String message;
}
