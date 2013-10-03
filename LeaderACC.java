package ACC_NEW;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class LeaderACC extends Component {

	public String lName;
	
	public Double lPos = 0.0;
	public Double lSpeed = 0.0;
	public Double lCreationTime = 0.0;
	
	public Double lGas = 0.0;
	public Double lBrake = 0.0;
	
	public Double lIntegratorSpeedError = 0.0;
	public Double lErrorWindup = 0.0;
	
	protected static final double kp = 0.05;
	protected static final double ki = 0.000228325;
	protected static final double kt = 0.01;
	protected static final double timePeriod = 100;
	protected static final double miliSecondToSecond = 1000;
	
	
	public LeaderACC() {
		lName = "L";
	}
	
	
	@Process
	@PeriodicScheduling((int) timePeriod)
	public static void speedControl(
			@In("lPos") Double lPos,
			@In("lSpeed") Double lSpeed,
			
			@Out("lGas") OutWrapper<Double> lGas,
			@Out("lBrake") OutWrapper<Double> lBrake,
			
			@InOut("lIntegratorSpeedError") OutWrapper<Double> lIntegratorSpeedError,
			@InOut("lErrorWindup") OutWrapper<Double> lErrorWindup	
			) {
	
		double timePeriodInSeconds = timePeriod/miliSecondToSecond;
		double speedError = ACCDatabase.driverSpeed.get(lPos) - lSpeed;
		lIntegratorSpeedError.value += (ki * speedError + kt * lErrorWindup.value) * timePeriodInSeconds;
		double pid = kp * speedError + lIntegratorSpeedError.value;
		lErrorWindup.value = saturate(pid) - pid;

		if(pid >= 0){
			lGas.value = pid;
			lBrake.value = 0.0;
		}else{
			lGas.value = 0.0;
			lBrake.value = -pid;
		}
	}
	
	private static double saturate(double val) {
		if(val > 1) val = 1;
		else if(val < -1) val = -1;
		return val;
	}

}

