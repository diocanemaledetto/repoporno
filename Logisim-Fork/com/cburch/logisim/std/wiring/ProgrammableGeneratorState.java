package com.cburch.logisim.std.wiring;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceData;
import com.cburch.logisim.util.LocaleManager;

public class ProgrammableGeneratorState implements InstanceData, Cloneable {
	Value sending = Value.FALSE;
	private int[] durationHigh;
	private int[] durationLow;
	private String SavedData = "";
	// number of clock ticks performed in the current state
	private int ticks, currentstate;
	private JTextField[] inputs;

	public ProgrammableGeneratorState(int i) {
		durationHigh = new int[i];
		durationLow = new int[i];
		clearValues();
	}

	public void clearValues() {
		this.ticks = 0;
		this.currentstate = 0;
		// set all the values to 1
		for (int i = 0; i < durationHigh.length; i++) {
			durationHigh[i] = 1;
			durationLow[i] = 1;
		}
		this.SavedData = "";
	}

	@Override
	public ProgrammableGeneratorState clone() {
		try {
			return (ProgrammableGeneratorState) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public void decodeSavedData(String s) {
		// if empty, all to false so don't do anything
		if (s == null || s.equals(""))
			return;
		// split the attribute content string in an array of strings with a single
		// information each one
		String[] datas = s.split(" "), tmp;
		int value, cnt = 0;
		for (int i = 0; i < datas.length; i++) {
			// if contains a '*' it has to fill the array with the first value for x (second
			// number) cycles
			if (datas[i].contains("*")) {
				tmp = datas[i].split("\\*");
				for (int j = 0; j < Integer.parseInt(tmp[1]); j++) {
					value = Integer.parseInt(tmp[0]);
					writeData(value, cnt);
					cnt++;
				}
			} else {
				value = Integer.parseInt(datas[i]);
				writeData(value, cnt);
				cnt++;
			}
		}
	}

	public void editWindow() {
		// array of jtextfields, here will be saved the new values
		inputs = new JTextField[this.durationHigh.length + this.durationLow.length];
		// save / clear text for buttons transalted
		String[] options = new String[] { new LocaleManager("resources/logisim", "gui").get("saveOption"),
				Strings.get("ramClearMenuItem") };
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbs = new GridBagConstraints();
		// insets between states
		Insets inset = new Insets(5, 0, 5, 0);
		// state number font
		Font state = new Font(Font.SANS_SERIF, Font.BOLD, 18);
		JLabel statenumber;
		JLabel up = new JLabel("<html><div style='text-align:center'>" + Strings.get("clockHighAttr") + "<br>("
				+ Strings.get("clockDurationValue", "") + ")</div></html>");
		gbs.ipadx = 10;
		gbs.ipady = 5;
		gbs.gridx = 1;
		gbs.anchor = GridBagConstraints.CENTER;
		gbs.insets = inset;
		panel.add(up, gbs);
		JLabel down = new JLabel("<html><div style='text-align:center'>" + Strings.get("clockHighAttr") + "<br>("
				+ Strings.get("clockDurationValue", "") + ")</div></html>");
		gbs.gridx = 2;
		panel.add(down, gbs);
		// 2 inputs a row
		for (int i = 0; i < inputs.length; i += 2) {
			// number of state to edit
			statenumber = new JLabel(String.valueOf(i / 2 + 1));
			statenumber.setFont(state);
			statenumber.setForeground(Color.DARK_GRAY);
			gbs.gridx = 0;
			gbs.gridy = i + 1;
			// x padding
			panel.add(statenumber, gbs);
			// high duration edit box
			inputs[i] = new JTextField(String.valueOf(getdurationHigh(i / 2)), 3);
			inputs[i].setHorizontalAlignment(JTextField.CENTER);
			gbs.gridx = 1;
			panel.add(inputs[i], gbs);
			// low duration edit box
			inputs[i + 1] = new JTextField(String.valueOf(getdurationLow(i / 2)), 3);
			inputs[i + 1].setHorizontalAlignment(JTextField.CENTER);
			gbs.gridx = 2;
			panel.add(inputs[i + 1], gbs);
		}
		JScrollPane scrollable = new JScrollPane(panel);
		scrollable.setPreferredSize(new Dimension(250, 250));
		scrollable.setBorder(null);
		scrollable.getVerticalScrollBar().setUnitIncrement(13);
		int option = JOptionPane.showOptionDialog(null, scrollable,
				Strings.getter("ProgrammableGeneratorComponent").get(), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, options, null);
		if (option == 0) {
			// save
			SaveValues(inputs);
			SaveData();
		} else if (option == 1)
			// clear
			clearValues();
	}

	private int getdurationHigh(int i) {
		return this.durationHigh[i];
	}

	public int getdurationHighValue() {
		return this.durationHigh[this.currentstate];
	}

	private int getdurationLow(int i) {
		return this.durationLow[i];
	}

	public int getdurationLowValue() {
		return this.durationLow[this.currentstate];
	}

	public String getSavedData() {
		// return the string to save in the .circ
		return SavedData;
	}

	public int getStateTick() {
		return this.ticks;
	}

	public void incrementCurrentState() {
		this.ticks = 1;
		this.currentstate++;
		if (this.currentstate >= this.durationHigh.length)
			this.currentstate = 0;
	}

	public void incrementTicks() {
		this.ticks++;
		if (this.ticks > getdurationHighValue() + getdurationLowValue())
			incrementCurrentState();
	}

	public void SaveData() {
		int size = this.durationHigh.length + this.durationLow.length, count = 0;
		String val, data = "", last = "x";
		boolean dirty = false;
		// input-and matrix
		for (int i = 0; i < size; i++) {
			// 1= not line selected, 2 = input line selected, 0 = nothing selected in that
			// input line
			if (i < this.durationHigh.length)
				val = String.valueOf(getdurationHigh(i));
			else
				val = String.valueOf(getdurationLow(i - this.durationHigh.length));
			if (!dirty && !val.equals("1"))
				dirty = true;
			if (val.equals(last))
				count++;
			else if (last.equals("x")) {
				last = val;
				count++;
			}
			if (!val.equals(last) || i == size - 1) {
				if (count >= 3)
					data += last + "*" + count + " ";
				else
					for (int j = 0; j < count; j++)
						data += last + " ";
				if (!val.equals(last) && i == size - 1)
					data += val + " ";
				count = 1;
				last = val;
			}
		}
		if (!dirty)
			data = "";
		this.SavedData = data;
	}

	private void SaveValues(JTextField[] inputs) {
		String onlynumber;
		int value;
		for (int i = 0; i < inputs.length; i++) {
			onlynumber = "";
			value = 0;
			// create a string composed by the digits of the text field
			for (int j = 0; j < inputs[i].getText().length(); j++) {
				if (Character.isDigit(inputs[i].getText().charAt(j)))
					onlynumber += inputs[i].getText().charAt(j);
			}
			// if there are no digits the value is 0 and it isn't saved
			if (onlynumber != "")
				value = Integer.parseInt(onlynumber);
			if (value >= 1) {
				if (i % 2 == 0)
					setdurationHigh(i / 2, value);
				else
					setdurationLow(i / 2, value);
			}
		}
	}

	public void setdurationHigh(int i, int value) {
		if (value != getdurationHigh(i))
			this.durationHigh[i] = value;
	}

	public void setdurationLow(int i, int value) {
		if (value != getdurationLow(i))
			this.durationLow[i] = value;
	}

	public boolean updateSize(int newsize) {
		if (newsize != this.durationHigh.length) {
			// update arrays size maintaining values
			int[] oldDurationHigh = Arrays.copyOf(durationHigh, durationHigh.length);
			int[] oldDurationLow = Arrays.copyOf(durationLow, durationLow.length);
			durationHigh = new int[newsize];
			durationLow = new int[newsize];
			clearValues();
			int lowerlength = (oldDurationHigh.length < newsize) ? oldDurationHigh.length : newsize;
			for (int i = 0; i < lowerlength; i++) {
				durationHigh[i] = oldDurationHigh[i];
				durationLow[i] = oldDurationLow[i];
			}
			SaveData();
			return true;
		}
		return false;
	}

	private void writeData(int value, int cnt) {
		if (cnt < this.durationHigh.length)
			setdurationHigh(cnt, value);
		else if (cnt < this.durationHigh.length + this.durationLow.length)
			setdurationLow(cnt - this.durationHigh.length, value);
	}
}
