// using UnityEngine;
using System;
using System.IO;
using System.Linq;
using Mono.Cecil;
using Mono.Cecil.Inject;

public class TooFewArgumentsException : Exception { }
public class AssemblyReadException : Exception { }

public enum Response
{
    PATCH_SUCCESS,
    UNPATCH_SUCCESS,
    IS_PATCHED_TRUE,
    IS_PATCHED_FALSE,
    ERROR,
    MISSING_ASSEMBLY_ERROR,
    ASSEMBLY_READ_ERROR,
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

public class PatchMethodLocation
{
    public readonly string assemblyFilename;
    public readonly string className;
    public readonly string methodName;


    public PatchMethodLocation(string assemblyFilename, string className, string methodName)
    {
        this.assemblyFilename = assemblyFilename;
        this.className = className;
        this.methodName = methodName;
    }

    public PatchMethodDefinition patchMethodDefinition()
    {
        // try
        // {
            var resolver = new DefaultAssemblyResolver();
            resolver.AddSearchDirectory("./resources/");
            AssemblyDefinition assemblyDefn = AssemblyDefinition.ReadAssembly(assemblyFilename, new ReaderParameters { AssemblyResolver = resolver });
            TypeDefinition classDefn = assemblyDefn.MainModule.GetType(className);
            MethodDefinition methodDefn = classDefn.GetMethod(methodName);
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

    CmdArgs(Command command, PatchMethodLocation gamePatchLocation, PatchMethodLocation patchToInjectLocation)
    {
        this.command = command;
        this.gamePatchLocation = gamePatchLocation;
        this.patchToInjectLocation = patchToInjectLocation;
    }

    public static CmdArgs parse(string[] args)
    {
        if (args.Length < 7)
        {
            throw new TooFewArgumentsException();
        }
        Command command = (Command)Enum.Parse(typeof(Command), args[0]);
        string gameAssemblyFilename = args[1];
        string gameClassInjectionSite = args[2];
        string gameMethodInjectionSite = args[3];
        string patchAssemblyFilename = args[4];
        string patchClass = args[5];
        string patchMethod = args[6];
        if (!File.Exists(gameAssemblyFilename)){
            throw new Exception("game not exists");
        }
        if(!File.Exists(patchAssemblyFilename)){
            throw new Exception("patch not found");
        }
        Console.WriteLine(File.Exists(patchAssemblyFilename) ? "patch exists." : "patch does not exist.");
        PatchMethodLocation gamePatchLocation = new PatchMethodLocation(gameAssemblyFilename, gameClassInjectionSite, gameMethodInjectionSite);
        PatchMethodLocation patchToInjectLocation = new PatchMethodLocation(patchAssemblyFilename, patchClass, patchMethod);
        return new CmdArgs(command, gamePatchLocation, patchToInjectLocation);
    }

}

public class Patcher
{
    public static Response patch(CmdArgs cmdArgs)
    {
        // try
        // {
            // new InjectionDefinition(
            //     cmdArgs.gamePatchLocation.methodDefinition(),
            //     cmdArgs.patchToInjectLocation.methodDefinition(),
            //     InjectFlags.None
            // ).Inject();
            PatchMethodDefinition gamePatchDefinition = cmdArgs.gamePatchLocation.patchMethodDefinition();
            PatchMethodDefinition patchPatchDefinition = cmdArgs.patchToInjectLocation.patchMethodDefinition();
            // gamePatchDefinition.methodDefinition.Body.GetILProcessor().InsertBefore(gamePatchDefinition.methodDefinition.Body.Instructions[0], Instruction.Create(OpCodes.Call, gamePatchDefinition.methodDefinition.Module.Import(patchPatchDefinition.methodDefinitiond)));
            gamePatchDefinition.methodDefinition.InjectWith(
                patchPatchDefinition.methodDefinition,
                flags: InjectFlags.None
            );
            gamePatchDefinition.assemblyDefinition.Write (Path.GetFileName(cmdArgs.gamePatchLocation.assemblyFilename));
            return Response.PATCH_SUCCESS;
        // }
        // catch (AssemblyReadException)
        // {
            // return Response.ASSEMBLY_READ_ERROR;
        // }
        // catch (Exception)
        // {
            // return Response.ERROR;
        // }
    }

    public static Response unpatch(CmdArgs cmdArgs)
    {
        return Response.UNIMPLEMENTED_ERROR;
    }

    public static Response isPatched(CmdArgs cmdArgs)
    {
        return Response.UNIMPLEMENTED_ERROR;
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

    public static Response exec(Command command, CmdArgs cmdArgs)
    {
        return Patcher.commandFunction(command)(cmdArgs);
    }

    public static void Main(string[] args)
    {
        // try
        // {
            CmdArgs cmdArgs = CmdArgs.parse(args);
            if (args.Length > 1)
            {
                Response response = exec(cmdArgs.command, cmdArgs);
                Console.Write(response);
            }
            else
            {
                Console.Write(Response.TOO_FEW_ARGUMENTS_ERROR);
            }
        // }
        // catch (Exception)
        // {
            // Console.Write(Response.ERROR);
        // }
    }
}