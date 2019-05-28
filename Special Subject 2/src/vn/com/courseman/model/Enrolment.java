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


/**
 * Represents an enrolment
 * 
 * @author dmle
 * 
 */
@DClass(schema = "watermelon")
public class Enrolment implements Comparable {

	  private static final String AttributeName_OrderDate = "orderDate";
	  private static final String AttributeName_numOfKg = "numOfKg";
	  private static final String AttributeName_TotalPrice = "totalPrice";
	  private static final String AttributeName_DisCount = "disCount";
  
  // attributes
  @DAttr(name = "id", id = true, auto = true, type = Type.Integer, length = 5, optional = false, mutable = false)
  private int id;
  private static int idCounter = 0;

  @DAttr(name = "watermelon", type = Type.Domain, length = 5, optional = false)
  @DAssoc(ascName = "watermelon-has-enrolments", role = "enrolment", 
    ascType = AssocType.One2Many, endType = AssocEndType.Many, 
    associate = @Associate(type = Watermelon.class, cardMin = 1, cardMax = 1), dependsOn = true)
  private Watermelon watermelon;

  @DAttr(name = "customer", type = Type.Domain, length = 5, optional = false)
  @DAssoc(ascName = "customer-has-enrolments", role = "enrolment", 
    ascType = AssocType.One2Many, endType = AssocEndType.Many, 
    associate = @Associate(type = Customer.class, cardMin = 1, cardMax = 1), dependsOn = true)
  private Customer customer;
  
  @DAttr(name = AttributeName_OrderDate, type = Type.String, length = 15, optional = true, min = 0.0)
  private String orderDate;
  
  @DAttr(name = AttributeName_numOfKg, type = Type.Double, length = 8, optional = true, min = 0.0)
  private Double numOfKg;
  
  @DAttr(name = AttributeName_DisCount, type = Type.Double, length = 4, optional = true, min = 0.0)
  private Double disCount;

  @DAttr(name="rateOrder",auto = true, type = Type.Char, length = 1,mutable = false, optional = true 
      /* Note: no need to do this:
       derivedFrom={"internalMark,examMark"}
       * because finalGrade and finalMark are updated by the same method and this is already specified by finalMark (below)
       */
  )
  private char rateOrder;

  // v2.6.4.b derived from two attributes
  @DAttr(name = AttributeName_TotalPrice,type=Type.Integer,auto=true,mutable = false,optional = true,
      serialisable=false,
      derivedFrom={AttributeName_numOfKg, AttributeName_DisCount})
  private Integer totalPrice;

  // v2.6.4.b
  private StateHistory<String, Object> stateHist;
  /*
   * aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
   */
  @DAssoc(ascName="enrolment-has-watermelon", role="enrolment",
		  ascType=AssocType.One2One,endType=AssocEndType.One,
		  associate=@Associate(type=Watermelon.class,cardMin=1,cardMax=1))
  private Collection<Watermelon> watermelons;
  private int waterCount;
  private double sumPrice;

  // constructor method
  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  @DOpt(type=DOpt.Type.RequiredConstructor)
  public Enrolment(@AttrRef("watermelon") Watermelon s, 
      @AttrRef("customer") Customer m) throws ConstraintViolationException {
    this(null, s, m,null, 0.0, 0.0, null);
  }

  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  public Enrolment(@AttrRef("watermelon") Watermelon s, 
      @AttrRef("customer") Customer m, 
      @AttrRef("orderDate") String orderDate,
      @AttrRef("numOfKg") Double numOfKg, 
      @AttrRef("disCount") Double disCount)
      throws ConstraintViolationException {
    this(null, s, m, orderDate,numOfKg, disCount, null);
  }

  // @version 2.0
  @DOpt(type=DOpt.Type.DataSourceConstructor)
  public Enrolment(Integer id, Watermelon s, Customer m,String orderDate, Double numDouble,
      Double disDouble, 
      // v2.7.3: not used but needed to load data from source
      Character rateOrder) throws ConstraintViolationException {
    this.id = nextID(id);
    this.watermelon = s;
    this.customer = m;
    this.orderDate=orderDate;
    this.numOfKg=numOfKg;
    //this.numOfKg = (numOfKg != null) ? numOfKg.doubleValue()
   //     : null;
    this.disCount=disCount;
    //this.disCount = (disCount != null) ? disCount.doubleValue() : null;

    // v2.6.4.b
    stateHist = new StateHistory<>();
    
    watermelons = new ArrayList<>();
    
    waterCount=0;
    
    updateTotalPrice(); 
  }

