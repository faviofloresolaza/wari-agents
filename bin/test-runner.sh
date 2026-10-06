#!/bin/sh
set -eu
root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd -P)
sh "$root/bin/build-runner.sh"
classes="$root/runner/build/test-classes"
mkdir -p "$classes"
javac --release 21 -d "$classes" "$root/runner/test/wari/agents/RunnerTest.java"
java -cp "$classes" wari.agents.RunnerTest "$root/runner/wari-agents-runner.jar"
