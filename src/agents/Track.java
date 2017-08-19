package agents;

import java.awt.Point;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.SwingUtilities;

import guis.TrackSetupGui;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class Track extends Agent{
	
	private boolean started = false;
	private int laps = 0;
	private int startX = 0;
	private int startYmin = 0;
	private int startYmax = 8;
	private int tileSize = 60;
	private Point[] carsPositions = new Point[4];
	private char[][] trackASCII = new char[15][30];
	private TrackSetupGui tsg = new TrackSetupGui(this);
	private AID[] cars = {new AID("c1",AID.ISLOCALNAME),
			new AID("c2",AID.ISLOCALNAME),
			new AID("c3",AID.ISLOCALNAME),
			new AID("c4",AID.ISLOCALNAME)}; 
	private AID judge = new AID("judge", AID.ISLOCALNAME);

	protected void setup(){
		System.out.println("Starting track agent.");
		
		System.out.println("Track is sending start positions.");
		
		loadTrack("/home/marcin/Dokumenty/test.txt");
		sendStartPositions();
	}
	
	protected void takeDown(){
		System.out.println("Taking down track agent.");
	}
	
	private void sendStartPositions(){
		addBehaviour(new OneShotBehaviour() {
			
			int startYstep = (startYmax-startYmin)/5;
			String all = "";
			ACLMessage msg;
			
			@Override
			public void action() {
				for(int i = 0; i<cars.length; i++){
					msg = new ACLMessage(ACLMessage.INFORM);
					msg.addReceiver(cars[i]);
					msg.setOntology("start-pos");
					int startY = startYmin + (int) Math.floor(startYstep*(i + 1));
					carsPositions[i] = new Point(startX, startY);
					msg.setContent(startX+","+startY);
					if(i<cars.length-1){
						all += startX+","+startY+";";
					}else{
						all += startX+","+startY;
					}
					send(msg);
				}
				msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(judge);
				msg.setOntology("start-pos");
				msg.setContent(all);
				send(msg);
				tsg.showGui();
			}
			
		});
	}
	
	public void startRace(int laps){
		
		this.laps = laps;
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				tsg.dispatchEvent(new WindowEvent(tsg, WindowEvent.WINDOW_CLOSING));
				tsg.dispose();
			}
		});
		
		if(this.laps > 0){
			addBehaviour(new OneShotBehaviour() {
				
				@Override
				public void action() {
					for(int i = 0; i<cars.length; i++){
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(cars[i]);
						msg.addReceiver(judge);
						msg.setOntology("laps");
						Track t = (Track) myAgent;
						String s = "";
						s += t.laps;
						msg.setContent(s);
						send(msg);
						started = true;
						doTheRace();
					}
				}
			});
		}else{
			addBehaviour(new OneShotBehaviour() {
				
				@Override
				public void action() {
					for(int i = 0; i<cars.length; i++){
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						msg.addReceiver(cars[i]);
						msg.addReceiver(judge);
						msg.setOntology("nolaps");
						msg.setContent("");
						send(msg);
					}
					doDelete();
				}
			});
		}
		
	}

	private void doTheRace(){
		addBehaviour(new CyclicBehaviour() {
			
			ACLMessage msg;
			ACLMessage response;
			boolean clear = false;
			
			@Override
			public void action() {
				
				msg = myAgent.receive();
				
				if(msg!=null){
					if(msg.getSender().getLocalName().equals(judge.getLocalName()) && msg.getOntology()=="race-end"){
						//destroy track
						started = false;
					}else if(msg.getOntology()=="cur-pos"){
						//update cars positions on visualisation
						
					}else if(msg.getOntology()=="is-clear"){
						//check track for car
						
						response = new ACLMessage(ACLMessage.INFORM);
						response.addReceiver(msg.getSender());
						response.setOntology("is-clear-response");
						if(clear){
							response.setContent("clear");
							clear = false;
						}else{
							response.setContent("notclear");
						}
						send(response);
						
					}
				}
				
				if(!started){
					doDelete();
				}
			}
		});
	}
	
	private void loadTrack(String path){
		
		String line;
		
		try(BufferedReader br = new BufferedReader(new FileReader(path))) {
		    for(int i = 0; i<15; i++) {
		    	line = br.readLine();
		    	for(int j = 0; j<30; j++){
		    		if(line.charAt(j)=='S'){
		    			startX = (j * tileSize) + (int)Math.floor(tileSize/2);
		    			startYmin = i * tileSize;
		    			startYmax = (i + 1) * tileSize;
		    		}
		    		trackASCII[i][j] = line.charAt(j);
		    	}
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void showPositions(){
		for(int i = 0; i<4; i++){
			System.out.println("Pozycja samochodu "+(i+1)+" - x:"+carsPositions[i].getX()+" y:"+carsPositions[i].getY());
		}
	}
	
}
