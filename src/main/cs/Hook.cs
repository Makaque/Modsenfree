using System;
using System.Reflection;
using System.IO;
using System.Linq;
using Harmony;

namespace HookNamespace
{

    public static class Field {
        const string enabled = "enabled";
        const string assembly = "assemblyName";
    }

    public class FileContents {
        readonly string file;
        readonly string contents;

        public FileContents(string file, string contents){
            this.file = file;
            this.contents = contents;
        }
    }

    public static class Hook
    {
        string modsDirectory = "./Mods";
        string modSettingsFilename = "mod.json";

        

        public static Assembly getModAssembly(FileContents fileContents)
        {
            string assemblyName = new DataHelper(fileContents.contents).GetValue<string>(Field.assemblyName, null);
            if(assemblyName != null){
                return Assembly.LoadFile(assemblyName);
            }
            return null;
        }

        public static bool isModEnabled(string settingsJson){
            DataHelper dataHelper = new DataHelper(settingsJson);
            dataHelper.GetValue<bool>(Field.enabled, false);
        }

        public static List<Assembly> getMods()
        {
            return Directory.GetDirectories(modsDirectory)
                .SelectMany(dir => Directory.GetFiles(dir))
                .Where(file => Path.GetFileName(file) = modSettingsFilename)
                .Select(file => new FileContents(file, File.ReadAllText(file)))
                .Where(fileContents => Hook.isModEnabled(fileContents.contents))
                .Select(fileContents => Hook.getModAssembly(fileContents))
                .Where(asm => asm != null)
                .toList();
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