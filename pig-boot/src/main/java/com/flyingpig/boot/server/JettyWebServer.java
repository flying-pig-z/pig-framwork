package com.flyingpig.boot.server;

import com.flyingpig.mvc.core.DispatcherServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.File;
import java.util.logging.LogManager;

public class JettyWebServer implements WebServer, AutoCloseable {
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String TEMP_DIR_PREFIX = "embedded-jetty-";

    private final Server server;
    private final int port;
    private final DispatcherServlet dispatcherServlet;
    private File tempDir;

    public JettyWebServer(int port, DispatcherServlet dispatcherServlet) {
        silenceDefaultLogging();
        this.port = port;
        this.dispatcherServlet = dispatcherServlet;
        this.server = new Server();

        // 配置连接器
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);
    }

    private void silenceDefaultLogging() {
        try {
            LogManager.getLogManager().reset();
        } catch (Exception e) {
            // 忽略异常
        }
    }

    private void printBanner() {
        System.out.println(ANSI_GREEN +
                "  ____   _  _____ \n" +
                " |  _ \\ (_)/ ____|\n" +
                " | |_) | _| |  __ \n" +
                " |  __/ | | | |_ |\n" +
                " | |    | | |__| |\n" +
                " |_|    |_|\\_____|" + ANSI_RESET);
        System.out.println(" :: PIG Framework ::    (v1.0.0)");
        System.out.println(ANSI_GREEN + "成功启动Jetty，服务端口为 " + port + ANSI_RESET);
    }

    @Override
    public void start() throws Exception {
        try {
            // 创建并配置临时目录
            this.tempDir = createTempDirectory();

            // 配置 ServletContextHandler
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            context.setResourceBase(tempDir.getAbsolutePath());

            // 添加 DispatcherServlet
            ServletHolder servletHolder = new ServletHolder("dispatcherServlet", dispatcherServlet);
            context.addServlet(servletHolder, "/");

            server.setHandler(context);

            // 启动服务器
            server.start();

            // 打印启动信息
            printBanner();

            // 等待请求
            server.join();
        } catch (Exception e) {
            try {
                stop();
            } catch (Exception suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
    }

    private File createTempDirectory() throws RuntimeException {
        File dir = new File(System.getProperty("java.io.tmpdir"), TEMP_DIR_PREFIX + port);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("Failed to create temp directory " + dir.getAbsolutePath());
        }
        return dir;
    }

    @Override
    public void stop() throws Exception {
        Exception firstException = null;

        // 停止Jetty服务器
        try {
            if (server != null) {
                server.stop();
            }
        } catch (Exception e) {
            firstException = e;
        }

        // 清理临时目录
        try {
            cleanupTempDirectory();
        } catch (Exception e) {
            if (firstException == null) {
                firstException = e;
            } else {
                firstException.addSuppressed(e);
            }
        }

        if (firstException != null) {
            throw firstException;
        }
    }

    private void cleanupTempDirectory() {
        if (tempDir != null && tempDir.exists()) {
            deleteDirectory(tempDir);
        }
    }

    private void deleteDirectory(File directory) {
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    }
                    if (!file.delete() && file.exists()) {
                        file.deleteOnExit();
                    }
                }
            }
            if (!directory.delete() && directory.exists()) {
                directory.deleteOnExit();
            }
        } catch (Exception e) {
            System.err.println("Error while deleting directory: " + directory.getAbsolutePath());
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}