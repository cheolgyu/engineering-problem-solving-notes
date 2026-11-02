# 설계를 읽다가 코드로 이동하기

비공개 원본에서 설명에 필요한 짧은 코드 구간을 발췌했습니다. 각 파일은 원래 구현의 일부이며 의존성·주변 코드가 생략되어 독립 실행 예제가 아닙니다. 원본 전체를 공개한 것으로 표시하지 않습니다.

공개 원본이 있는 Stockbot·Gamebot은 각 문서에서 원본 저장소의 고정 커밋·줄로 연결합니다. 아래 파일은 멍로그·Blink·ColaZZang·KullChip의 선별 근거입니다.

| 프로젝트 | 구현 코드 | 원본 기준 |
| --- | --- | --- |
| munglog | [idempotency.rs](munglog/idempotency.rs) | `d3c795b1` |
| munglog | [outbox-claim.rs](munglog/outbox-claim.rs) | `d3c795b1` |
| munglog | [event-enqueue.rs](munglog/event-enqueue.rs) | `d3c795b1` |
| munglog | [record-replica.kt](munglog/record-replica.kt) | `d3c795b1` |
| munglog | [auth-refresh.kt](munglog/auth-refresh.kt) | `d3c795b1` |
| munglog | [source-split.kt](munglog/source-split.kt) | `d3c795b1` |
| blink | [latest-state.kt](blink/latest-state.kt) | `d2e90698` |
| blink | [inference-budget.kt](blink/inference-budget.kt) | `d2e90698` |
| blink | [event-buffer.kt](blink/event-buffer.kt) | `d2e90698` |
| blink | [direction-port.kt](blink/direction-port.kt) | `d2e90698` |
| kullchip | [clock-alignment.kt](kullchip/clock-alignment.kt) | `4a9bf15e` |
| kullchip | [cola-watch-marker.kt](kullchip/cola-watch-marker.kt) | `5babf326` |
| kullchip | [commit-before-ack.kt](kullchip/commit-before-ack.kt) | `4a9bf15e` |
| kullchip | [ml0-alignment.kt](kullchip/ml0-alignment.kt) | `4a9bf15e` |
| kullchip | [ml2-input-lineage.kt](kullchip/ml2-input-lineage.kt) | `4a9bf15e` |
| kullchip | [proto-drift-guard.sh](kullchip/proto-drift-guard.sh) | `4a9bf15e` |
| kullchip | [shared-server-build.Dockerfile](kullchip/shared-server-build.Dockerfile) | `4a9bf15e` |
| kullchip | [xai-grounding-policy.java](kullchip/xai-grounding-policy.java) | `4a9bf15e` |

## 출처와 확인 범위

[manifest.json](manifest.json)에 원본 커밋·경로·줄과 원본/발췌 SHA-256을 기록했습니다. 연결된 코드가 설명한 방어·분리 구조를 갖는지 확인하는 자료입니다. 당시 테스트·실기기·게시 성공은 각 문서에서 별도 기록합니다.

녹음·위치·영상·계정 정보·운영 자격증명·채용용 연락처와 이력서 원본은 포함하지 않습니다.
