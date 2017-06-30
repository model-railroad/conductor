To build this from the command-line:

$ ./gradlew test


To build and use this from IntelliJ using the Community Edition:

- Open Existing Project, select the "Conductor" directory.
- File > Project Structure > Project > Project SDK: JRE or JDK 1.8.
- Create Run/Debug Configurations:
  - Junit > Name "All Tests"
    - Check Single Instance Only.
    - Test kind: all in package.
    - Package: com.alflabs.conductor
    - Search for tests: in single module.
    - Workding dir: Set to ".../randall-layout/jmri/conductor" (or $MODULE_DIR).
    - Use classpath of module: conductor_test.
    - JRE: 1.8.
    - Before launch: Build.
  - Application > Name "Entry Point"
    - Check Single Instance Only.
    - Main Class: com.alflabs.conductor.DevelopmentEntryPoint.
    - Workding dir: Set to ".../randall-layout/jmri/conductor".
    - Use classpath of module: conductor_main.
    - JRE: 1.8.
    - Before launch: Build.

Issue: On Windows with DPI scaling, the Java UI does not scale properly.
Solution from SO:
- For JRE 1.6, add -Dsun.java2d.dpiaware=false or -Dsun.java2d.uiScale=2.5
- For JRE 1.8, find the JRE/bin/java.exe > Properties > Compatibility > override dpi scaling. Meh.
