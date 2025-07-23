package com.centurionlauncher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;


public class Main {
    public static void main(String[] args) {
        try {
            File logFile = new File(System.getProperty("user.home"), "centurion-launcher-log.txt");
            PrintStream fileOut = new PrintStream(logFile);
            System.setOut(fileOut);
            System.setErr(fileOut); 
            System.out.println("--- Centurion Launcher Log ---");
            System.out.println("Application starting via Main wrapper...");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }        try {
            System.out.println("Calling App.main(args)...");
            App.main(args);
            System.out.println("App.main(args) called successfully.");
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Exception caught in Main.main while calling App.main:");
            e.printStackTrace();
        }
    }
}
