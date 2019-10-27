using UnityEngine;
using System;
using System.IO;
using Mono.Cecil;
using Mono.Cecil.Inject;

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

public class CmdArgs
{

    public readonly Command command;
    public readonly string assemblyFilename;

    CmdArgs(Command command, string assemblyFilename)
    {
        this.command = command;
        this.assemblyFilename = assemblyFilename;
    }

    public static CmdArgs parse(string[] args)
    {

        Command command = (Command)Enum.Parse(typeof(Command), args[0]);
        string assemblyFilename = args[1];
        return new CmdArgs(command, assemblyFilename);
    }

}

public class Patcher {
    public static Response patch(string assemblyFilename){
        String thisAssemblyFilename = System.Reflection.Assembly.GetCallingAssembly().Location;
        AssemblyDefinition gameAssembly = AssemblyDefinition.LoadAssembly(assemblyFilename);
        AssemblyDefinition patcherAssembly = AssemblyDefinition.LoadAssembly(thisAssemblyFilename);
        TypeDefinition gameClass = patcherAssembly.MainModule.GetType("MainCanvas");
        TypeDefinition patcherClass = patcherAssembly.MainModule.GetType("Patcher");
        MethodDefinition gameMethod = gameClass.Methods.find(m => m.Name = "Start");
        MethodDefinition patcherMethod = patcherClass.Methods.find(m => m.Name = "patchToInject");

        InjectionDefinition injector = InjectionDefinition(gameMethod, patcherMethod, InjectFlags.None);
        injector.Inject();

    }

    public static Response unpatch(string assemblyFilename){
        return Response.UNIMPLEMENTED_ERROR;
    }

    public static Response isPatched(string assemblyFilename){
        return Response.UNIMPLEMENTED_ERROR;
    }

    public static Response invalidCommand(string assemblyFilename){
        return Response.INVALID_COMMAND_ERROR;
    }

    public static Func<string,Response> commandFunction(Command command){
        switch (command){
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

    public static void patchToInject(){
        throw new Exception("patch successful");
    }
}

public class App
{

    public static Response exec(Command command, string assemblyFilename){
        return Patcher.commandFunction(command)(assemblyFilename);
    }

    public static void Main(string[] args)
    {
        try
        {
            CmdArgs cmdArgs = CmdArgs.parse(args);
            if (args.Length > 1)
            {
                Response response = exec(cmdArgs.command, cmdArgs.assemblyFilename);
                Console.Write(response);
            }
            else
            {
                Console.Write(Response.TOO_FEW_ARGUMENTS_ERROR);
            }
            //Console.WriteLine("Hello World");

        }
        catch (Exception)
        {
            Console.Write(Response.ERROR);
        }
    }
}