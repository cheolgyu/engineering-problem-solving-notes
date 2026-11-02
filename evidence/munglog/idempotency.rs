// Exact source excerpts from munglog; original repository is private.
// Original commit: d3c795b125a99d3cbb35bf8e646997e6dc4ec48e
// Original path: services/api/src/idempotency.rs
// Selected regions only; dependencies and omitted code are not included.
// Reading context: Typed actor/request scope, transaction lock and stored response reuse.

// Original lines 7-52
fn scoped_actor_key<T>(actor_key: &str) -> String {
    format!("{actor_key}:{}", std::any::type_name::<T>())
}

pub async fn load<T>(
    transaction: &mut Transaction<'_, Postgres>,
    actor_key: &str,
    request_id: Uuid,
) -> Result<Option<T>, ApiError>
where
    T: Message + Default,
{
    let actor_key = scoped_actor_key::<T>(actor_key);
    let lock_key = format!("{actor_key}:{request_id}");
    sqlx::query("SELECT pg_advisory_xact_lock(hashtextextended($1, 0))")
        .bind(lock_key)
        .execute(&mut **transaction)
        .await
        .map_err(ApiError::internal)?;

    sqlx::query(
        "DELETE FROM idempotency_keys WHERE actor_key = $1 AND request_id = $2 AND expires_at <= now()",
    )
    .bind(&actor_key)
    .bind(request_id)
    .execute(&mut **transaction)
    .await
    .map_err(ApiError::internal)?;

    let body = sqlx::query_scalar::<_, Vec<u8>>(
        "SELECT response_body FROM idempotency_keys WHERE actor_key = $1 AND request_id = $2",
    )
    .bind(&actor_key)
    .bind(request_id)
    .fetch_optional(&mut **transaction)
    .await
    .map_err(ApiError::internal)?;

    body.map(|bytes| {
        T::decode(bytes.as_slice()).map_err(|error| {
            tracing::error!(%error, "stored idempotency response is invalid");
            ApiError::internal("stored idempotency response is invalid")
        })
    })
    .transpose()
}

