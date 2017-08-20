package guis.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

@SuppressWarnings("serial")
public class TrackComponent extends JComponent {

	private int tileSize = 0;
	private char[][] trackASCII = new char[10][20];
	private Point[] carsPositions = new Point[4];
	private Color[] carsColors = {Color.RED,Color.BLUE,Color.GREEN,Color.YELLOW};
	private Color trackColor = Color.BLACK;
	
	private BufferedImage image;
	
	private Graphics2D gr;
	
	public TrackComponent(char[][] trackASCII, Point[] carsPositions, int tileSize){
		
		this.trackASCII = trackASCII;
		this.carsPositions = carsPositions;
		this.tileSize = tileSize;
		setDoubleBuffered(false);
		
		setPreferredSize(new Dimension(20*this.tileSize, 10*this.tileSize));
		
	}
	
	protected void paintComponent(Graphics g){

		gr = (Graphics2D) g;
		
		if(image==null){
			image = (BufferedImage) createImage(getSize().width,getSize().height);
			gr = (Graphics2D) image.createGraphics();
			gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			clear();
		}
		
		g.drawImage(image, 0, 0, null);

		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		
		//Shape s = new Rectangle(0, 0, tileSize, tileSize);
		
		//gr.draw(s);
		//gr.fill(s);
		
		gr.setPaint(trackColor);
		
		Shape s;
		int tmpX = 0;
		int tmpY = 0;
		int tmpXstart = 0;
		int tmpYstart = 0;
		int part = (int) Math.floor(tileSize/4);
		boolean draw = true;
		
		for(int i = 0; i<10; i++){
			for(int j = 0; j<20; j++){
				if(trackASCII[i][j] == '#' || trackASCII[i][j] == '!'){
					if(trackASCII[i][j] == '!'){
						gr.setPaint(Color.red);
					}else{
						gr.setPaint(trackColor);
					}
					tmpX = j*tileSize;
					tmpY = i*tileSize;
					s = new Rectangle(tmpX, tmpY, tileSize, tileSize);
					gr.draw(s);
					gr.fill(s);
				}else if(trackASCII[i][j] == 'D' || trackASCII[i][j] == 'U' || trackASCII[i][j] == 'R' || trackASCII[i][j] == 'L'){
					gr.setPaint(trackColor);
					tmpXstart = j*tileSize;
					tmpYstart = i*tileSize;
					for(int a = 0; a<4; a++){
						if(a%2!=0){
							draw = true;
						}else{
							draw = false;
						}
						for(int b = 0; b<4; b++){
							if(draw){
								s = new Rectangle(tmpXstart+b*part, tmpYstart+a*part, part, part);
								gr.draw(s);
								gr.fill(s);
								draw = false;
							}else{
								draw = true;
							}
						}
					}
				}
			}
		}
		
		for(int i = 0; i<4; i++){
			s = new Ellipse2D.Float(carsPositions[i].x,carsPositions[i].y,10,10);
			gr.setPaint(carsColors[i]);
			gr.draw(s);
			gr.fill(s);
		}
		
		gr.setPaint(trackColor);
		
	}
	
	public void clear(){
		gr.setPaint(Color.white);
		gr.fillRect(0, 0, getSize().width, getSize().height);
		repaint();
	}
	
	public void updatePos(Point[] updatedPositions){
		this.carsPositions = updatedPositions;
		repaint();
	}
	
}
