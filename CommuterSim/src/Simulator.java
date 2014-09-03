/*
 * Classname: Simulator
 * 
 * Version: 0.1
 *
 * Date: 06/05/2014
 * 
 * Copyright (c) Richard Thomas 2014
 * All rights reserved.
 */

package commuterSim;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;

/**
 * The core of the simulation engine and some of the simulator tab GUI.
 * 
 * @author Richard Thomas <gy13rjt@leeds.ac.uk>
 * @version 0.1, 06 May 2014
 */
public class Simulator {

	/**
	 * Number of residential centres along the commuting road
	 */
	private static final int TOTAL_LOCATIONS = 10;
	
	/**
	 * Number of days to average congestion over
	 */
	private static final int CONGESTION_AVERAGING_TIME = 5;
	
	/**
	 * Residents at each location
	 */
	private int residentsPerLoc;
	
	/**
	 * Accumulated cars on road adjacent to this location (today + last few days).
	 * Allows us to smooth congestion using a "moving averager" function.
	 */
	private int carsAccumByLoc[][] = new int[TOTAL_LOCATIONS][CONGESTION_AVERAGING_TIME];

   /**
    * Population summed over all locations
    */
   private int totalPopulation = 1;

   /**
    * Residential location indexed by person (P)
    */
   private int homeLocation[];

 	/**
	 * Individual preference for Car or Bike indexed by person(P)
	 */
	private boolean preferCarNotBike[];
	
	
	//-------------------------------------------------------------------------|
	
	/**
	 * Number of working days since start of simulation
	 */
	private int simDay;

	/**
	 * Moving average of number of total cars
	 */
	private double totalCarsAv;
	
	/**
	 * Whether to automatically randomly vary weather in sims
	 * (i.e. discourage cycling)
	 */
	private boolean rainAuto = false;
	
	/**
	 * Is it (bad weather) today?
	 */
	private boolean rainToday = false;
	
	/**
	 * Is simulator run button currently depressed and sim running?
	 */
	private boolean runActive = false;
	
	/**
	 * Number of days left for current (bad weather) period
	 */
	private int rainDaysLeft = 0;
	
	/**
	 * Whether to automatically randomly generate roadworks in sims
	 * (i.e. forced congestion discouraging driving). Only ever 1
	 * instance of roadworks created at a time. 
	 */
	
	/**
	 * Recording of bad weather events
	 */
	private boolean rainHistory[];
	
	/**
	 * Auto-matic generation of roadworks events
	 */
	private boolean roadworksAuto = false;
	
	/**
	 * Do roadworks exist (anywhere) today?
	 */
	private boolean roadworksToday = false;
	
	/**
	 * Residential location index where roadworks occurring.
	 * (This will affect all residents at that location and
	 *  commuting from further away).
	 */
	private int roadworksLocation = 0;
	
	/**
	 * Number of days left for roadworks to complete
	 */
	private int roadworksDaysLeft = 0;
	
	
	/**
	 * Recording of roadworks events
	 */
	private boolean roadworksHistory[];

	/**
	 * Car commute totals at every step of sim
	 */
	private int carTotalsHistory[];

	/**
	 * Car commute totals (Averaged) at every step of sim
	 */
	private double totalCarsAvHistory[];

	/**
	 * Bike commute totals at every step of sim
	 */
	private int bikeTotalsHistory[];	

	
	/**
	 * Indicates whether simulation is busy running/resetting
	 * (this is a thread-safe way to prevent multiple concurrent
	 * sim execution attempts.  
	 */
	AtomicBoolean simBusy = new AtomicBoolean(false);
	
	/**
	 * Handle to access config vars from preferences instance
	 */
	private Prefs prefInstance;
	private SimRender simCanvas;
	private ModelParams modelParamsInst;

	// Status bar (and console) message containers
	private static JLabel statusLabel;
	private JTextArea simConsole;
	
	// Class-wide visibility required to support callback instances(?)
	private JToggleButton rainfallButton;
	private JCheckBox rainfallAutoCheck;
	private JToggleButton runButton;
	private JToggleButton roadworksButton;
	private JCheckBox roadworksAutoCheck;

	private JScrollPane scroller;
	
	/**
	 * Indicates whether ChartResults has passed a reference in yet
	 */
	public static boolean chartAvailable = false;
	
