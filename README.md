# Calrorie_Diary_refactoring
### 해당 업로드된 프로젝트는 2023년 개발 된 Calorie_Diary 프로젝트의 리팩토링 버전입니다.

## 프로젝트 배경 및 목적
- 코로나 19 이후 사람들의 불균형한 영양 섭취 문제를 해결한다.
- 사용자 맞춤 형 식단 추천, 칼로리 및 영양관리를 제공한다.
- 다양한 기술을 활용하여 식단을 기록하도록 한다.

- ### 개발 상세 및 기능은 칼로리 다이어리.pdf를 참고

## 테이블 쿼리

**음식 추천 테이블**

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('1','시리얼3컵','372','81','6.9','3.3','22','660','아침');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('2','미숫가루(l00g)','403','71','16.6','5.8','0','35','아침');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('3','흰쌀밥한공기+계란후라이2개','478','76','17.5','12.6','0.9','481','아침');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('4','식빵3조각+우유200ml','323','48','11.7','9.2','13.24','610','아침');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('5','현미밥포케','403','65.3','12','7','7.5','278','아침');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('5','현미밥포케','403','65.3','12','7','7.5','278','점심');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('6','잡곡밥한공기+떡갈비+김치','757','78.3','41.8','31','1.2','621','점심');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('7','신라면+공기밥+김치+계란','874','154','15.5','21.6','4.6','1865','점심');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('8','김치볶음밥+계란후라이2개+아이스티400ml','794','114.8','20.75','25.4','40.2','1333','점심');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('9','햄치즈샌드위치+초코우유200ml+요플레200g','767','88','33','24','45','981','점심');


INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('10','닭칼국수+보쌈+무말랭이','1134','107','41','60','2.8','1475','점심');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('11','부대찌개(1인분-400g)+공깃밥','936','108','33','40','3','2913','저녁');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('12','제육볶음(1인분-300g)+공깃밥','882','198','45','34','21','832','저녁');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('13','순살치킨(1인분-300g)+제로콜라','840','35','49','54','0','470','저녁');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('14','쭈꾸미볶음(1인분-300g)+공깃밥','888','97','69','24','7','2204','저녁');

INSERT INTO food_commend (id,food_name,kcal,carbohydrate,protein,fat,sugars,salt,meal)
VALUE('15','수비드 닭가슴살 샐러드+공깃밥','680','81','48','17','0','340','저녁');
