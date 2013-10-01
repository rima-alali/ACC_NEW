package ACC_NEW;


import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;



public class FollowerEnvEnsembleACC extends Ensemble {

	@Membership
	public static boolean membership(
			@In("coord.currentFPos") Double currentFPos,
			@In("coord.currentFSpeed") Double currentFSpeed,
			@In("coord.fCreationTime") Double fCreationTime,
			@In("coord.followerGas") Double followerGas,
			@In("coord.followerBrake") Double followerBrake,
		
			@In("member.eFollowerGas") Double eFollowerGas,
			@In("member.eFollowerBrake") Double eFollowerBrake,
			@In("member.eFPos") Double eFPos,
			@In("member.eFollowerSpeed") Double eFollowerSpeed,
			@In("member.eLastTime") Double eLastTime
			){
			return true;
	}
	
	@KnowledgeExchange
	@PeriodicScheduling(50)
	public static void map(
		@Out("coord.currentFPos") OutWrapper<Double> currentFPos,
		@Out("coord.currentFSpeed") OutWrapper<Double> currentFSpeed,
		@Out("coord.fCreationTime") OutWrapper<Double> fCreationTime,
		@In("coord.followerGas") Double followerGas,
		@In("coord.followerBrake") Double followerBrake,
	
		@Out("member.eFollowerGas") OutWrapper<Double> eFollowerGas,
		@Out("member.eFollowerBrake") OutWrapper<Double> eFollowerBrake,
		@In("member.eFPos") Double eFPos,
		@In("member.eFollowerSpeed") Double eFollowerSpeed,
		@In("member.eLastTime") Double eLastTime
	
	) {
	
		eFollowerGas.value = followerGas;
		eFollowerBrake.value = followerBrake;
		currentFPos.value = eFPos;
		currentFSpeed.value = eFollowerSpeed;
		fCreationTime.value = eLastTime;
	}
}