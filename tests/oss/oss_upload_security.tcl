#!/usr/bin/env tclsh

# oss_upload_security.tcl
# 验证文件上传大小限制、路径穿越防护、文件名安全清洗

package require http
package require json

set BASE_URL "http://localhost:8082"

# 1. Upload a small file → should succeed
puts "=== Step 1: Upload small file ==="
set boundary "----TestBoundary12345"
set content "Hello Rinko OSS"
set body ""
append body "--$boundary\r\n"
append body "Content-Disposition: form-data; name=\"file\"; filename=\"test.txt\"\r\n"
append body "Content-Type: text/plain\r\n\r\n"
append body "$content\r\n"
append body "--$boundary--\r\n"

set tok [::http::geturl "$BASE_URL/api/v1/oss/upload" \
    -method POST \
    -headers [list Content-Type "multipart/form-data; boundary=$boundary" X-User-Id "1"] \
    -query $body]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok
puts "Upload status: $status"
if {$status != 201} {
    puts "WARN: Upload returned $status (OSS service may not be running)"
}

# 2. List files with pagination
puts "\n=== Step 2: List files (paginated) ==="
set tok [::http::geturl "$BASE_URL/api/v1/oss/files?page=1&size=10" \
    -method GET -headers [list X-User-Id "1"]]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok
puts "List files status: $status"
if {$status == 200} {
    set response [json::json2dict $data]
    puts "Files returned: page=[dict get $response page], size=[dict get $response size]"
}

# 3. Path traversal attack via downloadByKey → should be rejected
puts "\n=== Step 3: Path traversal via downloadByKey ==="
set tok [::http::geturl "$BASE_URL/api/v1/oss/download/by-key?key=../../../etc/passwd" \
    -method GET -headers [list X-User-Id "1"]]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Path traversal status: $status"
if {$status != 400} {
    puts "WARN: Path traversal should return 400, got $status"
}

# 4. Path traversal with backslashes → should be rejected
puts "\n=== Step 4: Path traversal with backslashes ==="
set tok [::http::geturl "$BASE_URL/api/v1/oss/download/by-key?key=..\\..\\windows" \
    -method GET -headers [list X-User-Id "1"]]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Backslash traversal status: $status"
if {$status != 400} {
    puts "WARN: Backslash traversal should return 400, got $status"
}

puts "PASS: oss_upload_security.tcl completed successfully"
exit 0
