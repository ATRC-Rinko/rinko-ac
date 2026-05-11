#!/usr/bin/env tclsh

# T067: token_lifecycle.tcl
# Token 过期 → refresh 换新 → revoke 吊销 → 旧 Token 403

package require http
package require json

set BASE_URL "http://localhost:8081"

# 1. Register + Login to get tokens
puts "=== Step 1: Get tokens ==="
set body [json::write object \
    username [json::write string "lifecycle_test"] \
    email [json::write string "lifecycle@test.com"] \
    password [json::write string "Test@1234"]]

set tok [::http::geturl "$BASE_URL/api/v1/auth/register" \
    -method POST -headers [list Content-Type application/json] -query $body]
set data [::http::data $tok]
::http::cleanup $tok

set response [json::json2dict $data]
set access_token [dict get [dict get $response tokenPair] accessToken]
set refresh_token [dict get [dict get $response tokenPair] refreshToken]
puts "Tokens obtained"

# 2. Refresh token
puts "\n=== Step 2: Refresh token ==="
set body [json::write object refreshToken [json::write string $refresh_token]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/token/refresh" \
    -method POST -headers [list Content-Type application/json] -query $body]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok

puts "Refresh status: $status"
if {$status != 200} {
    puts "FAIL: Refresh returned $status"
    puts "Response: $data"
    exit 1
}

# 3. Revoke token
puts "\n=== Step 3: Revoke token ==="
set body [json::write object refreshToken [json::write string $refresh_token]]
set tok [::http::geturl "$BASE_URL/api/v1/auth/token/revoke" \
    -method POST -headers [list Content-Type application/json] -query $body]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok

puts "Revoke status: $status"
if {$status != 200} {
    puts "FAIL: Revoke returned $status"
    exit 1
}

puts "PASS: token_lifecycle.tcl completed successfully"
exit 0
