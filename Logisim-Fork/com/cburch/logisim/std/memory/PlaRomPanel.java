package com.cburch.logisim.std.memory;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import com.cburch.logisim.prefs.AppPreferences;
import com.cburch.logisim.std.ttl.Drawgates;
import com.cburch.logisim.util.GraphicsUtil;

public class PlaRomPanel extends JPanel implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7182231893518001053L;
	private static final int IMAGE_BORDER = 20;
	private PlaRomData data;

	public PlaRomPanel(PlaRomData data) {
		this.data = data;
		super.addMouseListener(this);
	}

	private void drawNot(Graphics g, int x, int y) {
		int[] xp = new int[4];
		int[] yp = new int[4];
		xp[0] = x - 6;
		yp[0] = y;
		xp[1] = x;
		yp[1] = y + 13;
		xp[2] = x + 6;
		yp[2] = y;
		xp[3] = x - 6;
		yp[3] = y;
		g.drawPolyline(xp, yp, 4);
		g.drawOval(x - 3, y + 14, 6, 6);
	}

	private void drawOr(Graphics g, int x, int y) {
		GraphicsUtil.drawCenteredArc(g, x + 21, y - 1, 36, 180, 53);
		GraphicsUtil.drawCenteredArc(g, x - 21, y - 1, 36, 0, -53);
		GraphicsUtil.drawCenteredArc(g, x, y - 28, 30, -120, 60);
	}

	private int getColumn(int x) {
		x += (20 - IMAGE_BORDER);
		int column = x / 20;
		return column;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension((data.getInputs() + data.getOutputs() + 1) * 40 + (2 * IMAGE_BORDER),
				(data.getAnd() + 2) * 40 + 25 + (2 * IMAGE_BORDER));
	}

	private int getRow(int y) {
		y -= (20 + IMAGE_BORDER);
		int row = y / 20;
		return row;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// click area arownd node is 20*20
		int row = getRow(e.getY());
		int column = getColumn(e.getX());
		int column2 = getColumn(e.getX() + 10);
		if (row % 2 == 0 && row > 0 && column > 0) {
			row = row / 2 - 1;
			if (row <= data.getAnd() - 1) {
				// is a clickable area
				if (column <= data.getInputs() * 2) {// input and area
					column -= 1;
					if (column % 2 == 1 && !data.getInputAndValue(row, column)
							&& data.getInputAndValue(row, column - 1))
						data.setInputAndValue(row, column - 1, false);
					else if (column % 2 == 0 && !data.getInputAndValue(row, column)
							&& data.getInputAndValue(row, column + 1))
						data.setInputAndValue(row, column + 1, false);
					data.setInputAndValue(row, column, !data.getInputAndValue(row, column));
					repaint();
				} else if (column2 > data.getInputs() * 2 + 3) {// and or area
					column2 -= (data.getInputs() * 2 + 4);
					if (column2 % 2 == 0) {
						column2 /= 2;
						data.setAndOutputValue(row, column2, !data.getAndOutputValue(row, column2));
						repaint();
					}
				}
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void paintComponent(Graphics g) {
		if (AppPreferences.ANTI_ALIASING.getBoolean()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		super.paintComponent(g);
		int inputs = data.getInputs();
		int outputs = data.getOutputs();
		int and = data.getAnd();
		GraphicsUtil.switchToWidth(g, 2);
		g.setColor(Color.DARK_GRAY);
		g.setFont(new Font("sans serif", Font.BOLD, 14));
		GraphicsUtil.drawCenteredText(g, "\u2190" + Strings.getter("demultiplexerInTip").toString(),
				40 * (inputs + 1) - (20 - IMAGE_BORDER) + 5, IMAGE_BORDER);
		GraphicsUtil.drawCenteredText(g, Strings.getter("multiplexerOutTip").toString() + "\u2192",
				IMAGE_BORDER + 20 + 40 * inputs - 10, IMAGE_BORDER + 100 + 40 * and);
		for (Integer i = 1; i <= inputs; i++) {
			Color inputColor = data.getInputValue(i - 1).getColor();
			Color notColor = data.getInputValue(i - 1).not().getColor();
			g.setColor(inputColor);
			// draw input value
			GraphicsUtil.drawCenteredText(g, data.getInputValue(i - 1).toString(), 40 * i - (20 - IMAGE_BORDER),
					IMAGE_BORDER);
			g.drawLine(40 * i - (20 - IMAGE_BORDER), IMAGE_BORDER + 15, 40 * i - (20 - IMAGE_BORDER),
					IMAGE_BORDER + 22);
			g.drawLine(40 * i - (30 - IMAGE_BORDER), IMAGE_BORDER + 22, 40 * i - (10 - IMAGE_BORDER),
					IMAGE_BORDER + 22);
			g.drawLine(40 * i - (30 - IMAGE_BORDER), IMAGE_BORDER + 22, 40 * i - (30 - IMAGE_BORDER),
					IMAGE_BORDER + 30);
			g.drawLine(40 * i - (10 - IMAGE_BORDER), IMAGE_BORDER + 22, 40 * i - (10 - IMAGE_BORDER),
					IMAGE_BORDER + 70 + 40 * (and - 1));
			g.setColor(notColor);
			g.drawLine(40 * i - (30 - IMAGE_BORDER), IMAGE_BORDER + 50, 40 * i - (30 - IMAGE_BORDER),
					IMAGE_BORDER + 70 + 40 * (and - 1));
			g.setColor(Color.BLACK);
			drawNot(g, 40 * i - (30 - IMAGE_BORDER), IMAGE_BORDER + 30);
		}
		for (Integer i = 1; i <= and; i++) {
			g.drawLine(IMAGE_BORDER + 10, IMAGE_BORDER + 30 + 40 * i, IMAGE_BORDER + 4 + 40 * inputs,
					IMAGE_BORDER + 30 + 40 * i);
			g.setColor(data.getAndValue(i - 1).getColor());
			g.drawLine(IMAGE_BORDER + 36 + 40 * inputs, IMAGE_BORDER + 30 + 40 * i,
					IMAGE_BORDER + 40 * (inputs + 1) + 20 + 40 * (outputs - 1), IMAGE_BORDER + 30 + 40 * i);
			g.setColor(Color.BLACK);
			Drawgates.paintAnd(g, IMAGE_BORDER + 36 + 40 * inputs, IMAGE_BORDER + 30 + 40 * i, 32, 32, false);
		}
		for (Integer i = 1; i <= outputs; i++) {
			g.drawLine(IMAGE_BORDER + 20 + 40 * (inputs + i), IMAGE_BORDER + 70, IMAGE_BORDER + 20 + 40 * (inputs + i),
					IMAGE_BORDER + 54 + 40 * and);
			g.setColor(data.getOutputValue(i - 1).getColor());
			g.drawLine(IMAGE_BORDER + 20 + 40 * (inputs + i), IMAGE_BORDER + 82 + 40 * and,
					IMAGE_BORDER + 20 + 40 * (inputs + i), IMAGE_BORDER + 89 + 40 * and);
			GraphicsUtil.drawCenteredText(g, data.getOutputValue(i - 1).toString(),
					IMAGE_BORDER + 20 + 40 * (inputs + i), IMAGE_BORDER + 100 + 40 * and);
			g.setColor(Color.BLACK);
			drawOr(g, IMAGE_BORDER + 20 + 40 * (inputs + i), IMAGE_BORDER + 54 + 40 * and);
		}
		for (int i = 0; i < and; i++) {
			for (int j = 0; j < inputs * 2; j++) {
				if (data.getInputAndValue(i, j)) {
					g.setColor(Color.WHITE);
					g.fillOval(IMAGE_BORDER + 10 + 20 * j - 4, IMAGE_BORDER + 70 + 40 * i - 4, 8, 8);
					g.setColor(Color.BLACK);
					g.drawOval(IMAGE_BORDER + 10 + 20 * j - 4, IMAGE_BORDER + 70 + 40 * i - 4, 8, 8);
				}
			}
			for (int k = 0; k < outputs; k++) {
				if (data.getAndOutputValue(i, k)) {
					g.setColor(Color.WHITE);
					g.fillOval(IMAGE_BORDER + 20 + 40 * (inputs + 1) + 40 * k - 4, IMAGE_BORDER + 70 + 40 * i - 4, 8,
							8);
					g.setColor(Color.BLACK);
					g.drawOval(IMAGE_BORDER + 20 + 40 * (inputs + 1) + 40 * k - 4, IMAGE_BORDER + 70 + 40 * i - 4, 8,
							8);
				}
			}
		}
	}
}