package vn.com.courseman.model;

import java.util.ArrayList;
import java.util.Collection;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.util.Tuple;
import domainapp.basics.util.cache.StateHistory;
import vn.com.courseman.model.reports.CustomerByNameReport;
import vn.com.courseman.model.reports.OrderByRateReport;

/**
 * Represents an enrolment
 * 
 * @author dmle
 * 
 */
@DClass(schema = "courseman")
public class WatermelonOrder implements Comparable {
	public static final String A_id = "id";
	public static final String AttributeName_OrderDate = "orderDate";
	public static final String AttributeName_NumOfKg = "numOfKg";
	public static final String AttributeName_TotalPrice = "totalPrice";
	public static final String AttributeName_DisCount = "disCount";
	public static final String A_RateOrder = "rateOrder";
	public static final String A_rptOrderByRate = "rptOrderByRate";

	// attributes
	@DAttr(name = A_id, id = true, auto = true, type = Type.Integer, length = 5, optional = false, mutable = false)
	private int id;
	private static int idCounter = 0;

	@DAttr(name = "watermelon", type = Type.Domain, length = 5, optional = false)
	@DAssoc(ascName = "watermelon-has-orders", role = "order", ascType = AssocType.One2Many, endType = AssocEndType.Many, associate = @Associate(type = Watermelon.class, cardMin = 1, cardMax = 1), dependsOn = true)
	private Watermelon watermelon;

	@DAttr(name = "customer", type = Type.Domain, length = 5, optional = false)
	@DAssoc(ascName = "customer-has-orders", role = "order", ascType = AssocType.One2Many, endType = AssocEndType.Many, associate = @Associate(type = Customer.class, cardMin = 1, cardMax = 1), dependsOn = true)
	private Customer customer;

	@DAttr(name = AttributeName_OrderDate, type = Type.String, length = 15, optional = false, min = 0.0)
	private String orderDate;

	@DAttr(name = AttributeName_NumOfKg, type = Type.Double, length = 8, optional = false, min = 0.0)
	private Double numOfKg;

	@DAttr(name = AttributeName_DisCount, type = Type.Double, length = 4, optional = false, min = 0.0)
	private Double disCount;

	@DAttr(name = A_RateOrder, auto = true, type = Type.Char, length = 1, mutable = false, optional = true
	/*
	 * Note: no need to do this: derivedFrom={"internalMark,examMark"} because
	 * finalGrade and finalMark are updated by the same method and this is already
	 * specified by finalMark (below)
	 */
	)
	private char rateOrder;

	@DAttr(name = A_rptOrderByRate, type = Type.Domain, serialisable = false,
			// IMPORTANT: set virtual=true to exclude this attribute from the object state
			// (avoiding the view having to load this attribute's value from data source)
			virtual = true)
	private OrderByRateReport rptOrderByRate;

	// v2.6.4.b derived from two attributes
	@DAttr(name = AttributeName_TotalPrice, type = Type.Integer, auto = true, mutable = false, optional = true, serialisable = false, derivedFrom = {
			AttributeName_NumOfKg, AttributeName_DisCount })
	private Integer totalPrice;

	// v2.6.4.b
	private StateHistory<String, Object> stateHist;

	// constructor method
	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	@DOpt(type = DOpt.Type.RequiredConstructor)
	public WatermelonOrder(@AttrRef("watermelon") Watermelon s, @AttrRef("customer") Customer m)
			throws ConstraintViolationException {
		this(null, s, m, null, 0.0, 0.0, null);
	}

	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	public WatermelonOrder(@AttrRef("watermelon") Watermelon s, @AttrRef("customer") Customer m,
			@AttrRef("orderDate") String orderDate, @AttrRef("numOfKg") Double numOfKg,
			@AttrRef("disCount") Double disCount) throws ConstraintViolationException {
		this(null, s, m, orderDate, numOfKg, disCount, null);
	}

	// @version 2.0
	@DOpt(type = DOpt.Type.DataSourceConstructor)
	public WatermelonOrder(Integer id, Watermelon s, Customer m, String orderDate, Double numOfKg, Double disCount,
			// v2.7.3: not used but needed to load data from source
			Character rateOrder) throws ConstraintViolationException {
		this.id = nextID(id);
		this.watermelon = s;
		this.customer = m;
		if (!orderDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
			throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE,
					new Object[] { "'" + orderDate + "'(the date should follow the format 'dd/mm/yyyy')" });
		} else {
			this.orderDate = orderDate;
		}
		this.numOfKg = (numOfKg != null) ? numOfKg.doubleValue() : null;
		this.disCount = (disCount != null) ? disCount.doubleValue() : null;

