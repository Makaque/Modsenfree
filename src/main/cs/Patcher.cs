using UnityEngine;
using System;
using System.IO;
using Mono.Cecil;
using Mono.Cecil.Inject;

public class TooFewArgumentsException : Exception {}

public enum Response
{
    PATCH_SUCCESS,
    UNPATCH_SUCCESS,
    IS_PATCHED_TRUE,
    IS_PATCHED_FALSE,
    ERROR,
    MISSING_ASSEMBLY_ERROR,
    REPEAT_OPERATION_ERROR,
    INVALID_COMMAND_ERROR,
    TOO_FEW_ARGUMENTS_ERROR,
    UNIMPLEMENTED_ERROR
}

public enum Command
{
    PATCH, UNPATCH, IS_PATCHED
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

    public MethodDefinition methodDefinition()
    {
        AssemblyDefinition assemblyDefn = AssemblyDefinition.LoadAssembly(assemblyFilename);
        TypeDefinition classDefn = patchAssembly.MainModule.GetType(className);
        MethodDefinition methodDefn = gameClass.Methods.find(m => m.Name = methodName);
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
        if (args.Length < 7) {
            throw new TooFewArgumentsException();
        }
        Command command = (Command)Enum.Parse(typeof(Command), args[0]);
        string gameAssemblyFilename = args[1];
        string gameClassInjectionSite = args[2];
        string gameMethodInjectionSite = args[3];
        string patchAssemblyFilename = args[4];
        string patchClass = args[5];
        string patchMethod = args[6];
        PatchMethodLocation gamePatchLocation = new PatchMethodLocation(gameAssemblyFilename, gameClassInjectionSite, gameMethodInjectionSite);
        PatchMethodLocation patchToInjectLocation = new PatchMethodLocation(patchAssemblyFilename, patchClass, patchMethod);
        return new CmdArgs(command, gamePatchLocation, patchToInjectLocation);
    }

}

public class Patcher
{
    public static Response patch(CmdArgs cmdArgs)
    {
        InjectionDefinition(
            cmdArgs.gamePatchLocation.methodDefinition,
            cmdArgs.patchToInjectLocation.methodDefinition,
            InjectFlags.None
        ).Inject();
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

    public static void patchToInject()
    {
        throw new Exception("patch successful");
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
        try
        {
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
        }
        catch (Exception)
        {
            Console.Write(Response.ERROR);
        }
    }
}