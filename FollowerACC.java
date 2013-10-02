package ACC_NEW;


import java.util.HashMap;

import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.knowledge.Component;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;

public class FollowerACC extends Component {

public String name;
public Double currentFPos = 0.0;      
public Double currentFSpeed = 0.0;    
public Double followerGas = 0.0 ;
public Double followerBrake = 0.0;
public Double fLastTime = 0.0;

public Double currentLPos = 60.0;       
public Double currentLSpeed = 0.0;  
public Double lPosMin = 60.0;       
public Double lSpeedMin = 0.0;     
public Double lPosMax = 60.0;       
public Double lSpeedMax = 0.0;     
public Double lCreationTime = 0.0;	  

public Double integratorError = 0.0;
public Double errorWindup = 0.0;


protected static final double kpD = 0.193;
protected static final double kp = 0.12631;
protected static final double ki = 0.001;
protected static final double kt = 0.01;
protected static final double secNanoSecFactor = 1000000000;
protected static final int timePeriod = 100;

public FollowerACC() {
	name = "F";
}

@Process
@PeriodicScheduling(timePeriod)
public static void speedControl(
			@In("currentFPos") Double currentFPos,
			@In("currentFSpeed") Double currentFSpeed,
			@In("currentLPos") Double currentLPos,
			@In("currentLSpeed") Double currentLSpeed,
			@In("lCreationTime") Double lCreationTime,
			
			@Out("followerGas") OutWrapper<Double> followerGas,
			@Out("followerBrake") OutWrapper<Double> followerBrake,
			
			@InOut("lPosMin") OutWrapper<Double> lPosMin,
			@InOut("lSpeedMin") OutWrapper<Double> lSpeedMin,
			@InOut("lPosMax") OutWrapper<Double> lPosMax,
			@InOut("lSpeedMax") OutWrapper<Double> lSpeedMax,
			@InOut("fLastTime") OutWrapper<Double> fLastTime,
			@InOut("integratorError") OutWrapper<Double> integratorError,
			@InOut("errorWindup") OutWrapper<Double> errorWindup
		) {
	
		double currentTime = System.nanoTime()/secNanoSecFactor;
		double lTimePeriod = 0.0;
		HashMap<String, Double> boundaries = new HashMap<String, Double>();

		if( lCreationTime < fLastTime.value ){
			lTimePeriod = fLastTime.value > 0.0 ? currentTime - fLastTime.value : 0.0;
			boundaries = calculateBoundaries(lSpeedMin.value, lSpeedMax.value, lPosMin.value, lPosMax.value, ACCDatabase.lTorques, lTimePeriod);
			lPosMin.value += boundaries.get("xMin");
			lPosMax.value += boundaries.get("xMax");
			lSpeedMin.value += boundaries.get("dxMin");
			lSpeedMax.value += boundaries.get("dxMax");
			System.out.println("////... pos: min "+lPosMin.value+" ... max "+lPosMax.value + "     time :"+currentTime);
			System.out.println("////... speed: min "+lSpeedMin.value+" ... max "+lSpeedMax.value);
		}else { 
			lTimePeriod = lCreationTime > 0.0 ? currentTime - lCreationTime : 0.0;
			boundaries = calculateBoundaries(currentLSpeed, currentLSpeed, currentLPos, currentLPos, ACCDatabase.lTorques, lTimePeriod);
			lPosMin.value = currentLPos + boundaries.get("xMin");
			lPosMax.value = currentLPos + boundaries.get("xMax");
			lSpeedMin.value = currentLSpeed + boundaries.get("dxMin");
			lSpeedMax.value = currentLSpeed + boundaries.get("dxMax");
			System.out.println("\\\\... pos: min "+lPosMin.value+" ... max "+lPosMax.value+"      time :"+currentTime);
			System.out.println("\\\\... speed: min "+lSpeedMin.value+" ... max "+lSpeedMax.value);
		}
		
		
			double distanceError = - 50 + (currentLPos - currentFPos);
			double pidDistance = kpD * distanceError;
			double error = pidDistance + currentLSpeed - currentFSpeed;
			integratorError.value += (ki * error + kt * errorWindup.value) * timePeriod;
			double pidSpeed = kp * error + integratorError.value;
			errorWindup.value = saturate(pidSpeed) - pidSpeed;
	
			if(pidSpeed >= 0){
				followerGas.value = pidSpeed;
				followerBrake.value = 0.0;
			}else{
				followerGas.value = 0.0;
				followerBrake.value = -pidSpeed;
			}
			
			fLastTime.value = currentTime;
			
			
			if((lPosMin.value - currentFPos) <= 40){
				System.err.println("brake   -  minLPos :"+lPosMin.value+"  maxPos:"+lPosMax.value+" ,  currentFPos :"+currentFPos+"   creationTime: "+lCreationTime+"   currentTime: "+currentTime+"  flastTime: "+fLastTime.value);
				followerGas.value = 0.0;
				followerBrake.value = 1.0;
				
				
			}
	}


	private static double saturate(double val) {
		if(val > 1) val = 1;
		else if(val < -1) val = -1;
		return val;
	}
	
	private static HashMap<String, Double> calculateBoundaries( Double dxMin, Double dxMax, Double xMin, Double xMax, LookupTable torques, Double dt){
			double ddxMin = ACCDatabase.getAcceleration(dxMin, xMin, torques, 0.0, 1.0);
			double ddxMax = ACCDatabase.getAcceleration(dxMax, xMax, torques, 1.0, 0.0);
			dxMin = ddxMin * dt;
			dxMax = ddxMax * dt;
			xMin = dxMin * dt;
			xMax = dxMax * dt;
			HashMap<String, Double> val=new HashMap<String, Double>();
			val.put("xMin", xMin);
			val.put("xMax", xMax);
			val.put("dxMin", dxMin);
			val.put("dxMax", dxMax);
			return val;
	}
}
