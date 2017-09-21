package guis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import guis.components.TrackComponent;

@SuppressWarnings("serial")
public class TrackGui extends JFrame {
	
	private TrackComponent tc;	
	private int tileSize = 0;
	private Point[] carsPositions = new Point[4];
	private char[][] trackASCII = new char[10][20];
	private HashMap<Integer, String> finalResults = new HashMap<>();
	private Color[] colors = {Color.RED,Color.BLUE,Color.GREEN,Color.YELLOW};
	
	public TrackGui(char[][] trackASCII,Point[] carsPositions,int tileSize){
		
		super("Track view");
		
		this.trackASCII = trackASCII;
		this.carsPositions = carsPositions;
		this.tileSize = tileSize;
		
		setResizable(false);
		setLayout(new BorderLayout());
		tc = new TrackComponent(this.trackASCII, this.carsPositions, this.tileSize);
		add(tc, BorderLayout.CENTER);
		
	}
	
	public void showGui(){
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}
	
	public void updatePos(Point[] updatedPositions){
		tc.updatePos(updatedPositions);
	}
	
	public void showHideCars() {
		tc.showHideCars();
	}

	public void showStandings(String data) {
		String[] splitted = data.split(",");
		for(int i = 0; i<splitted.length; i++) {
			String[] carAndData = splitted[i].split("-");
			finalResults.put(Integer.parseInt(carAndData[0]), carAndData[1]);
		}
		JPanel panel = new JPanel(new GridLayout(5, 1));
		JPanel header = new JPanel(new GridLayout(1, 5));
		JPanel[] row = {new JPanel(new GridLayout(1, 5)),new JPanel(new GridLayout(1, 5)),new JPanel(new GridLayout(1, 5)),new JPanel(new GridLayout(1, 5))};
		
		header.add(new JLabel("POS."));
		header.add(new JLabel("NAME"));
		header.add(new JLabel("TIME"));
		header.add(new JLabel());
		header.add(new JLabel("PEN."));

		panel.add(header);
		
		for(int i = 0; i<row.length; i++) {
			Integer standing = i+1;
			row[i].add(new JLabel(standing.toString()));
			String[] tmp = finalResults.get(standing).split(":");
			JLabel name = new JLabel(tmp[2]);
			name.setOpaque(true);
			name.setBackground(colors[Integer.parseInt(tmp[2].substring(1))-1]);
			row[i].add(name);
			long timestamp = Long.parseLong(tmp[0]);
			int min = 0;
			while(timestamp>=60000) {
				min++;
				timestamp -= 60000;
			}
			row[i].add(new JLabel(min+":"+timestamp));
			row[i].add(new JLabel());
			row[i].add(new JLabel(tmp[1]));
			panel.add(row[i]);
		}
		
		JOptionPane.showMessageDialog(null,panel,"Final Standings",JOptionPane.INFORMATION_MESSAGE);
	}
	
}
