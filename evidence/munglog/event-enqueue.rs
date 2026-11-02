// Exact source excerpts from munglog; original repository is private.
// Original commit: d3c795b125a99d3cbb35bf8e646997e6dc4ec48e
// Original path: services/api/src/events.rs
// Selected regions only; dependencies and omitted code are not included.
// Reading context: Enqueue receives the same transaction as event storage; already deleted events do not enqueue.

// Original lines 202-217
        if inserted.is_some() {
            if collector_deleted_at.is_some() {
                accepted_event_ids.push(event.id.to_string());
                continue;
            }
            notifications::enqueue_event(
                &mut transaction,
                &device,
                event.id,
                event.risk_level,
                event.started_at,
            )
            .await?;
            accepted_event_ids.push(event.id.to_string());
            continue;
        }

