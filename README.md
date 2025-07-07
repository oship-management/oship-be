# 🚢 Oship (오쉽)

> 복잡한 해외배송 과정을 간편하게 해결하는 **물류 통합 플랫폼**

---

## 📌 프로젝트 개요

- **기간**: 2025.05.27 ~ 2025.07.07
- **인원**: 5인 개발 (팀 프로젝트)
- Spring Boot와 Vue.js 기반의 해외배송 통합 관리 플랫폼입니다.  
  배송 주문 수집부터 요금 비교, 라벨 생성, 결제, 실시간 추적까지 전 과정을 자동화하여 **셀러의 배송 운영 효율을 극대화**합니다.

---

## 💡 핵심 기능

- 📦 **주문 수집 자동화**  
  다양한 플랫폼의 주문 데이터를 엑셀 업로드로 통합 수집할 수 있도록 구현하였습니다.  
  → 주문 관리 시간을 줄이고, 실무 효율성을 크게 향상시킵니다.

- 💰 **배송 요금 비교 견적**  
  포워딩사별 요율을 자동 비교하여 최적의 배송사를 추천합니다.  
  → 다양한 견적을 한 번에 비교할 수 있는 통합 견적 시스템을 도입했습니다.

- 🧾 **자동 라벨 생성**  
  배송사 API를 연동해 라벨을 자동으로 생성하고 출력합니다.  
  → 수동 생성 오류를 방지하고, 출력 시간도 대폭 단축하였습니다.

- 💳 **배송비 결제 통합**  
  TossPayments 연동을 통해 실시간 카드 결제가 가능하도록 구현했습니다.  
  → Spring Retry와 RestTemplate 기반으로 결제 실패 시 재시도 로직을 적용했습니다.

- 🚚 **배송 상태 추적**  
  배송 현황을 통합 추적 화면에서 실시간으로 확인할 수 있습니다.  
  → 고객 문의 감소와 운영 편의성을 고려한 화면 구성으로 설계했습니다.

- ⚙️ **안정적인 시스템 구성**  
  외부 API 장애에 대응할 수 있도록 재시도 로직과 예외 처리를 설계하고,  
  Docker, Testcontainers, GitHub Actions 기반의 자동화된 테스트 및 배포 환경을 구축했습니다.


---

## 🖥️ 기술 스택

- Frontend : Vue.js, Vite, SCSS

- Backend : Spring Boot, JPA, MySQL, Redis

- Infra : Docker, GitHub Actions, Testcontainers

- API 연동 : TossPayments, FedEx API

- 추적 시스템 : Async Notification System

---

## 🚀 설치 및 실행 방법

```bash
# 1. 프로젝트 클론
git clone https://github.com/your-org/oship.git
cd oship

# 2. 백엔드 설정
cd backend
./gradlew build
docker-compose up -d

# 3. 프론트엔드 실행
cd ../frontend
npm install
npm run dev
```

---
## 📑 문서자료

- [API 명세서](링크추가예정)

- [ERD](https://www.erdcloud.com/d/P59dEbgyLSHW2zCGK)

- [브로셔](https://www.notion.so/teamsparta/14-2162dc3ef514809a980bd4f2317ad7c9?p=21e2dc3ef514811ca0b2ca6b9a5f36ca&pm=s)

---

## 👨‍👩‍👧‍👦 팀원 소개
| 이름           | 역할                 | 주요 업무                    |
|--------------| ------------------ | ------------------------ |
| 🐔 박민혁 (팀장)  | Frontend, Backend   | View 구성, FedEx API 연동    |
| 🐣 김예은 (부팀장) | Backend            | 주문/통계 서비스, 비동기 알림 시스템    |
| 🐣 김국민       | Infra              | CI/CD, 모니터링              |
| 🐣 권하은       | Backend            | Toss 결제 연동, Spring Retry |
| 🐣 서보경       | Backend            | 템플릿 기반 엑셀 업로드, 운송사 요율 비교 |
