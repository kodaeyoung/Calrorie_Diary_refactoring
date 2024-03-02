package com.example.cal_dia_mem.profile.controller;

import com.example.cal_dia_mem.board.entity.BoardEntity;
import com.example.cal_dia_mem.profile.dto.ProfileDTO;
import com.example.cal_dia_mem.profile.entity.ProfileEntity;
import com.example.cal_dia_mem.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    @Autowired
    private ProfileService profileService;


    // 프로필 수정 시 사용자 기존 데이터 출력
    @GetMapping("/profile/modify")
    public String memberProfile(HttpSession session, Model model){
        // 세션에서 이메일 정보 가져오기
        String myEmail = (String) session.getAttribute("sessionEmail");
        // 이메일을 기반으로 프로필 정보 가져오기
        ProfileDTO profileDTO = profileService.modifyProfile(myEmail);
        // 각 비율 계산하여 모델에 추가
        calculatePercentages(profileDTO, model);
        // 프로필 정보와 BMI 모델에 추가하여 프로필 페이지 반환
        model.addAttribute("modifyProfile", profileDTO);
        model.addAttribute("bmi", calculateBMI(profileDTO));
        return "/profile/profile";
    }

    @PostMapping("/profile/modify")
    public String profileUpdate(@ModelAttribute ProfileDTO profileDTO, HttpServletRequest request){

        profileService.save(profileDTO);
        request.setAttribute("message","회원 정보 수정이 완료되었습니다.");
        request.setAttribute("searchUrl","/index/call");
        return "/member/message";
    }

    // 각 비율 계산하여 모델에 추가하는 메소드
    private void calculatePercentages(ProfileDTO profileDTO, Model model) {
        calculateMusclePercentage(profileDTO, model);
        calculateBMIPercentage(profileDTO, model);
        calculateBodyFatPercentage(profileDTO, model);
        calculateWeightPercentage(profileDTO, model);
    }

    // 근육량 비율 계산
    private void calculateMusclePercentage(ProfileDTO profileDTO, Model model) {
        if (!profileDTO.getMuscle().isEmpty() && !profileDTO.getPurposeMuscle().isEmpty()) {
            double muscle = Double.parseDouble(profileDTO.getMuscle());
            double purposeMuscle = Double.parseDouble(profileDTO.getPurposeMuscle());
            double percentMuscle = (muscle <= purposeMuscle) ? muscle * 100 / purposeMuscle : purposeMuscle * 100 / muscle;
            double roundedPercentMuscle = Math.round(percentMuscle * 10.0) / 10.0; // 소수점 한 자리까지 반올림
            model.addAttribute("percentMuscle", roundedPercentMuscle);
        }
    }

    // BMI 비율 계산
    private void calculateBMIPercentage(ProfileDTO profileDTO, Model model) {
        if (!profileDTO.getCurrentWeight().isEmpty() && !profileDTO.getHeight().isEmpty() && !profileDTO.getPurposeBMI().isEmpty()) {
            double currentBMI = calculateBMI(profileDTO);
            double purposeBMI = Double.parseDouble(profileDTO.getPurposeBMI());
            double percentBMI = (currentBMI <= purposeBMI) ? currentBMI * 100 / purposeBMI : purposeBMI * 100 / currentBMI;
            double roundedPercentBMI = Math.round(percentBMI * 10.0) / 10.0; // 소수점 한 자리까지 반올림
            model.addAttribute("percentBMI", roundedPercentBMI);
        }
    }

    // 체지방 비율 계산
    private void calculateBodyFatPercentage(ProfileDTO profileDTO, Model model) {
        if (!profileDTO.getBodyFat().isEmpty() && !profileDTO.getPurposeBodyFat().isEmpty()) {
            int bodyFat = Integer.parseInt(profileDTO.getBodyFat());
            int purposeBodyFat = Integer.parseInt(profileDTO.getPurposeBodyFat());
            double percentBodyFat = (bodyFat <= purposeBodyFat) ? bodyFat * 100.0 / purposeBodyFat : purposeBodyFat * 100.0 / bodyFat;
            double roundedPercentBodyFat = Math.round(percentBodyFat * 10.0) / 10.0; // 소수점 한 자리까지 반올림
            model.addAttribute("percentBodyFat", roundedPercentBodyFat);
        }
    }

    // 체중 비율 계산
    private void calculateWeightPercentage(ProfileDTO profileDTO, Model model) {
        if (!profileDTO.getCurrentWeight().isEmpty() && !profileDTO.getPurposeWeight().isEmpty()) {
            double currentWeight = Double.parseDouble(profileDTO.getCurrentWeight());
            double purposeWeight = Double.parseDouble(profileDTO.getPurposeWeight());
            double percentWeight = (currentWeight <= purposeWeight) ? currentWeight * 100 / purposeWeight : purposeWeight * 100 / currentWeight;
            double roundedPercentWeight = Math.round(percentWeight * 10.0) / 10.0; // 반올림하여 소수점 한 자리까지 표시
            model.addAttribute("percentWeight", roundedPercentWeight);
        }
    }

    // BMI 계산 메소드
    private double calculateBMI(ProfileDTO profileDTO) {
        if (!profileDTO.getCurrentWeight().isEmpty() && !profileDTO.getHeight().isEmpty()) {
            double weight = Double.parseDouble(profileDTO.getCurrentWeight());
            double height = Double.parseDouble(profileDTO.getHeight());
            return Math.round((weight / (height / 100) / (height / 100)) * 10.0) / 10.0; //
        }
        return 1.0; // Default BMI
    }
}
