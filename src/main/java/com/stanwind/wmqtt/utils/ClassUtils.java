package com.stanwind.wmqtt.utils;

import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

public class ClassUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class.getSimpleName());

    /**
     * 获取指定anno class
     * @param packageName
     * @param annotationClass
     * @return
     */
    public static List<Class<?>> getClassList(String packageName, Class<? extends Annotation> annotationClass) {
        List<Class<?>> classList = getClassList(packageName);
        classList.removeIf(next -> !next.isAnnotationPresent(annotationClass));

        return classList;
    }

    /**
     * 获取包下class列表
     * @param packageName
     * @return
     */
    public static List<Class<?>> getClassList(String packageName) {
        List<Class<?>> classList = new LinkedList();
        try {

            ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resourcePatternResolver.getResources(packageName.replace(".", "/") + "/**/*.class");
            for (Resource resource : resources) {
                String url = resource.getURL().toString();
                String className = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
                doAddClass(classList, packageName + "." + className);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return classList;
    }

    /**
     * addClass
     * @param classList
     * @param packagePath
     * @param packageName
     */
    private static void addClass(List<Class<?>> classList, String packagePath, String packageName) {
        try {
            File[] files = new File(packagePath).listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory();
                }
            });
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    if (file.isFile()) {
                        String className = fileName.substring(0, fileName.lastIndexOf("."));
                        if (packageName != null) {
                            className = packageName + "." + className;
                        }
                        doAddClass(classList, className);
                    } else {
                        String subPackagePath = fileName;
                        if (packagePath != null) {
                            subPackagePath = packagePath + "/" + subPackagePath;
                        }
                        String subPackageName = fileName;
                        if (packageName != null) {
                            subPackageName = packageName + "." + subPackageName;
                        }
                        addClass(classList, subPackagePath, subPackageName);
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * doAddClass
     * @param classList
     * @param className
     */
    private static void doAddClass(List<Class<?>> classList, String className) {
        Class<?> cls = loadClass(className, false);
        classList.add(cls);
    }

    /**
     * loadClass
     * @param className
     * @param isInitialized
     * @return
     */
    public static Class<?> loadClass(String className, boolean isInitialized) {
        Class<?> cls;
        try {
            cls = Class.forName(className, isInitialized, getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return cls;
    }

    /**
     * 获取当前线程classLoader
     * @return
     */
    public static ClassLoader getClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

}
