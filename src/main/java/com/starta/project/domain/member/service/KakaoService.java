package com.starta.project.domain.member.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.starta.project.domain.member.dto.KakaoMemberDto;
import com.starta.project.domain.member.entity.Member;
import com.starta.project.domain.member.entity.MemberDetail;
import com.starta.project.domain.member.entity.UserRoleEnum;
import com.starta.project.domain.member.repository.MemberDetailRepository;
import com.starta.project.domain.member.repository.MemberRepository;
import com.starta.project.global.jwt.JwtUtil;
import com.starta.project.global.messageDto.MsgResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.net.URI;
import java.util.Random;
import java.util.UUID;

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

    @Transactional
    public MsgResponse kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getToken(code);
        log.info("accessToken값 : " +accessToken);
        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
        KakaoMemberDto kakaoMemberInfo = getKakaoUserInfo(accessToken);
        // 3. 필요시에 회원가입
        Member kakaoMember = registerKakaoUserIfNeeded(kakaoMemberInfo);

        String jwtAccessToken = jwtUtil.createToken(kakaoMember.getUsername(), kakaoMember.getRole()); // 최종적으로는 토큰이 반환됨
        String jwtRefreshToken = refreshTokenService.createRefreshToken(kakaoMember.getUsername(), kakaoMember.getRole());

        jwtUtil.addJwtToHeader(jwtAccessToken,jwtRefreshToken, response);

        return new MsgResponse("카카오 로그인 성공!!");


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

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "4ced98994663e71c06cadbedd2b2ac21");
        body.add("redirect_uri", "http://localhost:8080/api/member/kakao/callback");
        body.add("code", code);

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(body);

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());

        log.info(jsonNode.toString());
        jsonNode.get("access_token");
        log.info(String.valueOf(jsonNode.get("access_token")));
        log.info("카카오 API 응답: " + response.getBody());

        log.info("getToken() 메서드 종료");
        return jsonNode.get("access_token").asText();

    }
    private KakaoMemberDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        log.info("accessToken: " + accessToken);

        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("https://kapi.kakao.com")
                .path("/v2/user/me")
                .encode()
                .build()
                .toUri();

        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        log.info( "Authorization " +  "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        log.info("Content-type " + "application/x-www-form-urlencoded;charset=utf-8");

        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
                .post(uri)
                .headers(headers)
                .body(new LinkedMultiValueMap<>());

        // HTTP 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(
                requestEntity,
                String.class
        );

        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
        System.out.println(jsonNode);

        Long id = jsonNode.get("id").asLong();
//        String nickname = jsonNode.get("properties").get("nickname").asText();
        String profileImg = jsonNode.get("properties").get("thumbnail_image").asText();

        log.info("카카오 사용자 정보: " + id + ", "  + ", " + profileImg);
        return new KakaoMemberDto(id, profileImg);
    }


    private Member registerKakaoUserIfNeeded(KakaoMemberDto kakaoUserInfo) {
        log.info("kakaoUserInfo 값 확인" + kakaoUserInfo.toString());
        Long kakaoId = kakaoUserInfo.getId();
        String profilImg = kakaoUserInfo.getProfilImg();
        Member kakaoUser = memberRepository.findByKakaoId(kakaoId).orElse(null);

        // 1. 카카오 신규로그인
        if (kakaoUser == null) {

            String randomNickname = generateCustomNickname();
            // 중복 아닌 닉네임일 때까지 반복
            while (memberDetailRepository.findByNickname(randomNickname).isPresent()) {
                randomNickname = generateCustomNickname();
            }
            String kakaoUsername = "k" + kakaoId.toString();  // username은 nullable false이고
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            Member savedMember = memberRepository.save(new Member(kakaoUsername, encodedPassword, UserRoleEnum.USER, kakaoId));
            MemberDetail memberDetail = new MemberDetail(randomNickname,profilImg);
            memberDetail.setMember(savedMember);
            memberDetailRepository.save(memberDetail);

            return savedMember;
        }
        return kakaoUser;
    }
    private String generateCustomNickname() {
        Random random = new Random();
        StringBuilder nickname = new StringBuilder();

        // 첫 글자는 'k'
        nickname.append('k');
        // 두 번째부터 다섯 번째 문자: 영소문자와 숫자 랜덤
        String alphabetAndDigits = "abcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < 4; i++) {
            nickname.append(alphabetAndDigits.charAt(random.nextInt(alphabetAndDigits.length())));
        }
        return nickname.toString();
    }
}
