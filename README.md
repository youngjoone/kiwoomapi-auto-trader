# 📈 Kiwoom API 자동 주식매매 시스템

---

키움증권 API를 활용하여, 특정 매수/매도 전략에 따라 자동으로 주식 거래를 수행하는 풀스택 애플리케이션입니다. Spring Boot로 구현된 백엔드가 거래 로직과 API 통신을 담당하며, React와 TypeScript로 만들어진 프론트엔드를 통해 시스템 상태와 거래 현황을 실시간으로 모니터링할 수 있습니다.

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.1-green) ![React](https://img.shields.io/badge/React-18-blue) ![TypeScript](https://img.shields.io/badge/TypeScript-5.2.2-blue) ![Gradle](https://img.shields.io/badge/Gradle-8.8-blueviolet)

## ✨ 핵심 기능

- **전략 기반 자동 매매**
  - **자동 매수**: 매일 아침 9시, 전일 상한가를 기록한 종목들을 자동으로 매수합니다. (`DailyUpperLimitBuyStrategy`)
  - **자동 매도**: 매수된 모든 종목에 대해, 설정된 익절(Take-Profit) 및 손절(Stop-Loss) 비율에 도달하면 즉시 시장가로 매도하여 수익 또는 손실을 확정합니다.
- **실시간 모니터링**
  - 키움증권 API와 **WebSocket**으로 실시간 연결하여, 보유 종목의 현재가를 밀리초 단위로 추적합니다.
  - 웹 기반 대시보드를 통해 시스템의 핵심 상태(API 토큰, 웹소켓 연결 상태)를 실시간으로 확인할 수 있습니다.
- **웹 대시보드**
  - 현재 보유 중인 종목 목록과 실시간 수익률(PNL)을 조회할 수 있습니다.
  - 과거에 거래했던 모든 내역(수익/손실 포함)을 조회하고 관리할 수 있습니다.
  - 시스템의 주요 동작과 오류를 확인할 수 있는 로그 페이지를 제공합니다.
- **동적 설정 관리**
  - 총 투자 금액, 익절/손절 비율 등 매매 전략의 주요 파라미터를 API를 통해 동적으로 변경할 수 있습니다.

## ⚙️ 시스템 아키텍처 및 동작 흐름

1.  **애플리케이션 시작**: `ApplicationStartupRunner`가 키움증권 API 서버와 **WebSocket 연결**을 수립합니다.
2.  **스케줄링**: 매일 아침 9시, Spring 스케줄러가 `DailyUpperLimitBuyStrategy`를 실행합니다.
3.  **매수 대상 선정**: 전략은 키움증권 REST API를 통해 전일 상한가 종목 리스트를 요청합니다.
4.  **자동 매수 주문**: `OrderService`가 선정된 종목들에 대해, 설정된 금액만큼 **시장가 매수 주문**을 실행합니다.
5.  **실시간 감시 시작**: 매수 주문이 성공하면, `KiwoomWebSocketClient`는 해당 종목들의 **실시간 시세 구독**을 시작합니다.
6.  **실시간 데이터 수신**: WebSocket을 통해 수신된 실시간 체결 정보를 `OrderService`로 전달합니다.
7.  **자동 매도 판단**: `OrderService`는 수신된 현재가를 기준으로 실시간 수익률을 계산하여, 설정된 익절/손절 라인 도달 여부를 판단합니다.
8.  **자동 매도 주문**: 조건 충족 시, `OrderService`는 해당 종목에 대한 **시장가 매도 주문**을 실행하고 거래를 종료합니다.
9.  **UI 업데이트**: 사용자의 웹 브라우저는 주기적으로 백엔드 REST API를 호출하여, 시스템 상태, 보유 종목, 거래 내역 등 최신 정보를 화면에 표시합니다.

## 🛠️ 기술 스택

### Backend
- **Java 21**
- **Spring Boot 3.3.1**
  - **Spring Web**: RESTful API 제공
  - **Spring Data JPA**: 데이터베이스 연동 (H2)
  - **Spring WebSocket**: 클라이언트(UI)와의 실시간 통신 (STOMP)
  - **Spring Retry**: API 호출 실패 시 재시도 로직 구현
- **Gradle**: 의존성 관리 및 빌드 자동화
- **Lombok**: 보일러플레이트 코드 제거

### Frontend
- **React 18**
- **TypeScript**
- **Vite**: 프론트엔드 빌드 및 개발 서버
- **Tailwind CSS**: UI 스타일링

### Database
- **H2 Database**: 내장형 관계형 데이터베이스 (개발 및 테스트용)

### APIs & Protocols
- **Kiwoom Securities REST API**: 주문 실행, 계좌 정보 조회 등
- **WebSocket**: 실시간 시세 데이터 수신

## 🚀 시작하기

### 사전 요구사항
- **Java 21 (JDK)** 설치
- **Node.js 18+** 및 **npm** 설치

### 1. API 키 및 계좌번호 설정

프로젝트를 실행하기 전, 백엔드의 설정 파일에 키움증권 API 키와 계좌번호를 입력해야 합니다.

- **파일 위치**: `app/src/main/resources/application.properties`
- **수정 항목**:
  - `kiwoom.api.appkey`: 발급받은 App Key
  - `kiwoom.api.secretkey`: 발급받은 Secret Key
  - `kiwoom.account.cano`: 실제 투자에 사용할 계좌번호

### 2. 백엔드 실행

```bash
# 프로젝트 루트 디렉토리로 이동
cd kiwoomapi-auto-trader

# Gradle을 사용하여 애플리케이션 실행
./gradlew bootRun
```

### 3. 프론트엔드 실행

별도의 터미널을 열고 다음을 실행합니다.

```bash
# 프론트엔드 디렉토리로 이동
cd kiwoomapi-auto-trader/frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev
```

이제 웹 브라우저에서 `http://localhost:5173` (또는 터미널에 표시된 주소)으로 접속하여 대시보드를 확인할 수 있습니다.

## 🔮 향후 개선 과제

- **전략 고도화 및 백테스팅**: 다양한 매매 전략을 추가하고, 과거 데이터를 이용한 백테스팅 기능을 구현하여 전략의 유효성을 검증합니다.
- **리스크 관리 강화**: 종목별 최대 투자 비중 설정, 일일 최대 손실 한도 설정 등 리스크 관리 로직을 추가합니다.
- **모니터링 및 알림**: 시스템 오류, 웹소켓 연결 끊김, 주문 실패 등의 이벤트 발생 시 관리자에게 이메일 또는 슬랙으로 알림을 보내는 기능을 구현합니다.
- **Secrets 관리**: API 키 등 민감한 정보를 외부 설정 파일이나 HashiCorp Vault, AWS Secrets Manager와 같은 전문 솔루션을 통해 안전하게 관리합니다.
