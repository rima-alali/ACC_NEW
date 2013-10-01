package ACC_NEW;


import java.util.ArrayList;
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
public Double fCreationTime = 0.0;    
public Double followerGas = 0.0 ;
public Double followerBrake = 0.0;
public Double fLastTime = 0.0;

public Double currentLPos = 60.0;       
public Double currentLSpeed = 0.0;  
public Double minLPos = 60.0;       
public Double minLSpeed = 0.0;     
public Double minLAcc = 0.0;     
public Double maxLPos = 60.0;       
public Double maxLSpeed = 0.0;     
public Double maxLAcc = 0.0;     
public Double lCreationTime = 0.0;	  

public Double integratorError = 0.0;
public Double es = 0.0;


protected static final double kpD = 0.193;
protected static final double kp = 0.12631;
protected static final double ki = 0.001;
protected static final double kd = 0;
protected static final double kt = 0.01;
protected static final double secNanoSecFactor = 1000000000;


public FollowerACC() {
	name = "F";
}


@Process
@PeriodicScheduling(100)
public static void speedControl(

			@In("currentFPos") Double currentFPos,
			@In("currentFSpeed") Double currentFSpeed,
			@In("fCreationTime") Double fCreationTime,
			@In("currentLPos") Double currentLPos,
			@In("currentLSpeed") Double currentLSpeed,
			@In("lCreationTime") Double lCreationTime,
			
			@Out("followerGas") OutWrapper<Double> followerGas,
			@Out("followerBrake") OutWrapper<Double> followerBrake,
			
			@InOut("minLPos") OutWrapper<Double> minLPos,
			@InOut("minLSpeed") OutWrapper<Double> minLSpeed,
			@InOut("minLAcc") OutWrapper<Double> minLAcc,
			@InOut("maxLPos") OutWrapper<Double> maxLPos,
			@InOut("maxLSpeed") OutWrapper<Double> maxLSpeed,
			@InOut("maxLAcc") OutWrapper<Double> maxLAcc,
			@InOut("fLastTime") OutWrapper<Double> fLastTime,
			@InOut("integratorError") OutWrapper<Double> integratorError,
			@InOut("es") OutWrapper<Double> es
		) {
	
		double currentTime = System.nanoTime()/secNanoSecFactor;
		double fTimePeriod = fCreationTime > 0.0? currentTime - fCreationTime : 0.0; 
		double lTimePeriod = 0.0;
		HashMap<String, Double> boundaries = new HashMap<String, Double>();

		if( lCreationTime < fLastTime.value ){
			lTimePeriod = fLastTime.value > 0.0 ? currentTime - fLastTime.value : 0.0;
			boundaries = calculateBoundaries(minLSpeed.value, maxLSpeed.value, minLPos.value, maxLPos.value, lTimePeriod);
			minLPos.value += boundaries.get("minLPos");
			maxLPos.value += boundaries.get("maxLPos");
			minLSpeed.value += boundaries.get("minLSpeed");
			maxLSpeed.value += boundaries.get("maxLSpeed");
			System.out.println("////... pos: min "+minLPos.value+" ... max "+maxLPos.value + "     time :"+currentTime);
			System.out.println("////... speed: min "+minLSpeed.value+" ... max "+maxLSpeed.value);
			System.out.println("////... acc : min "+minLAcc+" ... max "+maxLAcc);
		
		}else { 
			lTimePeriod = lCreationTime > 0.0 ? currentTime - lCreationTime : 0.0;
			boundaries = calculateBoundaries(currentLSpeed, currentLSpeed, currentLPos, currentLPos, lTimePeriod);
			minLPos.value = currentLPos + boundaries.get("minLPos");
			maxLPos.value = currentLPos + boundaries.get("maxLPos");
			minLSpeed.value = currentLSpeed + boundaries.get("minLSpeed");
			maxLSpeed.value = currentLSpeed + boundaries.get("maxLSpeed");
			System.out.println("\\\\... pos: min "+minLPos.value+" ... max "+maxLPos.value+"      time :"+currentTime);
			System.out.println("\\\\... speed: min "+minLSpeed.value+" ... max "+maxLSpeed.value);
			System.out.println("\\\\... acc : min "+minLAcc+" ... max "+maxLAcc);
		}
		
			double distanceError = - 50 + (currentLPos - currentFPos);
			double pidDistance = kpD * distanceError;
			double error = pidDistance + currentLSpeed - currentFSpeed;
			integratorError.value += (ki * error + kt * es.value) * fTimePeriod;
			double pidSpeed = kp * error + integratorError.value;
			es.value = saturate(pidSpeed) - pidSpeed;
			pidSpeed = saturate(pidSpeed);
		

			if(pidSpeed >= 0){
				followerGas.value = pidSpeed;
				followerBrake.value = 0.0;
			}else{
				followerGas.value = 0.0;
				followerBrake.value = -pidSpeed;
			}
			
			fLastTime.value = currentTime;
			
			
			if((minLPos.value - currentFPos) <= 40){
				System.err.println("brake   -  minLPos :"+minLPos.value+"  maxPos:"+maxLPos.value+" ,  currentFPos :"+currentFPos+
						"   creationTime: "+lCreationTime+"   currentTime: "+currentTime+"  flastTime: "+fLastTime.value);
			}
	}


	private static double saturate(double pid) {
		if(pid > 1) pid = 1;
		else if(pid < -1) pid = -1;
		return pid;
	}
	
	private static Double checkSafety(Double wcLPos, Double fPos){
		if( (fPos - wcLPos) <= 40 ) return 1.0;
		return 0.0;
	}
	
	private static HashMap<String, Double> calculateBoundaries( Double minLSpeed, Double maxLSpeed, Double minLPos, Double maxLPos, Double dt){
			double minLAcc = ACCDatabase.getAcceleration(minLSpeed, minLPos, ACCDatabase.lTorques, 0.0, 1.0);
			double maxLAcc = ACCDatabase.getAcceleration(maxLSpeed, maxLPos, ACCDatabase.lTorques, 1.0, 0.0);
			minLSpeed = minLAcc * dt;
			maxLSpeed = maxLAcc * dt;
			minLPos = minLSpeed * dt;
			maxLPos = maxLSpeed * dt;
			HashMap<String, Double> val=new HashMap<String, Double>();
			val.put("minLPos", minLPos);
			val.put("maxLPos", maxLPos);
			val.put("minLSpeed", minLSpeed);
			val.put("maxLSpeed", maxLSpeed);
			return val;
	}
}
