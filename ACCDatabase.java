package ACC_NEW;

public class ACCDatabase {
	
	protected static final LookupTable driverSpeed = new LookupTable();
	protected static final LookupTable fTorques = new LookupTable();
	protected static final LookupTable lTorques = new LookupTable();
	protected static final LookupTable routeSlops = new LookupTable();
	protected static final LookupTable fAcceleration = new LookupTable();
	protected static final LookupTable lAcceleration = new LookupTable();
	protected static final LookupTable fAccelerationMin = new LookupTable();
	protected static final LookupTable lAccelerationMin = new LookupTable();
	protected static final LookupTable fAccelerationMax = new LookupTable();
	protected static final LookupTable lAccelerationMax = new LookupTable();
	protected static final double mass = 1000;
	protected static final double g = 9.80665;

	
	public static void driverBehaviour(){
		driverSpeed.put(0.0, 90);
		driverSpeed.put(1000.0, 90);
		driverSpeed.put(2000.0, 90);
		driverSpeed.put(3000.0, 150);
		driverSpeed.put(4000.0, 170);
		driverSpeed.put(5000.0, 90);
		driverSpeed.put(6000.0, 90);
		driverSpeed.put(7000.0, 90);
		driverSpeed.put(8000.0, 90);
		driverSpeed.put(9000.0, 90);
		driverSpeed.put(10000.0, 90);
		
	}
	
	public static void leaderTorques(){
		lTorques.put(0.0, 165.0);
		lTorques.put(8.0, 180.0);
		lTorques.put(20.0, 180.0);
		lTorques.put(28.0, 170.0);
		lTorques.put(40.0, 170.0);
		lTorques.put(60.0, 150.0);
		lTorques.put(80.0, 115.0);
		lTorques.put(100.0, 97.0);
		lTorques.put(120.0, 80.0);
		lTorques.put(140.0, 70.0);
		lTorques.put(160.0, 60.0);
		lTorques.put(180.0, 50.0);
		lTorques.put(200.0, 40.0);
		lTorques.put(100000.0, 1.0);
	}

	public static void followerTorques(){	
		fTorques.put(0.0, 165.0);
		fTorques.put(8.0, 180.0);
		fTorques.put(20.0, 180.0);
		fTorques.put(28.0, 170.0);
		fTorques.put(40.0, 170.0);
		fTorques.put(60.0, 150.0);
		fTorques.put(80.0, 115.0);
		fTorques.put(100.0, 97.0);
		fTorques.put(120.0, 80.0);
		fTorques.put(140.0, 70.0);
		fTorques.put(160.0, 60.0);
		fTorques.put(180.0, 50.0);
		fTorques.put(200.0, 40.0);
		fTorques.put(100000.0, 1.0);
	}
	
	public static void routeSlops(){
		routeSlops.put(0.0, 0.0);
		routeSlops.put(1000.0, 0.0);
		routeSlops.put(2000.0, Math.PI/60);
		routeSlops.put(3000.0, Math.PI/30);
		routeSlops.put(4000.0, Math.PI/20);
		routeSlops.put(5000.0, Math.PI/15);
		routeSlops.put(6000.0, 0.0);
		routeSlops.put(7000.0, 0.0);
		routeSlops.put(8000.0, -Math.PI/18);
		routeSlops.put(9000.0, -Math.PI/36);
		routeSlops.put(10000.0, 0.0);
		routeSlops.put(11000.0, 0.0);
		routeSlops.put(12000.0, 0.0);
		routeSlops.put(13000.0, 0.0);
		routeSlops.put(14000.0, 0.0);
		routeSlops.put(15000.0, 0.0);
		routeSlops.put(100000.0, 0.0);
	}

	public static Double getAcceleration(Double speed, Double pos, LookupTable torques,Double gas, Double brake){
		double FEng = gas * torques.get(speed) / 0.005;
		double FResistance = brake * 10000;
		double FEngResistance = 0.0005 * speed;
		double FHill = Math.sin(ACCDatabase.routeSlops.get(pos)) * g * mass;
		double FFinal = FEng - FResistance - FEngResistance - FHill;
		double Acceleration = FFinal / mass;
		return Acceleration;
	}
}
