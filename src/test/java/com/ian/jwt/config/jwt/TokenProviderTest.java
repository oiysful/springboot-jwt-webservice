package com.ian.jwt.config.jwt;

import com.ian.jwt.domain.User;
import com.ian.jwt.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProperties jwtProperties;

    // generateToken() 검증 테스트
    @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰 생성")
    @Test
    void generateToken() {
        // given: 토큰에 유저 정보를 추가하기 위한 테스트 유저 생성
        User testUser = userRepository.save(User.builder()
                .email("user@gmail.com")
                .password("test")
                .build());

        // when: 토큰 제공자의 generateToken() Method 호출하여 토큰 생성
        String token = tokenProvider.generateToken(testUser, Duration.ofDays(14));

        // then: jjwt 라이브러리를 사용해 토큰 복호화, 토큰을 만들 때 클레임으로 넣어둔 ID가 given 절에서 만든 유저 ID와 동일한지 확인
        Long userId = Jwts.parser()
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody()
                .get("id", Long.class);

        assertThat(userId).isEqualTo(testUser.getId());
    }

    // validToken() 검증 테스트
    @DisplayName("validToken(): 만료된 토큰일 때 유효성 검증 실패")
    @Test
    void validToken_invalidToken() {
        // given: jjwt 라이브러리를 사용해 이미 만료된 시간으로 토큰 생성
        String token = JwtFactory.builder()
                .expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis()))
                .build()
                .createToken(jwtProperties);

        // when: 토큰 제공자의 validToken() Method 호출하여 유효한 토큰인지 검증, 결과 값 취득
        boolean result = tokenProvider.validToken(token);

        // then: return false 확인
        assertThat(result).isFalse();
    }

    @DisplayName("validToken(): 유효한 토큰일 때 유효성 검증 성공")
    @Test
    void validToken_validToken() {
        // given: jjwt 라이브러리를 사용해 14일 뒤 만료되는 토큰 생성
        String token = JwtFactory.withDefaultValues().createToken(jwtProperties);

        // when: 토큰 제공자의 validToken() Method 호출하여 유효한 토큰인지 검증, 결과 값 취득
        boolean result = tokenProvider.validToken(token);

        // then: return true 확인
        assertThat(result).isTrue();
    }

    // getAuthentication() 검증 테스트
    @DisplayName("getAuthentication(): 토큰 기반으로 인증 정보 취득")
    @Test
    void getAuthentication() {
        // given: jjwt 라이브러리를 사용해 subject="user@email.com" 토큰 생성
        String userEmail = "user@email.com";
        String token = JwtFactory.builder()
                .subject(userEmail)
                .build()
                .createToken(jwtProperties);

        // when: 토큰 제공자의 getAuthentication() Method 호출하여 인증 객체 반환
        Authentication authentication = tokenProvider.getAuthentication(token);

        // then: 반환받은 인증 객체의 유저 이름을 가져와 given 절에서 설정한 subject 값 "user@email.com"과 동일한지 확인
        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(userEmail);
    }

    // getuserid() 검증 테스트
    @DisplayName("getuserId(): 토큰으로 유저 ID 취득")
    @Test
    void getUserId() {
        // given: jjwt 라이브러리를 사용해 id=1인 클레임을 추가하여 토큰 생성
        Long userId = 1L;
        String token = JwtFactory.builder()
                .claims(Map.of("id", userId))
                .build()
                .createToken(jwtProperties);

        // when: 토큰 제공자의 getUserId() Method 호출하여 유저 ID 반환
        Long userIdByToken = tokenProvider.getUserId(token);

        // then: 반환받은 유저 ID가 given 절에서 설정한 유저 ID 값과 같은 1인지 확인
        assertThat(userIdByToken).isEqualTo(userId);
    }
}
