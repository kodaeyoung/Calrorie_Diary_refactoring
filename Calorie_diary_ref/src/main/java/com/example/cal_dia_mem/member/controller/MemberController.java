package com.example.cal_dia_mem.member.controller;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.example.cal_dia_mem.board.dto.BoardDTO;
import com.example.cal_dia_mem.board.service.BoardService;
import com.example.cal_dia_mem.diary.dto.DiaryDTO;
import com.example.cal_dia_mem.diary.repository.DiaryRepository;
import com.example.cal_dia_mem.diary.service.DiaryService;
import com.example.cal_dia_mem.foodCommend.dto.FoodCommendDTO;
import com.example.cal_dia_mem.foodCommend.service.FoodCommendService;
import com.example.cal_dia_mem.member.dto.MemberDTO;
import com.example.cal_dia_mem.member.service.MemberService;
import com.example.cal_dia_mem.profile.dto.ProfileDTO;
import com.example.cal_dia_mem.profile.service.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ProfileService profileService;

    private final DiaryService diaryService;

    private final BoardService boardService;

    private final FoodCommendService foodCommendService;

    // 회원 가입 페이지로 이동
    @GetMapping("/member/save")
    public String saveForm() {
        return "/member/createaccount";
    }


    //memberService에 memberDTO넘겨줌
    @PostMapping("/member/save")
    public String save(@Valid MemberDTO memberDTO, Errors errors, org.springframework.ui.Model model) {

        if (errors.hasErrors()) {
            //회원가입 실패 시 입력값 유지
            model.addAttribute("memberDTO", memberDTO);
            //유효성 통과 못한 필드와 메시지 핸들링
            Map<String, String> validatorResult = MemberService.validateHandling(errors);
            for (String key : validatorResult.keySet()) {
                model.addAttribute(key, validatorResult.get(key));
            }
            return "/member/createaccount";
        }

        // 회원 및 프로필 정보 저장
        ProfileDTO profileDTO = createInitialProfile(memberDTO);
        memberService.save(memberDTO);
        profileService.save(profileDTO);

        return "/member/login";
    }

    // 로그인 페이지로 이동
    @GetMapping("/member/login")
    public String showLoginForm() {
        return "/member/login";
    }

    // 회원 로그인 처리
    @PostMapping("/member/login")
    public String login(@ModelAttribute MemberDTO memberDTO, HttpServletRequest request, Model model) {
        Optional<MemberDTO> loginResult = memberService.login(memberDTO);
        //로그인 성공
        if (loginResult.isPresent()) {

            // 로그인 성공 시 사용자 정보 가져오기
            MemberDTO member = loginResult.get();
            // 세션에 사용자 정보 설정
            setSessionAttributes(request.getSession(), member);
            // 프로필 정보 설정
            ProfileDTO profileDTO = retrieveProfile(member.getMemberEmail());
            // 목표 값 계산 및 설정
            calculateAndSetPurposeValues(profileDTO);
            model.addAttribute("modifyProfile", profileDTO);
            // 사용자의 일일 다이어리 정보 추가
            addDiaryInfo(model, member.getMemberEmail());
            // 영양소 상태 정보 추가
            addNutrientInfo(model, member.getMemberEmail());
            // 인기 게시글 정보 추가
            addPopularBoardInfo(model);
            // 음식 추천 정보 추가
            addFoodRecommendationInfo(model);
            model.addAttribute("selectDate", "오늘");
            return "index";
        }
            //로그인 실패
        else {
            // 알림창 및 리다이렉션
            model.addAttribute("message", "아이디와 비밀번호가 일치하지 않습니다!");
            model.addAttribute("searchUrl", "/member/login");
            return "/member/message";
        }
    }

    @GetMapping("/index/call")
    public String index(HttpServletRequest request, Model model,HttpSession session){
        String myEmail = (String) session.getAttribute("sessionEmail");
        ProfileDTO profileDTO = profileService.modifyProfile(myEmail);
        calculateAndSetPurposeValues(profileDTO);
        model.addAttribute("modifyProfile", profileDTO);
        addDiaryInfo(model, myEmail);
        addNutrientInfo(model, myEmail);
        addPopularBoardInfo(model);
        addFoodRecommendationInfo(model);
        model.addAttribute("selectDate", "오늘");
        return "index";
    }

    @PostMapping("/index/call/past")
    public String index(Model model,HttpSession session, @RequestParam("year") String year,@RequestParam("month") String month,@RequestParam("date") String date) {
        String myEmail = (String) session.getAttribute("sessionEmail");
        //인덱스에 표현할 프로필
        ProfileDTO profileDTO = profileService.modifyProfile(myEmail);
        calculateAndSetPurposeValues(profileDTO);
        model.addAttribute("modifyProfile", profileDTO);
        addPopularBoardInfo(model);
        addFoodRecommendationInfo(model);
      //-----------------------시간 처리 함수-----------------------------------
        java.util.Date utilDate = null;
        java.sql.Date selectedDate = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = year + "-" + month + "-" + date;
        try {
            utilDate = dateFormat.parse(dateString);
            selectedDate = new java.sql.Date(utilDate.getTime());
            // 이제 selectedDate를 사용하여 원하는 작업 수행
        } catch (ParseException e) {
            e.printStackTrace();
            // 날짜 변환 중 에러가 발생한 경우 처리
        }
      //----------------------------------------------------------------------

        if(selectedDate==null){
            model.addAttribute("selectDate"," ");
        }
        else {
            model.addAttribute("selectDate",selectedDate+"일");
            addDiaryInfo(model, myEmail, selectedDate);
            addNutrientInfo(model, myEmail, selectedDate);
        }
        return "index";
    }


    // 회원 로그아웃 처리
    @GetMapping("/member/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "/member/login";
    }

    //이메일 중복체크
    @PostMapping("/member/email-check")
    public @ResponseBody Boolean emailCheck(@RequestParam("memberEmail") String memberEmail){
        return memberService.isEmailAvailable(memberEmail);
    }

    //닉네임 중복 체크
    @PostMapping("/member/nickname-check")
    public @ResponseBody Boolean checkNickname(@RequestParam("memberNickname") String memberNickname) {
        return memberService.isNicknameAvailable(memberNickname);
    }

    // 메서드: 회원 가입 시 초기 프로필 생성
    private ProfileDTO createInitialProfile(MemberDTO memberDTO) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setMemberEmail(memberDTO.getMemberEmail());
        profileDTO.setMemberName(memberDTO.getMemberName());

        //나머지는 공백으로 초기화(nullPointExeption 방지)
        profileDTO.setPurposeWeight("");
        profileDTO.setMuscle("");
        profileDTO.setHeight("");
        profileDTO.setPurposeMuscle("");
        profileDTO.setPurposeBodyFat("");
        profileDTO.setPurposeBMI("");
        profileDTO.setBodyFat("");
        profileDTO.setCurrentWeight("");
        profileDTO.setGender("");
        return profileDTO;
    }

    // 세션에 사용자 정보 설정하는 메소드
    private void setSessionAttributes(HttpSession session, MemberDTO member) {
        session.setAttribute("sessionNickname", member.getMemberNickname());
        session.setAttribute("sessionEmail", member.getMemberEmail());
        session.setAttribute("sessionName", member.getMemberName());
    }

    // 사용자 프로필 정보 가져오는 메소드
    private ProfileDTO retrieveProfile(String email) {
        ProfileDTO profileDTO = profileService.modifyProfile(email);
        return profileDTO != null ? profileDTO : new ProfileDTO(); // To handle null case gracefully
    }

    private void calculateAndSetPurposeValues(ProfileDTO profileDTO) {
        // 체중이 설정되어 있는 경우에만 계산
        if (!profileDTO.getCurrentWeight().equals("") && !profileDTO.getPurposeWeight().equals("")) {
            //목표 체중이 현재 체중보다 높다면 --> 증량이 목적
            if (Double.parseDouble(profileDTO.getCurrentWeight()) <= Double.parseDouble(profileDTO.getPurposeWeight())) {
                Double percentWeight = Double.parseDouble(profileDTO.getCurrentWeight()) * 100 / Double.parseDouble(profileDTO.getPurposeWeight());
                profileDTO.setPurposeWeight(String.format("%.1f", percentWeight));
            } else {
                Double percentWeight = Double.parseDouble(profileDTO.getPurposeWeight()) * 100 / Double.parseDouble(profileDTO.getCurrentWeight());
                profileDTO.setPurposeWeight(String.format("%.1f", percentWeight));
            }
        }

        // 근육량이 설정되어 있는 경우에만 계산
        if (!profileDTO.getMuscle().equals("") && !profileDTO.getPurposeMuscle().equals("")) {
            // 목표가 현재보다 크다면?--> 근육량 증량이 목표
            if (Double.parseDouble(profileDTO.getMuscle()) <= Double.parseDouble(profileDTO.getPurposeMuscle())) {
                Double percentMuscle = Double.parseDouble(profileDTO.getMuscle()) * 100 / Double.parseDouble(profileDTO.getPurposeMuscle());
                profileDTO.setPurposeMuscle(String.format("%.1f", percentMuscle));
            } else {
                Double percentMuscle = Double.parseDouble(profileDTO.getPurposeMuscle()) * 100 / Double.parseDouble(profileDTO.getMuscle());
                profileDTO.setPurposeMuscle(String.format("%.1f", percentMuscle));
            }
        }

        // 체지방량이 설정되어 있는 경우에만 계산
        if (!profileDTO.getPurposeBodyFat().equals("") && !profileDTO.getBodyFat().equals("")) {
            //목표 체지방이 현재 체지방보다 높다면 --> 벌크업이 목적
            if (Double.parseDouble(profileDTO.getBodyFat()) <= Double.parseDouble(profileDTO.getPurposeBodyFat())) {
                Double percentBodyFat = Double.parseDouble(profileDTO.getBodyFat()) * 100 / Double.parseDouble(profileDTO.getPurposeBodyFat());
                profileDTO.setPurposeBodyFat(String.format("%.1f", percentBodyFat));
            } else {
                Double percentBodyFat = Double.parseDouble(profileDTO.getPurposeBodyFat()) * 100 / Double.parseDouble(profileDTO.getBodyFat());
                profileDTO.setPurposeBodyFat(String.format("%.1f", percentBodyFat));
            }
        }

        // BMI가 설정되어 있는 경우에만 계산
        if (!profileDTO.getCurrentWeight().equals("") && !profileDTO.getHeight().equals("") && !profileDTO.getPurposeBMI().equals("")) {
            //목표BMI가 현재BMI보다 크다면-->증량이 목적
            if (Double.parseDouble(profileDTO.getCurrentWeight()) / (Double.parseDouble(profileDTO.getHeight()) / 100)
                    / (Double.parseDouble(profileDTO.getHeight()) / 100) <= Double.parseDouble(profileDTO.getPurposeBMI())) {

                double percentBmi = Double.parseDouble(profileDTO.getCurrentWeight()) / (Double.parseDouble(profileDTO.getHeight()) / 100)
                        / (Double.parseDouble(profileDTO.getHeight()) / 100) * 100 / Double.parseDouble(profileDTO.getPurposeBMI());

                Double percentBMI = Math.round(percentBmi * 100.0) / 100.0;
                profileDTO.setPurposeBMI(String.valueOf(percentBMI));
            } else {
                double current = Double.parseDouble(profileDTO.getCurrentWeight()) / (Double.parseDouble(profileDTO.getHeight()) / 100)
                        / (Double.parseDouble(profileDTO.getHeight()) / 100);
                double percentBmi = (Double.parseDouble(profileDTO.getPurposeBMI()) * 100) / current;

                Double percentBMI = Math.round(percentBmi * 100.0) / 100.0;
                profileDTO.setPurposeBMI(String.valueOf(percentBMI));
            }
        }

        if (profileDTO.getPurposeWeight().equals("")) profileDTO.setPurposeWeight("프로필을 입력해주세요");
        if (profileDTO.getPurposeMuscle().equals("")) profileDTO.setPurposeMuscle("프로필을 입력해주세요");
        if (profileDTO.getPurposeBodyFat().equals("")) profileDTO.setPurposeBodyFat("프로필을 입력해주세요");
        if (profileDTO.getPurposeBMI().equals("")) profileDTO.setPurposeBMI("프로필을 입력해주세요");
    }

    // 사용자의 일일 다이어리 정보 추가하는 메소드---------------------------------------------------------
    private void addDiaryInfo(Model model, String email) {
        Date todayDate = new Date(System.currentTimeMillis());
        List<DiaryDTO> dto = diaryService.callDiary(todayDate, email);
        model.addAttribute("todayList", dto);
    }
    private void addDiaryInfo(Model model, String email, Date selectedDate) {
        List<DiaryDTO> dto = diaryService.callDiary(selectedDate, email);
        model.addAttribute("todayList", dto);
    }
   //-----------------------------------------------------------------------------------------------

    // 영양소 상태 정보 추가하는 메소드-------------------------------------------------------------------
    private void addNutrientInfo(Model model, String email) {
        Date todayDate = new Date(System.currentTimeMillis());
        List<String> overNutrient = diaryService.overNutrient(todayDate, email);
        List<String> scarceNutrient = diaryService.scarceNutrient(todayDate, email);

        model.addAttribute("overNutrient", overNutrient != null ? overNutrient : "정확한 서비스를 제공 위해 프로필설정을 해주세요.");
        model.addAttribute("scarceNutrient", scarceNutrient != null ? scarceNutrient : "정확한 서비스를 제공 위해 프로필설정을 해주세요.");
    }
    private void addNutrientInfo(Model model, String email, Date selectedDate) {
        List<String> overNutrient = diaryService.overNutrient(selectedDate, email);
        List<String> scarceNutrient = diaryService.scarceNutrient(selectedDate, email);

        model.addAttribute("overNutrient", overNutrient != null ? overNutrient : "정확한 서비스를 제공 위해 프로필설정을 해주세요.");
        model.addAttribute("scarceNutrient", scarceNutrient != null ? scarceNutrient : "정확한 서비스를 제공 위해 프로필설정을 해주세요.");
    }

    //--------------------------------------------------------------------------------------------

    // 인기 게시글 정보 추가하는 메소드
    private void addPopularBoardInfo(Model model) {
        List<BoardDTO> popularBoard = boardService.popularBoard();
        model.addAttribute("poplarBoard", popularBoard);
    }

    // 음식 추천 정보 추가하는 메소드
    private void addFoodRecommendationInfo(Model model) {
        List<FoodCommendDTO> foodCommendDTOList = foodCommendService.commendFood();
        String commendInfo = foodCommendService.foodCommendInfo(foodCommendDTOList);

        model.addAttribute("commendNutrient", foodCommendDTOList);
        model.addAttribute("commendInfo", commendInfo);
    }
}