	/**
	 * Handle for the single instance of Class ChartResults
	 */
	public static ChartResults chartInst = null;
	
	/**
	 * Handle passing routine to enable access to
	 * non-static methods within ChartResults
	 * 
	 * @param chartref ChartResults reference (handle)
	 */
	public static void setChartResultsRef(ChartResults chartref) {
		chartInst = chartref;
		chartAvailable = true;
	}

	JScrollPane getSimConsolePane() {
		return scroller;
	}

	//-------------------------------------------------------------------------|

	/**
	 * Initialisation to default values (Constructor function)
	 */
	public Simulator(ModelParams modelParams, Prefs prefs) {
		prefInstance = prefs;
		modelParamsInst = modelParams;
		
		// Pass a handle for this object to prefs so it can do a callback
		// to the initSim() method after updating preferences
		Prefs.setSimulatorRef(this);
		
		// Create JScrollpane for a console output tab
		simConsole = new JTextArea(15,70);
		scroller = new JScrollPane(simConsole);
		simConsole.setLineWrap(true);
	}

	//-------------------------------------------------------------------------|

	/**
	 * Create panel for "Simulator" tab in main window frame
	 * 
	 * @return handle for "Simulator" panel
	 */
	public JPanel createSimulatorPanel() {

	JPanel simulatorPanel = new JPanel();
	simulatorPanel.setLayout(new BorderLayout());
	
	// Line simulator buttons up along the top (FlowLayout)
	JPanel simButtonPanel = new JPanel();

	JButton resetButton = new JButton("Reset");
	resetButton.addActionListener(new ResetButtonListener());
	simButtonPanel.add(resetButton);
	
	JButton stepButton = new JButton("Step");
	stepButton.addActionListener(new StepButtonListener());
	simButtonPanel.add(stepButton);
	
	runButton = new JToggleButton("Run/Stop");
	runButton.addActionListener(new RunButtonListener());
	simButtonPanel.add(runButton);

	rainfallAutoCheck = new JCheckBox("Auto-Weather");
	rainfallAutoCheck.addActionListener(new rainfallAutoCheckListener());
	simButtonPanel.add(rainfallAutoCheck);
	
	roadworksAutoCheck = new JCheckBox("Auto-Roadworks");
	roadworksAutoCheck.addActionListener(new roadworksAutoCheckListener());
	simButtonPanel.add(roadworksAutoCheck);

	rainfallButton = new JToggleButton("Rain");
	rainfallButton.addActionListener(new rainfallButtonListener());
	simButtonPanel.add(rainfallButton);

	roadworksButton = new JToggleButton("Roadworks");
	roadworksButton.addActionListener(new roadworksButtonListener());
	simButtonPanel.add(roadworksButton);

	simulatorPanel.add(BorderLayout.NORTH, simButtonPanel);

	scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	simulatorPanel.add(BorderLayout.CENTER, scroller);

	// Put simulation drawing canvas in the centre
	simCanvas = new SimRender();
	simulatorPanel.add(BorderLayout.CENTER, simCanvas);
	
	// Add a status bar at the bottom
	statusLabel = new JLabel(" ", JLabel.CENTER);
	simulatorPanel.add(BorderLayout.SOUTH, statusLabel);
		
	// Insert model params pane into its frame
	simulatorPanel.add(BorderLayout.EAST, modelParamsInst.createModelParamsPanel());
	
	// Initialise simulator internal state
	initSim();

	return simulatorPanel;
}
	
	//-------------------------------------------------------------------------|
	
