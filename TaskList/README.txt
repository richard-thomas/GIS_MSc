"TO DO" List Organizer AddIn for ArcMap
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Copyright (c) 2014 Richard Thomas and University of Leeds

The attached ESRI Addin
   TaskListOrg.esriaddin
will display a new AddIn named "ToDoListOrganizer".

This should automatically install itself and appear when ArcMap is fired up.
If not, simply select "TODO List Toolbar" from the "Customize"->"Addins" menu.

Full documentation is provided in associated Javadoc:
   http://richard-thomas.github.io/GIS_MSc/TaskList/JavaDoc/index.html

The source files are as follows:

   TaskList.java                 - main control
   DelTaskButton.java            - handles Del ('X') Addin GUI Button
   NewTaskButton.java            - handles New Task ('+') Addin GUI Button
   TaskListComboBox.java         - handles ComboBox Drop-down List Addin GUI 
   TaskItem.java                 - Basic class for each task item
   PersistentExtensionAddIn.java - handles openDoc and newDoc events

