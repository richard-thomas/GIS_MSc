TaskList: "TO DO" List Organizer AddIn for ESRI ArcMap Software
=======
Richard Thomas
18 March 2014

This ESRI Addin
   TaskListOrg.esriaddin
will display a new AddIn named "ToDoListOrganizer".

This should automatically install itself and appear when ArcMap is fired up.
If not, simply select "TODO List Toolbar" from the "Customize"->"Addins" menu.

Full documentation is provided in Javadoc HTML format from file:
   TaskListOrg\doc\index.html

The source files are as follows:

   TaskList.java                 - main control
   DelTaskButton.java            - handles Del ('X') Addin GUI Button
   NewTaskButton.java            - handles New Task ('+') Addin GUI Button
   TaskListComboBox.java         - handles ComboBox Drop-down List Addin GUI 
   TaskItem.java                 - Basic class for each task item
   PersistentExtensionAddIn.java - handles openDoc and newDoc events