	/**
	 * (Re-)Initialise all simulator state and randomly regenerate population
	 * counts per location and their usual modes of transport.
	 */
	public void initSim() {
		
		int maxDaysPlus1 = prefInstance.getMaxSimDays() + 1;
		
		// (Re-)create all arrays based on max simulation length
		// (Garbage collection will handle any old ones)
		rainHistory = new boolean[maxDaysPlus1];
		roadworksHistory = new boolean[maxDaysPlus1];
		carTotalsHistory = new int[maxDaysPlus1];
		bikeTotalsHistory = new int[maxDaysPlus1];
		totalCarsAvHistory = new double[maxDaysPlus1];

		// Set population count for each residential location
		residentsPerLoc = prefInstance.getPopulationPerLoc();
		totalPopulation = residentsPerLoc * TOTAL_LOCATIONS;

		// (Re-)create array of location by person based on population size
		// (Garbage collection will handle any previous one)
		homeLocation = new int[totalPopulation];
		int personID = 0;
		for (int i = 0; i < TOTAL_LOCATIONS; i++) {
			for (int j = 0; j < residentsPerLoc; j++) {
				homeLocation[personID] = i;
				personID++;
			}
		}

		// (Re-)generate (random) usual mode of transport for each commuter

		// Get probabilities of preference for each mode of transport
		double initCarProb = prefInstance.getInitCarProb();

		// (Re-)create arrays of transport modes chosen by individuals (recently)
		// (Garbage collection will handle any previous ones)
		preferCarNotBike = new boolean[totalPopulation];
		
		// For each person randomly select a preferred mode of transport to
		// start with, based on probabilities of each mode 
		for (int p = 0; p < totalPopulation; p++) {
			double rnd01 = Math.random();
			if (rnd01 < initCarProb) {
				preferCarNotBike[p] = true;
			}
			else {
				preferCarNotBike[p] = false;
			}
		}

		// Complete initialisation by chaining through to final setup function
		resetSim();		
	}

	/**
	 * (Re-)set simulator time to day 0. Ensure that if only setup function is
	 * called that the initial simulation setup is exactly the same as the last
	 * time (including rand seed if specified in configuration preferences)
	 */
	private void resetSim() {

		// TODO: set rand seed if requested in Prefs
		
		// Reset simulator state for day 0
		simDay = 0;
		rainToday = false;
		rainDaysLeft = 0;
		roadworksToday = false;
		roadworksDaysLeft = 0;
		
		// At day 0, assume average cars goes with global preference
		totalCarsAv = prefInstance.getInitCarProb() * totalPopulation;

		// Reset accumulated cars on road adjacent to this location
		// over the "last few days". (This will erroneously make average
		// congestion look very low at the start of the simulation).
		for (int i = 0; i < TOTAL_LOCATIONS; i++) {
			for (int j = 0; j < CONGESTION_AVERAGING_TIME; j++) {
				carsAccumByLoc[i][j] = 0;
			}
		}

		// Flag ready to console output
		simStatusRpt(simDay, "[Simulator Reset]\n");
	}
	
	//-------------------------------------------------------------------------|
	
