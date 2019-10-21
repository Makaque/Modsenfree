using UnityEngine;
using System;

enum Response {
    ERROR, PATCH_SUCCESS, RESPONDING
}

public class TestSharp {
    public static void Main (string[] args){
        if(args.Length > 0){
            Console.Write(Response.RESPONDING);
        } else {
            Console.Write(Response.ERROR);
        }
        //Console.WriteLine("Hello World");
    }
}