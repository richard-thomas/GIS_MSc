/*
 * Classname: TaskListComboBox
 * 
 * Version: 1.0
 *
 * Date: 18/03/2014
 * 
 * Copyright (c) Richard Thomas 2014
 * All rights reserved.
 */

package uk.ac.leeds.geog.gy13rjt.todo;

import java.io.IOException;

import javax.swing.JOptionPane;

import com.esri.arcgis.addins.desktop.ComboBox;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.systemUI.IComboBoxHook;

/**
 * Drop-down list of tasks  (part of TODO List toolbar).
 * (Will do nothing if either of other 2 items of toolbar are missing).
 * 
 * @author Richard Thomas
 * @version 1.0, 18 March 2014
 */
public class TaskListComboBox extends ComboBox {

	/**
	 * Handle for the (single) instance of this class
	 */
	private static TaskListComboBox taskListComboBoxInstance = null;
	
	/**
	 * Handle for the (single) instance of associated TaskList class
	 */
	private TaskList taskListInstance = null;
	
	/**
	 * Is an item in task list drop-down currently selected?
	 */
	private boolean taskSelected = false;
	
	/**
	 * Identifying 'cookie' for currently selected task
	 */
	private int selectedTaskID = 0;
	
	/**
	 * Current value of text in edit box
	 */
	private String typedText = "";

	/**
	 * Disable list box until all other components of toolbar are ready
	 */
	@Override
	public boolean isEnabled() {
		if (taskListInstance == null) {
			return false;
		} else {
			return taskListInstance.getEnableListComboBox();
		}
	}

	/**
	 * Static function that returns handle for the (single) instance
	 * 
	 * @return handle for this instance
	 */
	public static TaskListComboBox getInstance() {
		if (taskListComboBoxInstance == null) {
			JOptionPane.showMessageDialog(null,
					"TaskListComboBox.getInstance(): taskListComboBoxInstance is null");
		}
		return taskListComboBoxInstance;
	}
		
	/**
	 * Called when the combo box is initialised. Subclasses must implement this method
	 * to add entries to the combo box using the add() method.
	 * 
	 * @exception java.io.IOException if there are interop problems.
	 * @exception com.esri.arcgis.interop.AutomationException if the component throws an ArcObjects exception.
	 */
	@Override
	public void initialize() {
		
		// Set status variables to indicate no selected task
		taskSelected = false;

		// Get handle for associated TaskList object
		taskListInstance = TaskList.getInstance();
		if (taskListInstance == null) {
			// **** DEBUG: should never happen
			JOptionPane.showMessageDialog(null,
					"TaskListComboBox.initialize(): taskListInstance is null");
		}
		
		taskListInstance.SetComboBox(this);

		taskListComboBoxInstance = this;
		if (taskListComboBoxInstance == null) {
			JOptionPane.showMessageDialog(null,
					"TaskListComboBox constructor(): taskListComboBoxInstance is null");
		}
	}

	/**
	 * Called every time a single character is added in the edit box
	 * 
	 * @param editString the String typed into the edit box
	 * @exception java.io.IOException if there are interop problems.
	 * @exception com.esri.arcgis.interop.AutomationException if the component throws an ArcObjects exception.
	 */
	@Override
	public void onEditChange(String editString) throws IOException,
			AutomationException {
		
		//Get typed (or just modified) text from ComboBox
		typedText = editString;
	}

	/**
	 * Called every time the enter key is pressed in the box.
	 * "a way for your user to let your business logic know that they have added a value."
	 */
	@Override
	public void onEnter() throws IOException, AutomationException {
		
		if (taskListInstance != null) {

			// If a task has already been selected from pull-down list
			// then modify this rather than add a new task.
			if (taskSelected == true) {
				taskListInstance.modifyTask(typedText, selectedTaskID);
			}

			else {

				// Add new task to list (and get cookie)
				int cookie;
				cookie = add(typedText);

				// Get (more permanent) Task List data updated
				taskListInstance.addTask(typedText, cookie);
			}

			// Clear all text from edit box and reset ComboBox state variables 
			ReadyForNewTask();
	}
	else {
		JOptionPane.showMessageDialog(null,
					"TaskListComboBox.onEnter(): taskListInstance is null");
		}
	}

	/**
	 * Called by system when the combo box gets or loses focus
	 * 
	 * @param setFocus - true when combo box gets focus, false when it loses focus
	 * @exception java.io.IOException if there are interop problems.
	 * @exception com.esri.arcgis.interop.AutomationException if the component throws an ArcObjects exception.
	 */
	@Override
	public void onFocus(boolean setFocus) throws IOException, AutomationException {
		// Do nothing
	}

	/**
	 * Called by system when a selection changes
	 * 
	 * @param cookie the item selected
	 * @exception java.io.IOException if there are interop problems.
	 * @exception com.esri.arcgis.interop.AutomationException if the component throws an ArcObjects exception.
	 */
	@Override
	public void onSelChange(int cookie) throws IOException, AutomationException {
		
		// Set status variables to indicate newly selected task
		taskSelected = true;
		
		// Store ID of newly selected list item
		selectedTaskID = cookie;

	}
	
	/**
	 * Set text in edit box
	 * 
	 * @param editBoxMsg	new edit box text
	 */
	private void setEditBox(String editBoxMsg) {
		try {
			IComboBoxHook comboHook = hook;
			
			if (comboHook != null) {
				comboHook.setValue(editBoxMsg);
			}
		}
		catch (Exception e){
	        // Quietly suppress any error: not a big problem if box uncleared
			// (Don't expect to ever happen)
		}
	}
	
	/*
	 * Clear all text from edit box and reset ComboBox state variables
	 * ready for a new task to be entered 
	 */
	public void ReadyForNewTask() {

		// Clear text in edit box (returns to greyed out hint)
		setEditBox("");

		// Clear any task selection previously made
		taskSelected = false;
	}
}

