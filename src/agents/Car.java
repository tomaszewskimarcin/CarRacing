package agents;

import java.awt.Point;

import guis.CarGui;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;

@SuppressWarnings("serial")
public class Car extends Agent{

	private int number;
	private Point pos = new Point(0, 0);
	private int spd = 2;
	private int pace = 0;
	private int time = 0;
	private int bestLap = 0;
	private boolean hasSP = false;
	private boolean started = false;
	private AID track = new AID("track", AID.ISLOCALNAME);
	private AID judge = new AID("judge", AID.ISLOCALNAME);
	
	@Override
	protected void setup(){
		System.out.printf("Started car %s\n",getAID().getName());

		System.out.printf("Car agent %s getting start position.", getAID().getName());
		while(!hasSP){
			getStartPosition();
		}
		System.out.printf("Car agent %s waiting for start.", getAID().getName());
		while(!started){
			checkIfStarted();
		}
		System.out.printf("Car agent %s recived start command.", getAID().getName());
		doTheRace();
		
	}
	
	@Override
	protected void takeDown(){
		System.out.printf("Taking down car %s\n", getAID().getName());
	}
	
	private void getStartPosition(){
		addBehaviour(new OneShotBehaviour() {
			
			@Override
			public void action() {
				
			}
		});
	}
	
	private void checkIfStarted(){
		addBehaviour(new OneShotBehaviour() {
			
			@Override
			public void action() {
				
			}
		});
	}
	
	private void doTheRace(){
		CarGui gui = new CarGui(this);
		gui.showGui();
		addBehaviour(new CyclicBehaviour() {
			
			@Override
			public void action() {
				sendData();
				doDelete();
			}
		});
	};
	
	private void sendData(){
		
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
	
	public void setPace(int pace){
		if(pace>=0 && pace <=2){
			this.pace = pace;
			System.out.println(pace);
		}
	}
	
	public int getPace(){
		return pace;
	}
	
}
