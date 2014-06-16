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
package net.sf.opendse.model;

import java.util.Set;

import net.sf.opendse.model.parameter.Parameter;

/**
 * The {@code Specification} consists of an {@link Application},
 * {@link Architecture}, {@link Mappings}, and {@link Routings}.
 * 
 * @author Martin Lukasiewycz
 * 
 */
public class Specification implements IAttributes {

	protected Architecture<?, ?> architecture = null;
	protected Application<?, ?> application = null;
	protected Mappings<?, ?> mappings = null;
	protected Routings<?, ?, ?> routings = null;
	protected Attributes attributes = new Attributes();

	/**
	 * Constructs the specification.
	 * 
	 * @param application
	 *            the application
	 * @param architecture
	 *            the architecture
	 * @param mappings
	 *            the mappings
	 * @param routings
	 *            the routings
	 */
	public Specification(Application<?, ?> application, Architecture<?, ?> architecture, Mappings<?, ?> mappings,
			Routings<?, ?, ?> routings) {
		super();
		this.architecture = architecture;
		this.application = application;
		this.mappings = mappings;
		this.routings = routings;
	}

	/**
	 * Returns the architecture.
	 * 
	 * @param <A>
	 *            the type of architecture
	 * @return the architecture
	 */
	@SuppressWarnings("unchecked")
	public <A extends Architecture<Resource, Link>> A getArchitecture() {
		return (A) architecture;
	}

	/**
	 * Returns the application.
	 * 
	 * @param <A>
	 *            the type of application
	 * @return the application
	 */
	@SuppressWarnings("unchecked")
	public <A extends Application<Task, Dependency>> A getApplication() {
		return (A) application;
	}

	/**
	 * Returns the mappings.
	 * 
	 * @param <M>
	 *            the type of mappings
	 * @return the mappings
	 */
	@SuppressWarnings("unchecked")
	public <M extends Mappings<Task, Resource>> M getMappings() {
		return (M) mappings;
	}

	/**
	 * Returns the routings.
	 * 
	 * @param <R>
	 *            the type of routings
	 * @return the routings
	 */
	@SuppressWarnings("unchecked")
	public <R extends Routings<Task, Resource, Link>> R getRoutings() {
		return (R) routings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.adse.model.IAttributes#setAttribute(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void setAttribute(String identifier, Object object) {
		attributes.setAttribute(identifier, object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.adse.model.IAttributes#getAttribute(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <O> O getAttribute(String identifier) {
		return (O) attributes.getAttribute(identifier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.adse.model.IAttributes#getAttributeParameter(java.lang.String)
	 */
	@Override
	public Parameter getAttributeParameter(String identifier) {
		return attributes.getAttributeParameter(identifier);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.adse.model.IAttributes#getAttributes()
	 */
	@Override
	public Attributes getAttributes() {
		return attributes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.adse.model.IAttributes#getAttributeNames()
	 */
	@Override
	public Set<String> getAttributeNames() {
		return attributes.getAttributeNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.adse.model.IAttributes#isDefined(java.lang.String)
	 */
	@Override
	public boolean isDefined(String identifier) {
		return attributes.isDefined(identifier);
	}

}
