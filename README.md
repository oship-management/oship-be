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

#### 1. 프로젝트 클론
```bash
git clone https://github.com/your-org/oship.git
cd oship
```

#### 2. 백엔드 설정

```bash
cd backend
./gradlew build
docker-compose up -d
```

#### 3. 프론트엔드
- [🛒셀러 페이지 바로가기](https://github.com/oship-management/oship-fe-partner)
- [🤝파트너 페이지 바로가기](https://github.com/oship-management/oship-fe-partner)

---

## 📐 도메인 아키텍쳐
```bash
┌────────────┐       ┌────────────┐       ┌────────────┐
│   Order    │─────▶│  Payment   │─────▶│  Shipping  │
└────┬───────┘       └────┬───────┘       └────┬───────┘
     │                    │                    │
     ▼                    ▼                    ▼
┌────────────┐       ┌────────────┐       ┌─────────────┐
│   Excel    │       │ TossClient │       │ FedexClient │
└────────────┘       └────────────┘       └─────────────┘
```

- **Order**: 주문 등록 및 통계 조회 도메인 (엑셀 기반 대량 등록 포함)
- **Payment**: 결제 생성 및 Toss 연동, 결제 상태 관리
- **Shipping**: 배송 정보 관리, 송장번호 등록, 배송 추적 및 라벨 생성 기능
- **Excel**: 주문 엑셀 업로드 시 유효성 검사 및 파싱 처리
- **TossClient / FedexClient**: 외부 결제 및 배송 API와 통신하는 클라이언트 레이어

---

## 🗂️ 패키지 구조
Oship 프로젝트는 **도메인 중심 설계**(DDD 기반)의 레이어드 아키텍처를 따르며,  
핵심 로직은 domain 계층에, 외부 API 연동은 client 계층에, 전역 설정 및 공통 처리 로직은 global에 위치합니다.

```bash
org.example.oshipserver
├── client/                 # 외부 시스템(API) 연동
│   ├── fedex/              # FedEx 배송사 API 클라이언트
│   └── toss/               # TossPayments 결제 API 클라이언트
│
├── domain/                # 핵심 도메인 계층
│   ├── admin/              # 관리자 전용 기능
│   ├── auth/               # 로그인, 인증/인가 처리
│   ├── carrier/            # 배송사 관련 정보 및 설정
│   ├── log/                # 요청/응답 로그 추적
│   ├── notification/       # 비동기 알림 (이메일 등)
│   ├── order/              # 주문 등록, 조회, 통계 처리
│   ├── partner/            # 파트너사 관련 정보
│   ├── payment/            # 결제 처리 및 Toss 연동
│   ├── seller/             # 셀러 정보 및 배송 요율 설정
│   ├── shipping/           # 배송 라벨, 송장번호, 추적 등
│   └── user/               # 사용자 프로필, 권한, 설정 관리
│
├── global/                # 전역 설정 및 공통 유틸
│   ├── common/             # 공용 상수, 유틸 클래스
│   ├── config/             # Spring 설정 (Security, CORS 등)
│   ├── entity/             # 공통 엔티티 상속 구조 (BaseTime 등)
│   └── exception/          # 전역 에러 핸들링 구조
│
└── OshipServerApplication.java   # SpringBoot 메인 실행 클래스
```

---
## 📑 문서자료

- [API 명세서](https://documenter.getpostman.com/view/31276367/2sB34cpNFj)

- [ERD](https://www.erdcloud.com/d/P59dEbgyLSHW2zCGK)

- [브로셔](https://www.notion.so/teamsparta/14-2162dc3ef514809a980bd4f2317ad7c9?p=21e2dc3ef514811ca0b2ca6b9a5f36ca&pm=s)

---

## 👨‍👩‍👧‍👦 팀원 소개
| 이름           | 역할                 | 주요 업무                                            |
|--------------| ------------------ |--------------------------------------------------|
| 🐔 박민혁 (팀장)  | Frontend, Backend   | View 구성, Shipping Tracking Service, FedEx API 연동 |
| 🐣 김예은 (부팀장) | Backend            | Order Service, 비동기 알림 시스템, 월별 판매자 통계 캐싱          |
| 🐣 김국민       | Infra              | CI/CD, Infra, Monitoring                         |
| 🐣 권하은       | Backend            | Payment Service, Toss 결제 연동, Spring Retry        |
| 🐣 서보경       | Backend            | 템플릿 기반 엑셀 업로드, 운송사 요율 비교                         |


