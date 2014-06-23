/**
 * OpenDSE is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * OpenDSE is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with OpenDSE. If not, see http://www.gnu.org/licenses/.
 */
package net.sf.opendse.optimization.test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sf.opendse.io.SpecificationWriter;
import net.sf.opendse.model.Application;
import net.sf.opendse.model.Architecture;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Link;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Mappings;
import net.sf.opendse.model.Models;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Routings;
import net.sf.opendse.model.Specification;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.parameter.Parameter;
import net.sf.opendse.model.parameter.Parameters;
import net.sf.opendse.optimization.constraints.SpecificationConstraints;
import net.sf.opendse.optimization.encoding.SingleImplementation;
import net.sf.opendse.visualization.SpecificationViewer;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class FirstSpecificationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		withCommunication();
	}

	public static void noCommunication() {
		
		// 1. Application
		Application<Task, Dependency> application = new Application<Task, Dependency>();
		Task t1 = new Task("t1");
		Task t2 = new Task("t2");
		application.addVertex(t1);
		application.addVertex(t2);
		application.addEdge(new Dependency("d1"), t1, t2);
		// 2. Architecture
		Architecture<Resource, Link> architecture = new Architecture<Resource, Link>();
		Resource r1 = new Resource("r1");
		r1.setAttribute("costs", 100);
		Resource r2 = new Resource("r2");
		r2.setAttribute("costs", 50);
		Link l1 = new Link("l1");
		architecture.addVertex(r1);
		architecture.addVertex(r2);
		architecture.addEdge(l1, r1, r2);
		// 3. Mappings
		Mappings<Task, Resource> mappings = new Mappings<Task, Resource>();
		Mapping<Task, Resource> m1 = new Mapping<Task, Resource>("m1", t1, r1);
		Mapping<Task, Resource> m2 = new Mapping<Task, Resource>("m2", t2, r2);
		mappings.add(m1);
		mappings.add(m2);
		// 4. Routings
		Routings<Task, Resource, Link> routings = new Routings<Task, Resource, Link>();

		Specification specification = new Specification(application, architecture, mappings,
				routings);
		
		SpecificationViewer.view(specification);

	}

	public static void withCommunication() {
		
		// 1. Application
		Application<Task, Dependency> application = new Application<Task, Dependency>();
		Task t1 = new Task("t1");
		t1.setAttribute("memory", 40);
		Communication c1 = new Communication("c1");
		c1.setAttribute("memory", 1);
		Task t2 = new Task("t2");
		application.addVertex(t1);
		application.addVertex(t2);
		application.addEdge(new Dependency("d1"), t1, c1);
		application.addEdge(new Dependency("d2"), c1, t2);
		
		// 2. Architecture
		Architecture<Resource, Link> architecture = new Architecture<Resource, Link>();
		Resource r1 = new Resource("r1");
		r1.setAttribute("costs", 100);
		r1.setAttribute("memory"+SpecificationConstraints.CAPACITY_MAX, 128);
		Resource r2 = new Resource("r2");
		r2.setAttribute("costs", 50);
		r2.setAttribute("memory"+SpecificationConstraints.CAPACITY_MAX, 64);
		Resource r3 = new Resource("r3");
		r3.setAttribute("costs", 50);
		r3.setAttribute("memory"+SpecificationConstraints.CAPACITY_MAX, Parameters.select(64, 128, 196));
		Link l1 = new Link("l1");
		Link l2 = new Link("l2");
		architecture.addVertex(r1);
		architecture.addVertex(r2);
		architecture.addEdge(l1, r1, r2);
		architecture.addEdge(l2, r2, r3);
		
		// 3. Mappings
		Mappings<Task, Resource> mappings = new Mappings<Task, Resource>();
		Mapping<Task, Resource> m1 = new Mapping<Task, Resource>("m1", t1, r1);
		Mapping<Task, Resource> m2 = new Mapping<Task, Resource>("m2", t2, r2);
		m2.setAttribute("memory", 70);
		Mapping<Task, Resource> m3 = new Mapping<Task, Resource>("m3", t2, r3);
		m3.setAttribute("memory", 75);
		
		mappings.add(m1);
		mappings.add(m2);
		mappings.add(m3);
		
		
		Specification specification = new Specification(application, architecture, mappings);
		SpecificationWriter writer = new SpecificationWriter();
		writer.write(specification, "spec.xml");
		
		SingleImplementation single = new SingleImplementation();
		specification = single.get(specification, true);
		
		
		
		SpecificationViewer.view(specification);
		
		
	}

}
