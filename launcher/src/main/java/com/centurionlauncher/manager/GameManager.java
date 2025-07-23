package com.centurionlauncher.manager;

import com.centurionlauncher.auth.AuthContext;
import com.centurionlauncher.model.Game;
import javafx.concurrent.Task;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration; // Import Duration
import java.util.Comparator;
import java.util.zip.ZipInputStream;

/**
 * Lớp dịch vụ để quản lý việc cài đặt, gỡ cài đặt và khởi chạy game.
 */
public class GameManager {

    private final Path gamesBaseDirectory;
    private final HttpClient httpClient;

    public GameManager() {
        this.gamesBaseDirectory = Paths.get(System.getProperty("user.home"), "CenturionGames");
        try {
            if (!Files.exists(gamesBaseDirectory)) {
                Files.createDirectories(gamesBaseDirectory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Khởi tạo HttpClient một lần để tái sử dụng
        this.httpClient = createTrustAllHttpClient();
    }

    private Path getGameInstallationPath(Game game) {
        String safeFolderName = game.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
        return gamesBaseDirectory.resolve(safeFolderName);
    }

    public Path getExecutablePath(Game game) {
        // Tên file thực thi nên được lấy từ model Game hoặc có logic cụ thể.
        if ("Yume Nikki".equals(game.getName())) {
            return getGameInstallationPath(game).resolve("yumenikki/RPG_RT.exe");
        } else if ("Age of Empires".equals(game.getName())) {
            return getGameInstallationPath(game).resolve("Empires.exe");
        }
        // Cung cấp một giá trị mặc định an toàn
        return getGameInstallationPath(game).resolve("launch.exe");
    }

    public boolean isGameInstalled(Game game) {
        return Files.exists(getExecutablePath(game));
    }

    public boolean uninstallGame(Game game) {
        Path gamePath = getGameInstallationPath(game);
        try {
            Files.walk(gamePath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Task<Void> createInstallTask(Game game) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                // --- Giai đoạn 0: Lấy URL tải về từ API ---
                updateMessage("Fetching download link for " + game.getName() + "...");
                String fileId = game.getGameUrl(); 
                if (fileId == null || fileId.isEmpty()) {
                    throw new IOException("No file ID");
                }

                String apiUrl = String.format("http://localhost:8080/request/file/download/%s", fileId);
                System.out.println(apiUrl);


                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Authorization", "Bearer " + AuthContext.getInstance().getToken())
                        .timeout(Duration.ofMinutes(1))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    throw new IOException("Cant fetch download link " + response.statusCode() + " - " + response.body());
                }
                String downloadUrl = response.body();
                System.out.println("Download link " + downloadUrl);

                Path targetZipPath = gamesBaseDirectory.resolve(game.getName() + ".zip");
                Path installPath = getGameInstallationPath(game);

                updateMessage("Installing");
                try (BufferedInputStream in = new BufferedInputStream(new URL(downloadUrl).openStream());
                        FileOutputStream fileOutputStream = new FileOutputStream(targetZipPath.toFile())) {

                    URL url = new URL(downloadUrl);
                    long fileSize = url.openConnection().getContentLengthLong();
                    byte[] dataBuffer = new byte[8192];
                    int bytesRead;
                    long totalBytesRead = 0;

                    while ((bytesRead = in.read(dataBuffer, 0, 8192)) != -1) {
                        if (isCancelled()) {
                            updateMessage("Cancelled");
                            return null;
                        }
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        totalBytesRead += bytesRead;

                        if (fileSize > 0) {
                            updateProgress(totalBytesRead, fileSize);
                            double percent = ((double) totalBytesRead / fileSize) * 100;
                            updateMessage(String.format("Downloading... %.1f%% (%d/%d MB)",
                                    percent,
                                    totalBytesRead / (1024 * 1024),
                                    fileSize / (1024 * 1024)));
                        } else {
                            updateProgress(-1, -1); 
                            updateMessage(String.format("Downloading... %d MB", totalBytesRead / (1024 * 1024)));
                        }
                    }
                }

                updateMessage("Extracting...");
                updateProgress(-1, -1);

                if (!Files.exists(installPath)) {
                    Files.createDirectories(installPath);
                }

                try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(targetZipPath))) {
                    var zipEntry = zis.getNextEntry();
                    while (zipEntry != null) {
                        Path newPath = installPath.resolve(zipEntry.getName()).normalize();
                        if (!newPath.startsWith(installPath)) {
                            throw new IOException("Invalid zip entry");
                        }
                        if (zipEntry.isDirectory()) {
                            Files.createDirectories(newPath);
                        } else {
                            if (newPath.getParent() != null) {
                                Files.createDirectories(newPath.getParent());
                            }
                            Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        zipEntry = zis.getNextEntry();
                    }
                    zis.closeEntry();
                }

                updateMessage("Cleaning");
                Files.delete(targetZipPath);

                updateMessage("Installed");
                return null;
            }
        };
    }

    /**
     * Tạo một HttpClient bỏ qua kiểm tra chứng chỉ SSL.
     * Rất hữu ích khi làm việc với localhost qua HTTPS (chứng chỉ tự ký).
     * CẢNH BÁO: KHÔNG SỬ DỤNG TRONG MÔI TRƯỜNG PRODUCTION!
     */
    private static HttpClient createTrustAllHttpClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            return HttpClient.newBuilder().sslContext(sslContext).build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // Quay về HttpClient mặc định nếu có lỗi
            e.printStackTrace();
            return HttpClient.newHttpClient();
        }
    }
}
