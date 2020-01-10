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
            // File.WriteAllText("./Logsenfree.txt", msg);
        }

        public static Assembly getModAssembly(FileContents fileContents)
        {
            log("before data helper");
            DataHelper helper = new DataHelper(fileContents.contents);
            log("got data helper");
            string assemblyName = helper.GetValue<string>(Field.assembly, "");
            log("got assemblyName: " + assemblyName);
            if(assemblyName != null){
                string relativeFile = Path.GetDirectoryName(fileContents.file) + Path.DirectorySeparatorChar + assemblyName;
            log("got dirname");
                string absoluteFile = Path.GetFullPath(relativeFile);
            log("got full path");
                log("absolue file");
                log(absoluteFile);
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
            log("in getassemblies");
            List<Pair<FileContents,Assembly>> assemblies = new List<Pair<FileContents,Assembly>>();
            foreach (var fc in fileContents)
            {
            log("for fc");
                Assembly asm = Hook.getModAssembly(fc);
            log("got mod assembly");
            // Super important. Code implodes if you try to compare asm to null directly.
                if(!((object) asm).Equals(null)){
                    log("asm not null");
                    assemblies.Add(new Pair<FileContents, Assembly>(fc, asm));
                }
            }
            return assemblies;
        }

        public static List<Pair<FileContents,Assembly>> getMods()
        // public static void getMods()
        {
            // string[] dirs = Directory.GetDirectories(modsDirectory);
            // return 
            log("in getmods");
            string[] dirs = Directory.GetDirectories(modsDirectory);
            List<string> files = Hook.getDirFiles(dirs);
            List<string> settingsFiles = Hook.filterSettingsFiles(files);
            List<FileContents> fileContents = Hook.getFileContents(settingsFiles);
            List<FileContents> enabled = Hook.filterEnabled(fileContents);
            log("about to get assemblies");
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
            log("call getmods");
            // Hook.getMods();
            // Assembly asm = Assembly.LoadFrom(@"C:\Program Files (x86)\Steam\steamapps\common\Oxenfree\Mods\testmod\TestMod.dll");
            // harmony.PatchAll(Assembly.GetExecutingAssembly());
            foreach (var mod in Hook.getMods())
            {
                log("mod");
                try{
                    FileContents fileContents = mod.left;
                    Assembly asm = mod.right;
                    log("Contents = "+ fileContents.contents);
                    String modId = getModIdentifier(fileContents.contents);
                    log("ModId = "+ modId);
                    if (!modId.Equals("")){
                        HarmonyInstance harmonyInstance = HarmonyInstance.Create(modId);
                        harmonyInstance.PatchAll(asm);
                    }

                } catch (Exception e ){
                    log(e.StackTrace);
                }
            }
            log("Success");

            } catch (Exception e){
                log(e.StackTrace);
            }
            // try
            // {
            //     // Console.WriteLine(Hook.getMods().ToString());
            //     throw new Exception("blowing up");
            // }
            // catch (Exception e)
            // {
            //     System.IO.File.WriteAllText(@"./Logsenfree.txt", e.StackTrace);

            // }
        }
    }
}