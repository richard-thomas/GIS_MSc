/*
 * Classname: NewTaskButton
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

import com.esri.arcgis.addins.desktop.Button;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.interop.AutomationException;

/**
 * ArcMap AddIn 'New' (Task) GUI button (part of TODO List Toolbar).
 * (Will do nothing if either of other 2 items of toolbar are missing).
 * 
 * @author Richard Thomas
 * @version 1.0, 18 March 2014
 */
public class NewTaskButton extends Button {

	/**
	 * Handle for the (single) instance of associated TaskList class
	 */
	private TaskList taskListInstance = null;
	
	
	/**
	 * At startup, get TaskList instance (creating it if necessary) and let
	 * it know that 'New' button is present.
	 */
	@Override 
	public void init(IApplication arcApp){

		// Get handle for associated TaskList object
		taskListInstance = TaskList.getInstance();
		
		// Sanity check on Singleton creation (should never fail)
		if (taskListInstance == null) {
			JOptionPane.showMessageDialog(null,
					"NewTaskButton.init(): TaskList.getInstance() failed!");
		}
		
		// Let Task List know that 'New' button is present
		else {
			taskListInstance.SetNewTaskButtonPresent();
		}
	}

	/**
	 * Called when 'New' (New task) button is clicked.
	 * Simply passes event onto associated Task List object to handle.
	 * 
	 * @exception java.io.IOException if there are interop problems.
	 * @exception com.esri.arcgis.interop.AutomationException if the component throws an ArcObjects exception.
	 */
	@Override
	public void onClick() throws IOException, AutomationException {
		
		// Simply pass the event on to associated TaskList instance to handle
		if (taskListInstance != null) {
			taskListInstance.newButtonPressed();
		}
	}
}