  // setter methods
  public void setWatermelon(Watermelon s) {
    this.watermelon = s;
  }

  public void setCustomer(Customer m) {
    this.customer = m;
  }
  public void setOrderDate(String orderDate) {
	  this.orderDate=orderDate;
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
  
  @DOpt(type=DOpt.Type.LinkAdder)
  //only need to do this for reflexive association: @MemberRef(name="enrolments")
  public boolean addWatermelon(Watermelon w) {
    if (!watermelons.contains(w))
      watermelons.add(w);
    
    // IMPORTANT: enrolment count must be updated separately by invoking setEnrolmentCount
    // otherwise computeAverageMark (below) can not be performed correctly
    // WHY? average mark is not serialisable
//    enrolmentCount++;
//    
//    // v2.6.4.b
//    computeAverageMark();
    
    // no other attributes changed
    return false; 
  }
  
  @DOpt(type=DOpt.Type.LinkAdder)
  public boolean addWatermelon(Collection<Watermelon> waters) {
	  boolean added=false;
	  for(Watermelon w:waters) {
		  if(!watermelons.contains(w)) {
			 if(!added) added=true;
			 watermelons.add(w);
		  }
	  }
	  return false;
  }
  
  @DOpt(type=DOpt.Type.LinkAdderNew)
  public boolean addNewEnrolment(Collection<Watermelon> waters) {
	  watermelons.addAll(waters);
	  waterCount+= waters.size();
	  
	  computePriceByKg();
	  return false;
  }
  
  @DOpt(type=DOpt.Type.LinkAdderNew)
  public boolean removeWatermelon(Watermelon w) {
	  boolean removed= watermelons.remove(w);
	  if(removed) {
		  waterCount--;
	  }
	  computePriceByKg();
	  
	  return false;
  }
  
  public void setWatermelon(Collection<Watermelon> wa) {
	  this.watermelons=wa;
	  waterCount=wa.size();
	  computePriceByKg();
  }
  
  private void computePriceByKg() {
	  if(waterCount>0) {
		  double price=0d;
		  for(Watermelon w: watermelons) {
			  price+= w.getPricePerKg();
	  }
	  sumPrice=price;
	  }else {
		  sumPrice=0;
	  }
  }
  public double getSumPrice() {
	  return sumPrice;
  }
  
  
  
  

  @DOpt(type=DOpt.Type.DerivedAttributeUpdater)
  @AttrRef(value=AttributeName_TotalPrice)
  public void updateTotalPrice() {
    // updates both final mark and final grade
	if(waterCount>0) {
		double price=0d;
		for(Watermelon w: watermelons) {
			  price+= w.getPricePerKg();
		}
		if (numOfKg != null && disCount != null) {
		double finalPrice = price* numOfKg - (price*numOfKg)*disCount/100;
      
      // v2.6.4b: cache final mark
      stateHist.put(AttributeName_TotalPrice, totalPrice);

      // round the mark to the closest integer value
      totalPrice = (int) Math.round(finalPrice);

      if (totalPrice >2000)
        rateOrder = 'S';
      else if (totalPrice >= 1000)
        rateOrder = 'A';
      else if (totalPrice >=500 )
        rateOrder = 'B';
      else if(totalPrice>=100)
        rateOrder = 'C';
      else
    	  rateOrder='D';
    }
	}
  }
  
  public Collection<Watermelon> getWatermelons(){
	  return watermelons;
  }
  @DOpt(type=DOpt.Type.LinkCountGetter)
  public Integer getWatermelonCount() {
    return waterCount;
    //return enrolments.size();
  }
  @DOpt(type=DOpt.Type.LinkCountSetter)
  public void setWatermelonCount(int count) {
    waterCount = count;
  }
  
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
        throw new IllegalStateException(
            "Enrolment.getTotalPrice: cached value is null");

      return (Integer) val;
    } else {
      if (totalPrice != null)
        return totalPrice;
      else
        return 0;
    }

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
      return "Enrolment(" + getId() + "," + watermelon.getId() + ","
          + customer.getName() + ")";
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
    Enrolment other = (Enrolment) obj;
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
   *          specified for <tt>derivingValue</tt>, using
   *          <tt>minVal, maxVal</tt>
   */
  @DOpt(type = DOpt.Type.AutoAttributeValueSynchroniser)
  public static void updateAutoGeneratedValue(DAttr attrib,
      Tuple derivingValue, Object minVal, Object maxVal)
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
    if (o == null || (!(o instanceof Enrolment)))
      return -1;

    Enrolment e = (Enrolment) o;

    return this.watermelon.getId().compareTo(e.watermelon.getId());
  }
}
