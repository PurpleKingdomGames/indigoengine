#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

cd "$SCRIPT_DIR/.."
OUTPUT=$(./mill --no-server --disable-ticker visualize __.compile | jq -r '.[] | select(contains("out.dot"))')
cd "$SCRIPT_DIR"

echo $OUTPUT

scala-cli run simplify-deps.sc -- $OUTPUT > indigoengine.dot

npm install --silent
node dot-to-png.mjs