	/**
	 * Step simulator on by 1 day
	 * (Also called for "run" mode with runActive set).
	 */
	private void simStep() {
		
		// Bail out if we are at the end of the simulation buffer
		if (simDay >= prefInstance.getMaxSimDays())
			return;
		
		// Check to see if simulator already busy on another thread
		// Make sure the flag is set anyway, so we can get exclusive use if free
		boolean isAlreadyRunning = simBusy.getAndSet(true);
		
		// If it was already running our job is done so bail out at this point
		if (isAlreadyRunning == true)
			return;

		// Prepare to keep looping if the run button pressed
		// (as indicated by the runActive flag)
		while (true) {

			// Handle bad weather
			if (rainAuto == true) {
				if (rainDaysLeft > 0) {
					rainDaysLeft--;
				}
				else if (Math.random() < prefInstance.getRainStartProbability()) {
					rainDaysLeft = (int)(Math.random() * prefInstance.getRainMaxDays() + 0.5);
					rainToday = true;
					
					// Force the rainfall toggle button to be selected
					rainfallButton.setSelected(true);
				}
				else {
					rainToday = false;
					
					// Force the rainfall toggle button to be de-selected
					rainfallButton.setSelected(false);
				}
			}
			
			// Handle Roadworks
			if (roadworksAuto == true) {
				if (roadworksDaysLeft > 0) {
					roadworksDaysLeft--;
				}
				else if (Math.random() < prefInstance.getRoadworksStartProbability()) {
					roadworksDaysLeft = (int)(Math.random() * prefInstance.getRoadworksMaxDays() + 0.5);
					roadworksToday = true;
					roadworksLocation = (int)(Math.random() * TOTAL_LOCATIONS + 0.5);

					// Force the roadworks toggle button to be selected
					roadworksButton.setSelected(true);
				}
				else {
					roadworksToday = false;

					// Force the roadworks toggle button to be de-selected
					roadworksButton.setSelected(false);	
				}
			}

			// For each person, sum the number of cars and bikes setting off from
			// each location today. First (re-)create arrays of counts, so
			// values are automatically all initialised to 0.
			int carCommuters[] = new int[TOTAL_LOCATIONS];
			int bikeCommuters[] = new int[TOTAL_LOCATIONS];

			// Ideal car commuters per location (summing fuzzy fractional values)
			double idealCarCommuters[] = new double[TOTAL_LOCATIONS];
			
			int carTotalToday = 0;
			int bikeTotalToday = 0;
			
			// Summation over all people of 
			double sumFavourCarNotBike = 0.0;
			
			// Get simulation model adjustable parameters
			int slideExpense = modelParamsInst.getSlideValExpense().get();
			int slideTimeEffort = modelParamsInst.getSlideValTimeEffort().get();
			int slideCongestion = modelParamsInst.getSlideValCongestion().get();
			int slideRoadworks = modelParamsInst.getSlideValRoadworks().get();
			int slideWeather = modelParamsInst.getSlideValWeather().get();
			int slideIndividual = modelParamsInst.setGlideValIndividual().get();
			
			// Get total cars (from previous day) and standardise to up to +2.0 max
			// to allow saturation. (If everyone drove total cars = population)
			double stdTotalCarsAv = 2.0 * totalCarsAv / totalPopulation;
			
			// For each person in turn, decide whether they will take car or bike today.
			// Calculate each cost factor (cf) in turn, limiting its range to +/-100.
			// The mean value of these ranges from -100(bike) to + 100(car).
			for (int p = 0; p < totalPopulation; p++) {
				
				// Get distance and standardise to +/-2.0 max to allow saturation
				// in cfTimeEffort calculation (below...)
				double stdDist = homeLocation[p] / 5.0;
				
				// Time/Effort to cover given distance (expect +ve as favours car)
				double cfTimeEffort = slideTimeEffort * stdDist;
				//cfTimeEffort = (cfTimeEffort > 100.0) ? 100.0 : cfTimeEffort;
				//cfTimeEffort = (cfTimeEffort < -100.0) ? -100.0 : cfTimeEffort;
				
				// Fixed costs (expect -ve as favours bike)
				double cfExpense = slideExpense;
				
				// Bad weather (expect 0 or +ve as favours car)
				double cfWeather = rainToday ? slideWeather : 0.0;
				
				// Congestion (expect -ve as favours bike)
				double cfCongestion = slideCongestion * stdTotalCarsAv;
				//cfCongestion = (cfCongestion > 100.0) ? 100.0 : cfCongestion;
				//cfCongestion = (cfCongestion < -100.0) ? -100.0 : cfCongestion;

				// Roadworks (expect 0 or -ve as favours bike)
				double cfRoadworks = (roadworksToday && roadworksLocation <= homeLocation[p]) ?
						slideRoadworks * stdTotalCarsAv: 0.0;
				//cfRoadworks = (cfRoadworks > 100.0) ? 100.0 : cfRoadworks;
				//cfRoadworks = (cfRoadworks < -100.0) ? -100.0 : cfRoadworks;

				// Individual preference (-ve or +ve)
				double cfIndividual = preferCarNotBike[p] ? slideIndividual
						: -slideIndividual;
						
				// Find mean cost factor and reduce to range 0.0 (Bike) .. 1.0 (Car)
				double favourCarNotBike = (cfTimeEffort + cfExpense + cfWeather
						+ cfCongestion + cfRoadworks + cfIndividual) / 200.0 + 0.5;
				favourCarNotBike = (favourCarNotBike > 1.0) ? 1.0 : favourCarNotBike;
				favourCarNotBike = (favourCarNotBike < -1.0) ? -1.0 : favourCarNotBike;
										
				sumFavourCarNotBike += favourCarNotBike;
				
				idealCarCommuters[homeLocation[p]] += favourCarNotBike;
			}
			
			// TODO: this is an estimation... we haven't converted to actual decisions yet
			carTotalToday = (int) (sumFavourCarNotBike + 0.5);
			bikeTotalToday = totalPopulation - carTotalToday;
			
			// Record today's totals across whole population
			carTotalsHistory[simDay] = carTotalToday;
			bikeTotalsHistory[simDay] = bikeTotalToday;
			
			// Get moving averager length parameter (from slider)
			int movingAveragerLen = modelParamsInst.getSlideValAveragerLen().get();

			// Calculate moving sum average of total cars
			// (a proxy for congestion)
			int day = simDay;
			int movingSumTotalCars = 0;
			int count = 0;
			while (day >= 0 && count < movingAveragerLen) {
				movingSumTotalCars += carTotalsHistory[day];
				count++;
				day--;
			}
			totalCarsAv = ((double) movingSumTotalCars) / count;
			totalCarsAvHistory[simDay] = totalCarsAv;
			rainHistory[simDay] = rainToday;
			roadworksHistory[simDay] = roadworksToday;

			simDay++;

			// Write to console window
			String statusString = "Cars by location = {";
			for (int i = 0; i < TOTAL_LOCATIONS; i++) {
				int intCars = (int)(idealCarCommuters[i] + 0.5);
				statusString += intCars + " ";
			}
			statusString += "}, Total cars = " + carTotalToday
					+ ", Total bikes = " + bikeTotalToday;
			simStatusRpt(simDay, statusString);
			
			//simCanvas.DrawBlock(simDay, carTotalToday, bikeTotalToday);
			simCanvas.drawBars(idealCarCommuters, TOTAL_LOCATIONS, totalPopulation);
			
			// Stop looping if we are at the end of the simulation buffer
			// Or not in a run mode anyway
			if (simDay >= prefInstance.getMaxSimDays() || runActive == false)
				break;
			
			// Otherwise sleep briefly before looping again (time in ms)
			// Skipped this as still couldn't get canvas to repaint, so pointless
			/*
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				simStatusRpt(simDay, "Can't sleep!");
			}
			*/
		}
		
		// If run button was pressed, then clear it
		if (runActive == true) {
			runActive = false;
			runButton.setSelected(false);
		}

		// Clear the simulation busy flag so another thread can use the simulator
		simBusy.set(false);
		
		// Update JFreeChart pane
		if (chartAvailable = true) {
			chartInst.UpdateChart(simDay, carTotalsHistory, totalCarsAvHistory,
					bikeTotalsHistory);
		}

	}

