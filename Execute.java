package ACC_NEW;

import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.RepositoryKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.local.LocalKnowledgeRepository;
import cz.cuni.mff.d3s.deeco.provider.AbstractDEECoObjectProvider;
import cz.cuni.mff.d3s.deeco.provider.ClassDEECoObjectProvider;
import cz.cuni.mff.d3s.deeco.runtime.Runtime;
import cz.cuni.mff.d3s.deeco.scheduling.MultithreadedScheduler;
import cz.cuni.mff.d3s.deeco.scheduling.Scheduler;

public class Execute {

	public static void main(String[] args) {
		List<Class<?>> components = Arrays.asList(new Class<?>[] {
		LeaderACC.class,
		FollowerACC.class,
		EnvironmentACC.class
		});
		
		List<Class<?>> ensembles = Arrays.asList(new Class<?>[] {
		LeaderEnvEnsembleACC.class,
		FollowerEnvEnsembleACC.class,
		FollowerLeaderEnsembleACC.class
		});
		
		KnowledgeManager km = new RepositoryKnowledgeManager(new LocalKnowledgeRepository());
		Scheduler scheduler = new MultithreadedScheduler();
		ACCDatabase.initializer();
		
		Runtime rt = new Runtime(km, scheduler);
		System.out.println("comp "+ components.size() + " , ensembles : "+ ensembles.size());
		AbstractDEECoObjectProvider provider = new ClassDEECoObjectProvider(components, ensembles);
		System.out.println(" provider " + provider.getEnsembles());
		
		rt.registerComponentsAndEnsembles(provider);
		rt.startRuntime();
	}

}