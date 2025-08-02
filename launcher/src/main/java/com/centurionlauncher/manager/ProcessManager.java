package com.centurionlauncher.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessManager {
    private static Map<Long, Process> processes = new HashMap<>();
    public static void registerProcess(Process process, Long gameId) {
        processes.put(gameId, process);
    }

    public static void killAllProcesses() {
        for (Process process : processes.values()) {
            process.destroy();
        }
        processes.clear();
    }

    public static List<Process> getGameProcesses() {
        return new ArrayList<>(processes.values());
    }

    public static boolean isProcessRunning(Long gameId) {
        Process process = processes.get(gameId);
        return process != null && process.isAlive();
    }
}