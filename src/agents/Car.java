package agents;

import java.awt.Point;
import java.awt.event.WindowEvent;

import javax.swing.SwingUtilities;

import guis.CarGui;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class Car extends Agent{

	private int number;
	private Point pos = new Point(0, 0);
	private int spd = 6;
	private int curSpd;
	private int pace = 0;
	private int time = 0;
	private int curLap = 1;
	private int laps = 0;
	private int bestLap = 0;
	private double dirX = 0;
	private double dirY = 0;
	private double targetDirX = 0;
	private double targetDirY = 0;
	private double targetDirXL = 0;
	private double targetDirYL = 0;
	private double targetDirXR = 0;
	private double targetDirYR = 0;
	private int tileSize = 0;
	private int newX = 0;
	private int newY = 0;
	private int multiply = 10;
	private boolean hasSP = false;
	private boolean started = false;
	private boolean checkChange = false;
	private boolean checkLeft = false;
	private boolean checkRight = false;
	private boolean doCheckRight = false;
	private boolean penalty = false;
	private double force = 0.01;
	private AID track = new AID("track", AID.ISLOCALNAME);
	private AID judge = new AID("judge", AID.ISLOCALNAME);
	private Point[] otherPos = {new Point(),new Point(), new Point()};
	CarGui gui;
	private int stage = 0; 
	
	@Override
	protected void setup(){
		System.out.printf("Started car %s\n",getAID().getLocalName());
		
		gui = new CarGui(this);
		
		System.out.printf("Car agent %s getting start position.\n", getAID().getLocalName());
			getStartPosition();
		
	}
	
	@Override
	protected void takeDown(){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));
				gui.dispose();
			}
		});
		System.out.printf("Taking down car %s\n", getAID().getLocalName());
	}
	
	private void getStartPosition(){
		addBehaviour(new TickerBehaviour(this, 100) {
			
			@Override
			protected void onTick() {
				
				ACLMessage msg = myAgent.receive();
				if(msg!=null){
					if(msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology()=="start-pos"){
						String posS = msg.getContent();
						String[] posT = posS.split(",");
						pos.setLocation(Integer.parseInt(posT[0])*multiply, Integer.parseInt(posT[1])*multiply);
						dirX = Integer.parseInt(posT[2]);
						dirY = Integer.parseInt(posT[3]);
						targetDirX = dirX;
						targetDirY = dirY;
						tileSize = Integer.parseInt(posT[4]);
						
						//Test communication
						//System.out.println("|"+pos.x+"|"+pos.y+"|"+dirX+"|"+dirY+"|"+tileSize);
						
						hasSP = true;
						
						msg = myAgent.receive();
						if(msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology()=="other-pos") {
							storeOthers(msg.getContent());
						}
						
					}
				}else{
					block();
				}
				
				if(hasSP){
					System.out.printf("Car agent %s waiting for start.\n", getAID().getLocalName());
					checkIfStarted();
					stop();
				}
			}
		});
	}
	
	private void checkIfStarted(){
		addBehaviour(new TickerBehaviour(this, 100) {
			
			@Override
			protected void onTick() {
				
				ACLMessage msg = myAgent.receive();
				if(msg!=null){
					if(msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology()=="nolaps"){
						doDelete();
					}else if(msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology()=="laps"){
						laps = Integer.parseInt(msg.getContent());
						started = true;
					}
				}else{
					block();
				}
				
				if(started){
					System.out.printf("Car agent %s recived start command.\n", getAID().getLocalName());
					doTheRace();
					stop();
				}
			}
		});
	}
	
	private void doTheRace(){
		gui.showGui();
		addBehaviour(new CyclicBehaviour() {

			ACLMessage msg;
			boolean sended;
			
			@Override
			public void action() {
				if(pace == 0) {
					curSpd = spd;
				}else if(pace == 1) {
					curSpd = spd+2;
				}else if(pace == 2) {
					curSpd = spd-2;
				}
				
				msg = myAgent.receive();
				if(msg!=null && msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology() == "nextlap"){
					curLap++;
					System.out.println("Car "+getLocalName()+" lap "+curLap);
				}else if(msg!=null && msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology() == "other-pos"){
					storeOthers(msg.getContent());
				}else if(msg!=null && msg.getSender().getLocalName().equals(judge.getLocalName()) && msg.getOntology() == "penalty"){
					penalty = true;
				}else if(msg!=null && msg.getSender().getLocalName().equals(judge.getLocalName()) && msg.getOntology() == "penalty-end"){
					penalty = false;
				}else {
					switch(stage){
					case 0:
						//send ask to track for further track and change position
						if(sended){
							if(msg!=null && msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology()=="is-clear-response"){
								if(msg.getContent().equals("clear")){
									//action if clear
									if(!checkOtherCars()||penalty) {
										curSpd = 1;
									}
									pos.x += (int)Math.floor(curSpd*dirX);
									pos.y += (int)Math.floor(curSpd*dirY);
									if(checkChange){
										if(checkLeft){
											targetDirX = targetDirXL;
											targetDirY = targetDirYL;
											checkLeft = false;
											checkRight = false;
										}
										if(checkRight){
											targetDirX = targetDirXR;
											targetDirY = targetDirYR;
											checkLeft = false;
											checkRight = false;
										}
									}
								}else if(msg.getContent().equals("notclear")){
									//action if not clear
									//System.out.println("Not clear");
									if(!checkOtherCars()||penalty) {
										curSpd = 1;
									}
									pos.x += (int)Math.floor(curSpd*dirX);
									pos.y += (int)Math.floor(curSpd*dirY);
									if(!checkChange){
										setTargetDir();
										checkChange = true;
									}
									if(checkLeft){
										checkLeft = false;
										doCheckRight = true;
									}
									if(checkRight){
										checkRight = false;
									}
								}
								sended = false;
								stage++;
							}
						}else if(!sended){
							if(targetDirX != dirX || targetDirY != dirY){
								updateDir();
							}
							msg = new ACLMessage(ACLMessage.INFORM);
							msg.addReceiver(track);
							msg.setOntology("is-clear");
							if(!checkChange){
								newX = pos.x+(int)Math.floor((tileSize*multiply)*dirX);
								newY = pos.y+(int)Math.floor((tileSize*multiply)*dirY);
							}else if(!checkLeft && !checkRight){
								if(!doCheckRight && !checkLeft){
									newX = pos.x+(int)Math.floor((tileSize*multiply*1.1)*targetDirXL);
									newY = pos.y+(int)Math.floor((tileSize*multiply*1.1)*targetDirYL);
									checkLeft = true;
								}else if(doCheckRight && !checkRight){
									doCheckRight = false;
									newX = pos.x+(int)Math.floor((tileSize*multiply*1.1)*targetDirXR);
									newY = pos.y+(int)Math.floor((tileSize*multiply*1.1)*targetDirYR);
									checkRight = true;
								}
							}
							msg.setContent(newX/multiply+","+newY/multiply);
							send(msg);
							sended = true;
						}
						break;
					case 1:
						//send positions to track and judge
						msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(track);
						msg.addReceiver(judge);
						msg.setOntology("cur-pos");
						msg.setContent(pos.x/multiply+","+pos.y/multiply);
						send(msg);
						stage++;
						break;
					case 2:
						//check if ended and report if needed
						if(curLap>laps){
							msg = new ACLMessage(ACLMessage.INFORM);
							msg.addReceiver(judge);
							msg.setOntology("ended");
							msg.setContent("");
							send(msg);
							stage = 3;
						}else{
							stage = 0;
						}
						break;
					case 3:
						//wait for race end
						if(msg!=null && msg.getSender().getLocalName().equals(judge.getLocalName()) && msg.getOntology() == "race-end"){
							started = false;
						}
						break;
					}
					
				}
				
				if(!started){
					sendData();
				}
			}
			
		});
	}
	
	private void sendData(){
		addBehaviour(new OneShotBehaviour() {
			
			@Override
			public void action() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(judge);
				msg.setOntology("end-data");
				msg.setContent(time+","+bestLap);
				send(msg);
				doDelete();
			}
			
		});
	}
	
	private void updateDir(){
		if(dirX == targetDirX && dirY == targetDirY && dirX == targetDirX && dirY == targetDirY){
			checkChange = false;
		}
		if(targetDirX > dirX){
			if(dirX + force > targetDirX){
				dirX = targetDirX;
			}else{
				dirX += force;
			}
		}
		if(targetDirX < dirX){
			if(dirX - force < targetDirX){
				dirX = targetDirX;
			}else{
				dirX -= force;
			}
		}
		if(targetDirY > dirY){
			if(dirY + force > targetDirY){
				dirY = targetDirY;
			}else{
				dirY += force;
			}
		}
		if(targetDirY < dirY){
			if(dirY - force < targetDirY){
				dirY = targetDirY;
			}else{
				dirY -= force;
			}
		}
		if(dirX == targetDirX && dirY == targetDirY && dirX == targetDirX && dirY == targetDirY){
			checkChange = false;
		}
	}
	
	private void setTargetDir(){
		if(dirX == 0 && dirY == 1){
			targetDirXL = -1;
			targetDirXR = 1;
			targetDirYL = 0;
			targetDirYR = 0;
		}else if(dirX == 0 && dirY == -1){
			targetDirXL = -1;
			targetDirXR = 1;
			targetDirYL = 0;
			targetDirYR = 0;
		}else if(dirX == -1 && dirY == 0){
			targetDirXL = 0;
			targetDirXR = 0;
			targetDirYL = -1;
			targetDirYR = 1;
		}else if(dirX == 1 && dirY == 0){
			targetDirXL = 0;
			targetDirXR = 0;
			targetDirYL = -1;
			targetDirYR = 1;
		}
	}
	
	public int getNumber(){
		return number;
	}
	
	public void setPace(int pace){
		if(pace>=0 && pace <=2){
			this.pace = pace;
		}
	}
	
	public int getPace(){
		return pace;
	}	

	private void storeOthers(String content) {
		String[] splitted = content.split(":");
		for( int i = 0; i<3; i++) {
			String[] xy = splitted[i].split(",");
			int x = Integer.parseInt(xy[0])*multiply;
			int y = Integer.parseInt(xy[1])*multiply;
			otherPos[i] = new Point(x, y);
		}
	}
	
	private boolean checkOtherCars() {
		boolean check = true;
		
		if(Math.abs(dirX)>Math.abs(dirY)) {
			if(dirX>0) {
				for(int i = 0; i<3; i++) {
					if(pos.x<otherPos[i].x) {
						if(distance(pos.x,pos.y,otherPos[i].x,otherPos[i].y)<=15) {
							check = false;
						}
					}
				}
			}else if(dirX<=0) {
				for(int i = 0; i<3; i++) {
					if(pos.x>otherPos[i].x) {
						if(distance(pos.x,pos.y,otherPos[i].x,otherPos[i].y)<=15) {
							check = false;
						}
					}
				}
			}
		}else if(Math.abs(dirY)>Math.abs(dirX)) {
			if(dirY>0) {
				for(int i = 0; i<3; i++) {
					if(pos.y<otherPos[i].y) {
						if(distance(pos.x,pos.y,otherPos[i].x,otherPos[i].y)<=15) {
							check = false;
						}
					}
				}
			}else if(dirY<=0) {
				for(int i = 0; i<3; i++) {
					if(pos.y>otherPos[i].y) {
						if(distance(pos.x,pos.y,otherPos[i].x,otherPos[i].y)<=15) {
							check = false;
						}
					}
				}
			}
		}
		
		return check;
	}

	private double distance(int x1,int y1,int x2,int y2) {
		return Math.sqrt(((x2-x1)*(x2-x1))+((y2-y1)*(y2-y1)));
	}
	
}
