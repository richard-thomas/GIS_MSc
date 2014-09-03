/*
 * Classname: Prefs
 * 
 * Version: 0.1
 *
 * Date: 06/05/2014
 * 
 * Copyright (c) Richard Thomas 2014
 * All rights reserved.
 */

package commuterSim;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A class designed to handle via a GUI Text fields updating of
 * and access to the following simulation parameters:
 * <ul>
 * <li> random seed (will not apply if negative)
 * <li> variation from rational choice
 * <li> min residents at each location
 * <li> max residents at each location
 * <li> Distribution of randomly generated preferred mode of transport
 * <li> probability of starting bad weather period
 * <li> max duration of bad weather period
 * <li> probability of starting roadworks
 * <li> max duration of roadworks
 * </ul> 
  * 
 * @author Richard Thomas <gy13rjt@leeds.ac.uk>
 * @version 0.1, 06 May 2014
*/
public class Prefs {
		
	/**
	 * Maximum number of days to simulate.
	 * (Defines length of arrays dynamically created at (re-)initialisation
	 */
	private int maxSimDays;
	
	/**
	 * Whether to restart pseudo-random number generator in fixed place
	 */
	private boolean useRandSeed;
	
	/**
	 * Random seed for creating populations and randomising simulation
	 */
	private int randSeed;

	/**
	 * Number of residents at each location
	 */
	private int populationPerLoc;
	
	
	/**
	 * probability of starting bad weather period
	 */
	private double rainStartProbability;
	
	/**
	 * max duration of bad weather period
	 */
	private int rainMaxDays;
	
	/**
	 * probability of starting roadworks
	 */
	private double roadworksStartProbability;
	
	/**
	 * max duration of roadworks
	 */
	private int roadworksMaxDays;

	/**
	 * General population preference for taking car over bike (0..1)
	 * Initial probability of commuting by car
	 */
	private double initCarProb;

	private JTextField randSeedField;
	private JCheckBox useRandomSeedCheck;

	//-------------------------------------------------------------------------|
	// Accessor Methods
	//-------------------------------------------------------------------------|

	/**
	 * Accessor function for likelihood of preference for commuting by car
	 */
	public double getInitCarProb() {
		return initCarProb;
	}
	
	/**
	 * Accessor function for max number of days to simulate
	 */
	public int getMaxSimDays() {
		return maxSimDays;
	}
	
	/**
	 * Accessor function for pseudo-random number generator seed
	 */
	public int getRandSeed() {
		return randSeed;
	}
	
	/**
	 * Accessor function for pseudo-random number generator option
	 */
	public boolean getUseRandSeed() {
		return useRandSeed;
	}
	
 	/**
	 * Initialisation to default values (Constructor function)
	 */
	public Prefs() {
		forceDefaults();
	}
	
	/**
	 * Accessor function for number of residents at each location
	 */
	public int getPopulationPerLoc() {
		return populationPerLoc;
	}
	
	/**
	 * Accessor function for probability of bad weather starting
	 */
	public double getRainStartProbability() {
		return rainStartProbability;
	}
	
	/**
	 * Accessor function for probability of roadworks starting
	 */
	public double getRoadworksStartProbability() {
		return roadworksStartProbability;
	}
	
	/**
	 * Accessor function for max days of roadworks in a row
	 */
	public int getRoadworksMaxDays() {
		return roadworksMaxDays;
	}
	
	/**
	 * Accessor function for max days of bad weather in a row
	 */
	public int getRainMaxDays() {
		return rainMaxDays;
	}
	
	/**
	 * Indicates whether Simulator has passed a reference in yet
	 */
	public static boolean simulatorAvailable = false;
	
	/**
	 * Handle for the single instance of Class Simulator
	 */
	public static Simulator simulatorInst = null;
	
	/**
	 * Handle passing routine to all this class to access
	 * non-static methods within the simulator
	 * 
	 * @param simRef Simulator reference (handle)
	 */
	public static void setSimulatorRef(Simulator simRef) {
		simulatorInst = simRef;
		simulatorAvailable = true;
	}

