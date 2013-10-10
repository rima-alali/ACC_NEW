package ACC;


import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.ode.FirstOrderIntegrator;
import org.apache.commons.math3.ode.nonstiff.MidpointIntegrator;


import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.annotations.TriggerOnChange;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class Follower extends Component {

	public String name;
	public Double fPos = 0.0;      
	public Double fSpeed = 0.0;    
	public Double fGas = 0.0 ;
	public Double fBrake = 0.0;
	public Double fLastTime = 0.0;
	
	public Double fLPos = 0.0;       
	public Double fLSpeed = 0.0;  
	public Double fLPosMin = 0.0;       
	public Double fLSpeedMin = 0.0;     
	public Double fLPosMax = 0.0;       
	public Double fLSpeedMax = 0.0;     
	public Double fLCreationTime = 0.0;	  
	
	public Double fDistance = 0.0;
	public Double fHeadwayDistance = 0.0;
	public Double fTargetAcc = 0.0;
	public Double fIntegratorError = 0.0;
	public Double fErrorWindup = 0.0;
	
	
	protected static final double kpD = 0.193;
	protected static final double kp = 0.12631;
	protected static final double ki = 0.001;
	protected static final double kt = 0.01;
	protected static final double secNanoSecFactor = 1000000000;
	protected static final double timePeriod = 100;
	protected static final double miliSecondToSecond = 1000;
	protected static final double wantedDistance = 50;
	protected static final double THRESHOLD = 40;

    
	
	public Follower() {
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
					@In("fHeadwayDistance") Double fHeadwayDistance,
					
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
			double timePeriodInSeconds = timePeriod/miliSecondToSecond;
			
			
			
			
//			HashMap<String, Double> boundaries = new HashMap<String, Double>();
//			
//			if( lCreationTime < fLastTime.value ){
//				lTimePeriod = fLastTime.value > 0.0 ? currentTime - fLastTime.value : 0.0;
//				boundaries = calculateBoundaries(lSpeedMin.value, lSpeedMax.value, lPosMin.value, lPosMax.value, ACCDatabase.lTorques, lTimePeriod);
//				lPosMin.value += boundaries.get("xMin");
//				lPosMax.value += boundaries.get("xMax");
//				lSpeedMin.value += boundaries.get("dxMin");
//				lSpeedMax.value += boundaries.get("dxMax");
//				System.out.println("////... pos: min "+lPosMin.value+" ... max "+lPosMax.value + "     time :"+currentTime);
//				System.out.println("////... speed: min "+lSpeedMin.value+" ... max "+lSpeedMax.value);
//			}else { 
//				lTimePeriod = lCreationTime > 0.0 ? currentTime - lCreationTime : 0.0;
//				boundaries = calculateBoundaries(currentLSpeed, currentLSpeed, currentLPos, currentLPos, ACCDatabase.lTorques, lTimePeriod);
//				lPosMin.value = currentLPos + boundaries.get("xMin");
//				lPosMax.value = currentLPos + boundaries.get("xMax");
//				lSpeedMin.value = currentLSpeed + boundaries.get("dxMin");
//				lSpeedMax.value = currentLSpeed + boundaries.get("dxMax");
//				System.out.println("\\\\... pos: min "+lPosMin.value+" ... max "+lPosMax.value+"      time :"+currentTime);
//				System.out.println("\\\\... speed: min "+lSpeedMin.value+" ... max "+lSpeedMax.value);
//			}

			
			
			double[] minBoundaries = new double[1]; 
			double[] maxBoundaries = new double[1];
			double startTime = 0.0;
			
			

			if( fLastTime.value != 0.0 && fLCreationTime <= fLastTime.value  ){
				startTime = fLastTime.value;
			}else{
				startTime = fLCreationTime;
				fLPosMin.value = fLPos;
				fLPosMax.value = fLPos;
				fLSpeedMin.value = fLSpeed;
				fLSpeedMax.value = fLSpeed;
				System.out.println("@@@@@@... pos: min "+fLPosMin.value+" ... max "+fLPosMax.value+"      time :"+currentTime);
				System.out.println("@@@@@@... speed: min "+fLSpeedMin.value+" ... max "+fLSpeedMax.value);
			}
			
			//------------------------------------------------ knowledge evaluation ------------------------------------------
			
			try{
				
				double accMin = ACCDatabase.getAcceleration(fLSpeedMin.value, fLPosMin.value, ACCDatabase.lTorques, 0.0, 1.0, ACCDatabase.lMass);
				double accMax = ACCDatabase.getAcceleration(fLSpeedMax.value, fLPosMax.value, ACCDatabase.lTorques, 1.0, 0.0, ACCDatabase.lMass);
				
				FirstOrderIntegrator integrator = new MidpointIntegrator(1);
				integrator.setMaxEvaluations((int) timePeriod);
				FirstOrderDifferentialEquations f = new Derivation(); // why I should put if F^min and F^max
				//------------- min ----------------------
	
				minBoundaries[0] = accMin;
				integrator.integrate(f, startTime, minBoundaries, currentTime, minBoundaries);
				fLSpeedMin.value += minBoundaries[0];
				integrator.integrate(f, startTime, minBoundaries, currentTime, minBoundaries);
				fLPosMin.value += minBoundaries[0];
				//------------- max ----------------------
				
				maxBoundaries[0] = accMax;
				integrator.integrate(f, startTime, maxBoundaries, currentTime, maxBoundaries);
				fLSpeedMax.value += maxBoundaries[0];
				integrator.integrate(f, startTime, maxBoundaries, currentTime, maxBoundaries);
				fLPosMax.value += maxBoundaries[0];
				
				
				System.out.println("///... pos: min "+fLPosMin.value+" ... max "+fLPosMax.value+"      time :"+currentTime);
				System.out.println("///... speed: min "+fLSpeedMin.value+" ... max "+fLSpeedMax.value);
			
			} catch ( Exception e ){
				System.err.println("error : "+e.getMessage());//the error at the first of the execution because of the fLastTime is zero => the integration range is so big   
			}
			
			//-------------------------------------------------------safety part----------------------------------------------------------
			
			
			
			//----------------------------------------------------- controller part -------------------------------------------------------
				
				double distanceError = - wantedDistance + (fLPos - fPos);
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

				fLastTime.value = currentTime;
	}
	
	
	private static double saturate(double val) {
		if(val > 1) val = 1;
		else if(val < -1) val = -1;
		return val;
	}
	

	@Process
	@PeriodicScheduling((int) timePeriod)
	public static void computeAccelerationCACC(
			@In("fPos") Double fPos,
			@In("fLPos") Double fLPos,
			@In("fHeadwayDistance") @TriggerOnChange Double fHeadwayDistance, // it should not depend on that, because it may be will be out range of radar????? why I would put this field
			@Out("fTargetAcc") OutWrapper<Double> fTargetAcc
            ){
		
		if ( inaccuracy(distance(fLPos, fPos)) <= THRESHOLD){
		
		}
	}
	
	
	@Process
	@PeriodicScheduling((int) timePeriod)
	public static void computeAccelerationACC(
			@In("fPos") Double fPos,
			@In("fLPos") Double fLPos,
			@In("fHeadwayDistance") @TriggerOnChange Double fHeadwayDistance, // the same .....
			@Out("fTargetAcc") OutWrapper<Double> fTargetAcc
	){
		if ( inaccuracy(distance(fLPos, fPos)) > THRESHOLD){
		
		}
	}

	private static double inaccuracy(double distance){
		
		return 0;
			
	}

	private static double distance(double x, double y){
		return x - y;
	}

	public static class Derivation implements FirstOrderDifferentialEquations{

		@Override
		public int getDimension() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public void computeDerivatives(double t, double[] y, double[] yDot)
				throws MaxCountExceededException, DimensionMismatchException {
			// TODO Auto-generated method stub
			int params = 1;
			int order = 1;
			DerivativeStructure x = new DerivativeStructure(params, order, 0, y[0]);
			DerivativeStructure f = x.divide(t);
			yDot[0] = f.getValue();
		}
	}
}