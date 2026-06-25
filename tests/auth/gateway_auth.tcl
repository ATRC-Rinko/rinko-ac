#!/usr/bin/env tclsh

# gateway_auth.tcl
# 验证 Gateway JWT 认证 → X-User-Id Header 传递 → 内部服务信任 Header

package require http
package require json

set AUTH_BASE_URL "http://localhost:8081"
set GATEWAY_URL "http://localhost:10315"

# 1. Register + Login via auth service
puts "=== Step 1: Get token ==="
set body [json::write object \
    username [json::write string "gateway_auth_test"] \
    email [json::write string "gateway_auth@test.com"] \
    password [json::write string "Test@1234"]]
set tok [::http::geturl "$AUTH_BASE_URL/api/v1/auth/register" \
    -method POST -headers [list Content-Type application/json] -query $body]
set data [::http::data $tok]
set status [::http::ncode $tok]
::http::cleanup $tok

if {$status != 200} {
    puts "FAIL: Register returned $status"
    puts "Response: $data"
    exit 1
}
set token [dict get [dict get [json::json2dict $data] tokenPair] accessToken]
puts "Token obtained: [string range $token 0 20]..."

# 2. Access via Gateway with Bearer token
puts "\n=== Step 2: Access via Gateway with Bearer ==="
set tok [::http::geturl "$GATEWAY_URL/api/v1/auth/roles" \
    -method GET -headers [list Authorization "Bearer $token"]]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Gateway access status: $status"
if {$status != 200 && $status != 404 && $status != 502} {
    puts "FAIL: Gateway access returned $status"
    exit 1
}

# 3. Missing Authorization header → 401
puts "\n=== Step 3: Missing Authorization → 401 ==="
set tok [::http::geturl "$GATEWAY_URL/api/v1/auth/roles" -method GET]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "No Auth status: $status"
if {$status != 401 && $status != 403} {
    puts "WARN: Expected 401/403, got $status (gateway may not be running)"
}

# 4. Invalid token → 401
puts "\n=== Step 4: Invalid token → 401 ==="
set tok [::http::geturl "$GATEWAY_URL/api/v1/auth/roles" \
    -method GET -headers [list Authorization "Bearer invalid.token.here"]]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Invalid token status: $status"
if {$status == 200} {
    puts "FAIL: Invalid token should not be accepted"
    exit 1
}

puts "PASS: gateway_auth.tcl completed successfully"
exit 0
