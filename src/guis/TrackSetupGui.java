package guis;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.*;
import javax.swing.text.NumberFormatter;

import agents.Track;

@SuppressWarnings("serial")
public class TrackSetupGui extends JFrame{
	
	private JButton start = new JButton("START");
	private JFormattedTextField laps;
	
	@SuppressWarnings("unused")
	private Track t;

	
	public TrackSetupGui(Track t){
		
		super(t.getLocalName());
		
		this.t = t;
		
		NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);
	    formatter.setAllowsInvalid(false);
	    formatter.setCommitsOnValidEdit(true);
	    laps = new JFormattedTextField(formatter);
		
		JPanel panel = new JPanel(new GridLayout(1, 3));
		
		panel.add(new JLabel("LAPS:"));
		panel.add(laps);
		panel.add(start);
		
		getContentPane().add(panel);
		
		setResizable(false);
		
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(laps.getText() != null && !laps.getText().equals("")){
					t.startRace(Integer.parseInt(laps.getText()));
				}
			}
		});
		
	}
	
	public void showGui() {
		pack();
		//t.showPositions();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}
}
