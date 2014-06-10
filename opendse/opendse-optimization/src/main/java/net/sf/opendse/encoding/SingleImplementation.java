package net.sf.opendse.encoding;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.opendse.model.Link;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Routings;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import net.sf.opendse.optimization.ImplementationWrapper;
import net.sf.opendse.optimization.OptimizationModule;
import net.sf.opendse.optimization.SpecificationWrapper;
import net.sf.opendse.optimization.io.SpecificationWrapperInstance;
import net.sf.opendse.test.generator.RoutingGenerator;

import org.opt4j.core.Individual;
import org.opt4j.core.optimizer.Archive;
import org.opt4j.core.start.Opt4JModule;
import org.opt4j.core.start.Opt4JTask;
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule;

import com.google.inject.Module;

/**
 * The {@code SingleImplementation} determines an implementing
 * {@code Specification} from a general {@code Specification}.
 * 
 * @author Martin Lukasiewycz
 * 
 */
public class SingleImplementation {

	public Specification get(final Specification spec) {
		return get(spec, false);
	}

	public Specification get(final Specification spec, boolean generateRoutings) {
		if (generateRoutings) {
			RoutingGenerator routingsGenerator = new RoutingGenerator();
			Routings<Task, Resource, Link> fullRoutings = routingsGenerator.fill(
					spec.getApplication(), spec.getArchitecture());

			for (Task task : fullRoutings.getTasks()) {
				spec.getRoutings().remove(task);
				spec.getRoutings().set(task, fullRoutings.get(task));
			}
			RoutingFilter.filter(spec);
		}

		EvolutionaryAlgorithmModule ea = new EvolutionaryAlgorithmModule();
		ea.setGenerations(2);
		ea.setAlpha(1);
		ea.setLambda(1);

		Module specModule = new Opt4JModule() {

			@Override
			protected void config() {
				SpecificationWrapperInstance sw = new SpecificationWrapperInstance(spec);
				bind(SpecificationWrapper.class).toInstance(sw);
			}
		};

		OptimizationModule opt = new OptimizationModule();

		Collection<Module> modules = new ArrayList<Module>();
		modules.add(ea);
		modules.add(opt);
		modules.add(specModule);

		Opt4JTask task = new Opt4JTask(false);
		task.init(modules);

		try {
			task.execute();
			Archive archive = task.getInstance(Archive.class);

			for (Individual individual : archive) {
				Specification impl = ((ImplementationWrapper) individual.getPhenotype())
						.getImplementation();
				return impl;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			task.close();
		}

		return null;

	}

}
