package com.alflabs.rtac.suite;

import com.alflabs.annotations.LargeTest;
import com.alflabs.annotations.NonNull;
import com.google.common.reflect.ClassPath;
import org.junit.Test;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AutoSuite extends Suite {

    public AutoSuite(Class<?> klass) throws InitializationError, IOException {
        super(klass, getSuiteClasses(klass));
    }

    @NonNull
    public static Class<?>[] getSuiteClasses(Class<?> klass) throws IOException {
        List<Class<?>> collected = new ArrayList<>();

        ClassLoader classLoader = klass.getClassLoader();
        System.out.println("@@ AutoSuite: ClassLoader " + classLoader.toString());
        ClassPath cp = ClassPath.from(classLoader);
        nextClass: for (ClassPath.ClassInfo classInfo : cp.getAllClasses()) {
            if (classInfo.getName().endsWith("Test")
                    && classInfo.getPackageName().startsWith("com.alflabs.")) { // TODO make annotation argument
                Class<?> loaded = classInfo.load();

                // Skip classes tagged as large tests.
                // TODO: Add a @annotation argument to make this optional in the suite definition.s
                if (loaded.getAnnotation(LargeTest.class) != null) {
                    continue;
                }

                // Only accept classes that have at least one method tagged @Test
                for (Method method : loaded.getDeclaredMethods()) {
                    if (method.getAnnotation(Test.class) != null) {
                        // Found one. We can test this class.
                        collected.add(loaded);
                        continue nextClass;
                    }
                }
            }
        }

        System.out.println("@@ AutoSuite: Found " + collected.size() + " test classes.");
        System.out.println();
        return collected.toArray(new Class<?>[collected.size()]);
    }
}
