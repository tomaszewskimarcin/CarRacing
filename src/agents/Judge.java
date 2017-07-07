package agents;

import java.awt.Point;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class Judge extends Agent{
	
	private boolean hasSP = false;
	private boolean started = false;
	private int ended = 0;
	private AID[] cars = {new AID("c1",AID.ISLOCALNAME),
			new AID("c2",AID.ISLOCALNAME),
			new AID("c3",AID.ISLOCALNAME),
			new AID("c4",AID.ISLOCALNAME)};
	private AID track = new AID("track", AID.ISLOCALNAME);
	private Point[] positions;
	
	@Override
	protected void setup(){
		System.out.println("Starting judge agent");
		
		initializePositions();
	}
	
	@Override
	protected void takeDown(){
		System.out.println("Taking down judge agent");
	}
	
	private void initializePositions(){
		addBehaviour(new TickerBehaviour(this, 100) {
			
			@Override
			protected void onTick() {
				
				ACLMessage msg = myAgent.receive();
				if(msg!=null){
					if(msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology()=="start-pos"){
						positions = new Point[cars.length];
						String raw = msg.getContent();
						String[] rawTab = raw.split(";");
						String[] curPos;
						String curX;
						String curY;
						for(int i = 0; i<cars.length;i++){
							curPos = rawTab[i].split(",");
							curX = curPos[0];
							curY = curPos[1];
							positions[i] = new Point(Integer.parseInt(curX), Integer.parseInt(curY));
						}
						hasSP = true;
					}
				}else{
					block();
				}
				
				if(hasSP){
					checkForStart();
					stop();
				}
			}
		});
	}
	
	private void checkForStart(){
		addBehaviour(new TickerBehaviour(this, 100) {
			
			@Override
			protected void onTick() {
				
				ACLMessage msg = myAgent.receive();
				if(msg!=null){
					if(msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology()=="nolaps"){
						doDelete();
					}else if(msg.getSender().getLocalName().equals(track.getLocalName()) && msg.getOntology()=="laps"){
						started = true;
					}
				}else{
					block();
				}
				
				if(started){
					System.out.println("Judge agent recived start command.");
					doTheRace();
					stop();
				}
			}
		});
	}

	private void doTheRace(){
		addBehaviour(new CyclicBehaviour() {
			
			ACLMessage msg;
			ACLMessage response;
			
			@Override
			public void action() {
				
				msg = myAgent.receive();
				
				if(msg!=null){
					if(msg.getOntology()=="cur-pos"){
						//check if penalty need to be assigned
					}else if(msg.getOntology()=="ended"){
						//count cars that ended race
						ended++;
					}
				}
				
				if(ended==4){
					//all cars ended
					response = new ACLMessage(ACLMessage.INFORM);
					for(int i = 0; i<cars.length;i++){
						response.addReceiver(cars[i]);
					}
					response.addReceiver(track);
					response.setOntology("race-end");
					response.setContent("");
					send(response);
					started = false;
				}
				
				if(!started){
					standings();
				}
			}
		});
	}
	
	private void standings(){
		
	}
	
}