		// v2.6.4.b
		stateHist = new StateHistory<>();

//    watermelons = new ArrayList<>();
//    
//    waterCount=0;

		updateTotalPrice();
	}

	// setter methods
	public void setWatermelon(Watermelon s) {
		this.watermelon = s;
	}

	public void setCustomer(Customer m) {
		this.customer = m;
	}

	public void setOrderDate(String orderDate) throws ConstraintViolationException {
		if (!orderDate.matches("\\d{2}/\\d{2}/\\d{4}")) {
			throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE,
					new Object[] { "'" + orderDate + "'(the date should follow the format 'dd/mm/yyyy')" });
		} else {
			this.orderDate = orderDate;
		}
	}

	public void setNumOfKg(Double numOfKg) {
		// update final grade = false: to keep the integrity of its cached value
		setNumOfKg(numOfKg, false);
	}

	public void setNumOfKg(Double numOfKg, boolean updateTotalPrice) {
		this.numOfKg = numOfKg;
		if (updateTotalPrice)
			updateTotalPrice();
	}

	public void setDisCount(Double disCount) {
		// update final grade = false: to keep the integrity of its cached value
		setDisCount(disCount, false);
	}

	public void setDisCount(Double disCount, boolean updateTotalPrice) {
		this.disCount = disCount;
		if (updateTotalPrice)
			updateTotalPrice();
	}

//  @DOpt(type=DOpt.Type.LinkAdder)
//  //only need to do this for reflexive association: @MemberRef(name="enrolments")
//  public boolean addWatermelon(Watermelon w) {
//    if (!watermelons.contains(w))
//      watermelons.add(w);
//    
//    // IMPORTANT: enrolment count must be updated separately by invoking setEnrolmentCount
//    // otherwise computeAverageMark (below) can not be performed correctly
//    // WHY? average mark is not serialisable
////    enrolmentCount++;
////    
////    // v2.6.4.b
////    computeAverageMark();
//    
//    // no other attributes changed
//    return false; 
//  }

//  @DOpt(type=DOpt.Type.LinkAdder)
//  public boolean addWatermelon(Collection<Watermelon> waters) {
//	  boolean added=false;
//	  for(Watermelon w:waters) {
//		  if(!watermelons.contains(w)) {
//			 if(!added) added=true;
//			 watermelons.add(w);
//		  }
//	  }
//	  return false;
//  }

//  @DOpt(type=DOpt.Type.LinkAdderNew)
//  public boolean addNewWatermelonOrder(Collection<Watermelon> waters) {
//	  watermelons.addAll(waters);
//	  waterCount+= waters.size();
//	  
//	  computePriceByKg();
//	  return false;
//  }

//  @DOpt(type=DOpt.Type.LinkAdderNew)
//  public boolean removeWatermelon(Watermelon w) {
//	  boolean removed= watermelons.remove(w);
//	  if(removed) {
//		  waterCount--;
//	  }
//	  computePriceByKg();
//	  
//	  return false;
//  }

//  public void setWatermelon(Collection<Watermelon> wa) {
//	  this.watermelons=wa;
//	  waterCount=wa.size();
//	  computePriceByKg();
//  }

//  private void computePriceByKg() {
//	  if(waterCount>0) {
//		  double price=0d;
//		  for(Watermelon w: watermelons) {
//			  price+= w.getPricePerKg();
//	  }
//	  sumPrice=price;
//	  }else {
//		  sumPrice=0;
//	  }
//  }
//  public double getSumPrice() {
//	  return sumPrice;
//  }

	@DOpt(type = DOpt.Type.DerivedAttributeUpdater)
	@AttrRef(value = AttributeName_TotalPrice)
	public void updateTotalPrice() {
		// updates both final mark and final grade

		if (numOfKg != null && disCount != null) {
			double finalPrice = watermelon.getPricePerKg() * numOfKg
					- (watermelon.getPricePerKg() * numOfKg) * disCount / 100;

			// v2.6.4b: cache final mark
			stateHist.put(AttributeName_TotalPrice, totalPrice);

			// round the mark to the closest integer value
			totalPrice = (int) Math.round(finalPrice);

			if (totalPrice > 2000)
				rateOrder = 'S';
			else if (totalPrice >= 1000)
				rateOrder = 'A';
			else if (totalPrice >= 500)
				rateOrder = 'B';
			else if (totalPrice >= 100)
				rateOrder = 'C';
			else
				rateOrder = 'D';
		}
	}

