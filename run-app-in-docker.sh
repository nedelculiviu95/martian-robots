#!/bin/sh
set -eu

if [ "$#" -ne 1 ]; then
    printf 'Usage: %s INPUT_FILE | --test\n' "$0" >&2
    exit 2
fi

input_file=$1
if [ "$input_file" != "--test" ]; then
    if [ ! -f "$input_file" ] || [ ! -r "$input_file" ]; then
        printf 'Cannot read input file: %s\n' "$input_file" >&2
        exit 2
    fi
fi

if ! command -v docker >/dev/null 2>&1; then
    printf 'Docker is required. Install and start Docker before running this script.\n' >&2
    exit 1
fi

project_dir=$(CDPATH= cd "$(dirname "$0")" && pwd)

cleanup_test_container() {
    if [ -n "${test_container:-}" ]; then
        docker rm --force "$test_container" >/dev/null 2>&1 || true
    fi
}

run_tests() {
    docker build --target test --tag martian-robots-tests:local "$project_dir" >&2

    reports_dir="$project_dir/docker-reports"
    mkdir -p "$reports_dir"
    # Replace generated reports so an earlier run cannot be mistaken for this one.
    rm -rf "$reports_dir/tests" "$reports_dir/jacoco"

    test_container=$(docker create --label martian-robots.test=true martian-robots-tests:local \
        ./gradlew --no-daemon clean test jacocoTestReport --continue)
    trap cleanup_test_container EXIT
    trap 'exit 130' INT
    trap 'exit 143' TERM

    test_status=0
    docker start --attach "$test_container" || test_status=$?
    if [ "$test_status" -eq 0 ]; then
        test_status=$(docker inspect --format '{{.State.ExitCode}}' "$test_container")
    fi

    report_status=0
    docker cp "$test_container:/build/build/reports/." "$reports_dir" || report_status=1
    cleanup_test_container
    test_container=

    test_report="$reports_dir/tests/test/index.html"
    coverage_report="$reports_dir/jacoco/test/html/index.html"
    if [ -f "$test_report" ]; then
        printf '\nTest report: %s\n' "$test_report"
    else
        printf 'Test report was not generated. See the build output above.\n' >&2
        report_status=1
    fi

    if [ -f "$coverage_report" ]; then
        printf 'Coverage report: %s\n' "$coverage_report"
    else
        printf 'Coverage report was not generated. See the build output above.\n' >&2
        report_status=1
    fi

    if [ "$test_status" -eq 0 ]; then
        test_status=$report_status
    fi
    exit "$test_status"
}

if [ "$input_file" = "--test" ]; then
    run_tests
fi

# build image
docker build --tag martian-robots:local "$project_dir" >&2

# run the application
exec docker run --rm -i martian-robots:local < "$input_file"
