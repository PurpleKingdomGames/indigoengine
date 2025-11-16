#!/usr/bin/env bash

set -e

./mill clean
./mill __.compile
./mill -j1 __.fastLinkJS
./mill -j2 __.test
./mill -j2 __.checkFormat
./mill -j1 __.fix --check
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
