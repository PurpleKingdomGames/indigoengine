#!/bin/bash

cd ..
OUTPUT=$(./mill --no-server --disable-ticker visualize __.compile | jq -r '.[] | select(contains("out.dot"))')
cd  diagrams

echo $OUTPUT

scala-cli run simplify-deps.sc -- $OUTPUT > indigoengine.dot