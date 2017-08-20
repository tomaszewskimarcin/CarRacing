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
	private int spd = 2;
	private int curSpd;
	private int pace = 0;
	private int time = 0;
	private int curLap = 1;
	private int laps = 0;
	private int bestLap = 0;
	private double dirX = 0;
	private double dirY = 0;
	private int tileSize = 0;
	private boolean hasSP = false;
	private boolean started = false;
	private AID track = new AID("track", AID.ISLOCALNAME);
	private AID judge = new AID("judge", AID.ISLOCALNAME);
	CarGui gui = new CarGui(this);
	private int stage = 0; 
	
	@Override
	protected void setup(){
		System.out.printf("Started car %s\n",getAID().getLocalName());

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
						pos.setLocation(Integer.parseInt(posT[0]), Integer.parseInt(posT[1]));
						dirX = Integer.parseInt(posT[2]);
						dirY = Integer.parseInt(posT[3]);
						tileSize = Integer.parseInt(posT[4]);
						
						//Test communication
						//System.out.println("|"+pos.x+"|"+pos.y+"|"+dirX+"|"+dirY+"|"+tileSize);
						
						hasSP = true;
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
		curSpd = spd;
		addBehaviour(new CyclicBehaviour() {

			ACLMessage msg;
			boolean sended;
			
			@Override
			public void action() {
			
				switch(stage){
				case 0:
					//send ask to track for further track and change position
					if(sended){
						msg = myAgent.receive();
						if(msg!=null && msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology()=="is-clear-response"){
							if(msg.getContent().equals("clear")){
								//action if clear
								
							}else if(msg.getContent().equals("notclear")){
								//action if not clear
								
							}
							sended = false;
							stage++;
						}
					}else{
						msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(track);
						msg.setOntology("is-clear");
						msg.setContent("");
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
					msg.setContent(pos.x+","+pos.y);
					send(msg);
					stage++;
					break;
				case 2:
					//check for penalties||nextlap
					msg = myAgent.receive();
					if(msg!=null && msg.getSender().getLocalName().equals(judge.getLocalName()) && msg.getOntology() == "penalty"){
						curSpd = curSpd/2;
					}else if(msg!=null && msg.getSender().getLocalName().equals(judge.getLocalName()) && msg.getOntology() == "penalty-end"){
						curSpd = spd;
					}else if(msg!=null && msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology() == "nextlap"){
						curLap++;
					}
					stage++;
					break;
				case 3:
					//check if ended and report if needed
					if(curLap>laps){
						msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(judge);
						msg.setOntology("ended");
						msg.setContent("");
						send(msg);
						stage = 4;
					}else{
						stage = 0;
					}
					break;
				case 4:
					//wait for race end
					msg = myAgent.receive();
					if(msg!=null && msg.getSender().getLocalName().equals(judge.getLocalName()) && msg.getOntology() == "race-end"){
						started = false;
					}
					break;
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

}
