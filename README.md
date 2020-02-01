##Build instructions
Must have `sh` on the path to compile C# files from SBT. You can install git, MinGW-w64,
or Windows subsystems for Linux to get a Linux shell prompt on Windows, then add `sh` to
the path.

Must have Mono compiler and runtime in `modsenfree/Libraries/Mono/bin`

The following dependencies need to be in `modsenfree/resources`
- 0Harmony.dll
- Assembly-CSharp.dll
- Mono.Cecil.dll
- Mono.Cecil.Inject.dll
- UnityEngine.dll
- UnityEngine.UI.dll

Must have SBT 1.3.2

From SBT console:
`compile`

Compiled C# files will be in `modsenfree/target/cs`

To run, from SBT: 
`run`

## Manual Install
After building the project, move the `Hook.dll` file into the `Managed` directory of the Oxenfree install, normally 
located in `C:\Program Files (x86)\Steam\steamapps\common\Oxenfree\Oxenfree_Data\Managed` for the steam version.
