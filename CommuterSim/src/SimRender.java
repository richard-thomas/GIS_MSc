/*
 * Classname: SimRender
 * 
 * Version: 0.1
 *
 * Date: 06/05/2014
 * 
 * Copyright (c) Richard Thomas 2014
 * All rights reserved.
 */

package commuterSim;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * Rendering of 2D graphics (bar chart) for the Simulator tab.
 * 
 * @author Richard Thomas <gy13rjt@leeds.ac.uk>
 * @version 0.1, 06 May 2014
 */
public class SimRender extends JPanel{
		
	/**
	 * Car commuters at each location (today)
	 */
	private int carCommuters[] = new int[10];

	/**
	 * Bike commuters at each location (today)
	 */
	private int bikeCommuters[] = new int[10];

	private int locationCount = 1;
	
	public void drawBars(double[] idealCarCommuters, int locations, int totalPopulation) {
		locationCount = locations;
		for (int i = 0; i < locations; i++) {
			carCommuters[i] = (int) (idealCarCommuters[i] + 0.5);
			bikeCommuters[i] = totalPopulation/10 - carCommuters[i];
		}
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int canvasHeight = getHeight();
		int blockWidth = 10;
		
		// Render bar graphs of car(red) and bike(green) commuters for the
		// (10) different locations.
		for (int i = 0; i < locationCount; i++) {
			int xStart = 20 + i * 30;
			int yEnd = canvasHeight - 10;
			int blockHeightCar = carCommuters[i]*10;
			int blockHeightBike = bikeCommuters[i]*10;
			
			g.setColor(Color.red);
			g.fillRect(xStart, yEnd - blockHeightCar, blockWidth, blockHeightCar);
			g.setColor(Color.green);
			g.fillRect(xStart + 10, yEnd - blockHeightBike, blockWidth, blockHeightBike);
		}
		
		// TODO: Might want to use built in Graphics2D methods
	}
}
