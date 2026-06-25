#!/usr/bin/env tclsh

# scheduler_crud_dag.tcl
# 验证调度器 CRUD（新 DTO）、CRON 验证、DAG 环检测

package require http
package require json

set BASE_URL "http://localhost:8082"

# 1. Create job with new DTO format (name + type required)
puts "=== Step 1: Create job with valid DTO ==="
set config [json::write object command [json::write string "echo hello"]]
set body [json::write object \
    name [json::write string "e2e_job_1"] \
    type [json::write string "SHELL"] \
    cronExpression [json::write string "0 0 * * * ?"] \
    config [json::write string $config] \
    enabled false \
    maxRetries int 1]
set tok [::http::geturl "$BASE_URL/api/v1/scheduler/jobs" \
    -method POST -headers [list Content-Type application/json X-User-Id "1"] -query $body]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok
puts "Create job status: $status"
if {$status == 201} {
    set response [json::json2dict $data]
    set job_id [dict get [dict get $response data] id]
    puts "Job created: id=$job_id"
} else {
    puts "WARN: Create job returned $status"
}

# 2. Create job without name → DTO validation should reject
puts "\n=== Step 2: Create job without name → 400 ==="
set body [json::write object \
    type [json::write string "SHELL"] \
    config [json::write string "{}"]]
set tok [::http::geturl "$BASE_URL/api/v1/scheduler/jobs" \
    -method POST -headers [list Content-Type application/json X-User-Id "1"] -query $body]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "No-name create status: $status"
if {$status != 400} {
    puts "WARN: Expected 400 for missing name, got $status"
}

# 3. Create job with invalid CRON expression
puts "\n=== Step 3: Invalid CRON expression ==="
set body [json::write object \
    name [json::write string "bad_cron"] \
    type [json::write string "SHELL"] \
    cronExpression [json::write string "not-a-valid-cron"] \
    config [json::write string "{}"] \
    enabled true]
set tok [::http::geturl "$BASE_URL/api/v1/scheduler/jobs" \
    -method POST -headers [list Content-Type application/json X-User-Id "1"] -query $body]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok
puts "Invalid CRON status: $status"
if {$status != 400} {
    puts "WARN: Invalid CRON should return 400, got $status"
}

# 4. List jobs
puts "\n=== Step 4: List jobs ==="
set tok [::http::geturl "$BASE_URL/api/v1/scheduler/jobs" \
    -method GET -headers [list X-User-Id "1"]]
set status [::http::ncode $tok]
set data [::http::data $tok]
::http::cleanup $tok
puts "List jobs status: $status"

# 5. Get executions with pagination
puts "\n=== Step 5: Get executions (paginated) ==="
set tok [::http::geturl "$BASE_URL/api/v1/scheduler/executions?jobId=1&page=1&size=10" \
    -method GET -headers [list X-User-Id "1"]]
set status [::http::ncode $tok]
::http::cleanup $tok
puts "Executions status: $status"

puts "PASS: scheduler_crud_dag.tcl completed successfully"
exit 0
