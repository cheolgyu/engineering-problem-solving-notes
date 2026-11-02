// Exact source excerpts from munglog; original repository is private.
// Original commit: d3c795b125a99d3cbb35bf8e646997e6dc4ec48e
// Original path: services/api/src/push.rs
// Selected regions only; dependencies and omitted code are not included.
// Reading context: Current membership and event state are checked when claiming one outbox task.

// Original lines 618-672
async fn claim_one(database: &PgPool) -> Result<Option<PendingDelivery>, sqlx::Error> {
    sqlx::query_as::<_, PendingDelivery>(
        r#"
        WITH candidate AS (
            SELECT outbox.id
            FROM notification_outbox AS outbox
            JOIN push_subscriptions AS subscription
              ON subscription.id = outbox.push_subscription_id
             AND subscription.user_id = outbox.user_id
             AND subscription.disabled_at IS NULL
             AND subscription.provider = 'WEB_PUSH'
            JOIN bark_events AS event
              ON event.id = outbox.event_id
             AND event.collector_deleted_at IS NULL
            JOIN household_members AS membership
              ON membership.household_id = event.household_id
             AND membership.user_id = outbox.user_id
            WHERE outbox.sent_at IS NULL
              AND outbox.available_at <= now()
              AND (outbox.claimed_at IS NULL OR outbox.claimed_at < now() - make_interval(mins => $1))
              AND (
                  subscription.provisioning_retry_used_at IS NULL
                  OR subscription.last_success_at >= subscription.registered_at
                  OR subscription.provisioning_retry_used_at <= now() - make_interval(secs => $2)
              )
            ORDER BY outbox.available_at, outbox.created_at
            FOR UPDATE OF outbox SKIP LOCKED
            LIMIT 1
        )
        UPDATE notification_outbox AS outbox
        SET claimed_at = now(), attempt_count = attempt_count + 1
        FROM candidate, push_subscriptions AS subscription, bark_events AS event
        WHERE outbox.id = candidate.id
          AND subscription.id = outbox.push_subscription_id
          AND subscription.user_id = outbox.user_id
          AND subscription.disabled_at IS NULL
          AND subscription.provider = 'WEB_PUSH'
          AND event.id = outbox.event_id
        RETURNING outbox.id AS outbox_id,
                  outbox.push_subscription_id,
                  outbox.attempt_count,
                  subscription.endpoint,
                  subscription.p256dh_key,
                  subscription.auth_secret,
                  event.id AS event_id,
                  event.risk_level,
                  event.started_at,
                  subscription.locale
        "#,
    )
    .bind(CLAIM_TIMEOUT_MINUTES)
    .bind(FRESH_SUBSCRIPTION_RETRY_SECONDS as f64)
    .fetch_optional(database)
    .await
}

