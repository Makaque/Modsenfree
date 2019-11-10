#!/usr/bin/env bash
MANAGED="/c/Program Files (x86)/Steam/steamapps/common/Oxenfree/Oxenfree_Data/Managed/"
SYSTEM="$MANAGED/System.Core.dll"
MONO35=./Libraries/Mono/lib/mono/3.5-api/Microsoft.Build.Engine.dll,./Libraries/Mono/lib/mono/3.5-api/Microsoft.Build.Framework.dll,./Libraries/Mono/lib/mono/3.5-api/Microsoft.Build.Tasks.v3.5.dll,./Libraries/Mono/lib/mono/3.5-api/Microsoft.Build.Utilities.v3.5.dll

./Libraries/Mono/bin/mcs ./src/main/cs/Patcher.cs -r:$MONO35,./Resources/UnityEngine.dll,./Resources/Mono.Cecil.dll,./Resources/Mono.Cecil.Inject.dll
./Libraries/Mono/bin/mcs -langversion:ISO-2 ./src/main/cs/Hook.cs -t:library -r:$MONO35,./Resources/0Harmony.dll,./Resources/Assembly-CSharp.dll
./Libraries/Mono/bin/mcs ./src/main/cs/HookTest.cs -r:$MONO35,./Resources/0Harmony.dll,./Resources/Assembly-CSharp.dll,./src/main/cs/Hook.dll
./Libraries/Mono/bin/mcs ./src/main/cs/TestMod.cs -t:library -r:$MONO35,./Resources/0Harmony.dll,./Resources/Assembly-CSharp.dll,./Resources/UnityEngine.dll,./Resources/UnityEngine.UI.dll
#./Libraries/Mono/bin/mcs ./src/main/cs/testmod/*.cs -t:library -r:./Resources/0Harmony.dll,./Resources/Assembly-CSharp.dll,./Resources/UnityEngine.dll,./Resources/UnityEngine.UI.dll

