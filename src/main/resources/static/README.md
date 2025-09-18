코딩의 집 웹 애플리케이션 (Spring Boot + Thymeleaf)

개요
- 로컬 진입점: http://localhost:8080/
- 헤더에는 쇼핑/커뮤니티/이벤트 링크와 로그인/회원가입(로그인 시 마이페이지/로그아웃)이 표시됩니다.
- 모든 이미지 영역은 연한 하늘색 그라데이션 플레이스홀더(.ph)로 대체합니다.

홈(/)
- 히어로: 검색창 + 바로가기(홈/인기/오늘의 딜)
- 오늘의 딜: 4열 × 2행(8개), “더보기” → `/deal/today`
- 인기 급상승: 4열 × 2행(8개), “더보기” → `/shop`
- 집들이: 4열 × 1행(4개), “더보기” → `/posts`
- 이벤트 배너: 밝은 블루 그라데이션 배너

페이지
- 쇼핑: `/shop` (오늘의 딜/인기 목록 4열 카드)
- 커뮤니티: `/community` (인기 집들이 4열 카드)
- 이벤트: `/events` (이벤트 4열 카드)
- 로그인: `/controller/register/login`
- 회원가입: `/account/register` (템플릿 `templates/register/register.html`)

데이터 연동
- ProductRepository, PostRepository에서 DTO 프로젝션으로 조회
- HomeService가 todayDeals/popularProducts/topPosts 제공
- 각 페이지 컨트롤러에서 모델에 바인딩하여 렌더링

스타일 구조(페이지별 로드)
- 전역: `static/css/app.css` (폰트/토큰/헤더/푸터/버튼/컨테이너)
- 공용 섹션: `static/css/sections.css` (섹션/그리드/카드/배너/플레이스홀더)
- 홈 전용: `static/css/home.css` (히어로/바로가기/비주얼)
- 로딩 규칙: 홈은 `app.css + sections.css + home.css`, 그 외 페이지는 `app.css + sections.css`

보안/접근
- 공개 GET: `/`, `/shop`, `/community`, `/events`, `/controller/register/login`, `/account/register`
- 회원가입: `POST /controller/register/register` (Content-Type: application/json, 간단 레이트리밋 적용)

개발 실행
1) 실행: `./gradlew bootRun`
2) 접속: `http://localhost:8080/`
3) 변경: Devtools로 정적 리소스/템플릿 자동 리로드

가이드
- 공용 UI는 `sections.css`, 페이지 특화는 전용 CSS에 추가
- 신규 페이지는 컨트롤러/템플릿 생성 후 필요한 경우 전용 CSS 추가, 공용 컴포넌트 우선 재사용
- 이미지 리소스는 사용하지 않으며, 플레이스홀더(.ph)를 유지합니다.
