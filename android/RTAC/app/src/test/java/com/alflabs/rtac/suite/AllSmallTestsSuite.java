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
import org.junit.runner.RunWith;

/**
 * Dynamic test suite that collects all test classes in "com.alflabs.**" that have at least one
 * method tagged @Test and where the class itself is not tagged @LargeTest.
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
 * <p/>
 * If you get an error with "LargeTest.class" not found in AS, try forcing a gradle sync. Often it fixes it.
 */
@RunWith(AutoSuite.class)
@AutoSuite.SkipAnnotation(LargeTest.class)
@AutoSuite.PackageRoot("com.alflabs.")
public class AllSmallTestsSuite { }
