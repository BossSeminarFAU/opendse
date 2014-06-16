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
package net.sf.opendse.optimization.evaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.opendse.io.Common;
import net.sf.opendse.io.SpecificationReader;
import net.sf.opendse.model.Specification;
import net.sf.opendse.optimization.ImplementationEvaluator;

import org.opt4j.core.Objective;

public abstract class AbstractExternalEvaluator implements ImplementationEvaluator {

	protected final String command;
	protected final int priority;

	Map<String, Objective> objectiveMap = new HashMap<String, Objective>();

	public AbstractExternalEvaluator(String command, int priority) {
		super();
		this.command = command;
		this.priority = priority;
	}

	protected class ObjectiveElement {
		protected final Objective.Sign sign;
		protected final String name;
		protected final double value;

		public ObjectiveElement(Objective.Sign sign, String name, double value) {
			this.sign = sign;
			this.name = name;
			this.value = value;
		}

		public Objective.Sign getSign() {
			return sign;
		}

		public String getName() {
			return name;
		}

		public double getValue() {
			return value;
		}
	}

	protected class ResultElement {

		protected final List<ObjectiveElement> objectiveElements = new ArrayList<ObjectiveElement>();
		protected final Specification specification;

		public ResultElement(Collection<ObjectiveElement> objectiveElements, Specification specification) {
			this.objectiveElements.addAll(objectiveElements);
			this.specification = specification;
		}

		public Collection<ObjectiveElement> getObjectiveElements() {
			return objectiveElements;
		}

		public Specification getSpecification() {
			return specification;
		}

	}

	class ErrorThread extends Thread {

		InputStream err;

		public ErrorThread(InputStream err) {
			super();
			this.err = err;
		}

		public void run() {
			BufferedReader input = new BufferedReader(new InputStreamReader(err));
			String line = null;
			try {
				while ((line = input.readLine()) != null) {
					System.out.println("[external: " + line + "]");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("DONE");
		}
	};

	protected Objective toObjective(ObjectiveElement objectiveElement) {
		return new Objective(objectiveElement.getName(), objectiveElement.getSign());
	}

	protected ResultElement getResultElement(File file){
		try {
			FileInputStream in = new FileInputStream(file);
			ResultElement re = getResultElement(in);
			in.close();
			return re;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected ResultElement getResultElement(InputStream in) {

		try {
			nu.xom.Builder parser = new nu.xom.Builder();
			nu.xom.Document doc = parser.build(in);

			/*
			 * Serializer serializer = new Serializer(System.out);
			 * serializer.setIndent(2); serializer.setMaxLength(2000);
			 * serializer.write(doc); serializer.flush();
			 */

			nu.xom.Element eResult = doc.getRootElement();
			ResultElement resultElement = getResultElement(eResult);

			return resultElement;

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	protected ResultElement getResultElement(nu.xom.Element eResult) throws IOException {
		nu.xom.Element eObjectives = eResult.getFirstChildElement("objectives");

		Collection<ObjectiveElement> objectiveElements = (eObjectives != null) ? getObjectiveElements(eObjectives)
				: new ArrayList<ObjectiveElement>();

		nu.xom.Element eSpecification = eResult.getFirstChildElement("specification");

		Specification spec = null;
		if (eSpecification != null) {
			SpecificationReader reader = new SpecificationReader();
			spec = reader.toSpecification(eSpecification);
		}

		return new ResultElement(objectiveElements, spec);
	}

	protected Collection<ObjectiveElement> getObjectiveElements(nu.xom.Element eObjectives) {
		List<ObjectiveElement> objectiveElements = new ArrayList<ExternalEvaluatorStream.ObjectiveElement>();

		for (nu.xom.Element eObjective : Common.iterable(eObjectives.getChildElements("objective"))) {
			objectiveElements.add(getObjectiveElement(eObjective));
		}

		return objectiveElements;
	}

	protected ObjectiveElement getObjectiveElement(nu.xom.Element eObjective) {
		Objective.Sign sign = null;
		String name = null;
		double value = 0.0;

		name = eObjective.getAttributeValue("name");
		String signName = eObjective.getAttributeValue("sign");
		if (signName.equals("MIN")) {
			sign = Objective.Sign.MIN;
		} else if (signName.equals("MAX")) {
			sign = Objective.Sign.MAX;
		}

		String valueString = eObjective.getValue();

		if (valueString != null && !valueString.equals("")) {
			value = new Double(valueString);
		}

		return new ObjectiveElement(sign, name, value);
	}

	@Override
	public int getPriority() {
		return priority;
	}

}
