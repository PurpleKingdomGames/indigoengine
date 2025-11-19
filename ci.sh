#!/usr/bin/env bash

set -e

./mill --no-server --disable-ticker clean
./mill --no-server --disable-ticker __.compile
./mill --no-server --disable-ticker -j2 __.fastLinkJS
./mill --no-server --disable-ticker -j2 __.fastLinkJSTest
./mill --no-server --disable-ticker __.test
./mill --no-server --disable-ticker __.checkFormat
./mill --no-server --disable-ticker -j2 __.fix --check

# # Indigo Plugin + Mill Plugin
# echo ">>> Indigo Plugin + Mill Plugin"
# cd indigo-plugin
# bash ci.sh
# cd ..

# SBT Indigo
# echo ">>> SBT-Indigo"
# cd sbt-indigo
# bash build.sh
# cd ..

# # Indigo
# echo ">>> Indigo"
# cd indigo
# bash ci.sh
# cd ..
