OrionPlot
=========

Introduction
------------

The purpose of this project is to facilitate the visualization, comprehension and dissemination of a particular set of bioinformatic data.

It's feature set includes

* Data Import/Export
* Customization of the false-color map used in visualization
* Image export of resultant visualization.

Dependencies
------------
OrionPlot utilizes several third-party libraries.  These include
* Guava (by Google); for making the impossible possible
* Batik (by Apache Software Foundation), for plotting, SVG support and image export
* Batik requires the following sub-dependencies
** xmlgraphics-commons
** commons-logging
** commons-io
** xalan
** xml-apis
** xml-apis-ext
** avalon-framework
** fop
* JSON  , because it's STILL not in the Java runtime
* MigLayout (by MiG Infocom), for making Swing sing


Setup
-----

Building OrionPlot from source requires the following tools

1. Java JDK 1.8
2. Gradle v2.2 or higher
3. An internet connection from which Gradle will download the third party libraries this project utilizes.

Once these are installed you can build Gradle from the command line by typing

> gradle fullJar

This project can also be be imported into eclipse, provided you first install the 'Gradle' plugin.



Using
-----

Build products can be found under bin/libs

GUI Application

java -jar ./Orion-Plot-<version>.jar bio.comp.orion.OrionLauncher

Command Line Tool

java -jar ./Orion-Plot-<version>.jar bio.comp.orion.OrionCommandLine <arguments>

Help is available for the command line tool by invoking the command above with the '-h' option

Design
------------

OrionPlot's design attempts to isolate data representation from user interface through the use of controller classes.  This is more commonly referred to as the MVP (Model-View-Presenter) design pattern.  

The main application window is referred to as an OrionFrame and contains within it an instance of OrionPlotPanel and a JTable containing the color index mapping.  These are the two primary user interface elements visible inside an OrionFrame.

User interface actions---like save, load and export image---are written as named inner classes of the OrionFrame.  These inner classes implement or extend javax.swing.Action, allowing them to be used with JButton, JMenuItem, et al.

Effort is made to avoid sub-classing Swing UI components wherever possible.  UI behavior is modified by implementing the appropriate interface and supplying instances of the resulting class to those UI elements.  Examples of these interfaces include:

* javax.swing.event.TableModelEvent
* javax.swing.event.TableModelListener
* javax.swing.table.TableCellRenderer
* javax.swing.table.TableModel


