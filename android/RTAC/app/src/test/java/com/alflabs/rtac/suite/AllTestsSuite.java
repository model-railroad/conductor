package com.alflabs.rtac.suite;

import org.junit.runner.RunWith;

/**
 * Dynamic test suite that collects all test classes in "com.alflabs.**" that have at least one
 * method tagged @Test, include those tagged with @LargeTest.
 * <p/>
 * To use this in Android Studio:
 * - Run > Edit Configurations > New Android JUnit configuration
 * - Name it All Small Tests Suite.
 * - Set as Single-Instance Only.
 * - Test Kind: Class
 * - Class: com.alflabs.rtac.suite.AllSmallTestsSuite
 * - Working Dir: MODULE_DIR
 * - Use classpath of module: app
 * - JRE: 1.8 (not Android)
 */
@RunWith(AutoSuite.class)
@AutoSuite.PackageRoot("com.alflabs.")
public class AllTestsSuite { }
