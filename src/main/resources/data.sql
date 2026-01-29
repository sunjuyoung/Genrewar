-- 초기 제시어 데이터
-- 로맨스 제시어 (스릴러 플레이어에게 배정)
INSERT INTO keyword (keyword_id, word, target_genre, difficulty) VALUES
    (gen_random_uuid(), '꽃다발', 'ROMANCE', 'EASY'),
    (gen_random_uuid(), '커플링', 'ROMANCE', 'EASY'),
    (gen_random_uuid(), '손잡기', 'ROMANCE', 'EASY'),
    (gen_random_uuid(), '프로포즈', 'ROMANCE', 'NORMAL'),
    (gen_random_uuid(), '웨딩드레스', 'ROMANCE', 'NORMAL'),
    (gen_random_uuid(), '첫키스', 'ROMANCE', 'NORMAL'),
    (gen_random_uuid(), '백허그', 'ROMANCE', 'HARD'),
    (gen_random_uuid(), '운명', 'ROMANCE', 'HARD'),
    (gen_random_uuid(), '고백', 'ROMANCE', 'NORMAL'),
    (gen_random_uuid(), '데이트', 'ROMANCE', 'EASY');

-- 스릴러 제시어 (로맨스 플레이어에게 배정)
INSERT INTO keyword (keyword_id, word, target_genre, difficulty) VALUES
    (gen_random_uuid(), '단검', 'THRILLER', 'EASY'),
    (gen_random_uuid(), '비명', 'THRILLER', 'EASY'),
    (gen_random_uuid(), '피', 'THRILLER', 'EASY'),
    (gen_random_uuid(), '시체', 'THRILLER', 'NORMAL'),
    (gen_random_uuid(), '살인마', 'THRILLER', 'NORMAL'),
    (gen_random_uuid(), '추격', 'THRILLER', 'NORMAL'),
    (gen_random_uuid(), '좀비', 'THRILLER', 'HARD'),
    (gen_random_uuid(), '복수', 'THRILLER', 'HARD'),
    (gen_random_uuid(), '납치', 'THRILLER', 'NORMAL'),
    (gen_random_uuid(), '공포', 'THRILLER', 'EASY');

-- 코미디 제시어
INSERT INTO keyword (keyword_id, word, target_genre, difficulty) VALUES
    (gen_random_uuid(), '바나나껍질', 'COMEDY', 'EASY'),
    (gen_random_uuid(), '방귀', 'COMEDY', 'EASY'),
    (gen_random_uuid(), '엉덩방아', 'COMEDY', 'EASY'),
    (gen_random_uuid(), '개그맨', 'COMEDY', 'NORMAL'),
    (gen_random_uuid(), '콩트', 'COMEDY', 'NORMAL'),
    (gen_random_uuid(), '몰래카메라', 'COMEDY', 'NORMAL'),
    (gen_random_uuid(), '슬랩스틱', 'COMEDY', 'HARD'),
    (gen_random_uuid(), '블랙코미디', 'COMEDY', 'HARD'),
    (gen_random_uuid(), '웃음', 'COMEDY', 'EASY'),
    (gen_random_uuid(), '유머', 'COMEDY', 'NORMAL');

-- SF 제시어
INSERT INTO keyword (keyword_id, word, target_genre, difficulty) VALUES
    (gen_random_uuid(), '로봇', 'SF', 'EASY'),
    (gen_random_uuid(), '우주선', 'SF', 'EASY'),
    (gen_random_uuid(), '외계인', 'SF', 'EASY'),
    (gen_random_uuid(), '타임머신', 'SF', 'NORMAL'),
    (gen_random_uuid(), '워프', 'SF', 'NORMAL'),
    (gen_random_uuid(), '안드로이드', 'SF', 'NORMAL'),
    (gen_random_uuid(), '특이점', 'SF', 'HARD'),
    (gen_random_uuid(), '디스토피아', 'SF', 'HARD'),
    (gen_random_uuid(), '레이저', 'SF', 'EASY'),
    (gen_random_uuid(), '홀로그램', 'SF', 'NORMAL');

-- 판타지 제시어
INSERT INTO keyword (keyword_id, word, target_genre, difficulty) VALUES
    (gen_random_uuid(), '마법', 'FANTASY', 'EASY'),
    (gen_random_uuid(), '용', 'FANTASY', 'EASY'),
    (gen_random_uuid(), '엘프', 'FANTASY', 'EASY'),
    (gen_random_uuid(), '마법사', 'FANTASY', 'NORMAL'),
    (gen_random_uuid(), '던전', 'FANTASY', 'NORMAL'),
    (gen_random_uuid(), '성검', 'FANTASY', 'NORMAL'),
    (gen_random_uuid(), '소환수', 'FANTASY', 'HARD'),
    (gen_random_uuid(), '마왕', 'FANTASY', 'HARD'),
    (gen_random_uuid(), '요정', 'FANTASY', 'EASY'),
    (gen_random_uuid(), '주문', 'FANTASY', 'NORMAL');

-- 미스터리 제시어
INSERT INTO keyword (keyword_id, word, target_genre, difficulty) VALUES
    (gen_random_uuid(), '단서', 'MYSTERY', 'EASY'),
    (gen_random_uuid(), '탐정', 'MYSTERY', 'EASY'),
    (gen_random_uuid(), '암호', 'MYSTERY', 'EASY'),
    (gen_random_uuid(), '알리바이', 'MYSTERY', 'NORMAL'),
    (gen_random_uuid(), '트릭', 'MYSTERY', 'NORMAL'),
    (gen_random_uuid(), '밀실', 'MYSTERY', 'NORMAL'),
    (gen_random_uuid(), '서술트릭', 'MYSTERY', 'HARD'),
    (gen_random_uuid(), '반전', 'MYSTERY', 'HARD'),
    (gen_random_uuid(), '용의자', 'MYSTERY', 'EASY'),
    (gen_random_uuid(), '추리', 'MYSTERY', 'NORMAL');
