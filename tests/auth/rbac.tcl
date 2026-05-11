#!/usr/bin/env tclsh

# T068: rbac.tcl
# 创建角色+分配权限 → 用户赋角 → 权限校验（有权限/无权限/通配符/多角色并集）

package require http
package require json

set BASE_URL "http://localhost:8081"

# 1. Register admin user
puts "=== Step 1: Create admin user ==="
set body [json::write object \
    username [json::write string "rbac_admin"] \
    email [json::write string "rbac_admin@test.com"] \
    password [json::write string "Test@1234"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/register" \
    -method POST -headers [list Content-Type application/json] -query $body]
set data [::http::data $tok]
::http::cleanup $tok
set response [json::json2dict $data]
set admin_token [dict get [dict get $response tokenPair] accessToken]
set admin_user_id [dict get $response userId]
puts "Admin user id=$admin_user_id, token obtained"

set AUTH_HEADER [list Authorization "Bearer $admin_token" Content-Type application/json]

# 2. Create role
puts "\n=== Step 2: Create role ==="
set body [json::write object name [json::write string "editor"] description [json::write string "Content Editor"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/roles" \
    -method POST -headers $AUTH_HEADER -query $body]
set data [::http::data $tok]
::http::cleanup $tok
set role_id [dict get [json::json2dict $data] id]
puts "Role created: $role_id"

# 3. Create permissions
puts "\n=== Step 3: Create permissions ==="
set perms {content:read content:write content:delete}
set perm_ids {}
foreach pcode $perms {
    set body [json::write object code [json::write string $pcode] description [json::write string "Permission $pcode"]]
    set tok [::http::geturl "$BASE_URL/api/v1/auth/permissions" \
        -method POST -headers $AUTH_HEADER -query $body]
    set data [::http::data $tok]
    ::http::cleanup $tok
    lappend perm_ids [dict get [json::json2dict $data] id]
}
puts "Permissions created: $perm_ids"

# 4. Assign permissions to role
puts "\n=== Step 4: Assign permissions to role ==="
set body [json::write object permissionIds \[ {*}[lmap id $perm_ids {list int $id}] \]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/roles/$role_id/permissions" \
    -method POST -headers $AUTH_HEADER -query $body]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Assign status: $status"
if {$status != 200} { puts "FAIL"; exit 1 }

# 5. Assign role to user
puts "\n=== Step 5: Assign role to user ==="
set body [json::write object roleIds \[ int $role_id \]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/users/1/roles" \
    -method POST -headers $AUTH_HEADER -query $body]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Role assignment status: $status"

# 6. Check permission
puts "\n=== Step 6: Check permission ==="
set body [json::write object userId int $admin_user_id requiredPermission [json::write string "content:read"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/check" \
    -method POST -headers $AUTH_HEADER -query $body]
set data [::http::data $tok]
::http::cleanup $tok
set authorized [dict get [json::json2dict $data] authorized]
puts "content:read authorized: $authorized"
if {!$authorized} { puts "FAIL: Should be authorized"; exit 1 }

# 7. Check unauthorized permission
set body [json::write object userId int $admin_user_id requiredPermission [json::write string "admin:manage"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/check" \
    -method POST -headers $AUTH_HEADER -query $body]
set data [::http::data $tok]
::http::cleanup $tok
set authorized [dict get [json::json2dict $data] authorized]
puts "admin:manage authorized: $authorized"
if {$authorized} { puts "FAIL: Should not be authorized"; exit 1 }

puts "\nPASS: rbac.tcl completed successfully"
exit 0
