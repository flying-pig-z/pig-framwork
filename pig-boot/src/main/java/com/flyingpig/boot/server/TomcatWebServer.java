package com.flyingpig.boot.server;

import com.flyingpig.mvc.core.DispatcherServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.util.logging.LogManager;

public class TomcatWebServer implements WebServer, AutoCloseable {
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String TEMP_DIR_PREFIX = "embedded-tomcat-";

    private final Tomcat tomcat;
    private final int port;
    private final DispatcherServlet dispatcherServlet;
    private File tempDir;

    public TomcatWebServer(int port, DispatcherServlet dispatcherServlet) {
        silenceDefaultLogging();
        this.port = port;
        this.dispatcherServlet = dispatcherServlet;
        this.tomcat = new Tomcat();

        // 配置Tomcat属性
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir"));
        tomcat.setPort(port);
        tomcat.setSilent(true);
    }


    private void silenceDefaultLogging() {
        try {
            LogManager.getLogManager().reset();
            System.setProperty("org.apache.juli.ClassLoaderLogManager.level", "SEVERE");
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
        System.out.println(ANSI_GREEN + "成功启动Tomcat，服务端口为 " + port + ANSI_RESET);
    }

    @Override
    public void start() throws Exception {
        try {
            // 创建并配置临时目录
            this.tempDir = createTempDirectory();

            // 配置Context和Servlet
            Context context = tomcat.addContext("", tempDir.getAbsolutePath());
            Wrapper servletWrapper = tomcat.addServlet("", "dispatcherServlet", dispatcherServlet);
            servletWrapper.setLoadOnStartup(1);
            context.addServletMappingDecoded("/", "dispatcherServlet");

            // 启动服务器
            tomcat.start();
            tomcat.getConnector();
            System.out.println("Tomcat started on port: " + tomcat.getConnector().getLocalPort());

            // 打印启动信息
            printBanner();

            // 等待请求
            tomcat.getServer().await();
        } catch (Exception e) {
            // 如果启动过程中发生异常，确保资源被清理
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

        // 停止和销毁Tomcat
        try {
            if (tomcat != null) {
                try {
                    tomcat.stop();
                } catch (Exception e) {
                    firstException = e;
                }
                try {
                    tomcat.destroy();
                } catch (Exception e) {
                    if (firstException == null) {
                        firstException = e;
                    } else {
                        firstException.addSuppressed(e);
                    }
                }
            }
        } finally {
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