	/**
	 * Create panel for "Preferences" tab in main window frame
	 * 
	 * @return handle for Preferences panel
	 */
	public JPanel createPrefsPanel() {
		JPanel PrefsPanel = new JPanel();
		PrefsPanel.setLayout(new GridLayout(18,1));

		useRandomSeedCheck = new JCheckBox("Use Random Seed");
		//useRandomSeedCheck.addActionListener(new useRandomSeedCheckListener());
		PrefsPanel.add(useRandomSeedCheck);

		JLabel randSeedLabel = new JLabel("Pseudo-random number generator seed");
		PrefsPanel.add(randSeedLabel);
		randSeedField = new JTextField(String.valueOf(randSeed), 4);
		PrefsPanel.add(randSeedField);

		JLabel maxSimDaysLabel = new JLabel("Maximum number of days to simulate");
		PrefsPanel.add(maxSimDaysLabel);
		JTextField maxSimDaysField = new JTextField(String.valueOf(maxSimDays), 4);
		PrefsPanel.add(maxSimDaysField);

		JLabel residentsPerLocLabel = new JLabel("Population at each location");
		PrefsPanel.add(residentsPerLocLabel);
		JTextField populationPerLocField = new JTextField(String.valueOf(populationPerLoc), 4);
		PrefsPanel.add(populationPerLocField);

		JLabel initCarProbLabel = new JLabel("General preference of population to commute by car");
		PrefsPanel.add(initCarProbLabel);
		JTextField initCarProbField = new JTextField(String.valueOf(initCarProb), 4);
		PrefsPanel.add(initCarProbField);

		JLabel rainStartProbabilityLabel = new JLabel("Probability of bad weather starting");
		PrefsPanel.add(rainStartProbabilityLabel);
		JTextField rainStartProbabilityField = new JTextField(String.valueOf(rainStartProbability), 4);
		PrefsPanel.add(rainStartProbabilityField);

		JLabel roadworksStartProbabilityLabel = new JLabel("Probability of roadworks starting");
		PrefsPanel.add(roadworksStartProbabilityLabel);
		JTextField roadworksStartProbabilityField = new JTextField(String.valueOf(roadworksStartProbability), 4);
		PrefsPanel.add(roadworksStartProbabilityField);

		JLabel rainMaxDaysLabel = new JLabel("Max days of bad weather in a row");
		PrefsPanel.add(rainMaxDaysLabel);
		JTextField rainMaxDaysField = new JTextField(String.valueOf(rainMaxDays), 4);
		PrefsPanel.add(rainMaxDaysField);

		JLabel roadworksMaxDaysLabel = new JLabel("Max days of roadworks in a row");
		PrefsPanel.add(roadworksMaxDaysLabel);
		JTextField roadworksMaxDaysField = new JTextField(String.valueOf(roadworksMaxDays), 4);
		PrefsPanel.add(roadworksMaxDaysField);

		// Need to press update button to read in (and check) text field values
		JButton updatePrefsButton = new JButton("Update & Re-initialise Simulator");
		updatePrefsButton.addActionListener(new updatePrefsButtonListener());
		PrefsPanel.add(updatePrefsButton);

		return PrefsPanel;
	}

	/**
	 * Set prefs to default values.
	 * (Each are described in detail where they are defined above).
	 */
	private void forceDefaults() {
		useRandSeed = false;
		randSeed = 1;
		maxSimDays = 50;
		populationPerLoc = 50;
		rainStartProbability = 0.1;
		rainMaxDays = 5;
		roadworksStartProbability = 0.05;
		roadworksMaxDays = 10;
		initCarProb = 0.8;
	}

	/**
	 * "Update Preferences" button event handler
	 * (Implemented as an inner class to allow multiple ActionListeners)
	 */
	class updatePrefsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			//Simulator.tmpStatusLabel.setText("rand seed = " + randSeedField.getText());

			// TODO: read each value back from text fields, sanity check then either
			//      accept or replace them.
			
			// Re-initialise the simulator (assuming its available)
			if (simulatorAvailable = true) {
				simulatorInst.initSim();
			}
		}
	}
}
