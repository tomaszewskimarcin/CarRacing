package guis;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JFrame;
import guis.components.TrackComponent;

@SuppressWarnings("serial")
public class TrackGui extends JFrame {
	
	private TrackComponent tc;	
	private int tileSize = 0;
	private Point[] carsPositions = new Point[4];
	private char[][] trackASCII = new char[10][20];
	
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

}
