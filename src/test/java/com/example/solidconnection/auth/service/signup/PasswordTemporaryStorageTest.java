package com.example.solidconnection.auth.service.signup;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.solidconnection.support.TestContainerSpringBootTest;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@DisplayName("비밀번호 임시 저장소 테스트")
@TestContainerSpringBootTest
class PasswordTemporaryStorageTest {

    @Autowired
    private PasswordTemporaryStorage passwordTemporaryStorage;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String email = "test@email.com";
    private final String rawPassword = "password123";

    @Test
    void 인코딩된_비밀번호를_임시_저장소에_저장하고_조회한다() {
        // when
        passwordTemporaryStorage.save(email, rawPassword);
        Optional<String> foundPassword = passwordTemporaryStorage.findByEmail(email);

        // then
        assertThat(foundPassword).isPresent();
        assertThat(passwordEncoder.matches(rawPassword, foundPassword.get())).isTrue();
    }

    @Test
    void 임시_저장된_비밀번호를_삭제한다() {
        // given
        passwordTemporaryStorage.save(email, rawPassword);

        // when
        passwordTemporaryStorage.deleteByEmail(email);
        Optional<String> foundPassword = passwordTemporaryStorage.findByEmail(email);

        // then
        assertThat(foundPassword).isEmpty();
    }
}
