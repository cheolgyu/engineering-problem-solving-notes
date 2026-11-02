# Blink | 눈동작을 스크롤·페이지·시선 게임으로

손을 쓰기 어려운 순간에도 화면을 조작하려고 만들었습니다. **BlinkRoll은 스크롤, BlinkPage는 페이지 넘김, BlinkGame은 시선으로 좌우 이동**합니다. MediaPipe/TFLite 기반 관측을 입력으로 연결한 앱 3종을 개발·출시했고 AI 에이전트를 구현·리팩터링·시험에 활용했습니다.

**카메라·ML 관측 → 순수 눈동작 해석 → 이벤트·의도 계약 → Roll/Page Android 또는 Game Unity 실행 → 실행 결과 확인.** 촬영·거리 변화에 강한 공통 시선 모델 연구는 출시한 제품 기능과 구분합니다.

## 1. 같은 해석을 재사용하고 제품 실행은 각자 소유

eye-contract는 이벤트·의도·결과 타입, eye-engine은 순수 해석, eye-android는 카메라·모델 수명주기, runtime은 제품 구성·권한·시작·종료를 맡습니다. Roll의 전용 캡처와 Page/Game의 공유 입력 경로는 다르지만 의미 계약을 검사합니다.

Unity가 원본 영상을 받아 다시 판단하지 않도록 버전 있는 snapshot·event로 연결했습니다. [event ID 중복 검사·poll/confirm·대기열 상한](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/blink/event-buffer.kt#L8-L59)으로 한 사건의 전달과 확인을 관리합니다. 큐가 넘치면 버린 ID를 반환해 거부 처리하며 무한 적체를 막습니다.

## 2. 상태는 최신 값, 명령은 확인할 때까지

상태 관측을 모두 재생하면 소비자가 잠깐 멈춘 뒤 오래된 눈 상태가 연속 전달될 수 있습니다. [LatestValueDispatcher는 대기 중 상태를 최신 값으로 교체](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/blink/latest-state.kt#L8-L45)합니다. 개별 명령은 이 경로에 섞지 않고 앞의 이벤트 대기열·확인 계약을 사용합니다.

추론 작업은 [실행 1개·준비 1개의 InferenceWorkBudget](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/blink/inference-budget.kt#L8-L54)로 제한했습니다. 동기화된 허가 발급과 한 번만 해제되는 permit을 사용하고, 세션 reset이 이전 실행의 남은 허가를 가짜 여유 슬롯으로 만들지 않게 했습니다. 이 구조와 실제 장시간 발열·배터리 개선은 별도 근거입니다.

## 3. 오래된 시선 관측이 게임을 계속 움직이지 않게

[게임용 숫자 관측 포트의 복사·유효 시간·세대 경계](https://github.com/cheolgyu/engineering-problem-solving-notes/blob/306f41cf6bb116e31b5b483ae028eec047bb4d57/evidence/blink/direction-port.kt#L8-L46)를 두었습니다. 원본 배열을 공유하지 않고 복사한 숫자만 전달하며, 관측이 오래됐거나 얼굴·눈 감김 상태가 바뀌면 비웁니다. reset·pause·stop은 세대를 바꿔 이전 관측을 무효화합니다. 모델·카메라와 게임 규칙의 책임을 나눈 구현입니다.

눈 상태를 인식한 것, 접근성 명령을 접수한 것, 실제 화면이 이동한 것은 다릅니다. 같은 event ID로 결과를 추적하고 숫자형 신고를 반복 재생하는 계약 회귀와 실제 화면 이동을 구분해 확인했습니다.

## 4. 소스 성공을 배포 산출물까지 이어 확인

release 변형·R8·ABI·산출물 노출 검사와 지정 기기 기록을 연결하고, 같은 AAB hash·서명·소스·출시 기록을 보존했습니다. 원본 영상이나 연구 모델이 출시 산출물에 섞이지 않는 경계도 검사합니다. 모든 촬영 조건의 범용 인식 성능을 입증한 결과로 넓히지는 않습니다.

[공개 제품·연구 사례집](https://github.com/cheolgyu/blink-engineering-case-study)에는 별도 촬영 평가와 채택을 보류한 실험도 있습니다. 신기능·공통 모델 실험은 중단 상태이며 최신 Game 후보의 제출과 실제 게시 상태를 구분합니다.

## 코드 근거

위 링크는 **비공개 원본 d2e90698의 정확한 발췌**입니다. [원본 커밋·경로·줄·해시](../evidence/manifest.json)를 제공합니다. 원본 촬영·모델 자산·계정·채용 연락처는 포함하지 않습니다.