//  public Collection<Watermelon> getWatermelons(){
//	  return watermelons;
//  }
//  @DOpt(type=DOpt.Type.LinkCountGetter)
//  public Integer getWatermelonCount() {
//    return waterCount;
//    //return enrolments.size();
//  }
//  @DOpt(type=DOpt.Type.LinkCountSetter)
//  public void setWatermelonCount(int count) {
//    waterCount = count;
//  }
//  
	// getter methods
	public int getId() {
		return id;
	}

	public Watermelon getWatermelon() {
		return watermelon;
	}

	public Customer getCustomer() {
		return customer;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public Double getNumOfKg() {
		return numOfKg;
	}

	public Double getDisCount() {
		return disCount;
	}

	public char getRateOrder() {
		return rateOrder;
	}

	// v2.6.4.b
	public int getTotalPrice() {
		return getTotalPrice(false);// finalMark;
	}

	public int getTotalPrice(boolean cached) throws IllegalStateException {
		if (cached) {
			Object val = stateHist.get(AttributeName_TotalPrice);

			if (val == null)
				throw new IllegalStateException("WatermelonOrder.getTotalPrice: cached value is null");

			return (Integer) val;
		} else {
			if (totalPrice != null)
				return totalPrice;
			else
				return 0;
		}

	}

	public OrderByRateReport getRptOrderByRate() {
		return rptOrderByRate;
	}

	// override toString
	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean full) {
		if (full)
			return "Enrolment(" + watermelon + "," + customer + ")";
		else
			return "Enrolment(" + getId() + "," + watermelon.getId() + "," + customer.getName() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WatermelonOrder other = (WatermelonOrder) obj;
		if (id != other.id)
			return false;
		return true;
	}

	private static int nextID(Integer currID) {
		if (currID == null) { // generate one
			idCounter++;
			return idCounter;
		} else { // update
			int num;
			num = currID.intValue();

			// if (num <= idCounter) {
			// throw new
			// ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE,
			// "Lỗi giá trị thuộc tính ID: {0}", num + "<=" + idCounter);
			// }

			if (num > idCounter) {
				idCounter = num;
			}
			return currID;
		}
	}

	/**
	 * @requires minVal != null /\ maxVal != null
	 * @effects update the auto-generated value of attribute <tt>attrib</tt>,
	 *          specified for <tt>derivingValue</tt>, using <tt>minVal, maxVal</tt>
	 */
	@DOpt(type = DOpt.Type.AutoAttributeValueSynchroniser)
	public static void updateAutoGeneratedValue(DAttr attrib, Tuple derivingValue, Object minVal, Object maxVal)
			throws ConstraintViolationException {
		if (minVal != null && maxVal != null) {
			// check the right attribute
			if (attrib.name().equals("id")) {
				int maxIdVal = (Integer) maxVal;
				if (maxIdVal > idCounter)
					idCounter = maxIdVal;
			}
			// TODO add support for other attributes here
		}
	}

	// private static int nextID(Integer currID) {
	// if (currID == null) { // generate one
	// idCounter++;
	// return idCounter;
	// } else { // update
	// // int num = currID.intValue();
	// //
	// // if (num > idCounter)
	// // idCounter=num;
	// setIdCounter(currID);
	//
	// return currID;
	// }
	// }
	//
	// /**
	// * This method is required for loading this class metadata from storage
	// *
	// * @requires
	// * id != null
	// * @effects
	// * update <tt>idCounter</tt> from the value of <tt>id</tt>
	// */
	// public static void setIdCounter(Integer id) {
	// if (id != null) {
	// int num = id.intValue();
	//
	// if (num > idCounter)
	// idCounter=num;
	// }
	// }

	// implements Comparable interface
	public int compareTo(Object o) {
		if (o == null || (!(o instanceof WatermelonOrder)))
			return -1;

		WatermelonOrder e = (WatermelonOrder) o;

		return this.watermelon.getId().compareTo(e.watermelon.getId());
	}
}
