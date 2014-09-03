/*
 * Classname: Top
 * 
 * Version: 0.1
 *
 * Date: 06/05/2014
 * 
 * Copyright (c) Richard Thomas 2014
 * All rights reserved.
 */

package commuterSim;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartPanel;

/**
 * Top level (main) class for a small application to simulate the
 * switching of modes of transport by commuters depending on behaviour
 * of other commuters and changes in the environment.
 * 
 * @author Richard Thomas <gy13rjt@leeds.ac.uk>
 * @version 0.1, 06 May 2014
 */
public class Top {

	/**
	 * Top level method for program invocation.
	 * 
	 * @param args 		optional command line arguments (ignored)
	 */
	public static void main(String[] args) {
		
		// Create main and Model Parameters windows
		JFrame mainFrame = new JFrame("Commuter Sim");
		
		// Make program quit (not just disappear from screen) when 'X' pressed
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create instances to build the application
		Prefs prefs = new Prefs();
		ModelParams modelParams = new ModelParams();
		Simulator simulator = new Simulator(modelParams, prefs);
		ChartResults chart = new ChartResults();

		// Split window into separate tabs handled by different classes
		JTabbedPane tabPane;
		tabPane = new JTabbedPane();
		
		// First create text for an introductory text pane
		String introText = "\n\t\t\t'COMMUTER SIM': Bike vs Car!\t(Richard Thomas, May 2014)\n\n"
				+"\tThis programme is an experiment in trying to implement a relatively simple behaviour model within an interactive software environment.\n"
				+"\tSpecifically it is trying to model some of the trade-offs between commuting by car or bike. The basic model is of 10 population\n"
				+"\tcentres stretched along an arterial road, with everyone commuting to the same area at the end of the road:\n\n"
				+"\t [WORK AREA] - - - - - - - - [HOMES] - - - - [HOMES] - - - - [HOMES] - - - - [HOMES] - - - - [HOMES]\n\n"
				+"\tAlthough this is not shown in the GUI, the vertical bar graphs shown in the <Simulator> pane (which appear when you press <STEP>)\n"
				+"\trepresent the numbers of people commuting by car (in Red) or bike (in Green) as a 1 day snapshot. Note the layout is (as above) with\n"
				+"\tthe work area on the left - thus as you might expect the number of people using bikes is much higher close to the work area.\n"
				+"\tThe actual numbers commuting from each population centre that day (i.e. each bar graph pair) are recorded in the console tab pane\n"
				+"\tThe <Results Chart> tab pane dynamically updates a graph of the total bike and car commuters against time.\n\n"
				+"\tINSTRUCTIONS\n"
				+"\t\t<STEP> advances the simulation by 1 day\n"
				+"\t\t<RESET> resets the simulator and all graphs to day 0\n"
				+"\t\t<RUN> runs the simulation through to the end time (very quickly!)\n"
				+"\t\t<RAIN> is a toggle button to simulate bad weather that day (generally more of a deterrent to cycling)\n"
				+"\t\t<ROADWORKS> is a toggle button to simulate roadworks at one location that day (deterrent to drivers)\n"
				+"\tThe checkboxes allow for automatic random generation of periods of bad weather and roadworks.\n"
				+"\tThe slider on the right adjust how various factors will favour cyclists (slid to the left) or drivers (slid to the right).\n"
				+"\tThe time/effort slider is multiplied by the distance a commuter must travel, so sliding it further to the left you will see a\n"
				+"\tgradual drop-off in the number of cyclists from more distant locations. The Congestion and Roadworks sliders are both dependent on\n"
				+"\tthe number of cars.\n\n"
				+"\tIn hindsight, the model is disappointingly un-dynamic (though selecting auto-rain/roadworks does liven up the chart!). It is also\n"
				+"\tnot particularly helpful in examining any behaviour. I was hoping to see more oscillations from increasing congestion discouraging\n"
				+"\tcar use. However, hopefully it is a good example of how a model can be encapsulated in an interactive environment using just Java\n"
				+"\tAcknowledgement: Charting was generated using JFreechart under the GNU Lesser General Public Licence (LGPL).\n"
				+"\thttp://www.jfree.org/jfreechart/\n";
		
		// Add the 5 tab panes
		tabPane.add(new JTextArea(introText), "Introduction");
		tabPane.add(prefs.createPrefsPanel(), "Sim Configuration");
		tabPane.add(simulator.createSimulatorPanel(), "Simulator");
		tabPane.add(simulator.getSimConsolePane(), "Console");
		tabPane.add((ChartPanel) chart.InitChart(), "Results Chart");
		
		// Insert pane of tabs into main window
		mainFrame.getContentPane().add(tabPane);
		mainFrame.setSize(900, 600);
		mainFrame.setVisible(true);	
	}
}
