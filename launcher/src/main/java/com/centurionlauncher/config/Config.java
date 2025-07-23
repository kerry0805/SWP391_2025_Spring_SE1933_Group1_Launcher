// package com.centurionlauncher.config; 

// import io.github.cdimascio.dotenv.Dotenv;

// public class Config {

//     private static final Dotenv dotenv = Dotenv.load();

//     public static final String API_BASE_URL = dotenv.get("SPRING_API_URL");


//     private Config() {
//     }

//     static {
//         if (API_BASE_URL == null || API_BASE_URL.isEmpty()) {
//             System.err.println("SPRING_API_URL not in file .env!");
//         } else {
//             System.out.println("Call API");
//         }
//     }
// }
