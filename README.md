##Build instructions
Must have `sh` on the path to compile C# files from SBT. You can install git, MinGW-w64,
or Windows subsystems for Linux to get a Linux shell prompt on Windows, then add `sh` to
the path.

Must have Mono compiler and runtime in `modsenfree/Libraries/Mono/bin`

The following dependencies need to be in `modsenfree/resources`:
- 0Harmony.dll
- Assembly-CSharp.dll
- Mono.Cecil.dll
- Mono.Cecil.Inject.dll
- UnityEngine.dll
- UnityEngine.UI.dll

Must have SBT 1.3.2

For a full compile with standalone jar, in an SBT console, run the following commands:
````
compile
assembly
````

Compiled C# files and executable jar will be in 
`modsenfree/target/modsenfree`. 
The structure of the `modsenfree` subdirectory within `target` is as follows:
````
-modsenfree
  -Modsenfree-Standalone_0.1.jar
  -hook
    -Hook.dll
  -patcher
    -Patcher.exe
    -Mono.Cecil.dll
    -Mono.Cecil.Inject.dll
````

## Manual Install
This section concerns only files within the `target` directory following
a full project build. `modsenfree` refers only to the subdirectory within 
`target`, not the root project directory.

After building the project, the `Hook.dll` file within `modsenfree/hook` should
be copied into the `Managed` subdirectory of the Oxenfree install, normally 
located in `C:\Program Files (x86)\Steam\steamapps\common\Oxenfree\Oxenfree_Data\Managed` 
for the steam version.

`modsenfree`, containing both `Modsenfree_Standalone_0.1.jar`, the `patcher` directory, and the `hook` directory,
should be kept anywhere on the system you wish for the install to reside. 

The program can then be run with:
````
java -jar /path/to/modsenfree/Modsenfree_standalone_0.1.jar
````