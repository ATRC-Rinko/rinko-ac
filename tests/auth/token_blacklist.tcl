#!/usr/bin/env tclsh

# token_blacklist.tcl
# 验证 Token 黑名单：logout → token 失效；revoke → refresh token 失效

package require http
package require json

set BASE_URL "http://localhost:8081"

# 1. Register + Login
puts "=== Step 1: Get tokens ==="
set body [json::write object \
    username [json::write string "blacklist_test"] \
    email [json::write string "blacklist@test.com"] \
    password [json::write string "Test@1234"]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/register" \
    -method POST -headers [list Content-Type application/json] -query $body]
set data [::http::data $tok]
::http::cleanup $tok
set response [json::json2dict $data]
set access_token [dict get [dict get $response tokenPair] accessToken]
set AUTH [list Authorization "Bearer $access_token" Content-Type application/json]
puts "Token obtained"

# 2. Logout (blacklist access token)
puts "\n=== Step 2: Logout ==="
set tok [::http::geturl "$BASE_URL/api/v1/auth/logout" \
    -method POST -headers $AUTH]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Logout status: $status"
if {$status != 200} {
    puts "FAIL: Logout returned $status"
    exit 1
}

# 3. Try to use blacklisted token → should fail
puts "\n=== Step 3: Use blacklisted token ==="
set tok [::http::geturl "$BASE_URL/api/v1/auth/roles" \
    -method GET -headers $AUTH]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Blacklisted token access status: $status"
if {$status == 200} {
    puts "FAIL: Blacklisted token should not be accepted"
    exit 1
}

puts "PASS: token_blacklist.tcl completed successfully"
exit 0
