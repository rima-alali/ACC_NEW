package ACC_NEW;

import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;



public class FollowerLeaderEnsembleACC extends Ensemble {

	@Membership
	public static boolean membership(
			@In("coord.currentLPos") Double currentLPos,
			@In("coord.currentLSpeed") Double currentLSpeed,
			@In("coord.lCreationTime") Double lCreationTime,
	
			@In("member.lPos") Double lPos,
			@In("member.lSpeed") Double lSpeed,
			@In("member.creationTime") Double creationTime
		){
		return true;
	}
	
	@KnowledgeExchange
	@PeriodicScheduling(50)
	public static void map(
			@Out("coord.currentLPos") OutWrapper<Double> fCurrentLPos,
			@Out("coord.currentLSpeed") OutWrapper<Double> fCurrentLSpeed,
			@Out("coord.lCreationTime") OutWrapper<Double> fLCreationTime,
	
			@In("member.lPos") Double lPos,
			@In("member.lSpeed") Double lSpeed,
			@In("member.creationTime") Double creationTime
	) {
	
	fCurrentLPos.value = lPos;
	fCurrentLSpeed.value = lSpeed;
	fLCreationTime.value = creationTime;
		
	}

}


