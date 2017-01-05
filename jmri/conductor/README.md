To build this from the command-line:

$ ./gradlew test


To build and use this from IntelliJ using the Community Edition:

- Open Existing Project, select the "Conductor" directory.
- Create Run/Debug Configurations:
  - Junit > Name "All Tests"
    - Test kind: all in package.
    - Package: com.alflabs.conductor
    - Search for tests: in single module.
    - Workding dir: Set to ".../randall-layout/jmri/conductor"
    - Use classpath of module: conductor_test
    - JRE: 1.8
    - Before launch: Build.
  - Application > Name "Entry Point"
    - Main Class: com.alflabs.conductor.EntryPoint
    - Workding dir: Set to ".../randall-layout/jmri/conductor"
    - Use classpath of module: conductor_main
    - JRE: 1.8
    - Before launch: Build.

