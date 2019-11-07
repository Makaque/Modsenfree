#!/usr/bin/env bash

./Libraries/Mono/bin/mcs ./src/main/cs/Patcher.cs -r:./Resources/UnityEngine.dll,./Resources/Mono.Cecil.dll,./Resources/Mono.Cecil.Inject.dll
#./Libraries/Mono/bin/mcs ./src/main/cs/Hook.cs -t:library -r:./Resources/0Harmony.dll
#./Libraries/Mono/bin/mcs ./src/main/cs/TestMod.cs -t:library -r:./Resources/0Harmony.dll,./Resources/Assembly-CSharp.dll,./Resources/UnityEngine.dll,./Resources/UnityEngine.UI.dll
./Libraries/Mono/bin/mcs ./src/main/cs/testmod/*.cs -t:library -r:./Resources/0Harmony.dll,./Resources/Assembly-CSharp.dll,./Resources/UnityEngine.dll,./Resources/UnityEngine.UI.dll