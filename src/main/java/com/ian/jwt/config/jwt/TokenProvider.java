package com.ian.jwt.config.jwt;

import com.ian.jwt.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;

    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();

        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
    }

    // JWT 토큰 생성 Method
    private String makeToken(Date expiry, User user) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)  // 헤더 typ: JWT
                .setIssuer(jwtProperties.getIssuer())  // 내용 iss: oiysful@gmail.com(properties 설정 값)
                .setIssuedAt(now)  // 내용 iat: 현재 시간
                .setExpiration(expiry)  // 내용 exp: expiry 멤버 변수 값
                .setSubject(user.getEmail())  // 내용 sub: 유저 email
                .claim("id", user.getId())  // claim id: 유저 ID
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())  // 서명: 비밀 값과 함께 해시값을 HS256 방식으로 암호화
                .compact();
    }

    // JWT 토큰 유효성 검증 Method
    public boolean validToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecretKey())  // 비밀 값으로 복호화
                    .parseClaimsJws(token);

            return true;
        } catch(Exception e) {  // 복호화 과정에서 Exception 발생 시 유효하지 않은 토큰으로 판단
            return false;
        }
    }

    // Token 기반으로 인증 정보를 가져오는 Method
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        return new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities),token, authorities);
    }

    // Token 기반으로 유저 ID를 가져오는 Method
    public Long getUserId(String token) {
        Claims claims = getClaims(token);

        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token) {

        return Jwts.parser()  // Claim 조회
                .setSigningKey(jwtProperties.getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }
}
