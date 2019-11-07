#!/usr/bin/env bash

./Libraries/Mono/bin/mcs ./src/main/cs/Patcher.cs -r:./Resources/UnityEngine.dll,./Resources/Mono.Cecil.dll,./Resources/Mono.Cecil.Inject.dll
./Libraries/Mono/bin/mcs ./src/main/cs/Hook.cs -t:library -r:./Resources/0Harmony.dll
