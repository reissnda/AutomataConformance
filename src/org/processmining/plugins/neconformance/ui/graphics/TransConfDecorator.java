package org.processmining.plugins.neconformance.ui.graphics;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

public class TransConfDecorator implements ITransitionPDecorator {
	private Color[] colors;
	private int[] values;
	private boolean lightColorLabel = false;
	private String label;

	private static Font DEFFONT = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
	private static int MARGIN = 3;

	@SuppressWarnings("unused")
	private TransConfDecorator() {
	};

	public TransConfDecorator(int[] values, Color[] colors, String label, boolean lightColorLabel) {
		this.values = values;
		this.colors = colors;
		this.label = label;
		this.lightColorLabel = lightColorLabel;
	}

	public void decorate(Graphics2D g2d, double x, double y, double width, double height) {
		int total = 0; for (int i : values) total+=i;
		int lastx = (int) x;
		
		g2d.setColor(Color.white);
		g2d.fillRect((int) x, (int) (y), (int) (width), (int) (height));

		for (int i = 0; i < values.length; i++) {
			if (values[i] <= 0) continue;
			
			int w = (int) ((values[i] * (width)) / (double) total);
			if (w <= 0) continue;
			
			g2d.setColor(colors[i]);
			g2d.fillRect((int) lastx, (int) (y), (int) (w), (int) (height));
			lastx += w;
		}

		g2d.setColor(Color.black);
		g2d.drawRect((int) x, (int) (y), (int) (width), (int) (height));
		
		JLabel nodeName;

		StringBuilder sb = new StringBuilder();
		sb.append("<html><div style=\"align:center;width:" + (width - (2 * MARGIN)) + "px;\">");
		sb.append(label.replace("+complete", "").replace("\\ncomplete", ""));
		sb.append("</div></html>");

		nodeName = new JLabel(sb.toString());
		sb.setLength(0);

		FontMetrics metrics = g2d.getFontMetrics(DEFFONT);
		int hgt = (int) height - (1 * MARGIN);
		int adv = metrics.stringWidth(nodeName.getText());

		final int labelX = (int) x + MARGIN;
		final int labelY = (int) y + 1;
		final int labelW = adv;
		final int labelH = hgt;

		nodeName.setPreferredSize(new Dimension(labelW, labelH));
		nodeName.setSize(new Dimension(labelW, labelH));

		nodeName.setFont(DEFFONT);
		nodeName.validate();
		if (lightColorLabel) {
			nodeName.setForeground(Color.WHITE);
		} else {
			nodeName.setForeground(Color.BLACK);
		}
		nodeName.paint(g2d.create(labelX, labelY, labelW, labelH));
		
	}

}
