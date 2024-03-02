package com.example.cal_dia_mem.member.service;

import com.example.cal_dia_mem.member.dto.MemberDTO;
import com.example.cal_dia_mem.profile.entity.ProfileEntity;
import com.example.cal_dia_mem.member.entity.SiteUserEntity;
import com.example.cal_dia_mem.member.repository.MemberRepository;
import com.example.cal_dia_mem.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원 정보 저장
    public void save(MemberDTO memberDTO) {
        // dto -> entity로 변환
        // repository의 save메서드 호출
        SiteUserEntity siteUserEntity = SiteUserEntity.toSiteUserEntity(memberDTO);
        memberRepository.save(siteUserEntity);
    }

    // 유효성 검사 에러 핸들링
    public static Map<String, String> validateHandling(Errors errors) {
        Map<String, String> validatorResult = new HashMap<>();

        for (FieldError error : errors.getFieldErrors()) {
            String validKeyName = String.format("valid_%s", error.getField());
            validatorResult.put(validKeyName, error.getDefaultMessage());
        }
        return validatorResult;
    }

    // 로그인 시도
    public Optional<MemberDTO> login(MemberDTO memberDTO) {
        return memberRepository.findByMemberEmail(memberDTO.getMemberEmail())
                .filter(user -> user.getMemberPassword().equals(memberDTO.getMemberPassword()))
                .map(MemberDTO::tomemberDTO);
    }

    // 이메일 사용 가능 여부 확인
    public boolean isEmailAvailable(String memberEmail) {
        return !memberRepository.findByMemberEmail(memberEmail).isPresent();
    }

    // 닉네임 사용 가능 여부 확인
    public boolean isNicknameAvailable(String memberNickname) {
        return !memberRepository.findByMemberNickname(memberNickname).isPresent();
    }
}
