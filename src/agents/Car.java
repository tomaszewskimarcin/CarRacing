package agents;

import java.awt.Point;

import jade.core.Agent;

@SuppressWarnings("serial")
public class Car extends Agent{

	private int number;
	private Point pos = new Point(0, 0);
	private int spd = 2;
	private int mode;
	private int time = 0;
	private int bestLap = 0;
	
	@Override
	protected void setup(){
		System.out.printf("Started car %s\n",getAID().getName());
		
		Object[] args = getArguments();
		if(args.length == 2){
			pos.setLocation(Integer.parseInt((String) args[0]), Integer.parseInt((String) args[1]));
		}else if(args.length == 3){
			pos.setLocation(Integer.parseInt((String) args[0]), Integer.parseInt((String) args[1]));
			spd = Integer.parseInt((String) args[2]);
		}else{
			System.out.print("Missing arguments!\n");
			takeDown();
		}
		
	}
	
	@Override
	protected void takeDown(){
		System.out.printf("Taking down car %s\n", getAID().getName());
	}
	
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
