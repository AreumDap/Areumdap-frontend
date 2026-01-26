# Areumdap-FrontEnd

2025.12.22 ~ 2026.2.20  UMC 9th 프로젝트 아름답

<br>
## 📌 Git Convention

### (1) Branch type
- 기본 브랜치
  - main
    - 배포 및 최종 결과물 브랜치
    - 직접 커밋 ❌
  - dev
    - 개발 통합 브랜치
    - 기능 개발 완료 후 PR을 통해 병합
- 작업 브랜치
  - 새로운 기능 구현
    ```
    feat/#이슈번호/기능명
    ```
  - 버그 및 오류 해결
    ```
    fix/#이슈번호/버그명
    ```
  - UI 작업
    ```
    ui/#이슈번호/기능명
    ```
  - README나 WIKI 등의 문서 개정
    ```
    docs/#이슈번호/기능명
    ```
  - 리팩토링 (기능 변화 없음)
    ```
    refactor/#이슈번호/기능명
    ```

### (2) COMMIT 메시지
- 형식
    ```
    type: 커밋메시지 (#이슈번호)
    ```
- 규칙
    - `:` 앞 공백 없음, 뒤 공백 1칸 필수
    - 커밋 메시지는 한글 통일 및 공사형 권장
    ```
    // 예시
    feat: 로그인 UI 구현 (#12)
    fix: 로그인 오류 수정 (#21)
    docs: 협업 컨벤션 문서 추가 (#3)
    ```

### (3) Pull Request
- PR 생성 규칙
  - dev 브랜치로만 PR 생성
  - PR 제목은 커밋 메시지 규칙과 동일하게 작성
- PR 내용
  - 작업 내용 요약
  - 관련 이슈 번호 명시
  - UI 작성 시 스크린샷 첨부
- 병합 규칙
  - 충돌 발생 시 PR 생성자가 해결
  - 최소 1명 리뷰 후 병합
 

### (4) Issue 관리
- 모든 작업은 Issue 생성 후 진행
- Issue 제목은 명확하게 작성
  ```
  [feat] 로그인 화면 구현
  [fix] 캘린더 스크롤 오류
  ```
- 작업 브랜치는 해당 ISSUE 번호 기반으로 생성

<br>
<br>

## 🧩Coding Convention

### (1) 주석
- 주석은 WHY 중심으로 작성
- 불필요한 코드 설명 주석은 제거
- 복잡한 로직에는 역할 단위 주석 작성
  ```
  // 친구 요청 API 호출 (서버 스펙상 POST 필요)
  requestFriend()
  ```
- 코드 설명용 주석 ❌
  ```
  // i에 1을 더한다 ❌ 
  i++;
  ```

### (1) 네이밍
- 변수/함수
  - camelCase 사용
    ``` 
    userName
    getUserInfo()
    loadFriendList()
    ```
- 클래스/컴포넌트
  - PascalCase 사용
    ```
    LoginActivity
    HomeFragment
    UserService
    ```
- 상수
  - UPPER_SNAKE_CASE 사용
    ```
    static final int MAX_COUNT = 10;
    ```
- 의미 없는 이름 사용 금지
    ```
    ❌ a, b, temp, data
    ⭕ userList, friendCount
    ```
 
### (3) 파일 & 폴더 구조
- 파일명은 역할이 명확하게 드러나도록 작성
  ```
  LoginActivity
  LoginViewModel
  LoginRepository
  ```
- 기능 단위로 패키지 구성
  ```
  login/
  friend/
  calendar/
  ```

### (4) 로그
- 디버그용 로그는 커밋 전 제거
- 필요한 로그는 Tag를 기능 단위로 통일하고, 에러 로그는 예외 포함
  ```
  Log.e("LOGIN", "login failed", e)
  ```

### (5) 리팩토링 및 코드 최적화
- 중복 로직은 함수/유틸로 분리
- UI 로직과 데이터 처리 로직을 분리
- 불필요한 중첩 조건문/반복 호출 최소화
- "한 함수는 하나의 책임"을 우선

### (6)코드 리뷰 기준
- 네이밍이 직관적인가
- 중복 코드가 없는가
- 불필요한 주석/로그가 없는가
- 컨벤션이 지켜졌는가
- 리팩토링/최적화 여지가 있는가 (중복, 과도한 분기 등) 
