package guis;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import agents.Car;

@SuppressWarnings("serial")
public class CarGui extends JFrame{
	
	@SuppressWarnings("unused")
	private Car c;
	private JComboBox<String> pace;
	private String[] paces = {"normal","aggresive","defensive"};
	
	public CarGui(Car c){
		
		super(c.getLocalName());
		
		pace = new JComboBox<>(paces);
		
		this.c = c;
		
		JPanel panel = new JPanel(new GridLayout(1, 2));
		
		panel.add(new JLabel("PACE:"));
		panel.add(pace);
		
		getContentPane().add(panel);
		
		setResizable(false);
		
		pace.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(c.getPace() != pace.getSelectedIndex()){
					c.setPace(pace.getSelectedIndex());
				}
			}
		});
		
	}
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}
}
