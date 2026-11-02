ARG SERVICE_NAME
RUN --mount=type=cache,target=/usr/local/cargo/registry \
    --mount=type=cache,target=/usr/local/cargo/git \
    cargo build --release \
        --bin kullchip-admin-api \
        --bin kullchip-grpc-unary \
        --bin kullchip-grpc-relay \
        --bin kullchip-grpc-notifier \
        --bin kullchip-batch-server \
    && cp /build/server/target/release/kullchip-${SERVICE_NAME} /build/app

FROM gcr.io/distroless/cc-debian12

COPY --from=builder /build/app /usr/local/bin/kullchip
WORKDIR /app

ENTRYPOINT ["/usr/local/bin/kullchip"]
