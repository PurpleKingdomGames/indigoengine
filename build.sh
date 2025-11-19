#!/usr/bin/env bash

set -e

./mill clean
./mill __.compile
./mill -j2 __.fastLinkJS
./mill -j2 __.fastLinkJSTest
./mill __.test
./mill __.checkFormat
./mill -j2 __.fix --check
./mill __.publishLocal

# # Indigo Plugin + Mill Plugin
# echo ">>> Indigo Plugin + Mill Plugin"
# cd indigo-plugin
# bash build.sh
# cd ..

# SBT Indigo
# echo ">>> SBT-Indigo"
# cd sbt-indigo
# bash build.sh
# cd ..

# # Indigo
# echo ">>> Indigo"
# cd indigo
# bash build.sh
# cd ..
