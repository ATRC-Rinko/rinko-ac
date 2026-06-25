#!/usr/bin/env tclsh

# Rinko-AC Distributed Test Suite
# 覆盖所有模块的 e2e 分布式测试

set TEST_DIR [file dirname [info script]]

proc run_test {module name} {
    global TEST_DIR
    set path [file join $TEST_DIR $module $name]
    if {![file exists $path]} {
        puts "  SKIP: $module/$name (not found)"
        return "SKIP"
    }
    puts -nonewline "  $module/$name ... "
    flush stdout
    if {[catch {exec tclsh $path 2>@1} output]} {
        if {[string match "*PASS*" $output]} {
            puts "PASS"
            return "PASS"
        }
        puts "FAIL"
        puts "    [string range $output 0 200]"
        return "FAIL"
    }
    if {[string match "*PASS*" $output]} {
        puts "PASS"
        return "PASS"
    }
    puts "FAIL"
    puts "    [string range $output 0 200]"
    return "FAIL"
}

# Test suite definition
set suites {
    auth {
        register_login.tcl
        token_lifecycle.tcl
        token_blacklist.tcl
        token_refresh_roles.tcl
        rbac.tcl
        role_inheritance.tcl
        permission_realtime.tcl
        gateway_auth.tcl
    }
    notify {
        notify_send_inbox.tcl
    }
    oss {
        oss_upload_security.tcl
    }
    scheduler {
        scheduler_crud_dag.tcl
        scheduler_security.tcl
    }
    log {
        log_query_level.tcl
    }
}

set total 0
set passed 0
set failed 0
set skipped 0

puts "======================================"
puts "  Rinko-AC Distributed Test Suite"
puts "======================================"

dict for {module tests} $suites {
    puts "\n--- Module: $module ---"
    foreach test $tests {
        incr total
        set result [run_test $module $test]
        switch $result {
            "PASS" { incr passed }
            "FAIL" { incr failed }
            "SKIP" { incr skipped }
        }
    }
}

puts "\n======================================"
puts "  Results: $passed PASS / $failed FAIL / $skipped SKIP / $total TOTAL"
puts "======================================"

if {$failed > 0} { exit 1 }
exit 0
