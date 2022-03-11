/*
 * Classname: TaskList
 * 
 * Version: 1.0
 *
 * Date: 18/03/2014
 * 
 * Copyright (c) Richard Thomas 2014
 * All rights reserved.
 */

package uk.ac.leeds.geog.gy13rjt.todo;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import com.esri.arcgis.carto.IDocumentInfo;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.system.IStatusBar;

/**
 * Main handler class for TO DO List Organizer.
 * 
 * Holds state and contents of task list in a linked list.
 * Regenerates associated ComboBox list from scratch when necessary.
 * This class is a Singleton: creates itself on first call to method getInstance()
 * so only one instance can ever exist.
 * 
 * @author Richard Thomas
 * @version 1.0, 18 March 2014
 */
public class TaskList {

	/**
	 * Header for task list in comment field of Map Properties
	 */
	private static final String commentHeader = "---- TODO Task List ----\r\n";
	
	/**
	 * Footer for task list in comment field of Map Properties
	 */
	private static final String commentFooter = "---- TODO Task List (End) ----\r\n";

	/**
	 * Handle for the (single) instance of this class
	 */
	private static TaskList taskListInstance = null;
	
	/**
	 * Handle for ArcMap application
	 */
	private IApplication arcApp;

	/**
	 * Handle for task list combo box instance
	 */
	private TaskListComboBox listComboBox;

	/**
	 * Is AddIn "New Task button" ready? 
	 */
	private boolean newTaskButtonPresent = false;
	
	/**
	 * Is Persistent Extension Addin present and ready?
	 */
	private boolean persistentPresent = false;
	
	/**
	 * Only allow ListComboBox to be enabled if all
	 * components of toolbar are ready
	 */
	private boolean enableListComboBox = false;

	/**
	 *  Task list stored as a linked list of task items (item = text + cookie)
	 */
	private static LinkedList<TaskItem> taskLList = null;

	
	//--------------------------------------------------------------------------
	// Initialisation Methods
	//--------------------------------------------------------------------------
	
	/*
	 * No Private constructor!
	 * Normally, for a singleton, there would be a private constructor so class
	 * can only create itself (via a call to static method getInstance()).
	 * However, as an ArcGIS extension constructor function never gets called
	 * (instead it calls a hidden TaskList.<init> function)
	/*
	//private void TaskList() {}

	/**
	 * Get handle for the (single) instance of this class.
	 * Create the instance if not already existing.
	 * 
	 * @return	handle for single instance of this class
	 */
	public static TaskList getInstance() {
		
		// Create an instance of this class if one doesn't already exist
		// Will only do so on 1st invocation of this method
		if (taskListInstance == null) {
			taskListInstance = new TaskList();
		}
		return taskListInstance;
	}
	
	/**
	 * Is everything ready for List ComboBox to be enabled?
	 * 
	 * @return	true if list can be enabled
	 */
	public boolean getEnableListComboBox() {
		return enableListComboBox;
	}
	
	/**
	 * Used only by DelTaskButton to pass a handle to Application during init
	 * 
	 * @param thisApp Handle for this ArcMap application
	 */
	public void setApplication(IApplication thisApp){
		arcApp = thisApp;
		
		// Kick off initialisations if all other AddIns are ready
		initIfReady();
	}
	
	/**
	 * Used only by TaskListComboBox to pass a handle to its (single) instance
	 * 
	 * @param comboInstance		handle for associated ComboBox
	 */
	public void SetComboBox(TaskListComboBox comboInstance) {
		if (comboInstance == null) {
			JOptionPane.showMessageDialog(null,
					"TaskList.SetComboBox(): comboInstance is null");
		}
		listComboBox = comboInstance;
		
		// Kick off initialisations if all other AddIns are ready
		initIfReady();
	}
	
	/**
	 * Used only by NewTaskButton to indicate it is present
	 */
	public void SetNewTaskButtonPresent() {
		newTaskButtonPresent = true;
		
		// Kick off initialisations if all other AddIns are ready
		initIfReady();
	}

	/**
	 * Used only by PersistentExtensionAddIn to indicate it is present
	 */
	public void SetPersistentPresent() {
		persistentPresent = true;
		
		// Kick off initialisations if all other AddIns are ready
		initIfReady();
	}
	
	/**
	 * Initialise everything once all AddIns are ready.
	 * Called successively as each AddIns becomes ready.
	 */
	private void initIfReady() {

		if (arcApp != null && listComboBox != null
				&& newTaskButtonPresent == true && persistentPresent == true) {

			// OK to enable task list ComboBox now
			enableListComboBox = true;
		}
	}

