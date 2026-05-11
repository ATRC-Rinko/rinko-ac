CREATE TABLE oauth2_clients (
    id              BIGINT PRIMARY KEY,
    client_id       VARCHAR(255) NOT NULL UNIQUE,
    client_secret   VARCHAR(255) NOT NULL,
    redirect_uris   TEXT NOT NULL,
    grant_types     VARCHAR(255) NOT NULL,
    scopes          VARCHAR(500) NOT NULL DEFAULT '',
    access_token_ttl_seconds INT NOT NULL DEFAULT 3600,
    refresh_token_ttl_seconds INT NOT NULL DEFAULT 2592000,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_oauth2_clients_client_id ON oauth2_clients(client_id);

COMMENT ON TABLE oauth2_clients IS 'OAuth2 客户端注册表';
COMMENT ON COLUMN oauth2_clients.redirect_uris IS '逗号分隔的允许回调URI列表';
COMMENT ON COLUMN oauth2_clients.grant_types IS '逗号分隔：authorization_code,client_credentials,refresh_token';
COMMENT ON COLUMN oauth2_clients.scopes IS '逗号分隔的授权范围';
