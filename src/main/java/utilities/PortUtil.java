package utilities;

import java.io.IOException;
import java.net.ServerSocket;

public class PortUtil {
    public static boolean isPortFree(Integer port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;   // port is free
        } catch (IOException e) {
            return false;  // port is in use
        }
    }

    public static void freePort(Integer port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            // just checking — cannot forcefully kill another process's port
        } catch (IOException e) {
            // port is in use — kill the process holding it
            killProcessOnPort(port);
        }
    }

    private static void killProcessOnPort(Integer port) {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win")) {
                // Windows
                // step 1 — find PID using the port
                Process find = Runtime.getRuntime().exec(
                        "cmd /c netstat -ano | findstr :" + port
                );
                String output = new String(find.getInputStream().readAllBytes());

                // step 2 — extract PID (last column)
                String pid = extractPid(output);

                if (pid != null) {
                    // step 3 — kill it
                    Runtime.getRuntime().exec("taskkill /PID " + pid + " /F");
                    System.out.println("Killed process " + pid + " on port " + port);
                }

            } else {
                // Linux / Mac
                Process find = Runtime.getRuntime().exec(
                        new String[]{"sh", "-c", "lsof -ti :" + port}
                );
                String pid = new String(find.getInputStream().readAllBytes()).trim();

                if (!pid.isEmpty()) {
                    Runtime.getRuntime().exec("kill -9 " + pid);
                    System.out.println("Killed process " + pid + " on port " + port);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to free port " + port, e);
        }
    }

    private static String extractPid(String netstatOutput) {
        // netstat output looks like:
        // TCP  0.0.0.0:8080  0.0.0.0:0  LISTENING  1234
        //                                              └── PID is last column
        for (String line : netstatOutput.split("\n")) {
            if (line.contains("LISTENING")) {
                String[] parts = line.trim().split("\\s+");
                return parts[parts.length - 1].trim();
            }
        }
        return null;
    }
}
