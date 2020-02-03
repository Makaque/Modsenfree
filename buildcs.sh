#!/usr/bin/env bash
MANAGED="/c/Program Files (x86)/Steam/steamapps/common/Oxenfree/Oxenfree_Data/Managed/"
SYSTEM="$MANAGED/System.Core.dll"
MONO35=./Libraries/Mono/lib/mono/3.5-api/Microsoft.Build.Engine.dll,./Libraries/Mono/lib/mono/3.5-api/Microsoft.Build.Framework.dll,./Libraries/Mono/lib/mono/3.5-api/Microsoft.Build.Tasks.v3.5.dll,./Libraries/Mono/lib/mono/3.5-api/Microsoft.Build.Utilities.v3.5.dll
RESOURCES="./resources"
HOOK_OUTPUT="./target/modsenfree/hook"
PATCHER_OUTPUT="./target/modsenfree/patcher"
RUN_OUTPUT="./target/scala-2.13"
SRC="./src/main/cs"

mkdir -p $HOOK_OUTPUT
mkdir -p $PATCHER_OUTPUT
mkdir -p $RUN_OUTPUT
./Libraries/Mono/bin/mcs $SRC/Patcher.cs -out:$PATCHER_OUTPUT/Patcher.exe -r:$MONO35,$RESOURCES/UnityEngine.dll,$RESOURCES/Mono.Cecil.dll,$RESOURCES/Mono.Cecil.Inject.dll
./Libraries/Mono/bin/mcs -langversion:ISO-2 $SRC/Hook.cs -out:$HOOK_OUTPUT/Hook.dll -t:library -r:$MONO35,$RESOURCES/0Harmony.dll,$RESOURCES/Assembly-CSharp.dll
cp $RESOURCES/Mono.Cecil.dll $PATCHER_OUTPUT/
cp $RESOURCES/Mono.Cecil.Inject.dll $PATCHER_OUTPUT/
#cp $RESOURCES/UnityEngine.dll $OUTPUT/

TESTMODDIR="./target/modsenfree/Mods/testmod"
mkdir -p $TESTMODDIR
./Libraries/Mono/bin/mcs $SRC/testmod/TestMod.cs -out:$TESTMODDIR/TestMod.dll -t:library -r:$MONO35,$RESOURCES/0Harmony.dll,$RESOURCES/Assembly-CSharp.dll,$RESOURCES/UnityEngine.dll,$RESOURCES/UnityEngine.UI.dll
cp $SRC/testmod/mod.json $TESTMODDIR


cp -r $PATCHER_OUTPUT $RUN_OUTPUT
cp -r $HOOK_OUTPUT $RUN_OUTPUT
cp -r "./target/modsenfree/Mods" $RUN_OUTPUT