	/**
	 * Called in event of existing map document being opened
	 */
	public void openMapDocumentEvent() {
		
		// Reset the task list
		taskLList = new LinkedList<TaskItem>();
		
		// Retrieve task list (if it exists) from Map Doc Comments section
		getTasksFromComments();
		
		// Re-populate the ComboBox from the linked list
		refreshComboBox();
	}

	/**
	 * Called in event of new map document being created
	 */
	public void newMapDocumentEvent() {

		// Reset the task list
		taskLList = new LinkedList<TaskItem>();

		// Re-populate the ComboBox from the linked list
		refreshComboBox();
	}

	//--------------------------------------------------------------------------
	// Active operation methods
	//--------------------------------------------------------------------------

	/**
	 * Write message to status bar at bottom of ArcMap GUI
	 * 
	 * @param statusMessage		Text message to display
	 */
	private void updateStatusBar(String statusMessage) {
		if (arcApp == null) {
			JOptionPane.showMessageDialog(null,
					"TaskList.updateStatusBar(): arcApp is null");
		}
		try {
			IStatusBar statusBar  = arcApp.getStatusBar();
			statusBar.setMessage(0, statusMessage);
		} catch (Exception e) {
			
			// Quietly suppress on failure: no great loss
		}
	}
	
	/**
	 * 'X' (Del) GUI button (another addin) simply invokes this method
	 * when button is pressed. If a task is selected delete it.
	 * Otherwise just clear text box. For efficiency, this method will
	 * not regenerate the task list from scratch, but use the combo box
	 * method to remove it, then separately delete the task from the
	 * (more definitive) linked list.
	 */
	public void delButtonPressed() {
		
		// Only continue if all tools in toolbar present and ready
		if (enableListComboBox == false)
			return;
		
		// Linked List iterator for task list
		ListIterator<TaskItem> iterTask;

		// Cookie of task to be deleted (-1 is default for no selection)
		int targetTaskCookie = -1;
		
		// For diagnostics capture text of deleted task
		String deletedTask = null;
		
		// Get currently selected task in list (if any)
		if (listComboBox != null) {
			try {
				targetTaskCookie = listComboBox.getSelected();
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
			}

			if (targetTaskCookie != -1) {
				
				// Delete task item from ComboBox
				try {
					listComboBox.remove(targetTaskCookie);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}

				// Start iterating from start of task list
				iterTask = taskLList.listIterator(0);
			
				// Iterate through the task list
				while (iterTask.hasNext() == true) {
					
					// Get each task item in turn
					TaskItem currentItem = iterTask.next();
					
					// Delete task if its cookie matches the target
					if (currentItem.cookie == targetTaskCookie) {
						deletedTask = currentItem.taskText;
						iterTask.remove();
					};
				}
			}
			
			// Update copy of task list stored in "comments"
			writeTasksToComments();

			// Whether we deleted a task or just want to clear typed text
			// We now clear all text from edit box and reset ComboBox state variables 
			listComboBox.ReadyForNewTask();
			
			// Keep the user informed if we deleted a task (as edit box gets cleared)
			if (deletedTask != null) {
				updateStatusBar("[TODO List] Deleted task: \"" + deletedTask + "\"");
			} else {
				
				// Otherwise ensure we clear status bar
				updateStatusBar("");
			}
		}
	}	
	
	/**
	 * 'New' GUI button (another addin) simply invokes this method
	 * when button is pressed. This doesn't change the task list,
	 * but prepares the ComboBox text area for editing.
	 */
	public void newButtonPressed() {
		
		// Only continue if all tools in toolbar present and ready
		if (enableListComboBox == false)
			return;
		
		// Clear all text from edit box and reset ComboBox state variables 
		if (listComboBox != null) {
			listComboBox.ReadyForNewTask();
		}
		
		// *** DEBUG
		else {
			JOptionPane.showMessageDialog(null,
					"TaskList.newButtonPressed(): listComboBox is null");
		}

		// Keep the user informed (as edit box gets cleared)
		updateStatusBar("[TODO List] Ready for new task to be entered");
	}

