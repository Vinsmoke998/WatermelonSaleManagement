package vn.com.courseman.model.reports;

import java.util.Collection;
import java.util.Map;

import domainapp.basics.core.dodm.dsm.DSMBasic;
import domainapp.basics.core.dodm.qrm.QRM;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.basics.model.Oid;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.MetaConstants;
import domainapp.basics.model.meta.Select;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.query.Query;
import domainapp.basics.model.query.QueryToolKit;
import domainapp.basics.model.query.Expression.Op;
import domainapp.basics.modules.report.model.meta.Output;
import vn.com.courseman.model.Customer;
import vn.com.courseman.model.WatermelonOrder;
@DClass(schema="watermelon",serialisable=false)
public class OrderByRateReport {
	@DAttr(name = "id", id = true, auto = true, type = Type.Integer, length = 5, optional = false, mutable = false)
	private int id;
	private static int idCounter = 0;

	/** input: student name */
	@DAttr(name = "rateOrder", type = Type.Char, length = 1, optional = false)
	private char rateOrder;

	/** output: students whose names match {@link #name} */
	@DAttr(name = "orders", type = Type.Collection, optional = false, mutable = false, serialisable = false, filter = @Select(clazz = WatermelonOrder.class, attributes = {
			WatermelonOrder.A_id, WatermelonOrder.AttributeName_OrderDate, WatermelonOrder.AttributeName_NumOfKg,WatermelonOrder.AttributeName_DisCount, WatermelonOrder.A_RateOrder, WatermelonOrder.AttributeName_TotalPrice,
			WatermelonOrder.A_rptOrderByRate }), derivedFrom = { "rateOrder" })
	@DAssoc(ascName = "orders-by-rate-report-has-orders", role = "report", ascType = AssocType.One2Many, endType = AssocEndType.One, associate = @Associate(type = Customer.class, cardMin = 0, cardMax = MetaConstants.CARD_MORE))
	@Output
	private Collection<WatermelonOrder> orders;

	/** output: number of students found (if any), derived from {@link #students} */
	@DAttr(name = "numOrders", type = Type.Integer, length = 20, auto = true, mutable = false)
	@Output
	private int numOrders;

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
	  public OrderByRateReport(@AttrRef("rateOrder") Character rateOrder) throws NotPossibleException, DataSourceException {
	    this.id=++idCounter;
	    
	    this.rateOrder = rateOrder;
	    
	    doReportQuery();
	  }

	/**
	 * @effects return name
	 */
	public char getRateOrder() {
		return rateOrder;
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
	public void setRateOrder(Character rateOrder) throws NotPossibleException, DataSourceException {
//	    boolean doReportQuery = (name != null && !name.equals(this.name));

		this.rateOrder = rateOrder;

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
	@AttrRef(value = "orders")
	public void doReportQuery() throws NotPossibleException, DataSourceException {
		// the query manager instance

		QRM qrm = QRM.getInstance();

		// create a query to look up Student from the data source
		// and then populate the output attribute (students) with the result
		DSMBasic dsm = qrm.getDsm();

		// TODO: to conserve memory cache the query and only change the query parameter
		// value(s)
		Query q = QueryToolKit.createSearchQuery(dsm, WatermelonOrder.class, new String[] { WatermelonOrder.A_RateOrder },
				new Op[] { Op.MATCH }, new Object[] { "%" + rateOrder + "%" });

		Map<Oid, WatermelonOrder> result = qrm.getDom().retrieveObjects(WatermelonOrder.class, q);

		if (result != null) {
			// update the main output data
			orders = result.values();

			// update other output (if any)
			numOrders = orders.size();
		} else {
			// no data found: reset output
			resetOutput();
		}
	}

	/**
	 * @effects reset all output attributes to their initial values
	 */
	private void resetOutput() {
		orders = null;
		numOrders = 0;
	}

	/**
	 * A link-adder method for {@link #students}, required for the object form to
	 * function. However, this method is empty because students have already be
	 * recorded in the attribute {@link #students}.
	 */
	@DOpt(type = DOpt.Type.LinkAdder)
	public boolean addOrders(Collection<WatermelonOrder> orders) {
		// do nothing
		return false;
	}

	/**
	 * @effects return students
	 */
	public Collection<WatermelonOrder> getOrders() {
		return orders;
	}

	/**
	 * @effects return numStudents
	 */
	public int getNumOrders() {
		return numOrders;
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
		OrderByRateReport other = (OrderByRateReport) obj;
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
		return "OrderByRateReport (" + id + ", " + rateOrder + ")";
	}
}

