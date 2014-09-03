/*
 * Classname: PersistentExtensionAddIn
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

import com.esri.arcgis.addins.desktop.Extension;
import com.esri.arcgis.arcmapui.IDocumentEventsAdapter;
import com.esri.arcgis.arcmapui.IDocumentEventsNewDocumentEvent;
import com.esri.arcgis.arcmapui.IDocumentEventsOpenDocumentEvent;
import com.esri.arcgis.arcmapui.IMxDocument;
import com.esri.arcgis.arcmapui.MxDocument;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.interop.AutomationException;

/**
 * Set up event handlers to catch creation of new map document
 * or opening of existing ones. Each of these must reset the
 * task list. With an existing map document, any task list already
 * embedded in the document will be retrieved.
 * 
 * @author Richard Thomas
 * @version 1.0, 18 March 2014
 */
public class PersistentExtensionAddIn extends Extension {

	/**
	 * Handle for the (single) instance of associated TaskList class
	 */
	private TaskList taskListInstance = null;
	
	/**
	 * Handle for a map ("*.mxd") document
	 */
	private IMxDocument mxDoc;


	/**
	 * Initializes this application extension with the ArcMap application instance it is hosted in.
	 * 
	 * This method is automatically called by the host ArcMap application.
	 * It marks the start of the dockable window's lifecycle.
	 * Clients must not call this method.
	 * 
	 * @param app is a reference to ArcMap's IApplication interface
	 * @exception java.io.IOException if there are interop problems.
	 * @exception com.esri.arcgis.interop.AutomationException if the component throws an ArcObjects exception.
	 */
	@SuppressWarnings("serial")
	@Override
	public void init(IApplication app) throws IOException, AutomationException {

		// Get handle for associated TaskList object
		taskListInstance = TaskList.getInstance();

		// Sanity check on Singleton creation (should never fail)
		if (taskListInstance == null) {
			JOptionPane.showMessageDialog(null,
					"PersistentExtensionAddIn.init(): TaskList.getInstance() failed!");
		}

		// Let Task List know that Persistent extension is present
		else {
			taskListInstance.SetPersistentPresent();
		}

		/* This segment based on a code stub for openDocument from:
		 * http://resources.arcgis.com/en/help/arcobjects-java/concepts/engine/index.html
		 * 
		 * Copyright 2012 ESRI
		 * 
		 * All rights reserved under the copyright laws of the United States
		 * and applicable international laws, treaties, and conventions.
		 */
		
		mxDoc = (IMxDocument) app.getDocument();

		// Set up a callback function (in TaskList) on opening of an existing Map Document
		((MxDocument) mxDoc).addIDocumentEventsListener(
				new IDocumentEventsAdapter() {

					@Override
					public void openDocument(IDocumentEventsOpenDocumentEvent arg0)
							throws IOException, AutomationException {
						// Insert handler code
						taskListInstance.openMapDocumentEvent();
					}
				} );
		
		// Set up a callback function (in TaskList) on opening of a new Map Document
		((MxDocument) mxDoc).addIDocumentEventsListener(
				new IDocumentEventsAdapter() {

					@Override
					public void newDocument(IDocumentEventsNewDocumentEvent arg0)
							throws IOException, AutomationException {
						// Insert handler code
						taskListInstance.newMapDocumentEvent();
					}
				} );

		//JOptionPane.showMessageDialog(null, "PersistentExtensionAddIn.init(): startup!");
	}

	@Override
	public void shutdown() {
		//JOptionPane.showMessageDialog(null, "PersistentExtensionAddIn.shutdown(): Goodbye!");	
	}

}
