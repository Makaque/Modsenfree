using System;
using System.Reflection;
using Harmony;

namespace HookNamespace
{
    public static class Hook
    {
        public static void hookToInject()
        {
            // Current directory is Oxenfree top-level, not Assembly-CSharp.dll location
            var harmony = HarmonyInstance.Create("ca.mktsk.modsenfree.loader");
			harmony.PatchAll(Assembly.GetExecutingAssembly());
            System.IO.File.WriteAllText(@"./test.txt", "text");
            
        }
    }
}