/*
 * Classname: ChartResults
 * 
 * Version: 0.1
 *
 * Date: 06/05/2014
 * 
 * Copyright (c) Richard Thomas 2014
 * All rights reserved.
 */

package commuterSim;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.DefaultXYDataset;

/**
 * Generation of images for the "Results Chart" pane, using the open
 * source package JFreeChart.
 * 
 * @author Richard Thomas <gy13rjt@leeds.ac.uk>
 * @version 0.1, 06 May 2014
 */
public class ChartResults {
	
	
	DefaultXYDataset data1;
	JFreeChart chart;
	
	public ChartPanel InitChart() {
		
		// Pass a handle to Simulator so it can update
		// charts by calls to methods here
		Simulator.setChartResultsRef(this);

		data1 = new DefaultXYDataset();

		chart = ChartFactory.createScatterPlot(
				"Commuter (Car vs Bike) simulation", 
				"Days", // x axis label
				"Commuter Counts", // y axis label
				data1, // data
				PlotOrientation.VERTICAL, // orientation
				true, // legend
				true, // tooltips
				false // URLs
				);

		// Package the chart into a panel
		ChartPanel chartPanel = new ChartPanel(chart);
		return chartPanel;
	}
	
	public void UpdateChart(int simDays, int[] totalCars, double[] totalCarsAv,
			int[] totalBikes) {
		
		double dataTotalBikes[][] = new double[2][simDays];
		double dataTotalCars[][] = new double[2][simDays];
		double dataTotalCarsAv[][] = new double[2][simDays];
		
		for (int i = 0; i < simDays; i++) {
			dataTotalBikes[0][i] = i;
			dataTotalBikes[1][i] = (double) totalBikes[i];
			dataTotalCars[0][i] = i;
			dataTotalCars[1][i] = (double) totalCars[i];
			dataTotalCarsAv[0][i] = i;
			dataTotalCarsAv[1][i] = totalCarsAv[i];
		}

		data1.addSeries("Total Bikes", dataTotalBikes);
		data1.addSeries("Total Cars", dataTotalCars);
		data1.addSeries("Total Cars (Average)", dataTotalCarsAv);
	}
}
