package ACC_NEW;


import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;


public class EnvironmentACC extends Component {

	public String name = "E";
	public Double eFollowerGas = 0.0;    
	public Double eFollowerBrake = 0.0;  
	public Double eLeaderGas = 0.0;      
	public Double eLeaderBrake = 0.0;    
	
	public Double eFPos = 0.0;
	public Double eFollowerSpeed = 0.0; 
	public Double eLPos = 60.0;
	public Double eLeaderSpeed = 0.0;
	public Double eLastTime = 0.0;

	
	protected static final int timePeriod = 100;
	protected static final double secNanoSecFactor = 1000000000;
	
	
	public EnvironmentACC() {
		ACCDatabase.followerTorques();
		ACCDatabase.leaderTorques();
		ACCDatabase.routeSlops();
	}	
	
	@Process 
	@PeriodicScheduling(timePeriod)
	public static void environmentResponse(
			@In("eLeaderGas") Double lGas,
			@In("eLeaderBrake") Double lBrake,
			@In("eFollowerGas") Double fGas,
			@In("eFollowerBrake") Double fBrake,
			
			@InOut("eFPos") OutWrapper<Double> eFPos,			 
			@InOut("eFollowerSpeed") OutWrapper<Double> fSpeed,
			@InOut("eLPos") OutWrapper<Double> eLPos,			 
			@InOut("eLeaderSpeed") OutWrapper<Double> lSpeed,
			
			@Out("eLastTime") OutWrapper<Double> eLastTime
			){
	
		double currentTime = System.nanoTime()/secNanoSecFactor;
		
		// ----------------------- leader ----------------------------------------------------------------------
		double lAcceleration = ACCDatabase.getAcceleration(lSpeed.value, eLPos.value, ACCDatabase.lTorques, lGas, lBrake);
		lSpeed.value += lAcceleration * timePeriod;
		eLPos.value += lSpeed.value * timePeriod;
		//------------------------ follower ---------------------------------------------------------------------
		double fAcceleration = ACCDatabase.getAcceleration(fSpeed.value, eFPos.value, ACCDatabase.fTorques, fGas, fBrake);
		fSpeed.value += fAcceleration * timePeriod; 
		eFPos.value += fSpeed.value * timePeriod;
		//--------------------------------------------------------------------------------------------------------
		
		
		eLastTime.value = currentTime;
		System.out.println("Speed leader : "+lSpeed.value+", pos : "+eLPos.value+"... time :"+currentTime);
		System.out.println("Speed follower : "+fSpeed.value+", pos : "+eFPos.value+"... time :"+currentTime);
		System.out.println("................... distance : "+(eLPos.value - eFPos.value));
	}
}