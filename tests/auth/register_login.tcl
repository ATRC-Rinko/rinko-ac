#!/usr/bin/env tclsh

# T066: register_login.tcl
# 注册 → 登录获取 Token → 携带 Token 访问受保护资源 → 200

package require http
package require json

set BASE_URL "http://localhost:8081"
set TEST_USER "e2e_test_user"
set TEST_EMAIL "e2e@test.com"
set TEST_PASSWORD "Test@1234"

# 1. Register
puts "=== Step 1: Register ==="
set body [json::write object \
    username [json::write string $TEST_USER] \
    email [json::write string $TEST_EMAIL] \
    password [json::write string $TEST_PASSWORD]]

set headers [list Content-Type application/json]
set tok [::http::geturl "$BASE_URL/api/v1/auth/register" \
    -method POST -headers $headers -query $body]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok

puts "Register status: $status"
if {$status != 200} {
    puts "FAIL: Register returned $status"
    puts "Response: $data"
    exit 1
}

set response [json::json2dict $data]
set access_token [dict get [dict get $response tokenPair] accessToken]
puts "Token obtained: [string range $access_token 0 20]..."

# 2. Access protected resource with token
puts "\n=== Step 2: Access protected resource ==="
set tok [::http::geturl "$BASE_URL/api/v1/auth/roles" \
    -method GET -headers [list Authorization "Bearer $access_token"]]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok

puts "Protected resource status: $status"
if {$status != 200} {
    puts "FAIL: Protected resource returned $status"
    exit 1
}

puts "PASS: register_login.tcl completed successfully"
exit 0
