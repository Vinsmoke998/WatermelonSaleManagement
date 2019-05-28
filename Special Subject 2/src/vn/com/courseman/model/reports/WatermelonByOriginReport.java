package vn.com.courseman.model.reports;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import domainapp.basics.core.dodm.dsm.DSMBasic;
import domainapp.basics.core.dodm.qrm.QRM;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.basics.model.Oid;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.MetaConstants;
import domainapp.basics.model.meta.Select;
import domainapp.basics.model.query.Expression.Op;
import domainapp.basics.model.query.Query;
import domainapp.basics.model.query.QueryToolKit;
import domainapp.basics.modules.report.model.meta.Output;
import vn.com.courseman.model.Customer;
import vn.com.courseman.model.Origin;
import vn.com.courseman.model.TypeOfCustomer;
import vn.com.courseman.model.Watermelon;
@DClass(schema="watermelon",serialisable=false)
public class WatermelonByOriginReport {
	@DAttr(name = "id", id = true, auto = true, type = Type.Integer, length = 5, optional = false, mutable = false)
	private int id;
	private static int idCounter = 0;

	/** input: student name */
	@DAttr(name = "origin", type = Type.String, length = 30, optional = false)
	private String origin;

	/** output: students whose names match {@link #name} */
	@DAttr(name = "watermelons", type = Type.Collection, optional = false, mutable = false, serialisable = false, filter = @Select(clazz = Watermelon.class, attributes = {
			Watermelon.A_id, Watermelon.A_weightByKg, Watermelon.A_origin,Watermelon.A_pricePerKg}), derivedFrom = { "origin" })
	@DAssoc(ascName = "origin-has-watermelon", role = "report", ascType = AssocType.One2Many, endType = AssocEndType.One, associate = @Associate(type = Watermelon.class, cardMin = 0, cardMax = MetaConstants.CARD_MORE))
	@Output
	private Collection<Watermelon> watermelons;
	
	

	/** output: number of students found (if any), derived from {@link #students} */
	@DAttr(name = "numCustomers", type = Type.Integer, length = 20, auto = true, mutable = false)
	@Output
	private int numWatermelons;

	/**
	   * @effects 
	   *  initialise this with <tt>name</tt> and use {@link QRM} to retrieve from data source 
	   *  all {@link Student} whose names match <tt>name</tt>.
	   *  initialise {@link #students} with the result if any.
	   *  
	   *  <p>throws NotPossibleException if failed to generate data source query; 
	   *  DataSourceException if fails to read from the data source
	   * 
	   */
	  @DOpt(type=DOpt.Type.ObjectFormConstructor)
	  @DOpt(type=DOpt.Type.RequiredConstructor)
	  public WatermelonByOriginReport(@AttrRef("origin") String origin) throws NotPossibleException, DataSourceException {
	    this.id=++idCounter;
	    
	    this.origin = origin;
	    
	    doReportQuery();
	  }

	/**
	 * @effects return name
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * @effects
	 * 
	 *          <pre>
	 *  set this.name = name
	 *  if name is changed
	 *    invoke {@link #doReportQuery()} to update the output attribute value
	 *    throws NotPossibleException if failed to generate data source query; 
	 *    DataSourceException if fails to read from the data source.
	 *          </pre>
	 */
	public void setOrigin(String origin) throws NotPossibleException, DataSourceException {
//	    boolean doReportQuery = (name != null && !name.equals(this.name));

		this.origin = origin;

		// DONOT invoke this here if there are > 1 input attributes!
		doReportQuery();
	}

	/**
	 * This method is invoked when the report input has be set by the user.
	 * 
	 * @effects
	 * 
	 *          <pre>
	 *   formulate the object query
	 *   execute the query to retrieve from the data source the domain objects that satisfy it 
	 *   update the output attributes accordingly.
	 *  
	 *  <p>throws NotPossibleException if failed to generate data source query; 
	 *  DataSourceException if fails to read from the data source.
	 *          </pre>
	 */
	@DOpt(type = DOpt.Type.DerivedAttributeUpdater)
	@AttrRef(value = "watermelon")
	public void doReportQuery() throws NotPossibleException, DataSourceException {
		// the query manager instance

		QRM qrm = QRM.getInstance();

		// create a query to look up Student from the data source
		// and then populate the output attribute (students) with the result
		DSMBasic dsm = qrm.getDsm();

		// TODO: to conserve memory cache the query and only change the query parameter
		// value(s)
		// look up TypeOfCustomer t such that name match type 
		Query q1 = QueryToolKit.createSearchQuery(dsm, Origin.class, new String[] { Origin.A_name },
				new Op[] { Op.MATCH }, new Object[] { "%" + origin + "%" });
		Map<Oid, Origin> result1 = qrm.getDom().retrieveObjects(Origin.class, q1);
				
		
		Origin t = null;
		if (result1 != null) {
			for (Entry<Oid, Origin> e : result1.entrySet()) {
				t = e.getValue();
				break;
			}
		}
		
		// if (t == null) throws exception
		
		// look up Customers such that typeOfCustomer Op.EQ= t
		Query q2 = QueryToolKit.createSearchQuery(dsm, Watermelon.class, new String[] { Watermelon.A_origin },
				new Op[] { Op.EQ }, new Object[] { t });

		Map<Oid, Watermelon> result2 = qrm.getDom().retrieveObjects(Watermelon.class, q2);

		if (result2 != null) {
			// update the main output data
			watermelons = result2.values();

			// update other output (if any)
			numWatermelons = watermelons.size();
		} else {
			// no data found: reset output
			resetOutput();
		}
//		
//		if (result2 != null) {
//			// update the main output data
//			types = result2.values();
//
//			// update other output (if any)
//			numCustomers = types.size();
//		} else {
//			// no data found: reset output
//			resetOutput();
//		}
	}

	/**
	 * @effects reset all output attributes to their initial values
	 */
	private void resetOutput() {
		watermelons = null;
		numWatermelons = 0;
	}

	/**
	 * A link-adder method for {@link #students}, required for the object form to
	 * function. However, this method is empty because students have already be
	 * recorded in the attribute {@link #students}.
	 */
	@DOpt(type = DOpt.Type.LinkAdder)
	public boolean addWatermelon(Collection<Watermelon> watermelons) {
		// do nothing
		return false;
	}

	/**
	 * @effects return students
	 */
	public Collection<Watermelon> getWatermelon() {
		return watermelons;
	}

	/**
	 * @effects return numStudents
	 */
	public int getNumWatermelons() {
		return numWatermelons;
	}

	/**
	 * @effects return id
	 */
	public int getId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	/**
	 * @effects
	 * 
	 * @version
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	/**
	 * @effects
	 * 
	 * @version
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WatermelonByOriginReport other = (WatermelonByOriginReport) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @effects
	 * 
	 * @version
	 */
	@Override
	public String toString() {
		return "CustomerByTypeReport (" + id + ", " + origin + ")";
	}
}
