package ACC_NEW;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class LeaderACC extends Component {

	public String name;
	
	public Double lPos = 0.0;          
	public Double lSpeed = 0.0;        
	public Double creationTime = 0.0;  
	
	public Double leaderGas = 0.0;
	public Double leaderBrake = 0.0;
	
	public Double lastSpeedError = 0.0;
	public Double integratorSpeedError = 0.0;
	public Double errorWindup = 0.0;
	
	protected static final double kp = 0.05;
	protected static final double ki = 0.000228325;
	protected static final double kt = 0.01;
	protected static final double secNanoSecFactor = 1000000000;
	protected static final int timePeriod = 100;

	
	public LeaderACC() {
		name = "L";
		ACCDatabase.driverBehaviour();
	}
	
	
	@Process
	@PeriodicScheduling(timePeriod)
	public static void speedControl(
		@In("lPos") Double lPos,
		@In("lSpeed") Double lSpeed,
		@In("creationTime") Double creationTime,
		
		@Out("leaderGas") OutWrapper<Double> lGas,
		@Out("leaderBrake") OutWrapper<Double> lBrake,
	
		@InOut("integratorSpeedError") OutWrapper<Double> integratorSpeedError,
		@InOut("errorWindup") OutWrapper<Double> errorWindup	
	) {
	
		double speedError = ACCDatabase.driverSpeed.get(lPos) - lSpeed;
		integratorSpeedError.value += (ki * speedError + kt * errorWindup.value) *  timePeriod;
		double pid = kp * speedError + integratorSpeedError.value;
		errorWindup.value = saturate(pid) - pid;

		if(pid >= 0){
			lGas.value = pid;
			lBrake.value = 0.0;
		}else{
			lGas.value = 0.0;
			lBrake.value = -pid;
		}
	}
	
	private static double saturate(double pid) {
		if(pid > 1) pid = 1;
		else if(pid < -1) pid = -1;
		return pid;
	}
	
}
