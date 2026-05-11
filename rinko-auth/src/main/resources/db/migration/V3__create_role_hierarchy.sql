-- =====================================================
-- V3: 角色继承闭包表 + 种子数据
-- =====================================================

CREATE TABLE role_hierarchy (
    ancestor BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    descendant BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    depth INT NOT NULL CHECK (depth >= 0 AND depth <= 3),
    PRIMARY KEY (ancestor, descendant)
);

COMMENT ON TABLE role_hierarchy IS '角色继承闭包表';
COMMENT ON COLUMN role_hierarchy.depth IS '继承深度 0=自身 1=直接父 2=祖父 3=曾祖父';

-- =====================================================
-- 种子数据：内置角色 + 基础权限
-- =====================================================

-- 雪花 ID 起始值（为兼容演示环境固定 ID）
INSERT INTO roles (id, name, description, is_system) VALUES
    (1, 'admin', '系统管理员', TRUE),
    (2, 'user', '普通用户', TRUE);

INSERT INTO permissions (id, code, description) VALUES
    (1, 'user:read', '查看用户'),
    (2, 'user:write', '创建/编辑用户'),
    (3, 'user:delete', '删除用户'),
    (4, 'role:read', '查看角色'),
    (5, 'role:write', '创建/编辑角色'),
    (6, 'role:delete', '删除角色'),
    (7, 'permission:read', '查看权限'),
    (8, 'permission:write', '管理权限');

-- admin 拥有全部权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8);

-- user 拥有只读权限
INSERT INTO role_permissions (role_id, permission_id) VALUES
    (2, 1), (2, 4), (2, 7);

-- 自身引用（深度 0）
INSERT INTO role_hierarchy (ancestor, descendant, depth) VALUES
    (1, 1, 0),
    (2, 2, 0);
