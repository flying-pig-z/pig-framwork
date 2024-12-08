package com.flyingpig.mvc.util;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageScanner {

    /**
     * 扫描包中的类
     */
    public static Set<Class<?>> scanPackageClasses(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            String path = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    scanDirectory(new File(resource.toURI()), basePackage, classes);
                } else if (resource.getProtocol().equals("jar")) {
                    scanJarFile(resource, basePackage, classes);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package: " + basePackage, e);
        }
        return classes;
    }

    public static void scanDirectory(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, packageName + "." + file.getName(), classes);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' +
                            file.getName().substring(0, file.getName().length() - 6);
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        }
    }

    private static void scanJarFile(URL jarUrl, String basePackage, Set<Class<?>> classes) {
        try {
            String jarPath = jarUrl.toString().substring(0, jarUrl.toString().indexOf("!"));
            try (JarFile jarFile = new JarFile(new URL(jarPath).getPath())) {
                String packagePath = basePackage.replace('.', '/');
                Enumeration<JarEntry> entries = jarFile.entries();

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                        String className = entryName.substring(0, entryName.length() - 6)
                                .replace('/', '.');
                        try {
                            classes.add(Class.forName(className));
                        } catch (ClassNotFoundException ignored) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error scanning JAR file: " + jarUrl, e);
        }
    }
}
