# 코드로 읽는 개발 문제해결 기록

개인 제품을 만들며 생긴 문제와 설계 선택을 정리했습니다. **밑줄 링크를 누르면 설명한 구현의 고정 커밋·해당 줄로 이동합니다.**

바로 살펴보기: [Stockbot의 tx/rx 생산자·소비자](https://github.com/cheolgyu/stockbot-rust-portfolio/blob/27940257af4ed50fcdd3e262fbee2d316c07a06e/batch/src/job/bound/step_bound_upsert.rs#L34-L106) · [Gamebot의 IoU NMS](https://github.com/cheolgyu/gamebot-common-background/blob/3338a8495375b693bcb6d94a711737d729aeccad/src/main/java/com/highserpot/tf/tflite/YoloV4Classifier.kt#L54-L91) · [MungLog의 녹음 단위 학습·평가 분리](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/munglog/source-split.kt#L8-L45) · [Blink의 이벤트 확인 대기열](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/blink/event-buffer.kt#L8-L59)

| 프로젝트 | 어떤 제품인가 | 설계·구현 읽기 |
| --- | --- | --- |
| Stockbot | 주가 수집·분석·조회 | [웹](docs/stockbot.md) · [PDF](pdf/stockbot.pdf) · [검토용 소스 사본](https://github.com/cheolgyu/stockbot-rust-portfolio) |
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
