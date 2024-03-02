package com.example.cal_dia_mem.api.controller;

import com.example.cal_dia_mem.api.Service.ApiService;
import com.example.cal_dia_mem.api.dto.ApiDTO;
import jakarta.servlet.http.HttpSession;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//@org.springframework.web.bind.annotation.RestController
@Controller
public class RestController {
    @Autowired
    ApiService apiService;

    // API 페이지로 이동
    @GetMapping("/api")
    public String showApiPage(Model model) {
        return "/api/fooddb";
    }

    // API 호출 및 결과 표시
    @PostMapping("/api/foodData")
    public String callApi(Model model, @RequestParam String food_name, HttpSession session) throws ParseException {
        // 세션에서 이메일 가져오기
        String myEmail = (String) session.getAttribute("sessionEmail");
        // 모델에 이메일 추가
        model.addAttribute("memberEmail", myEmail);
        // API 호출 및 결과 받아오기
        String jsonStr = apiService.callApi(food_name);
        // JSON 파싱하여 데이터 리스트 생성
        List<ApiDTO> dataList = apiService.jsonParse(jsonStr);
        // 모델에 데이터 리스트 추가
        model.addAttribute("dataList", dataList);
        // API 페이지로 이동
        return "/api/fooddb";
    }

}

