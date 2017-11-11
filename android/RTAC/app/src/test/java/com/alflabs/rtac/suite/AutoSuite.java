/*
 * Project: RTAC
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.rtac.suite;

import com.alflabs.annotations.LargeTest;
import com.alflabs.annotations.NonNull;
import com.google.common.reflect.ClassPath;
import org.bouncycastle.crypto.util.Pack;
import org.junit.Test;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AutoSuite extends Suite {

    /** Argument for {@link AutoSuite} which indicates which annotation to exclude on tests, e.g. {@link LargeTest}. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface SkipAnnotation {
        /**
         * @return the annotation to exclude on tests, e.g. {@link LargeTest}
         */
        Class<? extends Annotation> value();
    }

    /** Argument for {@link AutoSuite} which indicates which package prefix to check for tests. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface PackageRoot {
        /**
         * @return the package prefix to check for tests, e.g. "com.alflabs."
         */
        String value();
    }

    public AutoSuite(Class<?> klass) throws InitializationError, IOException {
        super(klass, getSuiteClasses(klass));
    }

    @NonNull
    public static Class<?>[] getSuiteClasses(Class<?> klass) throws IOException {
        List<Class<?>> collected = new ArrayList<>();

        Class<? extends Annotation> skipAnnotation = null;
        SkipAnnotation skipArg = klass.getAnnotation(SkipAnnotation.class);
        if (skipArg != null) {
            skipAnnotation = skipArg.value();
        }

        String packageRoot = "";
        PackageRoot pgkRootArg = klass.getAnnotation(PackageRoot.class);
        if (pgkRootArg != null) {
            packageRoot = pgkRootArg.value();
        }

        ClassLoader classLoader = klass.getClassLoader();
        System.out.println("@@ AutoSuite for " + klass.toString());
        System.out.println("@@ AutoSuite: ClassLoader " + classLoader.toString());
        if (skipAnnotation != null) {
            System.out.println("@@ AutoSuite: Exclude all tests with @" + skipAnnotation.getSimpleName());
        }
        System.out.println("@@ AutoSuite: All tests from package " + packageRoot);
        if (packageRoot.isEmpty()) {
            System.out.println("@@ ERROR: Please add a @PackageRoot annotation on " + klass.getSimpleName());
            return new Class[0];
        }

        ClassPath cp = ClassPath.from(classLoader);
        nextClass: for (ClassPath.ClassInfo classInfo : cp.getAllClasses()) {
            if (classInfo.getName().endsWith("Test")
                    && classInfo.getPackageName().startsWith("com.alflabs.")) { // TODO make annotation argument
                Class<?> loaded = classInfo.load();

                // Skip classes tagged as large tests.
                if (skipAnnotation != null && loaded.getAnnotation(skipAnnotation) != null) {
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
