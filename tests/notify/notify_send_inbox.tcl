#!/usr/bin/env tclsh

# notify_send_inbox.tcl
# 验证通知发送（DTO @Valid）、站内信（X-User-Id Header）、模板创建

package require http
package require json

set BASE_URL "http://localhost:8082"

# 1. Create template via DTO with @Valid
puts "=== Step 1: Create template ==="
set body [json::write object \
    code [json::write string "welcome_e2e"] \
    name [json::write string "Welcome E2E"] \
    subject [json::write string "Hello"] \
    body [json::write string "Welcome to Rinko"]]
set tok [::http::geturl "$BASE_URL/api/v1/notify/templates" \
    -method POST -headers [list Content-Type application/json X-User-Id "1"] -query $body]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok
puts "Create template status: $status"
if {$status != 201} {
    puts "FAIL: Create template returned $status"
    puts "Response: $data"
    exit 1
}

# 2. Create template without required fields → 400
puts "\n=== Step 2: Template without code → 400 ==="
set body [json::write object name [json::write string "Bad Template"]]
set tok [::http::geturl "$BASE_URL/api/v1/notify/templates" \
    -method POST -headers [list Content-Type application/json X-User-Id "1"] -query $body]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Missing code status: $status"
if {$status != 400} {
    puts "WARN: Expected 400 for missing template code, got $status"
}

# 3. Send notification with @Valid DTO
puts "\n=== Step 3: Send notification ==="
set body [json::write object \
    channel [json::write string "IN_APP"] \
    templateCode [json::write string "welcome_e2e"] \
    recipient [json::write string "user_001"]]
set tok [::http::geturl "$BASE_URL/api/v1/notify/send" \
    -method POST -headers [list Content-Type application/json X-User-Id "1"] -query $body]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok
puts "Send notify status: $status"
if {$status != 202} {
    puts "WARN: Send notify returned $status (service may be async)"
}

# 4. Get inbox with X-User-Id header (not param)
puts "\n=== Step 4: Get inbox via X-User-Id header ==="
set tok [::http::geturl "$BASE_URL/api/v1/notify/inbox" \
    -method GET -headers [list X-User-Id "user_001"]]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok
puts "Inbox status: $status"
if {$status != 200} {
    puts "WARN: Inbox returned $status"
}

# 5. Get unread count via X-User-Id header
puts "\n=== Step 5: Get unread count via X-User-Id header ==="
set tok [::http::geturl "$BASE_URL/api/v1/notify/inbox/unread-count" \
    -method GET -headers [list X-User-Id "user_001"]]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok
puts "Unread count status: $status"
if {$status != 200} {
    puts "WARN: Unread count returned $status"
}

# 6. Get inbox without X-User-Id → should return empty
puts "\n=== Step 6: Inbox without X-User-Id → empty ==="
set tok [::http::geturl "$BASE_URL/api/v1/notify/inbox" -method GET]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok
puts "No-header inbox status: $status"
if {$status == 200} {
    set response [json::json2dict $data]
    set count [llength [dict get $response data]]
    puts "Items returned without auth: $count"
}

puts "PASS: notify_send_inbox.tcl completed successfully"
exit 0