	/**
	 * Modifies a selected task, ensuring it stays in the same place in the
	 * task list.
	 *  
	 * @param taskTextMod	Modified text
	 * @param cookieMod		Cookie value returned by ComboBox
	 */
	public void modifyTask(String taskTextMod, int cookieMod) {
		
		// Only continue if all tools in toolbar present and ready
		if (enableListComboBox == false)
			return;
		
		// Get a list-iterator of the elements in this list (in proper sequence),
		// starting at the first item 
		ListIterator<TaskItem> iterTask = taskLList.listIterator(0);
		
		// Search through linked list for item with same cookie value
		while (iterTask.hasNext() == true) {
			TaskItem currentItem = iterTask.next();
			if (currentItem.cookie == cookieMod) {
				
				// Update matching item with modified text
				iterTask.set(new TaskItem(taskTextMod));
			};
		}

		// Re-populate the ComboBox from the array and get new cookie values
		// and ensure edited task item stays in the same place
		refreshComboBox();
		
		// Keep the user informed (as edit box gets cleared)
		updateStatusBar("[TODO List] Modified task: \"" + taskTextMod + "\"");
	}

	/**
	 * Task added directly by associated ComboBox object
	 * 
	 * @param newTaskText	Text of new task item
	 * @param cookie		ComboBox number associated with task
	 */
	public void addTask(String newTaskText, int cookie) {

		// Only continue if all tools in toolbar present and ready
		if (enableListComboBox == false)
			return;
		
		// Add new task to end of linked list
		taskLList.addLast(new TaskItem(newTaskText, cookie));
		
		// Update copy of task list stored in "comments"
		writeTasksToComments();
		
		// Keep the user informed (as edit box gets cleared)
		updateStatusBar("[TODO List] Added task: \"" + newTaskText + "\"");
	}
	
	/*
	 * Rebuild ComboBox from the linked list, storing the associated new cookie
	 * values the ComboBox returns. 
	 */
	private void refreshComboBox() {

		// Only continue if all tools in toolbar present and ready
		if (enableListComboBox == false)
			return;
		
		ListIterator<TaskItem> iterTask;	// Linked List iterator for task list
		int taskCookie = -1;				// Cookie associated with current task
		

		// Clear the entire ComboBox list
		try {
			listComboBox.clear();
		}
		catch (Exception e){
			// Quietly suppress any exception as not end of the world
			// (Don't expect this to ever happen anyway)
		}
			
		// Start iterating from start of task list
		iterTask = taskLList.listIterator(0);

		// Iterate through the task list
		while (iterTask.hasNext() == true) {
			
			// Get each task item in turn
			TaskItem currentItem = iterTask.next();
			
			// Add current task item to end of ComboBox list
			try {
				taskCookie = listComboBox.add(currentItem.taskText);
			} catch (Exception e){
				JOptionPane.showMessageDialog(null, e.getMessage());
			}	
			
			// Store cookie returned by ComboBox with associated task
			iterTask.set(new TaskItem(currentItem.taskText, taskCookie));
		}
		
		// Also update copy of task list stored in "comments"
		writeTasksToComments();
	}
	
	/**
	 * Write Task List to Map Document "Comments"
	 */
	public void writeTasksToComments() {

		ListIterator<TaskItem> iterTask;	// Linked List iterator for task list
		String taskListTxt = commentHeader;	// Start with a header (title)
		IDocumentInfo docInfo = null;

		try {
			docInfo = (IDocumentInfo) arcApp.getDocument();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}

		// Start iterating from start of task list
		iterTask = taskLList.listIterator(0);

		// Iterate through the task list
		while (iterTask.hasNext() == true) {

			// Get each task item in turn
			TaskItem currentItem = iterTask.next();

			taskListTxt = taskListTxt + currentItem.taskText + "\r\n";
		}
		
		// Mark end of tasks with a footer
		taskListTxt = taskListTxt + commentFooter;

		try {
			docInfo.setComments(taskListTxt);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
	}
	
	/**
	 * Load task list (if present) from map document "Comments" section
	 */
	private void getTasksFromComments() {

		String docComments = "";
		IDocumentInfo docInfo = null;
		try {
			docInfo = (IDocumentInfo) arcApp.getDocument();

			if (docInfo != null) {
				docComments = docInfo.getComments();
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}


		// Find position of start of task list header (if it exists)
		int startOfHeader = docComments.indexOf(commentHeader);

		// (Only if header exists) strip all text up to end of header
		if (startOfHeader != -1) {
			String toppedComments = docComments.substring(
					startOfHeader + commentHeader.length());

			// Loop through each line stored in the remaining string
			for (String singleLine: toppedComments.split("\r\n")) {

				// Trim end of line character(s)
				String trimmedLine = singleLine.trim();

				// If we reach the footer, no more tasks to read in
				if (trimmedLine.equals(commentFooter.trim()) == true)
					break;

				// Add new task to end of linked list
				taskLList.addLast(new TaskItem(trimmedLine));
			}
		}
	}

}
