package com.example.cal_dia_mem.diary.service;
import com.example.cal_dia_mem.diary.dto.DiaryDTO;
import com.example.cal_dia_mem.diary.entity.DiaryEntity;
import com.example.cal_dia_mem.diary.repository.DiaryRepository;
import com.example.cal_dia_mem.foodCommend.dto.FoodCommendDTO;
import com.example.cal_dia_mem.profile.service.ProfileService;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final ProfileService profileService;

    // 칼로리 다이어리에 섭취 음식 및 영양성분 저장
    public void save(DiaryDTO diaryDTO) {
        DiaryEntity diaryEntity = DiaryEntity.toDiaryEntity(diaryDTO);
        diaryRepository.save(diaryEntity);
    }

    //다이어리 삭제
    public void diaryDelete(Integer id){
        diaryRepository.deleteById(id);
    }

    // 특정 날짜의 다이어리 호출
    public List<DiaryDTO> callDiary(Date createDate, String myEmail){
        List<DiaryEntity> diaryEntityList =diaryRepository.findByCreateDateAndMemberEmail(createDate,myEmail);

        // entity 리스트를 dto 리스트로 변환하는 스트림
        return diaryEntityList.stream()
                .map(DiaryEntity::entityToDto)
                .collect(Collectors.toList());
    }

    //ID로 다이어리 조회
    public List<DiaryDTO> idDiary(Integer id){
        Optional<DiaryEntity> diaryEntityList =diaryRepository.findById(id);
        List<DiaryDTO> diaryDtoList = diaryEntityList.stream()
                .map(DiaryEntity::entityToDto)
                .collect(Collectors.toList());
        return diaryDtoList;
    }

    //초과한 영양성부 확인
    public List<String> overNutrient(Date createDate, String myEmail){
        List<String> overNutrient = new ArrayList<>();
        String gen= profileService.returnGen(myEmail);
        String currentWeight = profileService.returnCurrentWeight(myEmail);
        List<DiaryEntity> diaryEntityList =diaryRepository.findByCreateDateAndMemberEmail(createDate,myEmail);

        // entity 리스트를 dto 리스트로 변환하는 스트림
        List<DiaryDTO> diaryDtoList = diaryEntityList.stream()
                .map(DiaryEntity::entityToDto)
                .collect(Collectors.toList());

        if(gen.equals("")||currentWeight.equals(""))return null;
        double minCarbohydrate = gen.equals("m") ? 400 : 350;
        double minProtein = Double.parseDouble(currentWeight) * 1.2;
        double minFat = 65;

        if (totalCarbohydrate(diaryDtoList) > minCarbohydrate) {
            overNutrient.add("탄수화물");
        }
        if (totalProtein(diaryDtoList) > minProtein) {
            overNutrient.add("단백질");
        }
        if (totalFat(diaryDtoList) > minFat) {
            overNutrient.add("지방");
        }
        return overNutrient;
    }

    // 부족한 영양분 받아오기
    public List<String> scarceNutrient(Date createDate, String myEmail){
        List<String> scarceNutrient = new ArrayList<>();
        String gen = profileService.returnGen(myEmail);
        String currentWeight = profileService.returnCurrentWeight(myEmail);
        List<DiaryEntity> diaryEntityList =diaryRepository.findByCreateDateAndMemberEmail(createDate,myEmail);

        // entity 리스트를 dto 리스트로 변환하는 스트림
        List<DiaryDTO> diaryDtoList = diaryEntityList.stream()
                .map(DiaryEntity::entityToDto)
                .collect(Collectors.toList());

        if(gen.equals("")||currentWeight.equals(""))return null;
        double minCarbohydrate = gen.equals("m") ? 110 : 100;
        double minProtein = Double.parseDouble(currentWeight) * 0.85;
        double minFat = gen.equals("m") ? 45 : 40;

        if (totalCarbohydrate(diaryDtoList) < minCarbohydrate) {
            scarceNutrient.add("탄수화물이 최소권장 섭취량보다 " + String.format("%.1f", minCarbohydrate - totalCarbohydrate(diaryDtoList)) + "g 만큼 부족합니다.");
        }
        if (totalProtein(diaryDtoList) < minProtein) {
            scarceNutrient.add("단백질이 체중별 권장 섭취량보다 " + String.format("%.1f", minProtein - totalProtein(diaryDtoList)) + "g 만큼 부족합니다.");
        }
        if (totalFat(diaryDtoList) < minFat) {
            scarceNutrient.add("지방이 최소권장 섭취량보다 " + String.format("%.1f", minFat - totalFat(diaryDtoList)) + "g 만큼 부족합니다.");
        }

        return scarceNutrient;
    }

    // 오늘 섭취한 탄수화물의 합 계산
    public double totalCarbohydrate(List<DiaryDTO> diaryDtoList) {
        return diaryDtoList.stream()
                .mapToDouble(dto -> parseDoubleOrDefault(dto.getCarbohydrate(), 0.0))
                .sum();
    }


    // 오늘 섭취한 단백질의 합 계산
    public double totalProtein(List<DiaryDTO> diaryDtoList) {
        return diaryDtoList.stream()
                .mapToDouble(dto -> parseDoubleOrDefault(dto.getProtein(), 0.0))
                .sum();
    }

    // 오늘 섭취한 지방의 합 계산
    public double totalFat(List<DiaryDTO> diaryDtoList) {
        return diaryDtoList.stream()
                .mapToDouble(dto -> parseDoubleOrDefault(dto.getFat(), 0.0))
                .sum();
    }
    // 오늘 섭취한 칼로리의 합 계산
    public double totalKcal(List<DiaryDTO> diaryDtoList) {
        return diaryDtoList.stream()
                .mapToDouble(dto -> parseDoubleOrDefault(dto.getKcal(), 0.0))
                .sum();
    }
    // 오늘 섭취한 당류의 합 계산
    public double totalSugars(List<DiaryDTO> diaryDtoList) {
        return diaryDtoList.stream()
                .mapToDouble(dto -> parseDoubleOrDefault(dto.getSugars(), 0.0))
                .sum();
    }

    // 오늘 섭취한 나트륨의 합 계산
    public double totalSalt(List<DiaryDTO> diaryDtoList) {
        return diaryDtoList.stream()
                .mapToDouble(dto -> parseDoubleOrDefault(dto.getSalt(), 0.0))
                .sum();
    }

    // 문자열을 double로 변환하거나 기본값을 반환하는 유틸
    private double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}