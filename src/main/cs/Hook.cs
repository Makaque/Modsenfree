using System;
using System.Reflection;
using System.IO;
// using System.Linq;
using Harmony;
using System.Collections.Generic;

namespace HookNamespace
{


    public static class Field
    {
        public const string enabled = "enabled";
        public const string identifier = "id";
        public const string assembly = "assemblyName";
        public const string hookMethod = "hookMethod";
    }

    public class Pair<T,U> {
        readonly public T left;
        readonly public U right;

        public Pair(T left, U right){
            this.left = left;
            this.right = right;
        }
    }

    public class FileContents
    {
        readonly public string file;
        readonly public string contents;

        public FileContents(string file, string contents)
        {
            this.file = file;
            this.contents = contents;
        }
    }

    public static class Hook
    {
        const string modsDirectory = "./Mods";
        const string modSettingsFilename = "mod.json";

        public static void log(string msg){
            Console.WriteLine(msg);
            string content = "";
            if (File.Exists("./Logsenfree.txt")) {
                content = File.ReadAllText("./Logsenfree.txt");
            }
            File.WriteAllText("./Logsenfree.txt", content + "\n" + msg);
        }

        public static Assembly getModAssembly(FileContents fileContents)
        {
            DataHelper helper = new DataHelper(fileContents.contents);
            string assemblyName = helper.GetValue<string>(Field.assembly, "");
            if(assemblyName != null){
                string relativeFile = Path.GetDirectoryName(fileContents.file) + Path.DirectorySeparatorChar + assemblyName;
                string absoluteFile = Path.GetFullPath(relativeFile);
                return Assembly.LoadFile(absoluteFile);
            }
            return null;
        }

        public static string getModIdentifier(String settingsJson)
        {
            DataHelper dataHelper = new DataHelper(settingsJson);
            return dataHelper.GetValue<String>(Field.identifier, "");
        }

        public static bool isModEnabled(string settingsJson)
        {
            DataHelper dataHelper = new DataHelper(settingsJson);
            return dataHelper.GetValue<bool>(Field.enabled, false);
        }

        public static List<string> getDirFiles(string[] dirs){
            List<string> files = new List<string>();
            foreach (var dir in dirs)
            {
                files.AddRange(Directory.GetFiles(dir));
            }
            return files;
        }

        public static List<string> filterSettingsFiles(List<string> files){
            List<string> settingsFiles = new List<string>();
            foreach (var file in files)
            {
                if(Path.GetFileName(file) == modSettingsFilename){
                    settingsFiles.Add(file);
                }
            }
            return settingsFiles;
        }

        public static List<FileContents> getFileContents(List<string> files){
            List<FileContents> fileContents = new List<FileContents>();
            foreach (var file in files)
            {
                fileContents.Add(new FileContents(file, File.ReadAllText(file)));
            }
            return fileContents;
        }

        public static List<FileContents> filterEnabled(List<FileContents> filesContentsss){
            List<FileContents> fileContents = new List<FileContents>();
            foreach (var fc in filesContentsss)
            {
                if(Hook.isModEnabled(fc.contents)){
                    fileContents.Add(fc);
                }
            }
            return fileContents;
        }

        public static List<Pair<FileContents,Assembly>> getAssemblies(List<FileContents> fileContents){
            List<Pair<FileContents,Assembly>> assemblies = new List<Pair<FileContents,Assembly>>();
            foreach (var fc in fileContents)
            {
                Assembly asm = Hook.getModAssembly(fc);
            // Super important. Code implodes if you try to compare asm to null directly.
                if(!((object) asm).Equals(null)){
                    assemblies.Add(new Pair<FileContents, Assembly>(fc, asm));
                }
            }
            return assemblies;
        }

        public static List<Pair<FileContents,Assembly>> getMods()
        {
            string[] dirs = Directory.GetDirectories(modsDirectory);
            List<string> files = Hook.getDirFiles(dirs);
            List<string> settingsFiles = Hook.filterSettingsFiles(files);
            List<FileContents> fileContents = Hook.getFileContents(settingsFiles);
            List<FileContents> enabled = Hook.filterEnabled(fileContents);
            List<Pair<FileContents,Assembly>> assemblies = Hook.getAssemblies(enabled);
            return assemblies;
            // Func<string,string> bla = (a) => "hi";
                // .SelectMany(Directory.GetFiles);
                // .Where(file => Path.GetFileName(file) == modSettingsFilename);
                // .Select(file => new FileContents(file, File.ReadAllText(file)))
                // .Where(fileContents => Hook.isModEnabled(fileContents.contents))
                // .Select(fileContents => Hook.getModAssembly(fileContents))
                // .Where(asm => asm != null)
                // .ToList();
        }

        public static void hookToInject()
        {
            try{
                // Current directory is Oxenfree top-level, not Assembly-CSharp.dll location
                // HarmonyInstance harmony = HarmonyInstance.Create("ca.mktsk.modsenfree.loader");
                foreach (var mod in Hook.getMods())
                {
                    try{
                        FileContents fileContents = mod.left;
                        Assembly asm = mod.right;
                        String modId = getModIdentifier(fileContents.contents);
                        if (!modId.Equals("")){
                            HarmonyInstance harmonyInstance = HarmonyInstance.Create(modId);
                            harmonyInstance.PatchAll(asm);
                        }

                    } catch (Exception e ){
                        log(e.StackTrace);
                    }
                }

            } catch (Exception e){
                log(e.StackTrace);
            }
        }
    }
}