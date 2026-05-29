#!/usr/bin/env bash

# Run from root.

set -e

export GPG_TTY=$(tty)

source ./credentials.sh

# ./mill --no-server __.compile
# ./mill --no-server __.reformat
# ./mill --no-server __.compile
# ./mill --no-server -j2 __.fix
# ./mill --no-server -j2 __.fastLinkJS
# ./mill --no-server -j2 __.fastLinkJSTest
# ./mill --no-server -j1 __.test.nativeLink
# ./mill --no-server __.test
# ./mill --no-server __.publishSonatypeCentral
./mill --no-server mill.javalib.SonatypeCentralPublishModule/publishAll

source ./credentials-cleanup.sh