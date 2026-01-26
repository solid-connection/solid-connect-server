package com.example.solidconnection.common.interceptor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.solidconnection.community.board.fixture.BoardFixture;
import com.example.solidconnection.community.post.domain.Post;
import com.example.solidconnection.community.post.domain.PostCategory;
import com.example.solidconnection.community.post.fixture.PostFixture;
import com.example.solidconnection.security.authentication.TokenAuthentication;
import com.example.solidconnection.security.userdetails.SiteUserDetails;
import com.example.solidconnection.siteuser.domain.SiteUser;
import com.example.solidconnection.siteuser.fixture.SiteUserFixture;
import com.example.solidconnection.support.TestContainerSpringBootTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@TestContainerSpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("차단된 유저 인터셉터 테스트")
class BannedUserInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SiteUserFixture siteUserFixture;

    @Autowired
    private PostFixture postFixture;

    @Autowired
    private BoardFixture boardFixture;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 차단된_사용자는_게시판_관련_접근이_차단된다() throws Exception {
        // given
        SiteUser bannedUser = siteUserFixture.차단된_사용자("차단된유저");
        setAuthentication(bannedUser);

        // when & then
        mockMvc.perform(get("/boards"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 차단된_사용자는_게시글_관련_접근이_차단된다() throws Exception {
        // given
        SiteUser bannedUser = siteUserFixture.차단된_사용자("차단된유저");
        setAuthentication(bannedUser);

        // when & then
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/posts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 차단된_사용자는_댓글_관련_접근이_차단된다() throws Exception {
        // given
        SiteUser bannedUser = siteUserFixture.차단된_사용자("차단된유저");
        setAuthentication(bannedUser);

        // when & then
        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                                        {
                                            "postId": 1,
                                            "content": "테스트 댓글 내용",
                                            "parentId": null
                                        }
                                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void 차단된_사용자는_채팅_관련_접근이_차단된다() throws Exception {
        // given
        SiteUser bannedUser = siteUserFixture.차단된_사용자("차단된유저");
        setAuthentication(bannedUser);

        // when & then
        mockMvc.perform(get("/chats/rooms"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 정상_사용자는_모든_경로_접근이_가능하다() throws Exception {
        // given
        SiteUser normalUser = siteUserFixture.사용자(1, "정상 유저1");
        Post post1 = postFixture.게시글(
                "제목1",
                "내용1",
                false,
                PostCategory.자유,
                boardFixture.자유게시판(),
                siteUserFixture.사용자(2, "정상 유저2")
        );
        setAuthentication(normalUser);

        // when & then
        mockMvc.perform(get("/boards"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/posts/" + post1.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "postId": 1,
                                            "content": "테스트 댓글 내용",
                                            "parentId": null
                                        }
                                        """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/chats/rooms"))
                .andExpect(status().isOk());
    }

    @Test
    void 차단된_사용자도_다른_경로_접근은_가능하다() throws Exception {
        // given
        SiteUser bannedUser = siteUserFixture.차단된_사용자("차단된유저");
        setAuthentication(bannedUser);

        // when & then
        mockMvc.perform(get("/my"))
                .andExpect(status().isOk());
    }

    private void setAuthentication(SiteUser user) {
        SiteUserDetails userDetails = new SiteUserDetails(user);
        Authentication authentication = new TokenAuthentication("token", userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
