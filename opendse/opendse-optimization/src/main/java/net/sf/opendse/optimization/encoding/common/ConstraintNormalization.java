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
package net.sf.opendse.optimization.encoding.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opt4j.satdecoding.Constraint;
import org.opt4j.satdecoding.Literal;
import org.opt4j.satdecoding.Term;
import org.opt4j.satdecoding.Constraint.Operator;

/**
 * The {@code ConstraintNormalization} normalizes constraints.
 * 
 * @author Martin Lukasiewycz
 * 
 */
class ConstraintNormalization {

	public void normalize(Constraint constraint) {
		switch (constraint.getOperator()) {
		case LE:
			inverse(constraint);
			unify(constraint);
			positive(constraint);
			sorting(constraint);
			trimming(constraint);
			gcd(constraint);
			break;
		case GE:
			unify(constraint);
			positive(constraint);
			sorting(constraint);
			trimming(constraint);
			gcd(constraint);
			break;
		default: // EQ
			throw new IllegalArgumentException("Cannot normalize equality constraints");
		}

	}

	private void inverse(Constraint constraint) {
		assert (constraint.getOperator() == Operator.LE);

		List<Term> terms = new ArrayList<Term>(constraint.size());
		for (Term term : constraint) {
			int coeff = -term.getCoefficient();
			Literal lit = term.getLiteral();
			terms.add(new Term(coeff, lit));
		}
		constraint.setRhs(-constraint.getRhs());
		constraint.setOperator(Operator.GE);
		constraint.clear();
		constraint.addAll(terms);
	}

	private void positive(Constraint constraint) {
		int rhs = constraint.getRhs();
		for (int i = 0; i < constraint.size(); i++) {
			Term term = constraint.get(i);
			int coeff = term.getCoefficient();

			if (coeff < 0) {
				Term t = new Term(-coeff, term.getLiteral().negate());
				constraint.set(i, t);
				rhs -= coeff;
			} else if (coeff == 0) {
				constraint.remove(i);
				i--;
			}
		}
		constraint.setRhs(rhs);
		if (rhs <= 0) {
			// trivially satisfied
			constraint.clear();
		}
	}

	private void unify(Constraint constraint) {
		assert (constraint.getOperator() == Operator.GE);

		Set<Object> variables = new HashSet<Object>();

		boolean foundDoubleAppearance = false;
		for (Literal literal : constraint.getLiterals()) {
			Object variable = literal.variable();
			if (variables.contains(variable)) {
				foundDoubleAppearance = true;
				break;
			} else {
				variables.add(variable);
			}
		}

		if (foundDoubleAppearance) {
			int rhs = constraint.getRhs();
			Map<Literal, Integer> map = new HashMap<Literal, Integer>();

			for (Term term : constraint) {
				int coeff = term.getCoefficient();
				Literal lit = term.getLiteral();

				if (map.containsKey(lit)) {
					map.put(lit, map.get(lit) + coeff);
				} else if (map.containsKey(lit.negate())) {
					map.put(lit.negate(), map.get(lit.negate()) - coeff);
					rhs -= coeff;
				} else {
					map.put(lit, coeff);
				}
			}

			constraint.clear();

			for (Entry<Literal, Integer> entry : map.entrySet()) {
				int coeff = entry.getValue();
				Literal lit = entry.getKey();

				Term term = new Term(coeff, lit);
				constraint.add(term);
			}

			constraint.setRhs(rhs);
		}

	}

	Comparator<Term> sorting = new Comparator<Term>() {
		@Override
		public int compare(Term o1, Term o2) {
			Integer c1 = o1.getCoefficient();
			Integer c2 = o2.getCoefficient();
			return c1.compareTo(c2);
		}

	};

	private void sorting(Constraint constraint) {
		Collections.sort(constraint, sorting);
	}

	private void trimming(Constraint constraint) {
		assert (constraint.getOperator() == Operator.GE);

		int rhs = constraint.getRhs();
		for (int i = 0; i < constraint.size(); i++) {
			Term term = constraint.get(i);
			int coeff = term.getCoefficient();

			if (coeff > rhs) {
				Term t = new Term(rhs, term.getLiteral());
				constraint.set(i, t);

			}
		}
	}

	private void gcd(Constraint constraint) {
		assert (constraint.getOperator() == Operator.GE);
		final int size = constraint.size();

		if (size > 0) {
			int g = constraint.get(0).getCoefficient();
			{
				int i = 1;
				int rhs = constraint.getRhs();
				while (i < size && g > 1) {
					int coeff = constraint.get(i).getCoefficient();
					if (coeff < rhs) {
						g = gcd(g, coeff);
					}
					i++;
				}
			}
			if (g > 1) {
				for (int i = 0; i < size; i++) {
					Term term = constraint.get(i);
					Literal lit = term.getLiteral();
					double coeff = (double) term.getCoefficient() / (double) g;
					constraint.set(i, new Term((int) Math.ceil(coeff), lit));
				}

				double rhs = (double) constraint.getRhs() / (double) g;
				constraint.setRhs((int) Math.ceil(rhs));
			}
		}
	}

	private int gcd(int n, int m) {
		if (m < n) {
			return gcd(m, n);
		} else {
			int r = m % n;
			if (r == 0) {
				return n;
			} else {
				return gcd(n, r);
			}
		}

	}

}
