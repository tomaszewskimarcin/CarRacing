package agents;

import java.awt.Point;
import java.sql.Timestamp;
import java.util.Date;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class Judge extends Agent{
	
	private boolean hasSP = false;
	private boolean started = false;
	private boolean sendResponse = true;
	private int ended = 0;
	private AID[] cars = {new AID("c1",AID.ISLOCALNAME),
			new AID("c2",AID.ISLOCALNAME),
			new AID("c3",AID.ISLOCALNAME),
			new AID("c4",AID.ISLOCALNAME)};
	private AID track = new AID("track", AID.ISLOCALNAME);
	private Point[] positions;
	private Timestamp start;
	private int[] penalties = {0,0,0,0};
	private Timestamp[] endTimestamps = {new Timestamp(0),new Timestamp(0),new Timestamp(0),new Timestamp(0)};
	private Timestamp[] timestamps = {new Timestamp(0),new Timestamp(0),new Timestamp(0),new Timestamp(0)};
	private Timestamp[] godmode = {new Timestamp(0),new Timestamp(0),new Timestamp(0),new Timestamp(0)};
	private int[] standings = {0,0,0,0};
	private int curStanding = 1;
	
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
					for(int i = 0; i<godmode.length; i++) {
						godmode[i] = new Timestamp(new Date().getTime() + 5000);
					}
					start = new Timestamp(new Date().getTime());
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
						String name = msg.getSender().getLocalName();
						int which = Integer.parseInt(name.substring(1))-1;
						
						String[] xy = msg.getContent().split(",");
						positions[which] = new Point(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]));
						
						if(timestamps[which].getTime() <= 0) {
							if(new Date().getTime() >= godmode[which].getTime()) {
								if(checkPenalty(msg.getSender(),which)) {
									timestamps[which] = new Timestamp(new Date().getTime() + 5000);
									penalties[which] += 5;
									response = new ACLMessage(ACLMessage.INFORM);
									response.addReceiver(msg.getSender());
									response.setOntology("penalty");
									response.setContent("");
									send(response);
								}
							}
						}
					}else if(msg.getOntology()=="ended"){
						//count cars that ended race
						String name = msg.getSender().getLocalName();
						int which = Integer.parseInt(name.substring(1))-1;
						endTimestamps[which] = new Timestamp(new Date().getTime());
						standings[which] = curStanding;
						curStanding++;
						ended++;
					}
				}
				
				for(int i = 0; i<cars.length; i++) {
					if(timestamps[i].getTime() != 0) {
						if(new Date().getTime() >= timestamps[i].getTime()) {
							timestamps[i] = new Timestamp(0);
							godmode[i] = new Timestamp(new Date().getTime() + 5000);
							response = new ACLMessage(ACLMessage.INFORM);
							response.addReceiver(cars[i]);
							response.setOntology("penalty-end");
							response.setContent("");
							send(response);
						}
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
				
				if(!started && sendResponse){
					standings(response);
					sendResponse = false;
					doDelete();
				}
			}
		});
	}
	
	private boolean checkPenalty(AID car, int number) {
		boolean check = false;
		
		for(int i = 0; i<cars.length; i++) {
			if(!cars[i].getLocalName().equals(car.getLocalName())) {
				if(distance(positions[number].x,positions[number].y,positions[i].x,positions[i].y)<=10) {
					check = true;
				}
			}
		}
		
		
		return check;
	}
	
	private void standings(ACLMessage response){
		String msgBody = "";
		for(int i = 0; i<cars.length; i++) {
			long diff = (endTimestamps[i].getTime()-start.getTime());
			/*int min = 0;
			while(sec >= 60000) {
				min++;
				sec -= 60000;
			}
			System.out.println("Car "+cars[i].getLocalName()+" time was: "+min+":"+sec);*/
			if(i == 0) {
				msgBody += standings[i]+"-"+diff+":"+penalties[i]+":"+cars[i].getLocalName();
			}else {
				msgBody += ","+standings[i]+"-"+diff+":"+penalties[i]+":"+cars[i].getLocalName();
			}
		}
		response = new ACLMessage(ACLMessage.INFORM);
		response.addReceiver(track);
		response.setOntology("final-times");
		response.setContent(msgBody);
		send(response);
		started = false;
	}
	
	private double distance(int x1,int y1,int x2,int y2) {
		return Math.sqrt(((x2-x1)*(x2-x1))+((y2-y1)*(y2-y1)));
	}
	
}
