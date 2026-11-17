#!/bin/bash
# Docker healthcheck without curl: HTTP GET /health/ready via bash /dev/tcp.
exec 3<>/dev/tcp/127.0.0.1/"${HTTP_PORT:-9080}" || exit 1
printf 'GET /health/ready HTTP/1.0\r\nHost: localhost\r\n\r\n' >&3
read -r -t 5 status_line <&3 || exit 1
exec 3<&-
[[ "$status_line" == *" 200 "* ]]
