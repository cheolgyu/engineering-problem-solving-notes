# Stockbot | 수집·배치·API·WASM 웹을 연결한 주가 분석 도구

“특정 시기나 가격 흐름이 여러 해에 반복될까?”를 기억에 남는 차트 몇 장 대신 같은 데이터 기준으로 비교하고 싶었습니다. 국내 종목의 일별 시세를 수집하고 가격·거래량·방향 구간을 계산해 웹에서 조회하는 개인 리서치 도구입니다.

**흐름: 외부 시세 → 응답 파일 → 파싱·PostgreSQL → 변화율·bound·기간 집계 → Actix API → Yew/Rust WASM 웹.** 2022-2025년 직접 구축한 구조와 2026년 AI 에이전트 보조 Axum·React 전환은 구분합니다.

## 1. 처리 단계와 실행 조건이 서로 달라지지 않게

작업을 한 함수에 모으면 실패 위치와 변경 범위를 찾기 어려웠습니다. Spring Batch를 참고해 [Batch·Job 실행 인터페이스](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/batch/src/utils/batch/batch.rs#L1-L60)와 [Step의 read/process/write 절차](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/batch/src/utils/batch/step.rs#L1-L50)를 나눴습니다. 수집·배치·스케줄러·API·웹을 9개 workspace crate로 구성하고, 공유 모델·클라이언트·유틸리티를 추출했습니다.

배치 종류·국가·실행 상태는 [같은 BatchParams를 JSON·utoipa 스키마·CLI 인자로 연결](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/batch_model/src/app_type/batch_run_type.rs#L1-L44)합니다. [핸들러의 OpenAPI·Swagger UI 등록](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/site-api/src/main.rs#L55-L125)도 코드에서 확인할 수 있습니다. 서버 DB 기능과 WASM 웹 의존성은 Cargo feature로 분리했습니다.

## 2. tx/rx 채널로 생산과 후속 처리를 겹치기

한 종목의 파싱·저장을 끝낼 때까지 다음 수집도 기다리는 구조를 나눴습니다. [가격 수집의 송신 명령과 여러 소비자](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/batch/src/job/input_data/setp_price_naver.rs#L86-L191)는 파일·종목 명령을 용량 16 채널에 넘기고, 수신 작업자가 파싱·DB 저장을 맡습니다. 수집 HTTP 요청 자체는 종목별 순차 await가 남아 있습니다.

bound는 [용량 1 채널·직접 send().await·복제 수신기](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/batch/src/job/bound/step_bound_upsert.rs#L34-L106)로 계산할 가격 묶음을 전달합니다. 큐가 차면 생산자가 기다리고 [소비자가 MakeBound 계산 후 upsert](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/batch/src/job/bound/step_bound_upsert.rs#L114-L154)합니다. Tokio 작업의 JoinHandle을 모아 소비자 종료를 기다립니다. bounded 채널을 쓴 사실과 전체 작업·메모리 상한을 보장하는 것은 다르며, 가격 수집의 별도 송신 작업까지 전역 역압으로 묶지는 않습니다.

## 3. 재수집과 분석 구간을 다른 문제로 다루기

종목마다 마지막 저장일을 읽고 종목·날짜 키로 upsert해 중복 수집을 다뤘습니다. 날짜 기준 [연도별 RANGE 파티션 DDL](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/batch/init/init-1.sql#L88-L125)과 집계도 구성했습니다. 원래 DDL의 연말 exclusive 경계는 보완 과제로 남겨 두었습니다.

bound는 같은 방향이 이어지는 가격 구간입니다. 예를 들어 100 → 110 → 120 → 115는 상승·하락 구간으로 나눕니다. 길이·변화율과 [두 끝점의 y=mx+b](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/batch/src/job/bound/ymxb.rs#L20-L69)를 비교용 특성으로 저장합니다. 증분 처리의 기존 마지막 구간 병합은 후속 과제입니다. 분석 값과 투자 수익 검증은 구분합니다.

## 4. 공통 ARM64 기반을 서비스별로 확장하기

교차 컴파일러·타깃·linker·sccache를 [공통 ARM64 Rust 빌드 이미지](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/aws/base-aarch64.dockerfile#L1-L18)에 구성하고 [API 전용 선행 빌드층](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/aws/base-aarch64-site-api.dockerfile#L1-L17) 등 세 서비스별 기반으로 확장했습니다. [최종 실행 이미지는 컴파일 환경과 분리](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/site-api/dockerfile#L1-L55)했습니다. 웹은 Trunk·WASM 도구로 정적 dist를 만듭니다.

Actions는 API·배치·스케줄러·웹의 네 경로입니다. [API 워크플로의 ARM64 → ECR → S3 → CodeDeploy](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/.github/workflows/site-api.yml#L1-L86)처럼 산출물별 목적지를 나눴습니다. 배치는 ECR 이미지, 웹은 S3 정적 파일로 배포합니다. 네 워크플로의 과거 성공 실행이 남아 있습니다.

## 5. 배치 부하와 상시 API를 분리하기

같은 서버의 배치가 CPU를 오래 점유한 기록을 계기로 DB / API+스케줄러 / 배치 전용의 EC2 3역할을 두었습니다. [작업 조건·계획 ID를 user-data로 전달](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/site-api/script/ec2-start.sh#L9-L54)하고 [기존 전용 EC2 설정 변경·시작](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/site-api/script/ec2-start.sh#L59-L72) → 부팅 후 배치 → 완료 API → [인스턴스 정지](https://github.com/cheolgyu/stockbot-rust/blob/18c1f8ea387f265b0b4072343067bf1e7a7cdd45/site-api/script/ec2-stop.sh#L2-L9)를 연결했습니다. 당시 S3는 웹·캐시·배포 묶음 3역할, CodeDeploy는 1앱·2그룹으로 구성했습니다. 수량은 당시 문서·스크립트 기준이며 현재 자원 실사 결과는 아닙니다.

Go의 MongoDB bulk upsert와 Java/Spring Batch의 Tasklet·청크 처리도 별도 버전에서 검토했습니다. 반복되는 표 헤더·열 순서는 [tb_row trait·derive crate](https://crates.io/crates/tb_row)로 추출했습니다. [Actix 공식 examples 기여](https://github.com/actix/examples/pull/250)도 남아 있습니다.

## 원본 코드와 검증 범위

[Stockbot 원본 저장소](https://github.com/cheolgyu/stockbot-rust)를 공개했습니다. 위 링크는 main 18c1f8e의 특정 줄로 고정되어 있습니다. 2026년 독립 예제·시험은 [후속 사례집](https://github.com/cheolgyu/stockbot-engineering-case-study)에 있으며, 원래 전체 배치에 적용된 수정과 구분합니다.
