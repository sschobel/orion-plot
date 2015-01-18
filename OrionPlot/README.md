OrionPlot
=========

Introduction
------------

The purpose of this project is to facilitate the visualization, comprehension and dissemination of a particular set of bioinformatic data.

It's feature set includes

* Data Import/Export
* Customization of the false-color map used in visualization
* Image export of resultant visualization.

Setup
-----

OrionPlot is offered in the form of an Eclipse project.

Using
-----

TODO

Design
------------

OrionPlot's design attempts to isolate data representation from user interface through the use of controller classes.  This is more commonly referred to as the MVC design pattern.  

The main application window is referred to as an OrionFrame and contains within it an instance of OrionPlotPanel and a JTable containing the color index mapping.  These are the two primary user interface elements visible inside an OrionFrame.

User interface actions---like save, load and export image---are written as named inner classes of the OrionFrame.  These inner classes implement or extend javax.swing.Action, allowing them to be used with JButton, JMenuItem, et al.

Effort is made to avoid sub-classing Swing UI components wherever possible.  UI behavior is modified by implementing the appropriate interface and supplying instances of the resulting class to those UI elements.  Examples of these interfaces include:

* javax.swing.event.TableModelEvent
* javax.swing.event.TableModelListener
* javax.swing.table.TableCellRenderer
* javax.swing.table.TableModel


