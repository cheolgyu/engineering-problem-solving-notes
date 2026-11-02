# MungLog | 집에서 소리를 감지하고 밖에서 확인하는 앱

집에 남는 Android 폰을 반려견 소리 알림기로 쓰려고 만들었습니다. 집에 두는 폰이 감지·원본 보관을 맡고, 밖에서 보는 폰과 React PWA가 알림·기록·요청 녹음을 확인합니다. Android·Rust·API·DB·웹을 연결해 출시·운영하며 구현·리팩터링·시험에 AI 에이전트를 활용했습니다.

**마이크 → Rust FFT 후보 → YAMNet/TFLite → 폰 원본·SQLite → 재전송 → Rust API·PostgreSQL → outbox·FCM/Web Push. 요청 녹음만 R2로 단기 전달합니다.**

## 1. 감지 모델과 집별 개인화를 분리

문·의자·게임 소리도 순간적으로 큽니다. Rust가 16kHz PCM의 FFT·음량·주변 소음으로 후보를 좁히고 Android YAMNet이 판정하도록 나눴습니다. 폰 자체의 재생음이 다시 후보가 되는 문제는 재생 억제 gate로 다뤘습니다.

개인화는 고정 YAMNet의 521개 점수 특징 위 작은 분류기를 학습합니다. 같은 녹음의 잘린 구간·증강본이 학습과 평가에 섞이면 좋은 점수처럼 보일 수 있어 [원본 recordingGroup 전체를 나누는 split과 검증](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/munglog/source-split.kt#L8-L45)을 구성했습니다. 별도 반영 비교·명시적 적용·이전 모델 복원을 나눴습니다. 학습 기능과 새 개인 모델의 현장 감지 성능은 각각 검증합니다.

## 2. 재전송과 알림이 다른 성공을 뜻하지 않게

서버는 원본 저장소가 아니라 최근 24시간 메타데이터 중계입니다. R2에는 요청한 한 녹음만 짧게 전달합니다. [이벤트 처리와 같은 트랜잭션에 알림 작업을 기록](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/munglog/event-enqueue.rs#L8-L23)하고, [호출 주체·응답 타입·request ID별 DB 잠금과 응답 재사용](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/munglog/idempotency.rs#L8-L53)으로 중복 요청을 다룹니다.

[현재 멤버십·삭제 여부를 확인하며 SKIP LOCKED로 outbox 점유](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/munglog/outbox-claim.rs#L8-L62)하는 worker가 발송을 맡습니다. 저장 성공·전송 접수·OS 알림 표시는 다른 단계입니다. 삭제 표식은 늦은 재전송이 기록을 되살리거나 새 녹음 요청을 만들지 못하게 합니다.

## 3. 오래된 응답이 새 계정·평가·만료를 되돌리지 않게

순수 Kotlin core-viewer와 HTTP/Protobuf data-viewer를 나눴습니다. 로그인·역할·가정 변경 때 이전 작업을 취소하고 완료 시점에도 현재 scope를 검사합니다. [401 때 한 번만 토큰을 갱신하며 같은 요청 bytes를 재사용](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/munglog/auth-refresh.kt#L8-L53)합니다. 재시도마다 새 변경 요청을 만드는 대신 처음 request ID를 유지합니다.

목록의 늦은 페이지가 이미 저장한 O/X를 덮거나 만료 기한을 늘릴 수 있습니다. [revision이 오래된 항목을 건너뛰고 최초 만료 상한을 보존하는 merge](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/munglog/record-replica.kt#L8-L53)를 구현했습니다. 서버 조회와 Android·HTTP 없는 병합 정책을 나눠 fake API로 경계를 시험할 수 있게 했습니다.

## 4. 원본과 파생 검색·운영 역할을 분리

Rust 소리 기억은 USearch·petgraph·rusqlite로 유사 기록과 앞부분 관계를 찾는 파생 캐시입니다. 모델·소유자·원본·보존 범위가 달라지면 재사용하지 않습니다. 검색 거리나 관계는 사용자의 정답·인과관계를 새로 만들지 않습니다.

GCP VM·Docker Compose·Cloudflare Tunnel/Pages·R2로 구성했습니다. 운영/DEV 환경 계약, migration 소유자와 API DML 계정 분리, 새 환경 health 실패 시 환경 복원을 적용했습니다. 앱 로그인과 가정 멤버십·OWNER 권한도 별도로 확인합니다.

## 출시와 근거 범위

[Google Play 제품 소개](https://play.google.com/store/apps/details?id=com.kullstudio.munglog). 2026.10.30 출시 기록에는 Play 게시와 기존 녹음의 두 폰 소리 맥락 경로 검증이 있습니다. 개인화·모델 반영의 현장 성능과 장시간 감지·발열은 별도 평가 대상입니다. 개발 중인 카메라는 출시 성과에 포함하지 않았습니다.

위 링크는 **비공개 원본 d3c795b1에서 발췌한 실제 코드**이며 [원본 경로·줄·해시](../evidence/manifest.json)를 함께 제공합니다. 개인 녹음·원본 영상·계정·운영 자격증명은 공개하지 않습니다.
