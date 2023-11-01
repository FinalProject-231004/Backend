package com.starta.project.domain.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starta.project.domain.member.dto.KaKaoFirstLoginDto;
import com.starta.project.domain.member.dto.KakaoUserInfoDto;
import com.starta.project.domain.member.dto.UpdateNicknameRequestDto;
import com.starta.project.domain.member.dto.UpdatePasswordRequestDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.global.jwt.JwtUtil;
import com.starta.project.global.messageDto.MsgResponse;
import com.starta.project.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j(topic = "KAKAO Login")
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberDetailRepository memberDetailRepository;
    private final RestTemplate restTemplate;   // RestTemplate Config 작성
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${kakao.client-id}")
    private String client_id;

    @Value("${kakao.redirect-uri}")
    private String redirect_uri;

    public MsgResponse kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);
        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        KakaoUserInfoDto kakaoMemberInfo = getKakaoUserInfo(accessToken);
        // 3. 로그인하기(필요시에 회원가입)
        Map<String, Object> result = registerKakaoUserIfNeeded(kakaoMemberInfo);

        Member kakaoMember = (Member) result.get("member");
        String message = (String) result.get("message");

        // 토큰 만들기
        String jwtAccessToken = jwtUtil.createToken(kakaoMember.getUsername(), kakaoMember.getRole());
        String jwtRefreshToken = refreshTokenService.createRefreshToken(kakaoMember.getUsername(), kakaoMember.getRole());

        jwtUtil.addJwtToHeader(jwtAccessToken, jwtRefreshToken, response);

        return new MsgResponse(message);


    }

    private String getToken(String code) throws JsonProcessingException {
        log.info("인가코드: " + code);
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kauth.kakao.com")
                .path("/oauth/token")
                .encode()
                .build()
                .toUri();
        log.info("요청 URL 만들기 끝: ");

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        log.info("HttpHeaders headers ");
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        log.info(" MultiValueMap<String, String> body");
        body.add("grant_type", "authorization_code");
        body.add("client_id", client_id);
        body.add("redirect_uri", redirect_uri);
        body.add("code", code);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(body);
        log.info(" RequestEntity<MultiValueMap<String, String>> requestEntity =");
        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );
        log.info("ResponseEntity<String> response = restTemplate.exchange");
        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());

        log.info("getToken 끝");
        return jsonNode.get("access_token").asText();

    }

    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        log.info("getKakaoUserInfo 시작");
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v2/user/me")
                .encode()
                .build()
                .toUri();
        log.info(" URI uri = UriComponentsBuilder");

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        log.info(" HttpHeaders headers = new HttpHeaders();");
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(new LinkedMultiValueMap<>());
        log.info("   RequestEntity<MultiValueMap<String, String>> requestEntity");
        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );
        log.info("ResponseEntity<String> response = restTemplate.exchange(");
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        log.info("JsonNode jsonNode: " + jsonNode);
        Long id = jsonNode.get("id").asLong();
//        String nickname = jsonNode.get("properties").get("nickname").asText();
        String profileImg = jsonNode.get("properties").get("thumbnail_image").asText();

        String email = jsonNode.get("kakao_account").get("email").asText();
        log.info("email: " + email);

        log.info("카카오 사용자 정보: " + id + ", " + ", " + profileImg + ", " + email);
        return new KakaoUserInfoDto(id, profileImg, email);
    }


    public Map<String, Object> registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
        log.info("registerKakaoUserIfNeeded 시작");
        Long kakaoId = kakaoUserInfo.getId();
        String email = kakaoUserInfo.getEmail();
        String message;

        Member kakaoMember = memberRepository.findByKakaoId(kakaoId).orElse(null);

        // 1. 카카오 신규로그인
        if (kakaoMember == null) {
            String randomNickname = generateCustomNickname();
            // 2. 닉네임 중복여부 확인
            while (memberDetailRepository.findByNickname(randomNickname).isPresent()) {
                randomNickname = generateCustomNickname();
            }
            String kakaoUsername = "k" + kakaoId.toString();

            // 이메일의 앞 3글자로 초기비번 설정
            String emailPrefix = kakaoUserInfo.getEmail().split("@")[0].toLowerCase();
            // 3글자 이상인 경우에는 앞에서부터 3글자만 사용
            emailPrefix = emailPrefix.length() >= 2 ? emailPrefix.substring(0, 2) : emailPrefix;
            String kakaoPassword = passwordEncoder.encode(emailPrefix + "quiz8@@");

            // 신규 회원 생성
            kakaoMember = memberRepository.save(new Member(kakaoUsername, kakaoPassword, UserRoleEnum.USER, kakaoId));
            MemberDetail memberDetail = new MemberDetail(randomNickname, kakaoUserInfo.getProfilImg());
            memberDetail.setMember(kakaoMember);
            memberDetailRepository.save(memberDetail);

            message = "신규유저입니다.";
        } else {
            message = "기존유저입니다.";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("member", kakaoMember);
        result.put("message", message);

        log.info("member : " + kakaoMember);
        log.info("message : " + message);

        return result;
    }

    private String generateCustomNickname() {
        log.info("generateCustomNickname 시작");
        Random random = new Random();
        StringBuilder nickname = new StringBuilder();

        // 첫 글자는 'k'
        nickname.append('k');
        // 두 번째부터 다섯 번째 문자: 영소문자와 숫자 랜덤
        String alphabetAndDigits = "abcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 4; i++) {
            nickname.append(alphabetAndDigits.charAt(random.nextInt(alphabetAndDigits.length())));
        }
        log.info("generateCustomNickname 끝");
        return nickname.toString();
    }

}
