-- =====================================================
-- V2: RBAC 核心表（角色、权限、用户角色关联、角色权限关联）
-- =====================================================

CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    description VARCHAR(256),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_roles_name ON roles(name);

CREATE TABLE permissions (
    id BIGINT PRIMARY KEY,
    code VARCHAR(128) NOT NULL,
    description VARCHAR(256)
);

CREATE UNIQUE INDEX idx_permissions_code ON permissions(code);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

COMMENT ON TABLE roles IS '角色表';
COMMENT ON TABLE permissions IS '权限表';
COMMENT ON TABLE user_roles IS '用户-角色关联表';
COMMENT ON TABLE role_permissions IS '角色-权限关联表';
