# 개인 개발 프로젝트 연대기

2021년부터 GitHub에 남은 개인 개발을 시작 시점 순으로 정리했습니다. 이어서 개발한 프로젝트에는 해당 시기를 함께 적었습니다. [프로젝트별 설계와 코드](#구현-바로-보기)

| 시기 | 프로젝트 · 만든 것 | 개발 기술 | 빌드·검증·배포 | 코드·문서·영상 |
| --- | --- | --- | --- | --- |
| 2020–2021 | **Gamebot** · 게임 화면에서 버튼을 탐지하고 반복 조작하는 Android 앱. 출시 후 종료 | 라벨링, Darknet YOLO, TFLite, 화면 캡처·접근성 입력 | Gradle 공통 엔진과 게임별 패키지·모델 분리 | [코드](https://github.com/cheolgyu/gamebot-common-background) · [설계](docs/gamebot.md) · [YouTube](https://www.youtube.com/watch?v=7n4ax93YGdg) |
| 2021–2022 | **초기 주식 데이터 도구** · 시세 수집, 기간별 통계, 웹 조회 | Go, PostgreSQL, Vue | Docker Compose 구성과 배포 기록 | [시세 수집](https://github.com/cheolgyu/sbp-inp-data) · [배치 실행](https://github.com/cheolgyu/sb-exe) · [가격 통계](https://github.com/cheolgyu/sbp-stat-price) · [거래량 통계](https://github.com/cheolgyu/sbp-stat-volume) · [조회 API](https://github.com/cheolgyu/sf-pub-api) · [Vue 웹](https://github.com/cheolgyu/sf-pub-site) |
| 2022 시작 · 2023 집중 · 2025 재개 | **[Stockbot Rust](https://github.com/cheolgyu/stockbot-rust-portfolio)** · 최종 버전. 수집부터 배치, 시계열 분석, 운영 조회까지<br>개발 이력 · [Go 버전](https://github.com/cheolgyu/stockbot-go)(2022), Spring Batch 별도 구현(2024) | Rust/Tokio, tx/rx, PostgreSQL 파티션·윈도 함수, Actix/utoipa, Yew/WASM | 공통 ARM64 Docker 빌드, GitHub Actions 4개, ECR·S3·CodeDeploy | [Rust 공개 코드 사본](https://github.com/cheolgyu/stockbot-rust-portfolio) · [설계](docs/stockbot.md) · [CI/CD](https://github.com/cheolgyu/stockbot-rust-portfolio/blob/main/docs/CI_CD.md) |
| 2023 | **tb_row** · 표 데이터를 지정한 열 순서로 직렬화하는 Rust 라이브러리 | Rust, Serde, derive 매크로 | crates.io 배포 | [코드](https://github.com/cheolgyu/tb_row) · [패키지](https://crates.io/crates/tb_row) |
| 2024 시작 | **웹·인증 프로젝트** · 건설사 사이트(dscity·dscwall), 개인 사이트, 인증 예제 | TypeScript, Java | — | |
| 2025–2026 | **ColaZZang** · 가족끼리 반려견 산책·배변 위치를 공유하는 앱 | Android, Wear OS, GPS, Room | 폰과 워치의 기록·마커 동기화 | [설계](docs/colazzang-kullchip.md) |
| 2026.03 | **KullChip** · 워치 센서로 반려견 행동을 분석한 실험 | 센서 시각 정렬, Room, ML0·ML1·ML2, Rust/JNI | 이전 서버의 공통 Docker 빌드·Protobuf CI 검사 | [설계·코드 발췌](docs/colazzang-kullchip.md) |
| 2026.06부터 | **BlinkRoll·BlinkPage** · 눈동작으로 스크롤·페이지 넘김 | Android, MediaPipe/TFLite, 공통 눈 입력 엔진 | Android 출시 빌드의 서명·ABI·AAB 확인 | [설계·코드 발췌](docs/blink.md) · [제품](https://kullstudio.com/) |
| 2026.07부터 | **MungLog** · 집의 폰이 반려견 소리를 감지해 밖에서 확인 | Rust FFT, YAMNet/TFLite, Rust API, PostgreSQL, React PWA | GCP·Docker Compose·Cloudflare, Google Play | [설계·코드 발췌](docs/munglog.md) · [Google Play](https://play.google.com/store/apps/details?id=com.kullstudio.munglog) |
| 2026.08부터 | **BlinkGame** · 시선으로 좌우 이동하는 게임 | Unity/C#, Android–Unity 브리지, 공통 눈 입력 엔진 | Android 릴리스 산출물 검증 | [설계·코드 발췌](docs/blink.md) · [Google Play](https://play.google.com/store/apps/details?id=com.kullstudio.blinkgame) |
| 2026.08 | **KullMotion · KullMenu** · 반려견 움직임 재생과 개체별 학습, 냉장고 재료 기반 레시피 탐색 | Android, Wear OS, 온디바이스 ML | — | |
| 2026.09부터 | **KullTrail** · 실제 반려견 산책을 탐험과 기록으로 연결하는 게임 | Unity, Android, GPS 경로 연동 | 개발 중 | |
| 2026.11 | **KullSound SDK · KullFeedback · KullPixify** · 소리 감지·개인화, 앱 피드백, 픽셀 펫 기능을 각각 SDK로 개발 | Android, 온디바이스 오디오, 피드백 API | SDK 개발·검증 | |

## 구현 바로 보기

[Stockbot의 tx/rx 생산자·소비자](https://github.com/cheolgyu/stockbot-rust-portfolio/blob/27940257af4ed50fcdd3e262fbee2d316c07a06e/batch/src/job/bound/step_bound_upsert.rs#L34-L106) · [Gamebot의 IoU NMS](https://github.com/cheolgyu/gamebot-common-background/blob/3338a8495375b693bcb6d94a711737d729aeccad/src/main/java/com/highserpot/tf/tflite/YoloV4Classifier.kt#L54-L91) · [MungLog의 녹음 단위 학습·평가 분리](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/munglog/source-split.kt#L8-L45) · [Blink의 이벤트 확인 대기열](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/blink/event-buffer.kt#L8-L59)

| 프로젝트 | 어떤 제품인가 | 설계·구현 읽기 |
| --- | --- | --- |
| [Stockbot Rust](https://github.com/cheolgyu/stockbot-rust-portfolio) | 주가 수집·분석·조회 | [설계 기록](docs/stockbot.md) · [PDF](pdf/stockbot.pdf) |
| Gamebot | 화면 객체 탐지와 Android 자동 조작, 출시 후 종료 | [웹](docs/gamebot.md) · [PDF](pdf/gamebot.pdf) |
| ColaZZang → KullChip | 가족 산책 기록에서 워치 센서·ML 분석으로 확장 | [웹](docs/colazzang-kullchip.md) · [PDF](pdf/colazzang-kullchip.pdf) |
| MungLog | 집의 폰에서 반려견 소리를 감지하고 밖에서 확인 | [웹](docs/munglog.md) · [PDF](pdf/munglog.pdf) |
| Blink | 눈동작 스크롤·페이지·시선 좌우 이동 앱 3종 | [웹](docs/blink.md) · [PDF](pdf/blink.pdf) |

## 링크가 가리키는 범위

Stockbot은 운영 식별값을 바꾼 공개 검토 사본, Gamebot은 공개 원본의 commit/line 링크입니다. Stockbot 원본·개발 이력은 이력서의 연락처로 이메일을 알려 주시면 초대합니다. 나머지는 비공개 원본에서 설명에 필요한 부분만 공개한 **실제 코드 발췌**이며, 재작성한 독립 예제가 아닙니다. [선별 코드 목록](evidence/README.md)과 [원본 커밋·줄·해시](evidence/manifest.json)를 함께 제공합니다. 생략된 의존성이 있어 발췌만으로 앱 전체를 실행할 수는 없습니다.

2026.11.02 코드 대조와 공개 편집을 반영했습니다. 구현·과거 시험·출시·미완료 연구를 나눠 적었습니다. 개인 프로젝트 기간은 고용 경력과 별도입니다.

<a id="ai-workflow"></a>

## AI 에이전트 활용과 직접 맡은 일

Gamebot은 2020-2021년 AI 코딩 에이전트 없이 데이터 준비부터 Darknet 학습·변환·Android 적용과 출시를 혼자 진행했습니다. Stockbot의 원래 Rust 수집·API·AWS·Actions도 직접 구축했습니다. 2026년 Stockbot 후속 전환, KullChip·MungLog·Blink의 최근 구현·리팩터링·시험에는 AI 에이전트를 활용했습니다.

문제와 작업 범위·현장 데이터·설계 경계·평가 기준은 직접 정하고 결과의 채택·중단을 판단했습니다. 코드 리뷰·회귀 시험·실제 기기·배포 산출물을 구분해 확인합니다. 예를 들어 [학습에 같은 원본이 섞이는지 검사](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/munglog/source-split.kt#L8-L45)하고, [모델 계보가 다른 ML 입력을 거부](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/kullchip/ml2-input-lineage.kt#L1-L42)하며, [이벤트의 전달·확인을 분리](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/blink/event-buffer.kt#L8-L59)합니다.

개인 연락처·채용 이력서·녹음과 촬영 원본·위치·운영 자격증명은 이 저장소에 포함하지 않습니다.
