using System;
using System.Reflection;
using System.IO;
using System.Linq;
using Harmony;
using System.Collections.Generic;

namespace HookNamespace
{

    public static class Field {
        public const string enabled = "enabled";
        public const string assembly = "assemblyName";
    }

    public class FileContents {
        readonly public string file;
        readonly public string contents;

        public FileContents(string file, string contents){
            this.file = file;
            this.contents = contents;
        }
    }

    public static class Hook
    {
        const string modsDirectory = "./Mods";
        const string modSettingsFilename = "mod.json";

        

        public static Assembly getModAssembly(FileContents fileContents)
        {
            string assemblyName = new DataHelper(fileContents.contents).GetValue<string>(Field.assembly, null);
            if(assemblyName != null){
                return Assembly.LoadFile(assemblyName);
            }
            return null;
        }

        public static bool isModEnabled(string settingsJson){
            DataHelper dataHelper = new DataHelper(settingsJson);
            return dataHelper.GetValue<bool>(Field.enabled, false);
        }

        public static List<Assembly> getMods()
        {
            return Directory.GetDirectories(modsDirectory)
                .SelectMany(dir => Directory.GetFiles(dir))
                .Where(file => Path.GetFileName(file) == modSettingsFilename)
                .Select(file => new FileContents(file, File.ReadAllText(file)))
                .Where(fileContents => Hook.isModEnabled(fileContents.contents))
                .Select(fileContents => Hook.getModAssembly(fileContents))
                .Where(asm => asm != null)
                .ToList();
        }

        public static void hookToInject()
        {
            // Current directory is Oxenfree top-level, not Assembly-CSharp.dll location
            var harmony = HarmonyInstance.Create("ca.mktsk.modsenfree.loader");
            getMods().ForEach(asm => harmony.PatchAll(asm));
            // harmony.PatchAll(Assembly.GetExecutingAssembly());
            // System.IO.File.WriteAllText("./test.txt", "text");

        }
    }
}