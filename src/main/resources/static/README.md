# Co-s House Front UI/UX Guide (Static)

본 문서는 팀원들이 패키지별 화면을 같은 룩앤필로 빠르게 맞추기 위한 사용법/규칙을 정리합니다. 정적 기준이며, 서버 템플릿(Thymeleaf)에도 동일 규칙을 그대로 적용할 수 있습니다.

## 페이지 구성
- 홈: `/images/home.html`
  - 헤더(고정): 좌 브랜드 + 네비게이션(쇼핑/커뮤니티/이벤트=20px)
  - 카테고리 라인: 홈/인기/오늘의 딜(텍스트, 위·아래 보더), 우측 통합검색 + 로그인
  - 배너: 캐러셀(높이 400px)
  - 섹션: 오늘의 기획전 → 오늘의 추천·집들이 → 오늘의 딜
- 패키지별 프리뷰(동일 헤더/풋바/카테고리 적용)
  - 인덱스: `/ui-kit.html` (미리보기 허브)
  - 장바구니: `/images/ui-kit/cart.html`
  - 주문/결제: `/images/ui-kit/order.html`
  - 상품상세: `/images/ui-kit/product.html`
  - 마이페이지: `/images/ui-kit/mypage.html`

## 공통 스타일(토큰/컴포넌트)
- CSS 위치: `static/css/app.css`
- 색상 토큰(통일 팔레트)
  - `--brand-50..900` (primary: `--brand-500 #3c9dfc`, hover: `--brand-600`)
  - `--color-primary`, `--color-primary-600`, `--color-ring`는 brand에 매핑됨
- 주요 유틸/컴포넌트
  - 버튼: `.btn`, `.btn-outline` (inline-flex, 중앙정렬, 링크에도 사용 가능)
  - 인풋: `.input` (focus ring = brand)
  - 표/배지/태그: `.table`, `.badge`, `.tag`
  - 탭/수량: `.tabs`, `.qty`
  - 캐러셀: `.carousel`, `.carousel-track`, `.carousel-dots`
  - 레이아웃: `body.page` + `main.page-main`(sticky footer), `.container`, `.card`, `.grid-2`

## 헤더/카테고리/풋바 스펙
- 헤더(상단 고정)
  - 좌: 브랜드(`.brand`) + 네비게이션(쇼핑/커뮤니티/이벤트, 20px, 굵게)
  - 우: (필요 시) 로그인 버튼 `.btn.btn-outline`
- 카테고리 라인(헤더 하단 2행)
  - 좌: 홈/인기/오늘의 딜(텍스트, 24px/800)
  - 우: 통합검색 입력 + 검색 버튼 + 로그인
  - 위·아래 보더: `1px solid var(--color-border)`
- 풋바(하단 바)
  - 색: `--foot-bg: rgba(204,204,204,0.8)`, 글자색 `--foot-fg: #111827`
  - sticky footer 구조: `body.page` + `main.page-main` 사용

## 라우팅(정적에서 서버로 연결)
- 로그인(서버): `/controller/register/login`
- 회원가입(서버): `/account/register`
- 비밀번호 재설정(서버): `/account/reset`
- 커뮤니티/이벤트/딜 섹션 링크는 홈의 앵커로 연결: `/images/home.html#house-tour`, `#today-plan`, `#deal` 등

## 사용 규칙
1. 색상은 토큰만 사용(직접 hex 하드코딩 금지). 예) 버튼/포커스 = brand 파생 토큰.
2. 헤더/카테고리/풋바 마크업/크기 통일(헤더 링크 20px, 카테고리 24px). 원하는 페이지 강조는 활성 클래스(추가 예정)로 처리.
3. 폰트는 app.css에 정의된 웹폰트 사용(Pretendard 본문, KkuBulLim 포인트). 각 페이지에서 중복 선언 금지.
4. 시안 이미지는 “참고만” 사용. 실제 UI는 HTML/CSS/토큰으로 구현.
5. 로그인/회원가입/리셋 링크는 서버 라우트 사용. (정적 데모용 # 링크 금지)
6. 반응형에서 640px 이하일 때 `.grid-2` → 1열, `product-grid` 2열 기본.

## 빠른 스니펫(헤더/카테고리)
```html
<header class="app-header">
  <div class="container" style="padding:15px 19px;">
    <div class="flex" style="align-items:center; justify-content:space-between; gap:16px;">
      <div class="flex" style="align-items:center; gap:22px;">
        <a class="brand" href="/images/home.html">
          <img class="logo" src="/images/logo.png" alt="로고" />
          <span class="name">코딩의 집</span>
        </a>
        <nav style="display:flex; gap:22px; font-weight:800; font-size:20px;">
          <a href="/images/home.html">쇼핑</a>
          <a href="/images/home.html#house-tour">커뮤니티</a>
          <a href="/images/home.html#today-plan">이벤트</a>
        </nav>
      </div>
    </div>
    <div class="flex" style="align-items:center; justify-content:space-between; gap:16px; margin-top:10px; border-top:1px solid var(--color-border); border-bottom:1px solid var(--color-border); padding:8px 0;">
      <div style="display:flex; gap:18px; font-size:24px; font-weight:800;">
        <a href="/images/home.html">홈</a>
        <a href="/images/home.html#popular">인기</a>
        <a href="/images/home.html#deal">오늘의 딜</a>
      </div>
      <div style="min-width:380px; display:flex; gap:8px; align-items:center;">
        <input class="input" placeholder="통합검색" style="height:40px; flex:1;" />
        <button class="btn" type="button">검색</button>
        <a class="btn btn-outline" href="/controller/register/login" style="height:40px;">로그인</a>
      </div>
    </div>
  </div>
</header>
```

## 팀 합의 포인트(추천)
- 활성 탭 강조: `.is-active` 클래스 스타일 정의하여 현재 위치(헤더/카테고리) 표시
- 컴포넌트 사용 우선순위: `.btn/.input/.card/.table` 등 공용 먼저 적용 → 페이지 특화 스타일 최소화
- 반응형 상세 기준: 헤더 2행에서 검색/로그인 줄바꿈 정책 합의(아이콘화 여부)
- 접근성: 버튼/링크 컨트라스트 준수, 포커스 링 유지(brand 컬러)

## 유지관리
- 새로운 정적 페이지 추가 시 위 “헤더/카테고리/풋바 스니펫”으로 시작
- 색/간격/폰트는 오직 `app.css`에서 관리(페이지 내 인라인 변경 지양)
- 서버 템플릿에서도 동일 구조를 사용할 경우, `fragments/layout.html`의 풋바를 선택적으로 포함 가능

---
문의/수정 제안은 PR 또는 코멘트로 남겨 주세요. 팀이 합의한 수치(폰트/여백/색)가 바뀌면 이 문서와 `app.css`만 업데이트하면 됩니다.
