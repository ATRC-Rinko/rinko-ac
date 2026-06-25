#!/usr/bin/env tclsh

# token_refresh_roles.tcl
# 验证 refresh token 返回的 access token 包含真实角色（非空列表）

package require http
package require json

set BASE_URL "http://localhost:8081"

# 1. Register user
puts "=== Step 1: Register ==="
set body [json::write object \
    username [json::write string "refresh_roles_test"] \
    email [json::write string "refresh_roles@test.com"] \
    password [json::write string "Test@1234"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/register" \
    -method POST -headers [list Content-Type application/json] -query $body]
set data [::http::data $tok]
::http::cleanup $tok
set response [json::json2dict $data]
set access_token [dict get [dict get $response tokenPair] accessToken]
set refresh_token [dict get [dict get $response tokenPair] refreshToken]
set admin_user_id [dict get $response userId]
puts "User id=$admin_user_id, token obtained"

set AUTH [list Authorization "Bearer $access_token" Content-Type application/json]

# 2. Create role with permission and assign to user
puts "\n=== Step 2: Create role and assign ==="
set body [json::write object name [json::write string "reader"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/roles" \
    -method POST -headers $AUTH -query $body]
set role_id [dict get [json::json2dict [::http::data $tok]] id]
::http::cleanup $tok

set body [json::write object roleIds \[ int $role_id \]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/users/$admin_user_id/roles" \
    -method POST -headers $AUTH -query $body]
::http::cleanup $tok
puts "Role '$role_id' assigned to user $admin_user_id"

# 3. Refresh token and verify roles in new access token
puts "\n=== Step 3: Refresh token and verify roles ==="
set body [json::write object refreshToken [json::write string $refresh_token]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/token/refresh" \
    -method POST -headers [list Content-Type application/json] -query $body]
set data [::http::data $tok]
set status [::http::ncode $tok]
::http::cleanup $tok

puts "Refresh status: $status"
if {$status != 200} {
    puts "FAIL: Refresh returned $status"
    puts "Response: $data"
    exit 1
}

set response [json::json2dict $data]
set new_access_token [dict get [dict get $response tokenPair] accessToken]
set new_refresh_token [dict get [dict get $response tokenPair] refreshToken]

# Decode JWT payload (base64decode the middle part)
set parts [split $new_access_token "."]
set payload [lindex $parts 1]
# pad and decode
set payload [string cat $payload "=="]
set decoded [binary decode base64 $payload]
puts "Access token payload: $decoded"

# Verify roles claim exists and is not empty
if {![regexp {\"roles\":\[([^\]]*)\]} $decoded _ roles_content]} {
    puts "FAIL: No roles claim in access token"
    exit 1
}
if {$roles_content eq ""} {
    puts "FAIL: Roles claim should not be empty after role assignment"
    exit 1
}
puts "Roles in refreshed token: $roles_content"

puts "PASS: token_refresh_roles.tcl completed successfully"
exit 0
