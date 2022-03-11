/*
 * Classname: TaskItem
 * 
 * Version: 1.0
 *
 * Date: 18/03/2014
 * 
 * Copyright (c) Richard Thomas 2014
 * All rights reserved.
 */

package uk.ac.leeds.geog.gy13rjt.todo;

/**
 * Holds one item from a task list.
 * 
 * @author Richard Thomas
 * @version 1.0, 18 March 2014
 */
public class TaskItem {

	/**
	 * Task description
	 */
	public String taskText = null;
	
	/**
	 * Numerical identifier arbitrarily assigned by ArcGIS ComboBox
	 */
	public int cookie = -1;
	
	
	// ---- Constructor Functions ----
	
	/**
	 * Flesh out both elements of class on creation
	 */
	public TaskItem(String string, int i) {
		taskText = string;
		cookie = i;
	}

	/**
	 * Flesh out only text on creation (do cookie later)
	 */
	public TaskItem(String string) {
		taskText = string;
		cookie = -1;
	}

	/**
	 * Only allocate storage if no arguments given
	 */
	public TaskItem() {
		// Leave default values
	}
}
