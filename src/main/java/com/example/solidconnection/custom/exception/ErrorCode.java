package com.example.solidconnection.custom.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static com.example.solidconnection.application.service.ApplicationSubmissionService.APPLICATION_UPDATE_COUNT_LIMIT;
import static com.example.solidconnection.siteuser.service.SiteUserService.MIN_DAYS_BETWEEN_NICKNAME_CHANGES;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // kakao
    KAKAO_REDIRECT_URI_MISMATCH(HttpStatus.BAD_REQUEST.value(), "리다이렉트 uri가 잘못되었습니다."),
    INVALID_OR_EXPIRED_KAKAO_AUTH_CODE(HttpStatus.BAD_REQUEST.value(), "사용할 수 없는 카카오 인증 코드입니다. 카카오 인증 코드는 일회용이며, 인증 만료 시간은 10분입니다."),
    KAKAO_ACCESS_TOKEN_FAIL(HttpStatus.NOT_FOUND.value(), "카카오 엑세스 토큰 발급에 실패했습니다"),
    KAKAO_USER_INFO_FAIL(HttpStatus.BAD_REQUEST.value(), "카카오 사용자 정보 조회에 실패했습니다."),
    INVALID_SERVICE_PUBLISHED_KAKAO_TOKEN(HttpStatus.BAD_REQUEST.value(), "우리 서비스에서 발급한 카카오 토큰이 아닙니다"),

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
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value(), "토큰이 필요한 경로에 빈 토큰으로 요청했습니다."),
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
    CANT_APPLY_FOR_SAME_UNIVERSITY(HttpStatus.BAD_REQUEST.value(), "1, 2지망에 동일한 대학교를 입력할 수 없습니다."),
    CAN_NOT_CHANGE_NICKNAME_YET(HttpStatus.BAD_REQUEST.value(), "마지막 닉네임 변경으로부터 " + MIN_DAYS_BETWEEN_NICKNAME_CHANGES + "일이 지나지 않았습니다."),

    // community
    INVALID_POST_CATEGORY(HttpStatus.BAD_REQUEST.value(),"잘못된 카테고리명입니다."),
    INVALID_BOARD_CODE(HttpStatus.BAD_REQUEST.value(), "잘못된 게시판 코드입니다."),
    INVALID_POST_ID(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 게시글입니다."),
    INVALID_POST_ACCESS(HttpStatus.BAD_REQUEST.value(), "자신의 게시글만 제어할 수 있습니다."),
    CAN_NOT_DELETE_OR_UPDATE_QUESTION(HttpStatus.BAD_REQUEST.value(), "질문글은 수정이나 삭제할 수 없습니다."),
    CAN_NOT_UPLOAD_MORE_THAN_FIVE_IMAGES(HttpStatus.BAD_REQUEST.value(), "5개 이상의 파일을 업로드할 수 없습니다."),
    INVALID_COMMENT_ID(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 댓글입니다."),
    INVALID_COMMENT_ACCESS(HttpStatus.BAD_REQUEST.value(), "자신의 댓글만 제어할 수 있습니다."),
    CAN_NOT_UPDATE_DEPRECATED_COMMENT(HttpStatus.BAD_REQUEST.value(),"이미 삭제된 댓글을 수정할 수 없습니다."),

    // general
    JSON_PARSING_FAILED(HttpStatus.BAD_REQUEST.value(), "JSON 파싱을 할 수 없습니다."),
    JWT_EXCEPTION(HttpStatus.BAD_REQUEST.value(), "JWT 토큰을 처리할 수 없습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST.value(), "값을 입력할 수 없습니다."),
    NOT_DEFINED_ERROR(HttpStatus.BAD_REQUEST.value(), "에러가 발생했습니다."),
    ;

    private final int code;
    private final String message;
}
