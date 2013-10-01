package ACC_NEW;

import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.ensemble.Ensemble;
import cz.cuni.mff.d3s.deeco.knowledge.OutWrapper;



public class LeaderEnvEnsembleACC extends Ensemble {

@Membership
public static boolean membership(
		@In("coord.leaderGas") Double lGas,
		@In("coord.leaderBrake") Double lBrake,
		@In("coord.lPos") Double lPos,
		@In("coord.lSpeed") Double lSpeed,
		@In("coord.creationTime") Double creationTime,
		
		@In("member.eLeaderGas") Double eLeaderGas,
		@In("member.eLeaderBrake") Double eLeaderBrake,
		@In("member.eLPos") Double eLPos,
		@In("member.eLeaderSpeed") Double eLeaderSpeed,
		@In("member.eLastTime") Double eLastTime
	){
	return true;
	}

@KnowledgeExchange
@PeriodicScheduling(50)
public static void map(
		@In("coord.leaderGas") Double lGas,
		@In("coord.leaderBrake") Double lBrake,
		@Out("coord.lPos") OutWrapper<Double> lPos,
		@Out("coord.lSpeed") OutWrapper<Double> lSpeed,
		@Out("coord.creationTime") OutWrapper<Double> creationTime,
		
		@Out("member.eLeaderGas") OutWrapper<Double> eLeaderGas,
		@Out("member.eLeaderBrake") OutWrapper<Double> eLeaderBrake,
		@In("member.eLPos") Double eLPos,
		@In("member.eLeaderSpeed") Double eLeaderSpeed,
		@In("member.eLastTime") Double eLastTime
	) {

	eLeaderGas.value = lGas;
	eLeaderBrake.value = lBrake;
	lPos.value = eLPos;
	lSpeed.value = eLeaderSpeed;
	creationTime.value = eLastTime;
}

}

