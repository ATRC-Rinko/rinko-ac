CREATE TABLE IF NOT EXISTS undo_log
(
    id
    SERIAL,
    branch_id
    BIGINT
    NOT
    NULL,
    xid
    VARCHAR
(
    128
) NOT NULL,
    context VARCHAR
(
    128
) NOT NULL,
    rollback_info BYTEA NOT NULL,
    log_status INT NOT NULL,
    log_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    log_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_undo_log PRIMARY KEY
(
    id
),
    CONSTRAINT ux_undo_log UNIQUE
(
    xid,
    branch_id
)
    );
