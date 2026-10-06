#!/bin/sh
set -eu
root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd -P)
classes="$root/runner/build/classes"
mkdir -p "$classes"
javac --release 21 -d "$classes" "$root/runner/src/wari/agents/Json.java" "$root/runner/src/wari/agents/Runner.java" "$root/runner/src/wari/agents/provider/ProviderAdapter.java" "$root/runner/src/wari/agents/provider/CodexAdapter.java" "$root/runner/src/wari/agents/provider/ProviderAdapters.java"
jar --create --file "$root/runner/wari-agents-runner.jar" --main-class wari.agents.Runner -C "$classes" .
printf 'Runner compilado: %s\n' "$root/runner/wari-agents-runner.jar"
