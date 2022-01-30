package org.processmining.plugins.neconformance.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.kutoolbox.groupedlog.GroupedXLog;
import org.processmining.plugins.neconformance.bags.LogBag;
import org.processmining.plugins.neconformance.bags.naieve.NaieveLogBag;
import org.processmining.plugins.neconformance.negativeevents.AbstractNegativeEventInducer;
import org.processmining.plugins.neconformance.negativeevents.NegativeEventInducer;
import org.processmining.plugins.neconformance.negativeevents.impl.LogBagWeightedNegativeEventInducer;
import org.processmining.plugins.neconformance.negativeevents.impl.LogTreeWeightedNegativeEventInducer;
import org.processmining.plugins.neconformance.trees.LogTree;
import org.processmining.plugins.neconformance.trees.ukkonen.UkkonenLogTree;
import org.processmining.plugins.neconformance.types.WeightedEventClass;

public class NegativeEventInspector extends JPanel {

	private static final long serialVersionUID = -1596900072533174028L;
	private XLog log;
	private GroupedXLog glog;
	private NegativeEventInducer inducer;
	
	private JPanel distributionPanel;
	
	private double[] bins;
	private Map<Double, Integer> negativeWeights;
	private Map<Double, Integer> generalizedWeights;
	private UIPluginContext context;
	
	public NegativeEventInspector(XLog log, UIPluginContext context) {
		this.log = log;
		this.glog = new GroupedXLog(log);
		this.context = context;
		
		this.bins = new double[]{0, .1, .2, .3, .4, .5, .6, .7, .8, .9, 1};
		resetBins();
		createInducer(false);
		
		init();
	}
	
	private void createInducer(boolean useBags) {
		XEventClasses eventClasses = XEventClasses.deriveEventClasses(XLogInfoImpl.STANDARD_CLASSIFIER, log);
		if (!useBags) {
			LogTree logTree = new UkkonenLogTree(glog.getGroupedLog());
			this.inducer = new LogTreeWeightedNegativeEventInducer(eventClasses,
				AbstractNegativeEventInducer.deriveStartingClasses(eventClasses, log), 
				logTree);
			((LogTreeWeightedNegativeEventInducer) this.inducer).setReturnZeroEvents(true);
		} else {
			LogBag logBag = new NaieveLogBag(glog.getGroupedLog());
			this.inducer = new LogBagWeightedNegativeEventInducer(eventClasses,
				AbstractNegativeEventInducer.deriveStartingClasses(eventClasses, log),
				logBag);
			((LogBagWeightedNegativeEventInducer) this.inducer).setReturnZeroEvents(true);
		}
		
	}
	
	private void resetBins() {
		this.negativeWeights = new HashMap<Double, Integer>();
		this.generalizedWeights = new HashMap<Double, Integer>();
		for (int i = 1; i < this.bins.length; i++) {
			negativeWeights.put(this.bins[i], 0);
			generalizedWeights.put(this.bins[i], 0);
		}
		
	}
	
	private void incrementBin(double value, Map<Double, Integer> target) {
		for (int i = 1; i < bins.length; i++) {
			if (value <= bins[i]) {
				target.put(bins[i], target.get(bins[i])+1);
				return;
			}
		}
	}

