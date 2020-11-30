using System;
using System.IO;
using System.Linq;
using Mono.Cecil;
using Mono.Cecil.Cil;
using Mono.Cecil.Inject;

public class TooFewArgumentsException : Exception { }
public class AssemblyReadException : Exception { }

public enum Response
{
    OP_SUCCESS, // Only used internally
    PATCH_SUCCESS,
    UNPATCH_SUCCESS,
    IS_PATCHED_TRUE,
    IS_PATCHED_FALSE,
    IS_PATCHED_CHECK_ERROR,
    ERROR,
    MISSING_GAME_ASSEMBLY_ERROR,
    MISSING_PATCH_ASSEMBLY_ERROR,
    MISSING_ASSEMBLY_ERROR,
    ASSEMBLY_READ_ERROR,
    GAME_ASSEMBLY_READ_ERROR,
    GAME_ASSEMBLY_BACKUP_ERROR,
    PATCH_ASSEMBLY_READ_ERROR,
    INJECTION_ERROR,
    REPEAT_OPERATION_ERROR,
    INVALID_COMMAND_ERROR,
    TOO_FEW_ARGUMENTS_ERROR,
    UNIMPLEMENTED_ERROR
}

public enum Command
{
    PATCH, UNPATCH, IS_PATCHED
}

    public class PatchMethodDefinition{

        public readonly AssemblyDefinition assemblyDefinition;
        public readonly MethodDefinition methodDefinition;

        public PatchMethodDefinition(AssemblyDefinition assemblyDefinition, MethodDefinition methodDefinition){
            this.assemblyDefinition = assemblyDefinition;
            this.methodDefinition = methodDefinition;
        }
    }


// Class to act as a container for information used to retrieve 
// AssemblyDefinition => TypeDefinition => MethodDefinition
// This simple class contains only the strings used to get the MethodDefinition object.
// Because of that, no exceptions are thrown during the construction of this object. 
public class PatchMethodLocation
{
    public readonly string assemblyFilename;
    public readonly string className;
    public readonly string methodName;
    public readonly int methodIndex;

    public readonly string resolver;


    public PatchMethodLocation(string assemblyFilename, string className, string methodName, int methodIndex = 0)
    {
        this.assemblyFilename = assemblyFilename;
        this.className = className;
        this.methodName = methodName;
        this.methodIndex = methodIndex;


    }

    // Method used to construct a PatchMethodDefinition. This creates and stores an AssemblyDefinition
    // and a MethodDeinition constructed from the fields of this class, if no error occurs. 
    public PatchMethodDefinition patchMethodDefinition(string resolverDirectory = null)
    {
        // try
        // {
            DefaultAssemblyResolver resolver = null;
            AssemblyDefinition assemblyDefn = null;
            if(resolverDirectory != null){
                resolver = new DefaultAssemblyResolver();
                resolver.AddSearchDirectory(resolverDirectory);
                assemblyDefn = AssemblyDefinition.ReadAssembly(assemblyFilename, new ReaderParameters { AssemblyResolver = resolver });
            } else {
                assemblyDefn = AssemblyDefinition.ReadAssembly(assemblyFilename);
            }
            TypeDefinition classDefn = assemblyDefn.MainModule.GetType(className);
            MethodDefinition methodDefn = classDefn.GetMethods(methodName)[methodIndex];
            return new PatchMethodDefinition(assemblyDefn, methodDefn);
        // }
        // catch (Exception)
        // {
            // throw new AssemblyReadException();
        // }
    }
}

public class CmdArgs
{

    public readonly Command command;
    public readonly PatchMethodLocation gamePatchLocation;
    public readonly PatchMethodLocation patchToInjectLocation;
    public readonly string gameAssemblyOutFilename;
    public readonly string gameResolver;
    public readonly string resolver; 

    CmdArgs(Command command, PatchMethodLocation gamePatchLocation, PatchMethodLocation patchToInjectLocation, string gameAssemblyOutFilename, string gameResolver, string resolver = null)
    {
        this.command = command;
        this.gamePatchLocation = gamePatchLocation;
        this.patchToInjectLocation = patchToInjectLocation;
        this.gameAssemblyOutFilename = gameAssemblyOutFilename;
        this.gameResolver = gameResolver;
        this.resolver = resolver;
    }


    public static (Response, CmdArgs) parse(string[] args)
    {


        Command command = (Command)Enum.Parse(typeof(Command), args[0]);

        if (args.Length < 7)
        {
            throw new TooFewArgumentsException();
        }
        Response response = Response.OP_SUCCESS;
        string gameAssemblyInFilename = args[1];
        string gameAssemblyOutFilename = args[2];
        string gameClassInjectionSite = args[3];
        string gameMethodNameInjectionSite = args[4];
        int gameMethodIndexInjectionSite = Int32.Parse(args[5]);
        string patchAssemblyFilename = args[6];
        string patchClass = args[7];
        string patchMethod = args[8];
        // TODO: Take game resolver as argument
        string gameResolver = new FileInfo(gameAssemblyInFilename).Directory.FullName;
        string resolver = null;
        if(args.Length > 8){
            resolver = args[8];
        }
        if (!File.Exists(gameAssemblyInFilename)){
            response = Response.MISSING_GAME_ASSEMBLY_ERROR;
        }
        if(!File.Exists(patchAssemblyFilename)){
            response = Response.MISSING_PATCH_ASSEMBLY_ERROR;
        }
        PatchMethodLocation gamePatchLocation = new PatchMethodLocation(gameAssemblyInFilename, gameClassInjectionSite, gameMethodNameInjectionSite, gameMethodIndexInjectionSite);
        PatchMethodLocation patchToInjectLocation = new PatchMethodLocation(patchAssemblyFilename, patchClass, patchMethod);
        return (response, new CmdArgs(command, gamePatchLocation, patchToInjectLocation, gameAssemblyOutFilename, gameResolver, resolver));
    }

}

