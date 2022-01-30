package org.processmining.plugins.neconformance.ui.wizard;

import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import org.processmining.plugins.kutoolbox.ui.FancyIntegerSlider;
import org.processmining.plugins.kutoolbox.ui.TwoColumnParameterPanel;
import org.processmining.plugins.kutoolbox.ui.UIAttributeConfigurator;

public class WizardSettingsPanel extends TwoColumnParameterPanel implements UIAttributeConfigurator {
	
	private static final long serialVersionUID = -2878063135102398114L;
	
	private int replayer;
	private boolean useBagInducer;
	private boolean useWeighted;
	private boolean useBothRatios;
	private boolean useCutOff;
	private int negWindow;
	private int genWindow;
	private boolean unmappedRecall, unmappedPrecision, unmappedGeneralization;
	private boolean useMultithreaded;

	private JRadioButton checkHeur;
	private JRadioButton checkHeRe;
	private JRadioButton checkArya;
	private JCheckBox checkBag;
	private JCheckBox checkWeighted;
	private JCheckBox checkRatio;
	private JCheckBox checkCut;
	private JCheckBox checkRecall, checkPrecision, checkGeneralization;
	private JCheckBox checkMt;
	private FancyIntegerSlider sliderNeg;
	private FancyIntegerSlider sliderGen;
	
	public WizardSettingsPanel() {
		super(75);
		this.init();
	}

	protected void init() {
		resetSettings();
		
		this.addDoubleLabel("Replay settings", 1);
		ButtonGroup group = this.addButtongroup();
		checkHeur = this.addRadiobutton("Use heuristic replayer", replayer == 0, 2, false);
		checkArya = this.addRadiobutton("Use alignment based replayer", replayer == 1, 3, false);
		checkHeRe = this.addRadiobutton("Use heuristic replayer with recovery", replayer == 2, 4, false);
		group.add(checkHeur);
		group.add(checkArya);
		group.add(checkHeRe);
		
		this.addDoubleLabel("Evaluation settings", 5);
		checkRecall = this.addCheckbox("Punish recall on unmapped log elements", unmappedRecall, 6, true);
		checkPrecision = this.addCheckbox("Punish precision on unmapped model elements", unmappedPrecision, 7, true);
		checkGeneralization = this.addCheckbox("Punish generalization on unmapped log elements", unmappedGeneralization, 58, true);
		checkGeneralization.setVisible(false); // Hide this one, we don't use it currently
		
		this.addDoubleLabel("Inducer settings", 10);
		checkBag = this.addCheckbox("Use bag-of-activity based inducer", useBagInducer, 11, false);
		checkWeighted = this.addCheckbox("Use weighted negative events", useWeighted, 12, false);
		checkRatio = this.addCheckbox("Use longest/shortest window instead of '1 minus'", useBothRatios, 13, false);
		checkCut = this.addCheckbox("Cut off window at next event occurrence", useCutOff, 14, false);
		
		sliderNeg = this.addIntegerSlider(-1, negWindow, 10, 15);
		sliderGen = this.addIntegerSlider(-1, genWindow, 10, 16);
		
		checkMt = this.addCheckbox("Use multithreaded calculation", useMultithreaded, 17, false);
	}

	@Override
	public String getTitle() {
		return "Conformance Checking Settings";
	}

	@Override
	public void resetSettings() {
		replayer = 0;
		useBagInducer = false;
		useWeighted = true;
		useBothRatios = false;
		useCutOff = false;
		unmappedRecall = true;
		unmappedPrecision = true;
		unmappedGeneralization = true;
		useMultithreaded = false;
		negWindow = -1;
		genWindow = -1;
	}

	@Override
	public void setSettings(Map<String, Object> settings) {
		replayer = Integer.parseInt(settings.get("replayer").toString());
		useBagInducer = settings.get("useBagInducer").toString().equals("1");
		useWeighted = settings.get("useWeighted").toString().equals("1");
		useBothRatios = settings.get("useBothRatios").toString().equals("1");
		useCutOff = settings.get("useCutOff").toString().equals("1");
		negWindow = Integer.parseInt(settings.get("negWindow").toString());
		genWindow = Integer.parseInt(settings.get("genWindow").toString());
		unmappedRecall = settings.get("unmappedRecall").toString().equals("1");
		unmappedPrecision = settings.get("unmappedPrecision").toString().equals("1");
		unmappedGeneralization = settings.get("unmappedGeneralization").toString().equals("1");
		useMultithreaded = settings.get("useMultithreaded").toString().equals("1");
		
		checkHeur.setSelected(replayer == 0);
		checkArya.setSelected(replayer == 1);
		checkHeRe.setSelected(replayer == 2);
		checkBag.setSelected(useBagInducer);
		checkWeighted.setSelected(useWeighted);
		checkRatio.setSelected(useBothRatios);
		checkCut.setSelected(useCutOff);
		checkRecall.setSelected(unmappedRecall);
		checkPrecision.setSelected(unmappedPrecision);
		checkGeneralization.setSelected(unmappedGeneralization);
		checkMt.setSelected(useMultithreaded);
		
		sliderNeg.setValue(negWindow);
		sliderGen.setValue(genWindow);
	}

	@Override
	public Map<String, Object> getSettings() {
		if (checkHeur.isSelected()) replayer = 0;
		if (checkArya.isSelected()) replayer = 1;
		if (checkHeRe.isSelected()) replayer = 2;
		useBagInducer = checkBag.isSelected();
		useWeighted = checkWeighted.isSelected();
		useBothRatios = checkRatio.isSelected();
		useCutOff = checkCut.isSelected();
		negWindow = sliderNeg.getValue() == 0 ? -1 : sliderNeg.getValue();
		genWindow = sliderGen.getValue() == 0 ? -1 : sliderGen.getValue();
		unmappedRecall = checkRecall.isSelected();
		unmappedPrecision = checkPrecision.isSelected();
		unmappedGeneralization = checkGeneralization.isSelected();
		useMultithreaded = checkMt.isSelected();
		
		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("replayer", ""+replayer);
		settings.put("useBagInducer", useBagInducer ? "1" : "0");
		settings.put("useWeighted", useWeighted ? "1" : "0");
		settings.put("useBothRatios", useBothRatios ? "1" : "0");
		settings.put("useCutOff", useCutOff ? "1" : "0");
		settings.put("unmappedRecall", unmappedRecall ? "1" : "0");
		settings.put("unmappedPrecision", unmappedPrecision ? "1" : "0");
		settings.put("unmappedGeneralization", unmappedGeneralization ? "1" : "0");
		settings.put("useMultithreaded", useMultithreaded ? "1" : "0");
		settings.put("negWindow", ""+negWindow);
		settings.put("genWindow", ""+genWindow);
		return settings;
	}

	@Override
	protected void updateFields() {
		
	}

}
