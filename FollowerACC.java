package ACC_NEW;


import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class FollowerACC extends Component {

	public String name;
	public Double fPos = 0.0;      
	public Double fSpeed = 0.0;    
	public Double fGas = 0.0 ;
	public Double fBrake = 0.0;
	public Double fLastTime = 0.0;
	
	public Double fLPos = 60.0;       
	public Double fLSpeed = 0.0;  
	public Double fLPosMin = 60.0;       
	public Double fLSpeedMin = 0.0;     
	public Double fLPosMax = 60.0;       
	public Double fLSpeedMax = 0.0;     
	public Double fLCreationTime = 0.0;	  
	
	public Double fIntegratorError = 0.0;
	public Double fErrorWindup = 0.0;
	
	
	protected static final double kpD = 0.193;
	protected static final double kp = 0.12631;
	protected static final double ki = 0.001;
	protected static final double kt = 0.01;
	protected static final double secNanoSecFactor = 1000000000;
	protected static final double timePeriod = 100;
	protected static final double miliSecondToSecond = 1000;
	protected static final double minLimit = 40;
	
	
	public FollowerACC() {
		name = "F";
	}
	
	@Process
	@PeriodicScheduling((int) timePeriod)
	public static void speedControl(
					@In("fPos") Double fPos,
					@In("fSpeed") Double fSpeed,
					@In("fLPos") Double fLPos,
					@In("fLSpeed") Double fLSpeed,
					@In("fLCreationTime") Double fLCreationTime,
					
					@Out("fGas") OutWrapper<Double> fGas,
					@Out("fBrake") OutWrapper<Double> fBrake,
					
					@InOut("fLPosMin") OutWrapper<Double> fLPosMin,
					@InOut("fLSpeedMin") OutWrapper<Double> fLSpeedMin,
					@InOut("fLPosMax") OutWrapper<Double> fLPosMax,
					@InOut("fLSpeedMax") OutWrapper<Double> fLSpeedMax,
					@InOut("fLastTime") OutWrapper<Double> fLastTime,
					@InOut("fIntegratorError") OutWrapper<Double> fIntegratorError,
					@InOut("fErrorWindup") OutWrapper<Double> fErrorWindup
				) {
	
			double currentTime = System.nanoTime()/secNanoSecFactor;
			double fLTimePeriod = 0.0;
			double timePeriodInSeconds = timePeriod/miliSecondToSecond;
			vehicleBeliefBoundaries boundaries = null;
			
			//------------------------------------------------ knowledge evaluation ------------------------------------------
				
			fLPosMin.value = fLPos;
			fLPosMax.value = fLPos;
			fLSpeedMin.value = fLSpeed;
			fLSpeedMax.value = fLSpeed;

			fLTimePeriod = timePeriodInSeconds + (currentTime - fLCreationTime);
			System.out.println("f time period :"+fLTimePeriod);
			for (int i = 0; i < fLTimePeriod; i++) {
					boundaries = vehilceKnowledgeEvolutionModel(fLSpeedMin.value, fLSpeedMax.value, fLPosMin.value, fLPosMax.value, ACCDatabase.lTorques);
					fLPosMin.value += boundaries.posMin * i/miliSecondToSecond;
					fLPosMax.value += boundaries.posMax * i/miliSecondToSecond;
					fLSpeedMin.value += boundaries.speedMin * i/miliSecondToSecond;
					fLSpeedMax.value += boundaries.speedMax * i/miliSecondToSecond;
//					System.out.println("i:"+i);
			}
			
			System.out.println("///... pos: min "+fLPosMin.value+" ... max "+fLPosMax.value+"      time :"+currentTime);
			System.out.println("///... speed: min "+fLSpeedMin.value+" ... max "+fLSpeedMax.value);
			
//		//-------------------------------------------------------safety part----------------------------------------------------------
//		//----------------------------------------------------- controller part -------------------------------------------------------
				double distanceError = - 50 + (fLPos - fPos);
				double pidDistance = kpD * distanceError;
				double error = pidDistance + fLSpeed - fSpeed;
				fIntegratorError.value += (ki * error + kt * fErrorWindup.value) * timePeriodInSeconds;
				double pidSpeed = kp * error + fIntegratorError.value;
				fErrorWindup.value = saturate(pidSpeed) - pidSpeed;

				if(pidSpeed >= 0){
					fGas.value = pidSpeed;
					fBrake.value = 0.0;
				}else{
					fGas.value = 0.0;
					fBrake.value = -pidSpeed;
				}
	}


	private static double saturate(double val) {
		if(val > 1) val = 1;
		else if(val < -1) val = -1;
		return val;
	}
	
	private static vehicleBeliefBoundaries vehilceKnowledgeEvolutionModel( Double speedMin, Double speedMax, Double posMin, Double posMax, LookupTable torques){
			double accMin = ACCDatabase.getAcceleration(speedMin, posMin, torques, 0.0, 1.0);
			double accMax = ACCDatabase.getAcceleration(speedMax, posMax, torques, 1.0, 0.0);
			vehicleBeliefBoundaries vB=new vehicleBeliefBoundaries();
			vB.posMin = speedMin;
			vB.posMax = speedMax;
			vB.speedMin = accMin;
			vB.speedMax = accMax;
			return vB;
	}
	
	
	private static class vehicleBeliefBoundaries{
		public Double posMin;
		public Double posMax;
		public Double speedMin;
		public Double speedMax;
	}
	
}