	//-------------------------------------------------------------------------|

	private void simStatusRpt(int day, String message) {
		
		// Compose message
		String statusStr = "Day " + day + ": " + message;
		
		// Write in status bar
		statusLabel.setText(statusStr);
		
		// Write to console window
		simConsole.append(statusStr + "\n");
	}

	/**
	 * Reset button event handler
	 * (Implemented as an inner class to allow multiple ActionListeners)
	 */
	class ResetButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			resetSim();
		}
	}

	/**
	 * Step button event handler
	 * (Implemented as an inner class to allow multiple ActionListeners)
	 */
	class StepButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
			// Step simulator on by 1 day (unless hit the buffers)
			simStep();
		}
	}

	/**
	 * Run button event handler
	 * (Implemented as an inner class to allow multiple ActionListeners)
	 */
	class RunButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			
			if (runButton.isSelected()) {
				runActive = true;

				// Step simulator on by 1 day (unless hit the buffers)
				// It will automatically re-run itself if it sees the 
				// runActive flag still set
				simStep();
			} else {
				runActive = false;
			}
		}
	}

	/**
	 * Rainfall Auto checkbox event handler
	 * (Implemented as an inner class to allow multiple ActionListeners)
	 */
	class rainfallAutoCheckListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (rainfallAutoCheck.isSelected()) {
				rainAuto = true;
			} else {
				rainAuto = false;
			}
		}
	}

	/**
	 * Rainfall on/off button event handler
	 * (Implemented as an inner class to allow multiple ActionListeners)
	 */
	class rainfallButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (rainfallButton.isSelected()) {
				rainToday = true;
			} else {
				rainToday = false;
			}
		}
	}
	
	/**
	 * Roadworks Auto checkbox event handler
	 * (Implemented as an inner class to allow multiple ActionListeners)
	 */
	class roadworksAutoCheckListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (roadworksAutoCheck.isSelected()) {
				roadworksAuto = true;
			} else {
				roadworksAuto = false;
			}
		}
	}

	/**
	 * Roadworks on/off button event handler
	 * (Implemented as an inner class to allow multiple ActionListeners)
	 */
	class roadworksButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (roadworksButton.isSelected()) {
				roadworksToday = true;
				roadworksLocation = (int)(Math.random() * TOTAL_LOCATIONS + 0.5);
			} else {
				roadworksToday = false;
			}
		}
	}

}
