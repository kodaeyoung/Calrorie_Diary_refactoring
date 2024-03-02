package com.example.cal_dia_mem.diary.controller;

import com.example.cal_dia_mem.api.dto.ApiDTO;
import com.example.cal_dia_mem.diary.dto.DiaryDTO;
import com.example.cal_dia_mem.diary.entity.DiaryEntity;
import com.example.cal_dia_mem.diary.service.DiaryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.Date;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    // 다이어리 저장 처리
    @PostMapping("/diary/save")
    public String saveData(@ModelAttribute @Valid DiaryDTO diaryDTO, Model model) {
        diaryService.save(diaryDTO);
        model.addAttribute("message", "저장 되었습니다.");
        model.addAttribute("searchUrl", "/diary");
        return "/member/message";
    }

    @GetMapping("/diary")
    public String diary(Model model, HttpSession session){
        String myEmail = (String) session.getAttribute("sessionEmail");
        Date todayDate = new Date(System.currentTimeMillis());

        // 오늘 날짜와 멤버 이메일을 매개변수로 회원 별 오늘 섭취한 음식 및 영양성분 받아오기
        List<DiaryDTO> list=diaryService.callDiary(todayDate,myEmail);
        double Carboydrate = diaryService.totalCarbohydrate(list);
        double protien = diaryService.totalProtein(list);
        double fat = diaryService.totalFat(list);
        double sugars = diaryService.totalSugars(list);
        double salt = diaryService.totalSalt(list);
        double kcal= diaryService.totalKcal(list);

        model.addAttribute("kcal",kcal);
        model.addAttribute("list",list);
        model.addAttribute("Carboydrate",Carboydrate);
        model.addAttribute("protien",protien);
        model.addAttribute("fat",fat);
        model.addAttribute("sugars",sugars);
        model.addAttribute("salt",salt);


        return "/camera/diary";
    }

    @GetMapping("/diary/delete")
    public String deleteDiary(@RequestParam Integer id) {
        diaryService.diaryDelete(id);
        return "redirect:/diary";
    }


}