public class Patcher
{
    public static Response patch(CmdArgs cmdArgs)
    {
        (Response response, PatchMethodDefinition gamePatchDefinition, PatchMethodDefinition patchPatchDefinition) = loadPatchDefinitions(cmdArgs);
        if(response != Response.OP_SUCCESS){
            return response;
        }

        try{
            String backupFileName = App.getBackupFileName(cmdArgs.gamePatchLocation.assemblyFilename);
            if(!File.Exists(backupFileName)){
                File.Copy(cmdArgs.gamePatchLocation.assemblyFilename, backupFileName);
            }
        } catch (Exception){
            return Response.GAME_ASSEMBLY_BACKUP_ERROR;
        }

        try{

            gamePatchDefinition.methodDefinition.InjectWith(
                patchPatchDefinition.methodDefinition,
                flags: InjectFlags.None
            );
            
            gamePatchDefinition.assemblyDefinition.Write (cmdArgs.gameAssemblyOutFilename);

        } catch (Exception){ 
            return Response.INJECTION_ERROR;
        }
        return Response.PATCH_SUCCESS;
    }

    private static (Response, PatchMethodDefinition, PatchMethodDefinition) loadPatchDefinitions(CmdArgs cmdArgs){
        PatchMethodDefinition gamePatchDefinition = null;
        PatchMethodDefinition patchPatchDefinition = null;

        try{
            gamePatchDefinition = cmdArgs.gamePatchLocation.patchMethodDefinition(cmdArgs.gameResolver);
        } catch (Exception){
            return (Response.GAME_ASSEMBLY_READ_ERROR, null, null);
        }

        try{
            patchPatchDefinition = cmdArgs.patchToInjectLocation.patchMethodDefinition(cmdArgs.resolver);
        } catch (Exception){
            return (Response.PATCH_ASSEMBLY_READ_ERROR, null, null);
        }

        return (Response.OP_SUCCESS, gamePatchDefinition, patchPatchDefinition);


    }

    public static Response unpatch(CmdArgs cmdArgs)
    {
        String backupFileName = App.getBackupFileName(cmdArgs.gamePatchLocation.assemblyFilename);
        File.Copy(backupFileName, cmdArgs.gamePatchLocation.assemblyFilename, true);
        return Response.UNPATCH_SUCCESS;
    }

    public static Response isPatched(CmdArgs cmdArgs)
    {
        (Response response, PatchMethodDefinition gamePatchDefinition, PatchMethodDefinition patchPatchDefinition) = loadPatchDefinitions(cmdArgs);
        if(response != Response.OP_SUCCESS){
            return response;
        }

        try{
            String hookName = patchPatchDefinition.methodDefinition.Name;
            foreach(Instruction instruction in gamePatchDefinition.methodDefinition.Body.Instructions){
                if(instruction.OpCode == OpCodes.Call){
                    MethodReference methodReference = instruction.Operand as MethodReference;
                    if(methodReference != null){
                        if(methodReference.Name == hookName){
                            return Response.IS_PATCHED_TRUE;
                        }
                    }
                }
                return Response.IS_PATCHED_FALSE;
            }
        } catch (Exception){ 
            return Response.INJECTION_ERROR;
        }
        return Response.PATCH_SUCCESS;
    }

    public static Response invalidCommand(CmdArgs cmdArgs)
    {
        return Response.INVALID_COMMAND_ERROR;
    }

    public static Func<CmdArgs, Response> commandFunction(Command command)
    {
        switch (command)
        {
            case Command.PATCH:
                return patch;
            case Command.UNPATCH:
                return unpatch;
            case Command.IS_PATCHED:
                return isPatched;
            default:
                return invalidCommand;
        }
    }

}

public class App
{

    public static String getBackupFileName(String fileName){
        return Path.GetDirectoryName(fileName)
            + Path.DirectorySeparatorChar
            + Path.GetFileNameWithoutExtension(fileName)
            + "-backup"
            + Path.GetExtension(fileName);
    }

    public static Response exec(Command command, CmdArgs cmdArgs)
    {
        return Patcher.commandFunction(command)(cmdArgs);
    }

    public static void Main(string[] args)
    {
        // try
        // {
            (Response parseResponse, CmdArgs cmdArgs) = CmdArgs.parse(args);
            if(parseResponse == Response.OP_SUCCESS){
                if (args.Length > 1)
                {
                    Response response = exec(cmdArgs.command, cmdArgs);
                    Console.Write(response);
                }
                else
                {
                    Console.Write(Response.TOO_FEW_ARGUMENTS_ERROR);
                }
            } else {
                Console.Write(parseResponse);
            }
        // }
        // catch (Exception)
        // {
            // Console.Write(Response.ERROR);
        // }
    }
}