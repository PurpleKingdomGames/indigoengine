#!/usr/bin/env bash

set -e

export JAVA_OPTS="-Xss10m -Xmx6G"

./mill --no-server __.compile
./mill --no-server __.reformat
./mill --no-server __.compile
./mill --no-server -j2 __.fix
./mill --no-server -j2 __.fastLinkJSTest
./mill --no-server -j1 __.test.nativeLink
./mill --no-server -j2 __.test

export JAVA_OPTS=""