	private void init() {
		this.setLayout(new BorderLayout());
		
		JPanel topRow = new JPanel();
		topRow.add(new JLabel("Log: "+XConceptExtension.instance().extractName(log)));
		
		this.add(topRow, BorderLayout.NORTH);
		
		JPanel rightPane = new JPanel();
		distributionPanel = new JPanel() {
			private static final long serialVersionUID = 3042808819703334081L;
			private int valueToPos(double min, double max, double value, double targetSize) {
				double normalizedValue = (value - min) / (max - min);
				double widthPos = (double) targetSize * normalizedValue;
				return (int) widthPos;
			}
			
			@Override	
			public void paint(Graphics g) {
				if (g == null)
					return;
				super.paint(g);
				
				int[] binPositions = new int[bins.length];
				for (int i = 0; i < bins.length; i++) {
					binPositions[i] = valueToPos(bins[0], bins[bins.length-1], bins[i], 
							this.getSize().width);
				}
				
				int max = 0;
				for (int v : negativeWeights.values())
					if (v > max) max = v;
				for (int v : generalizedWeights.values())
					if (v > max) max = v;
				
				// Draw the line and values
				g.setColor(Color.black);
				g.drawLine(0, this.getSize().height-50, this.getSize().width, this.getSize().height-50);
				for (int i = 0; i < bins.length; i++) {
					g.drawString(" ^- "+(bins[i]), binPositions[i], this.getSize().height-25);
				}
				
				// Draw the rectangles
				for (int i = 1; i < bins.length; i++) {
					int negnr = negativeWeights.get(bins[i]);
					int gennr = generalizedWeights.get(bins[i]);
					
					int width = (int) Math.floor(((double)binPositions[i] - (double)binPositions[i-1]) / 2d);
					int height1 = valueToPos(0, max, negnr, this.getSize().height-100);
					int height2 = valueToPos(0, max, gennr, this.getSize().height-100);

					g.setColor(Color.red);
					g.fillRect(binPositions[i-1], this.getSize().height-50-height1, width, height1);
					g.setColor(Color.blue);
					g.fillRect(binPositions[i-1]+width, this.getSize().height-50-height2, width, height2);
					g.setColor(Color.black);
					g.drawString(" v- "+negnr, binPositions[i-1], this.getSize().height-75-height1);
					g.drawString(" v- "+gennr, binPositions[i-1]+width, this.getSize().height-75-height2);
				}
			}
		};
		
		rightPane.setLayout(new BoxLayout(rightPane, BoxLayout.PAGE_AXIS));
		
		int negwin = -1;
		int genwin = -1;
		boolean useBag = false;
		boolean useWeighted = false;
		boolean useBothRat = false;
		boolean useCut = false;
		if (inducer instanceof LogTreeWeightedNegativeEventInducer) {
			negwin = ((LogTreeWeightedNegativeEventInducer) inducer).getNegWindowSize();
			genwin = ((LogTreeWeightedNegativeEventInducer) inducer).getGenWindowSize();
			useBag = false;
			useWeighted = ((LogTreeWeightedNegativeEventInducer) inducer).isUseWeighted();
			useBothRat = ((LogTreeWeightedNegativeEventInducer) inducer).isUseBothWindowRatios();
			useCut = ((LogTreeWeightedNegativeEventInducer) inducer).isUseWindowOccurrenceCut();
		}
		if (inducer instanceof LogBagWeightedNegativeEventInducer) {
			negwin = ((LogBagWeightedNegativeEventInducer) inducer).getNegWindowSize();
			genwin = ((LogBagWeightedNegativeEventInducer) inducer).getGenWindowSize();
			useBag = true;
			useWeighted = ((LogBagWeightedNegativeEventInducer) inducer).isUseWeighted();;
			useBothRat = ((LogBagWeightedNegativeEventInducer) inducer).isUseBothWindowRatios();;
			useCut = ((LogBagWeightedNegativeEventInducer) inducer).isUseWindowOccurrenceCut();;
		}
		
		
		final JCheckBox checkInducetype = new JCheckBox("Select to use bag prefix instead of sequence", useBag);
		checkInducetype.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				createInducer(checkInducetype.isSelected());
				recalculate();
			}
		});
		final JCheckBox checkWeighted = new JCheckBox("Use weighted metrics", useWeighted);
		checkWeighted.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (inducer instanceof LogTreeWeightedNegativeEventInducer)
					((LogTreeWeightedNegativeEventInducer) inducer).setUseWeighted(checkWeighted.isSelected());
				if (inducer instanceof LogBagWeightedNegativeEventInducer)
					((LogBagWeightedNegativeEventInducer) inducer).setUseWeighted(checkWeighted.isSelected());
				recalculate();
			}
		});
		final JCheckBox checkBothratios = new JCheckBox("Use longest and shortest matching windows", useBothRat);
		checkBothratios.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (inducer instanceof LogTreeWeightedNegativeEventInducer)
					((LogTreeWeightedNegativeEventInducer) inducer).setUseBothWindowRatios(checkBothratios.isSelected());
				if (inducer instanceof LogBagWeightedNegativeEventInducer)
					((LogBagWeightedNegativeEventInducer) inducer).setUseBothWindowRatios(checkBothratios.isSelected());
				recalculate();
			}
		});
		final JCheckBox checkStopoccurrence = new JCheckBox("Cut windows at next event occurrence", useCut);
		checkStopoccurrence.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (inducer instanceof LogTreeWeightedNegativeEventInducer)
					((LogTreeWeightedNegativeEventInducer) inducer).setUseWindowOccurrenceCut(checkStopoccurrence.isSelected());
				if (inducer instanceof LogBagWeightedNegativeEventInducer)
					((LogBagWeightedNegativeEventInducer) inducer).setUseWindowOccurrenceCut(checkStopoccurrence.isSelected());
				recalculate();
			}
		});
		final JSlider negSlider = new JSlider(-1, 10, negwin);
		negSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (negSlider.getValue() == 0)
					return;
				if (inducer instanceof LogTreeWeightedNegativeEventInducer)
					((LogTreeWeightedNegativeEventInducer) inducer).setNegWindowSize(negSlider.getValue());
				if (inducer instanceof LogBagWeightedNegativeEventInducer)
					((LogBagWeightedNegativeEventInducer) inducer).setNegWindowSize(negSlider.getValue());
				recalculate();
			}
		});
		final JSlider genSlider = new JSlider(-1, 10, genwin);
		genSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if (genSlider.getValue() == 0)
					return;
				if (inducer instanceof LogTreeWeightedNegativeEventInducer)
					((LogTreeWeightedNegativeEventInducer) inducer).setGenWindowSize(genSlider.getValue());
				if (inducer instanceof LogBagWeightedNegativeEventInducer)
					((LogBagWeightedNegativeEventInducer) inducer).setGenWindowSize(genSlider.getValue());
				recalculate();
			}
		});
		
		final JButton logExporter = new JButton("Export Log");
		logExporter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				logExporter.setEnabled(false);
				logExporter.setText("Exporting...");
				XFactory xFactory = XFactoryRegistry.instance().currentDefault();
				XLog newlog = xFactory.createLog((XAttributeMap) log.getAttributes().clone());
				for (XTrace trace : log) {
					XTrace newtrace = xFactory.createTrace((XAttributeMap) trace.getAttributes().clone());
					for (int position = 0; position < trace.size(); position++) {
						Set<WeightedEventClass> negatives = inducer.getNegativeEvents(trace, position);
						Set<WeightedEventClass> generalized = inducer.getGeneralizedEvents(trace, position);
						for (WeightedEventClass we : negatives) {
							XEvent ne = xFactory.createEvent();
							XConceptExtension.instance().assignName(ne, we.eventClass.getId());
							XLifecycleExtension.instance().assignTransition(ne,
									XLifecycleExtension.instance().extractTransition(trace.get(position))+"-negative");
							ne.getAttributes().put("weight", xFactory.createAttributeContinuous("weight", we.weight, null));
							newtrace.add(ne);
						}
						for (WeightedEventClass we : generalized) {
							XEvent ne = xFactory.createEvent();
							XConceptExtension.instance().assignName(ne, we.eventClass.getId());
							XLifecycleExtension.instance().assignTransition(ne,
									XLifecycleExtension.instance().extractTransition(trace.get(position))+"-generalized");
							ne.getAttributes().put("weight", xFactory.createAttributeContinuous("weight", we.weight, null));
							newtrace.add(ne);
						}
						newtrace.add(xFactory.createEvent((XAttributeMap) trace.get(position).getAttributes().clone()));
					}
					newlog.add(newtrace);
				}
				logExporter.setEnabled(true);
				logExporter.setText("Export Log");
				context.getProvidedObjectManager().createProvidedObject("Exported Log with Negative Events", 
						newlog, XLog.class, context);
			}
		});
		
		rightPane.add(checkInducetype);
		rightPane.add(checkWeighted);
		rightPane.add(checkBothratios);
		rightPane.add(checkStopoccurrence);
		rightPane.add(negSlider);
		rightPane.add(genSlider);
		
		rightPane.add(logExporter);
		
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, distributionPanel, rightPane);
		this.add(splitPane, BorderLayout.CENTER);
		
		recalculate();
	}
	
	public void recalculate() {
		resetBins();
		
		for (XTrace trace : log) {
			for (int position = 0; position < trace.size(); position++) {
				
				Set<WeightedEventClass> negatives = inducer.getNegativeEvents(trace, position);
				Set<WeightedEventClass> generalized = inducer.getGeneralizedEvents(trace, position);
				for (WeightedEventClass we : negatives) {
					incrementBin(we.weight, negativeWeights);
				}
				for (WeightedEventClass we : generalized) {
					incrementBin(we.weight, generalizedWeights);
				}
			}
		}
		
		distributionPanel.revalidate();
		distributionPanel.repaint();
	}
	
}
