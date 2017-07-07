package agents;

import javax.swing.JOptionPane;

public class Judge {
	
	private Car[] cars;
	private Car penaltyCar;
	private boolean penalty = true;
	private int penaltyTime = 5;
	private boolean race = false;
	
	
	public Judge(){
		checkForCars();
		checkForPenaltys();
	}
	
	private void checkForCars(){
		
	}
	
	private void checkForPenaltys(){
		while(race){
			if(penalty){
				penaltyCar = cars[cars.length];
				forcePenalty(penaltyCar, penaltyTime);
			}
		}
	}
	
	private void forcePenalty(Car c, int time){
		
	}
	
	public void listStandings(){
		String text = "";
		Car[] standings = new Car[cars.length];
		int i = 0;
		for( Car c : cars){
			standings[i] = c;
		}
		Car tmp;
		for(int j = 0; j<standings.length; j++){
			for( int x = 1; x<standings.length-1;x++){
				Car c1 = standings[x];
				Car c2 = standings[x-1];
				if(c2.getTime() > c1.getTime()){
					tmp = c2;
					standings[x - 1] = standings[x];
					standings[x] = tmp;
				}
			}
		}
		i = 0;
		for( Car cr : standings){
			if(i == 0){
				text += "place	name(number)	time	bestLap";
			}else{
				text += "\n"+i+".	"+cr.getName()+"("+cr.getNumber()+")	"+cr.getTime()+"	"+cr.getBestLap();
			}
		}
		JOptionPane.showMessageDialog(null, text);
	}

}
