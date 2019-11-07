using System;
// using UnityEngine;

namespace HookNamespace
{
    public static class Hook
    {
        public static void hookToInject()
        {
            throw new Exception("patch successful");
            // Write hook's code here
        }
    }
}