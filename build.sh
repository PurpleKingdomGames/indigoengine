#!/usr/bin/env bash

set -e

export JAVA_OPTS="-Xss10m -Xmx6G"

./mill --no-server __.compile
./mill --no-server __.reformat
./mill --no-server __.compile
./mill --no-server -j2 __.fix
./mill --no-server -j2 __.fastLinkJS
./mill --no-server -j2 __.fastLinkJSTest
./mill --no-server -j1 __.test.nativeLink
./mill --no-server __.test
./mill --no-server __.publishLocal

bash diagrams/build.sh

export JAVA_OPTS=""

# Will return when sbt 2.0 supports Scala.js

# SBT Indigo
# echo ">>> SBT-Indigo"
# cd sbt-indigo
# bash build.sh
# cd ..
