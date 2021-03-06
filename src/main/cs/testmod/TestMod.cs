using System;
using System.Reflection;
using Harmony;

namespace TestModNamespace
{
    [HarmonyPatch(typeof(Subtitle))]
    [HarmonyPatch("Show")]
    // [HarmonyPatch(new Type [] {typeof(string), typeof(bool)})]
    public class TestMod
    {
        static void Postfix(Subtitle __instance)
        {
            // Current directory is Oxenfree top-level, not Assembly-CSharp.dll location
            // var subtitle = Traverse.Create<Subtitle>().Method("MakeFoo").GetValue<Foo>();
            // Traverse.Create(foo).Property("myBar").Field("secret").SetValue("world");
            var str = __instance.text.text;
            __instance.text.text = "$$$ " + str; 
            System.IO.File.WriteAllText(@"./testmod.txt", "mod is working. " + str);
        }
    }
}