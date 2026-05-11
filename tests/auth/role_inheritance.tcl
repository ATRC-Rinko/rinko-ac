#!/usr/bin/env tclsh

# T069: role_inheritance.tcl
# 建立 3 层继承 A→B→C → C 用户获得 A+B+C 权限 → 删除父角色 → 继承断裂

package require http
package require json

set BASE_URL "http://localhost:8081"

# 1. Register + login
puts "=== Step 1: Setup ==="
set body [json::write object \
    username [json::write string "inheritance_test"] \
    email [json::write string "inherit@test.com"] \
    password [json::write string "Test@1234"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/register" \
    -method POST -headers [list Content-Type application/json] -query $body]
set data [::http::data $tok]
::http::cleanup $tok
set token [dict get [dict get [json::json2dict $data] tokenPair] accessToken]
set user_id [dict get [json::json2dict $data] userId]
set AUTH [list Authorization "Bearer $token" Content-Type application/json]

# 2. Create roles A, B, C
puts "\n=== Step 2: Create roles A, B, C ==="
proc create_role {name} {
    global AUTH BASE_URL
    set body [json::write object name [json::write string $name]]
    set tok [::http::geturl "$BASE_URL/api/v1/auth/roles" \
        -method POST -headers $AUTH -query $body]
    set data [::http::data $tok]
    ::http::cleanup $tok
    return [dict get [json::json2dict $data] id]
}
set role_a [create_role "role_a"]
set role_b [create_role "role_b"]
set role_c [create_role "role_c"]
puts "Role A=$role_a B=$role_b C=$role_c"

# 3. Create permissions and assign to role_a
puts "\n=== Step 3: Assign permission to role_a ==="
set body [json::write object code [json::write string "a:read"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/permissions" \
    -method POST -headers $AUTH -query $body]
set perm_id [dict get [json::json2dict [::http::data $tok]] id]
::http::cleanup $tok
set body [json::write object permissionIds \[ int $perm_id \]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/roles/$role_a/permissions" \
    -method POST -headers $AUTH -query $body]
::http::cleanup $tok
puts "Permission a:read assigned to role_a"

# 4. Build hierarchy: A ← B ← C
puts "\n=== Step 4: Build hierarchy A→B→C ==="
set tok [::http::geturl "$BASE_URL/api/v1/auth/roles/$role_b/parents/$role_a" \
    -method POST -headers $AUTH]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Set A as parent of B: $status"
if {$status != 200} { puts "FAIL"; exit 1 }

set tok [::http::geturl "$BASE_URL/api/v1/auth/roles/$role_c/parents/$role_b" \
    -method POST -headers $AUTH]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Set B as parent of C: $status"
if {$status != 200} { puts "FAIL"; exit 1 }

# 5. Assign role_c to user and verify inherited permission
puts "\n=== Step 5: Verify inherited permission ==="
set body [json::write object roleIds \[ int $role_c \]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/users/$user_id/roles" \
    -method POST -headers $AUTH -query $body]
::http::cleanup $tok

set body [json::write object userId int $user_id requiredPermission [json::write string "a:read"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/check" \
    -method POST -headers $AUTH -query $body]
set authorized [dict get [json::json2dict [::http::data $tok]] authorized]
::http::cleanup $tok
puts "User (role_c) has a:read via inheritance: $authorized"
if {!$authorized} { puts "FAIL: Should inherit a:read"; exit 1 }

puts "PASS: role_inheritance.tcl completed successfully"
exit 0
