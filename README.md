# Randall Layout Automation Source Repository

## What is it

This repository holds various projects used for the automation of the
HO-scale model train layout at the Randall Museum in San Francisco.

More info can be found here: http://ralf.alfray.com/trains/rtac

## Content

Here's a non-exhaustive list of projects or folders available here:

 * __Conductor__: A Java application that drives the train automation.
    It interprets an event-based script language and uses JMRI to
    get sensor data and act on throttles and turnouts.

    For more info, see __[jmri/conductor/README.md](./jmri/conductor/README.md)__

* __RTAC__: An Android application that connects to a Conductor server
    and displays the status of the automation as well as a layout map
    with the state of the block detectors.

    For more info, see __[android/RTAC/README.md](./android/RTAC/README.md)__

* __LibManifest__: A small utility Java library to share constants
    between the __Conductor__ Java application and the __RTAC__
    Android application.

    For more info, see __[android/RTAC/LibManifest/README.md](./android/RTAC/LibManifest/README.md)__

* __./docs__: An ongoing documentation effort for the Randall layout.

* __./arduino__ and __./pic16f__: Embedded software to reprogram or replace
    the NCE Button Boards used on the layout.

## Dependencies

__Conductor__ and __RTAC__ depend on 2 additional git submodules:
 * The [LibUtils](https://bitbucket.org/ralfoide/libutils) module.
 * A fork of the [AndroidSVG](https://github.com/ralfoide/androidsvg) project.

## License

Various projects use different licenses:
 * __Conductor__, __RTAC__, and __LibUtils__ are licensed under the __GNU GPL v3 license__.
 * __AndroidSVG__ is licensed under the __Apache license__.
 * Some arduino or pic16f files are licensed under the __MIT license__.
 * This list is not exhaustive. Consult each project as needed.
