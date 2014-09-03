/*
 * Classname: ModelParams
 * 
 * Version: 0.1
 *
 * Date: 06/05/2014
 * 
 * Copyright (c) Richard Thomas 2014
 * All rights reserved.
 */

package commuterSim;

import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Create a simulation Model Parameters Panel, which will directly
 * update the variables from sliders.
 * Each parameter is declared as an AtomicInteger primarily so that it
 * is an object rather than a primitive and will thus be passed by
 * reference (easier to modify via generated JSlider callback functions).
 * 
 * @author Richard Thomas <gy13rjt@leeds.ac.uk>
 * @version 0.1, 06 May 2014
 */
public class ModelParams {

	/**
	 * Fixed cost offset for this mode of transport
	 */
	private final AtomicInteger slideValExpense = new AtomicInteger();
	private final AtomicInteger slideValTimeEffort = new AtomicInteger();
	private final AtomicInteger slideValIndividual = new AtomicInteger();
	private final AtomicInteger slideValCongestion = new AtomicInteger();
	private final AtomicInteger slideValRoadworks = new AtomicInteger();
	private final AtomicInteger slideValWeather = new AtomicInteger();
	private final AtomicInteger slideValAveragerLen = new AtomicInteger();
	
	private JLabel sliderValueDisplayLabel;

	// get<X>(): Accessor methods for each parameter
	
	public AtomicInteger getSlideValExpense() {
		return slideValExpense;
	}

	public AtomicInteger getSlideValTimeEffort() {
		return slideValTimeEffort;
	}
	public AtomicInteger setGlideValIndividual() {
		return slideValIndividual;
	}
	public AtomicInteger getSlideValCongestion() {
		return slideValCongestion;
	}
	public AtomicInteger getSlideValRoadworks() {
		return slideValRoadworks;
	}
	public AtomicInteger getSlideValWeather() {
		return slideValWeather;
	}
	public AtomicInteger getSlideValAveragerLen() {
		return slideValAveragerLen;
	}
	
	/**
	 * Initialisation to default values (Constructor function)
	 */
	public ModelParams() {
		
		// Defaults
		slideValTimeEffort.set(70);
		slideValExpense.set(-20);
		slideValIndividual.set(74);
		slideValCongestion.set(-64);
		slideValRoadworks.set(-80);
		slideValWeather.set(25);
		slideValAveragerLen.set(10);
	}
		
	/**
	 * Create slider panel for "Model Parameters" pane
	 * 
	 * @return handle for "Model Parameters" panel
	 */
	public JPanel createModelParamsPanel() {

		JPanel modelParamsPanel = new JPanel();
		modelParamsPanel.setLayout(new BoxLayout(modelParamsPanel, BoxLayout.Y_AXIS));
		
		// Create label for reporting slider positions. Although placed at bottom,
		// need to create it before calling functions are defined. 
		sliderValueDisplayLabel = new JLabel("", JLabel.CENTER);
		
		modelParamsPanel.add(new JLabel("-    Simulation Model Parameters    -", JLabel.CENTER));
		modelParamsPanel.add(new JLabel("Bike < ----    (Adjust settings to favour)    ---- > Car", JLabel.CENTER));
		
		// Insert some padding
		modelParamsPanel.add(new JLabel("-", JLabel.CENTER));

		// Create sliders that will directly modify target variables themselves
		makeSlider(modelParamsPanel, slideValExpense,
				"Expense (fixed costs)");
		makeSlider(modelParamsPanel, slideValTimeEffort,
				"Time / Effort (scaled by <commute distance>)");
		makeSlider(modelParamsPanel, slideValIndividual,
				"Individual Preferences (scaled by pop distribution)");
		makeSlider(modelParamsPanel, slideValCongestion,
				"Congestion (scaled by <total cars>)");
		makeSlider(modelParamsPanel, slideValRoadworks,
				"Roadworks (scaled by <total cars>)");
		makeSlider(modelParamsPanel, slideValWeather,
				"Bad Weather");
		
		// Insert some padding
		modelParamsPanel.add(new JLabel("-", JLabel.CENTER));
		
		// Create unrelated slider
        modelParamsPanel.add(new JLabel("Congestion Moving Averager Length", JLabel.CENTER));
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 50, slideValAveragerLen.get());
		slider.setMinorTickSpacing(1);
		slider.setMajorTickSpacing(3);
		slider.setPaintTicks(true);
		slider.setLabelTable(slider.createStandardLabels(10));
		slider.setPaintLabels(true);	 
		modelParamsPanel.add(slider);

        // Create a slider change event listener to directly adjust the variable
        slider.addChangeListener(new ChangeListener() {
     		public void stateChanged(ChangeEvent event) {
     			
     			// Get new value of slider causing the event
     			int val = ((JSlider) event.getSource()).getValue();
     			
     			// Display slider value on common label
    			sliderValueDisplayLabel.setText("(Slider value = " + val + ")");

     			// Adjust the variable this slider connects to
    			slideValAveragerLen.set(val);
     		}
     	});
        
		// Insert some padding
		modelParamsPanel.add(new JLabel("-", JLabel.CENTER));
		
		// Add slider feedback label
		modelParamsPanel.add(sliderValueDisplayLabel);

		//modelParamsPanel.setSize(200, 500);

		return modelParamsPanel;
	}

	
	private JSlider makeSlider(JPanel sliderPanel, final AtomicInteger target, String label) {

		// Give it a label above
        JLabel sliderLabel = new JLabel(label, JLabel.CENTER);
		sliderPanel.add(sliderLabel);

		// Create new slider and add it to the panel
		JSlider slider = new JSlider(JSlider.HORIZONTAL, -100, 100, target.get());
		slider.setMinorTickSpacing(5);
		slider.setMajorTickSpacing(25);
		slider.setPaintTicks(true);
		slider.setLabelTable(slider.createStandardLabels(25));
		slider.setPaintLabels(true);	 
		sliderPanel.add(slider);
		
        // Create a slider change event listener to directly adjust the variable
        slider.addChangeListener(new ChangeListener() {
     		public void stateChanged(ChangeEvent event) {
     			
     			// Get new value of slider causing the event
     			int val = ((JSlider) event.getSource()).getValue();
     			
     			// Display slider value on common label
    			sliderValueDisplayLabel.setText("(Slider value = " + val + ")");

     			// Adjust the variable this slider connects to
     			target.set(val);
     		}
     	});
        
        return slider;
	}
}
