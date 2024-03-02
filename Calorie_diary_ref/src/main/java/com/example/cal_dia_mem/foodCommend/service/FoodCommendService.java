package com.example.cal_dia_mem.foodCommend.service;

import com.example.cal_dia_mem.diary.dto.DiaryDTO;
import com.example.cal_dia_mem.diary.entity.DiaryEntity;
import com.example.cal_dia_mem.diary.service.DiaryService;
import com.example.cal_dia_mem.foodCommend.dto.FoodCommendDTO;
import com.example.cal_dia_mem.foodCommend.entity.FoodCommendEntity;
import com.example.cal_dia_mem.foodCommend.repository.FoodCommendRepository;
import com.example.cal_dia_mem.member.entity.SiteUserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodCommendService {

    private final FoodCommendRepository foodCommendRepository;
    @Autowired
    private final DiaryService diaryService;

    private static final int NUM_RECOMMENDATIONS = 3;
    private static final int NUM_RECOMMENDATION_RANGES = 5;

    //날짜를 입력 받아 랜덤하게 추출
    public int generateRandomFoodRecommendationId(int index) {
        java.util.Date today = new java.util.Date();
        Date sqlDate = new Date(today.getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sqlDate);

        // 연, 월, 일 분리
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // 월은 0부터 시작하므로 1을 더해줍니다.
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        switch (index) {
            case 1:
                return (((year + month + day) * month * month / day) * 3) % NUM_RECOMMENDATION_RANGES + 1;
            case 2:
                return (((year - month - day) * day / month) * 3) % NUM_RECOMMENDATION_RANGES + 6;
            case 3:
                return (((year + month - day) * month / day) * 3) % NUM_RECOMMENDATION_RANGES + 11;
            default:
                throw new IllegalArgumentException("Invalid index: " + index);
        }
    }

    //
    // 음식 추천 목록 가져오기
    public List<FoodCommendDTO> commendFood() {
        List<Long> recommendationIds = new ArrayList<>();
        for (int i = 1; i <= NUM_RECOMMENDATIONS; i++) {
            recommendationIds.add((long) generateRandomFoodRecommendationId(i));
        }

        return recommendationIds.stream()
                .map(id -> foodCommendRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FoodCommendDTO::toFoodCommendDTO)
                .collect(Collectors.toList());
    }

    public String foodCommendInfo (List<FoodCommendDTO> foodCommendDTOList){
        double carbohydrate = totalNutrientValue(foodCommendDTOList, FoodCommendDTO::getCarbohydrate);
        double protein = totalNutrientValue(foodCommendDTOList, FoodCommendDTO::getProtein);
        double fat = totalNutrientValue(foodCommendDTOList, FoodCommendDTO::getFat);
        double kcal = totalNutrientValue(foodCommendDTOList, FoodCommendDTO::getKcal);

        return String.format("위 식단을 모두 섭취 시 총 %.2f 칼로리를 섭취하실 수 있습니다. 이 안에 포함된 영양 성분은 총 %.2f g의 탄수화물, %.2f g의 단백질, %.2f g의 지방입니다.",
                kcal, carbohydrate, protein, fat);

    }

    // 영양 성분 총합 구하기
    private double totalNutrientValue(List<FoodCommendDTO> foodCommendDTOList, Function<FoodCommendDTO, String> nutrientExtractor) {
        return foodCommendDTOList.stream()
                .mapToDouble(dto -> {
                    try {
                        return Double.parseDouble(nutrientExtractor.apply(dto));
                    } catch (NumberFormatException e) {
                        System.err.println("숫자로 변환할 수 없습니다.");
                        return 0.0;
                    }
                })
                .sum();
    }
}
