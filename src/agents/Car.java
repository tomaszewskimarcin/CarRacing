package agents;

import java.awt.Point;

public class Car {

	private int number;
	private String name;
	private Point pos = new Point(0, 0);
	private int spd = 2;
	private int mode;
	private int time = 0;
	private int bestLap = 0;
	
	public Car(int number, String name, int x,int y){
		this.number = number;
		this.name = name;
		mode = 1;
		pos.setLocation(x, y);
	}
	
	public void penalty(){
		
	}
	
	public int getNumber(){
		return number;
	}
	
	public String getName(){
		return name;
	}
	
	public int getTime(){
		return time;
	}
	
	public int getBestLap(){
		return bestLap;
	}
	
	public void setMode(int mode){
		if(mode>=1 && mode <=3){
			this.mode = mode;
		}
	}
	
	public int getMode(){
		return mode;
	}
	
	public void setSpd(int spd){
		this.spd = spd;
	}
	
	public int getSpd(){
		return spd;
	}
	
	public void setPos(int x, int y){
		pos.setLocation(x, y);
	}
	
	public Point getPos(){
		return pos;
	}
	
}
