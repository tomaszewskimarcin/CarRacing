package agents;

import java.awt.Point;

import guis.CarGui;
import jade.core.Agent;

@SuppressWarnings("serial")
public class Car extends Agent{

	private int number;
	private Point pos = new Point(0, 0);
	private int spd = 2;
	private int mode;
	private int time = 0;
	private int bestLap = 0;
	private boolean hasSP = false;
	private boolean started = false;
	
	@Override
	protected void setup(){
		System.out.printf("Started car %s\n",getAID().getName());
		
		while(!hasSP){
			getStartPosition();
		}
		while(!started){
			checkIfStarted();
		}
		doTheRace();
		
	}
	
	@Override
	protected void takeDown(){
		System.out.printf("Taking down car %s\n", getAID().getName());
	}
	
	private void getStartPosition(){
		hasSP = true;
	}
	
	private void checkIfStarted(){
		started = true;
	}
	
	private void doTheRace(){
		CarGui gui = new CarGui(this);
		gui.showGui();
	};
	
	public void penalty(){
		
	}
	
	public int getNumber(){
		return number;
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
