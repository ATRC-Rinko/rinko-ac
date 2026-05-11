#!/usr/bin/env tclsh

# T070: permission_realtime.tcl
# 修改角色权限 → 10s 内权限校验反映变化（验证缓存失效）

package require http
package require json

set BASE_URL "http://localhost:8081"

# 1. Register + login
puts "=== Step 1: Setup ==="
set body [json::write object \
    username [json::write string "realtime_test"] \
    email [json::write string "realtime@test.com"] \
    password [json::write string "Test@1234"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/register" \
    -method POST -headers [list Content-Type application/json] -query $body]
set data [::http::data $tok]
::http::cleanup $tok
set token [dict get [dict get [json::json2dict $data] tokenPair] accessToken]
set user_id [dict get [json::json2dict $data] userId]
set AUTH [list Authorization "Bearer $token" Content-Type application/json]

# 2. Create role and assign to user
puts "\n=== Step 2: Create role ==="
set body [json::write object name [json::write string "rt_role"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/roles" \
    -method POST -headers $AUTH -query $body]
set role_id [dict get [dict get [json::json2dict [::http::data $tok]] content 0] id]
::http::cleanup $tok

set body [json::write object roleIds \[ int $role_id \]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/users/$user_id/roles" \
    -method POST -headers $AUTH -query $body]
::http::cleanup $tok

# 3. Verify no permission initially
puts "\n=== Step 3: Verify no permission ==="
set body [json::write object userId int $user_id requiredPermission [json::write string "rt:read"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/check" \
    -method POST -headers $AUTH -query $body]
set authorized [dict get [json::json2dict [::http::data $tok]] authorized]
::http::cleanup $tok
puts "Initial check (should be false): $authorized"
if {$authorized} { puts "FAIL: Should not be authorized"; exit 1 }

# 4. Create permission and assign to role
puts "\n=== Step 4: Assign permission ==="
set body [json::write object code [json::write string "rt:read"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/permissions" \
    -method POST -headers $AUTH -query $body]
set perm_id [dict get [json::json2dict [::http::data $tok]] id]
::http::cleanup $tok

set body [json::write object permissionIds \[ int $perm_id \]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/roles/$role_id/permissions" \
    -method POST -headers $AUTH -query $body]
::http::cleanup $tok

# 5. Wait a moment for cache eviction
puts "\n=== Step 5: Verify real-time permission update ==="
after 2000
set body [json::write object userId int $user_id requiredPermission [json::write string "rt:read"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/check" \
    -method POST -headers $AUTH -query $body]
set authorized [dict get [json::json2dict [::http::data $tok]] authorized]
::http::cleanup $tok
puts "After assignment (should be true): $authorized"
if {!$authorized} { puts "FAIL: Permission change not reflected"; exit 1 }

puts "PASS: permission_realtime.tcl completed successfully"
exit 0
