package agents;

import java.awt.Point;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import guis.TrackGui;
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
	private int startY = 0;
	private int tileSize = 60;
	private int startDirX = 0;
	private int startDirY = 0;
	private boolean startline = false;
	private boolean errLoad = false;
	private boolean errValidate = false;
	private Point[] carsPositions = new Point[4];
	private char[][] trackASCII = new char[10][20];
	private TrackSetupGui tsg = new TrackSetupGui(this);
	private TrackGui tg;
	private AID[] cars = {new AID("c1",AID.ISLOCALNAME),
			new AID("c2",AID.ISLOCALNAME),
			new AID("c3",AID.ISLOCALNAME),
			new AID("c4",AID.ISLOCALNAME)}; 
	private AID judge = new AID("judge", AID.ISLOCALNAME);

	protected void setup(){
		System.out.println("Starting track agent.");
		
		loadTrack("track.txt");
		
		if(!errLoad){
			
			System.out.println("Track is sending start positions.");
			
			sendStartPositions();
			
		}
	}
	
	protected void takeDown(){
		System.out.println("Taking down track agent.");
	}
	
	private void sendStartPositions(){
		addBehaviour(new OneShotBehaviour() {
			
			String all = "";
			ACLMessage msg;
			
			@Override
			public void action() {
				for(int i = 0; i<cars.length; i++){
					msg = new ACLMessage(ACLMessage.INFORM);
					msg.addReceiver(cars[i]);
					msg.setOntology("start-pos");
					if(startDirY == 0 && startDirX != 0){
						System.out.println("Dup");
						int marigin = (tileSize-40)/5;
						int startYstep = ((i+1)*marigin)+(i*10);
						int startYtmp = startY + startYstep;
						msg.setContent((startX+(int)Math.floor(tileSize/2)-5)+","+startYtmp+","+startDirX+","+startDirY+","+tileSize);
						carsPositions[i] = new Point((startX+(int)Math.floor(tileSize/2) - 5),startYtmp);
					}else if(startDirY != 0 && startDirX == 0){
						int marigin = (tileSize-40)/5;
						int startXstep = ((i+1)*marigin)+(i*10);
						int startXtmp = startX + startXstep;
						msg.setContent(startXtmp+","+(startY + (int)Math.floor(tileSize/2)-5)+","+startDirX+","+startDirY+","+tileSize);
						carsPositions[i] = new Point(startXtmp,(startY + (int)Math.floor(tileSize/2)-5));
					}
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
				
				validateTrack();
				
				System.out.println("Starting track GUI");
				
				tg = new TrackGui(trackASCII,carsPositions, tileSize);
				tg.showGui();
				
				if(!errValidate){
					tsg.showGui();
				}else{
					JOptionPane.showMessageDialog(tg, "Error marked red.","Validation Error",JOptionPane.ERROR_MESSAGE);
				}
				
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
						int i = Integer.parseInt(msg.getSender().getLocalName().substring(msg.getSender().getLocalName().indexOf('c')+1));
						String[] newPos = msg.getContent().split(",");
						carsPositions[i-1] = new Point(Integer.parseInt(newPos[0]),Integer.parseInt(newPos[1]));
						tg.updatePos(carsPositions);
					}else if(msg.getOntology()=="is-clear"){
						//check track for car
						String[] pos = msg.getContent().split(",");
						clear = checkPosition(Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
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
		    for(int i = 0; i<10; i++) {
		    	line = br.readLine();
		    	for(int j = 0; j<20; j++){
		    		if(!startline){
			    		if(line.charAt(j)=='D'){
			    			startline = true;
			    			startX = (j * tileSize);
			    			startY = i * tileSize;
			    			startDirX = 0;
			    			startDirY = 1;
			    		}else if(line.charAt(j)=='U'){
			    			startline = true;
			    			startX = (j * tileSize);
			    			startY = i * tileSize;
			    			startDirX = 0;
			    			startDirY = -1;
			    		}else if(line.charAt(j)=='L'){
			    			startline = true;
			    			startX = (j * tileSize);
			    			startY = i * tileSize;
			    			startDirX = -1;
			    			startDirY = 0;
			    		}else if(line.charAt(j)=='R'){
			    			startline = true;
			    			startX = (j * tileSize);
			    			startY = i * tileSize;
			    			startDirX = 1;
			    			startDirY = 0;
			    		}
		    		}else if(started && (line.charAt(j) == 'D' || line.charAt(j) == 'U' || line.charAt(j) == 'L' || line.charAt(j) == 'R')){
		    			System.out.println("Error loading track on line "+i+" position "+j+". Double start character.");
		    			errLoad = true;
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
	
	private void validateTrack(){
		for(int i = 0; i<10 ; i++){
			for(int j = 0; j<20 ; j++){
				if(trackASCII[i][j] == '#' 
						|| trackASCII[i][j] == 'D' 
						|| trackASCII[i][j] == 'U' 
						|| trackASCII[i][j] == 'L' 
						|| trackASCII[i][j] == 'R' ){
					int counter = 0;
					if(i-1>=0){
						if(trackASCII[i-1][j] == '#' 
								|| trackASCII[i-1][j] == 'D' 
								|| trackASCII[i-1][j] == 'U' 
								|| trackASCII[i-1][j] == 'L' 
								|| trackASCII[i-1][j] == 'R'  
								|| trackASCII[i-1][j] == '!'){
							counter++;
						}
					}
					if(i+1<10){
						if(trackASCII[i+1][j] == '#' 
								|| trackASCII[i+1][j] == 'D' 
								|| trackASCII[i+1][j] == 'U' 
								|| trackASCII[i+1][j] == 'L' 
								|| trackASCII[i+1][j] == 'R' 
								|| trackASCII[i+1][j] == '!'){
							counter++;
						}
					}
					if(j+1<20){
						if(trackASCII[i][j+1] == '#' 
								|| trackASCII[i][j+1] == 'D' 
								|| trackASCII[i][j+1] == 'U' 
								|| trackASCII[i][j+1] == 'L' 
								|| trackASCII[i][j+1] == 'R' 
								|| trackASCII[i][j+1] == '!'){
							counter++;
						}
					}
					if(j-1>=0){
						if(trackASCII[i][j-1] == '#' 
								|| trackASCII[i][j-1] == 'D' 
								|| trackASCII[i][j-1] == 'U' 
								|| trackASCII[i][j-1] == 'L' 
								|| trackASCII[i][j-1] == 'R'
								|| trackASCII[i][j-1] == '!'){
							counter++;
						}
					}
					if(counter!=2){
						trackASCII[i][j] = '!';
						errValidate = true;
					}
				}
			}
		}
	}
	
	private boolean checkPosition(int x, int y){
		boolean check = false;
		
		if(x>=0 && y>=0){
			int shortenX = (int) Math.floor(x/tileSize);
			int shortenY = (int) Math.floor(y/tileSize);
			
			if(shortenX >= 0 && shortenX <20){
				if(shortenY >= 0 && shortenY < 10){
					char c = trackASCII[shortenY][shortenX];
					
					if(c == '#' ||
							c == 'D' ||
							c == 'U' ||
							c == 'L' ||
							c == 'R'){
						check = true;
					}
				}
			}
		}
		
		return check;
	}
	
	public void showPositions(){
		for(int i = 0; i<4; i++){
			System.out.println("Pozycja samochodu "+(i+1)+" - x:"+carsPositions[i].getX()+" y:"+carsPositions[i].getY());
		}
	}
	
	
}
