package vn.com.courseman.model;

import java.util.ArrayList;
import java.util.Calendar;
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
import domainapp.basics.model.meta.Select;
import domainapp.basics.util.Tuple;
import vn.com.courseman.model.reports.WatermelonByOriginReport;
//import vn.com.courseman.model.reports.WatermelonsByNameReport;
import vn.com.courseman.model.reports.WatermelonsByPriceReport;

/**
 * Represents a watermeon. The watermelon ID is auto-incremented from the current
 * year.
 * 
 * @author dmle
 * @version 2.0
 */
@DClass(schema="courseman")
public class Watermelon {
  public static final String A_weightByKg = "weightByKg";
  public static final String A_id = "id";
  public static final String A_origin = "origin";
  public static final String A_pricePerKg = "pricePerKg";
  public static final String A_rptWatermelonByPrice = "rptWatermelonByPrice";
  public static final String A_rptWatermelonByOrigin = "rptWatermelonByOrigin";

  // attributes of watermelon
  private String x;
  @DAttr(name = A_id, id = true, type = Type.String, auto = true, length = 10, 
      mutable = false, optional = false)
  private String id;
  //static variable to keep track of watermelon id
  private static int idCounter = 0;
 
  @DAttr(name = A_weightByKg, type = Type.Double, length = 6, optional = false)
  private Double weightByKg;

  @DAttr(name = A_origin, type = Type.Domain, length = 20, optional = false)
  @DAssoc(ascName="watermelon-has-origin",role="watermelon",
      ascType=AssocType.One2Many, endType=AssocEndType.One,
  associate=@Associate(type=Origin.class,cardMin=1,cardMax=1))
  private Origin origin;

  @DAttr(name = A_pricePerKg, type = Type.Double, length = 6, optional = false)
  private Double pricePerKg;

  @DAttr(name="wtype",type=Type.Domain,length = 6,optional = false)
  @DAssoc(ascName="type-has-watermelon",role="watermelon",
      ascType=AssocType.One2Many,endType=AssocEndType.Many,
      associate=@Associate(type=WType.class,cardMin=1,cardMax=1))
  private WType wtype;
  @DAttr(name="warehouse",type=Type.Domain,length = 6,optional = false)
  @DAssoc(ascName="warehouse-has-watermelon",role="watermelon",
      ascType=AssocType.One2Many,endType=AssocEndType.Many,
      associate=@Associate(type=Warehouse.class,cardMin=1,cardMax=1))
  private Warehouse warehouse;

  @DAttr(name="orders",type=Type.Collection,optional = false,
      serialisable=false,filter=@Select(clazz=WatermelonOrder.class))
  @DAssoc(ascName="watermelon-has-orders",role="watermelon",
      ascType=AssocType.One2Many,endType=AssocEndType.Many,
    associate=@Associate(type=WatermelonOrder.class,cardMin=0,cardMax=50))
  private Collection<WatermelonOrder> orders;  

  // derived
  private int watermelonCount;

  // v2.6.4b: derived: average of the final mark of all enrolments
  private double averageOrderPrice;
  
  // v5.0: to realise link to report
  @DAttr(name=A_rptWatermelonByOrigin,type=Type.Domain, serialisable=false, 
      // IMPORTANT: set virtual=true to exclude this attribute from the object state
      // (avoiding the view having to load this attribute's value from data source)
      virtual=true)
  private WatermelonByOriginReport rptWatermelonByOrigin;
  
//v5.0: to realise link to report
 @DAttr(name=A_rptWatermelonByPrice,type=Type.Domain, serialisable=false, 
     // IMPORTANT: set virtual=true to exclude this attribute from the object state
     // (avoiding the view having to load this attribute's value from data source)
     virtual=true)
 private WatermelonsByPriceReport rptWatermelonByPrice;
  
  // constructor methods
  // for creating in the application
  // without SClass
  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  @DOpt(type=DOpt.Type.RequiredConstructor)
  public Watermelon(@AttrRef("weightByKg") Double weightByKg, 
      @AttrRef("origin") Origin origin, 
      @AttrRef("pricePerKg") Double pricePerKg) {
    this(null, weightByKg,origin, pricePerKg, null,null);
  }
  
  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  public Watermelon(@AttrRef("weightByKg") Double weightByKg,  
      @AttrRef("origin") Origin origin, 
      @AttrRef("pricePerKg") Double pricePerKg, 
      @AttrRef("wtype") WType wtype){
    this(null, weightByKg,origin, pricePerKg, wtype,null);
  }
  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  public Watermelon(@AttrRef("weightByKg") Double weightByKg,  
      @AttrRef("origin") Origin origin, 
      @AttrRef("pricePerKg") Double pricePerKg, 
      @AttrRef("wtype") WType wtype,
  	  @AttrRef("warehouse") Warehouse warehouse){
    this(null, weightByKg,origin, pricePerKg, wtype,warehouse);
  }
  
  // a shared constructor that is invoked by other constructors
  @DOpt(type=DOpt.Type.DataSourceConstructor)
  public Watermelon(@AttrRef("id") String id, 
      @AttrRef("weightByKg") Double weightByKg, @AttrRef("origin") Origin origin, 
      @AttrRef("pricePerKg") Double pricePerKg, @AttrRef("wtype") WType wtype,@AttrRef("warehouse") Warehouse warehouse) 
  throws ConstraintViolationException {
    // generate an id
    this.id = nextID(id);

    // assign other values
    this.weightByKg = weightByKg;
    this.origin = origin;
    this.pricePerKg = pricePerKg;
    this.wtype = wtype;
    this.warehouse=warehouse;
    
    orders = new ArrayList<>();
    watermelonCount = 0;
    averageOrderPrice = 0D;
  }

  // setter methods
  public void setWeightByKg(Double weightByKg) {
    this.weightByKg = weightByKg;
  }
  public void setOrigin(Origin origin) {
    this.origin = origin;
  }

  // v2.7.3
  public void setNewOrigin(Origin origin) {
    // change this invocation if need to perform other tasks (e.g. updating value of a derived attribtes)
    setOrigin(origin);
  }
  
  public void setPricePerKg(Double pricePerKg) {
    this.pricePerKg = pricePerKg;
  }

  public void setType(WType wtype) {
    this.wtype = wtype;
  }
  public void setWarehouse(Warehouse warehouse) {
	    this.warehouse = warehouse;
	  }
  
  @DOpt(type=DOpt.Type.LinkAdder)
  //only need to do this for reflexive association: @MemberRef(name="orders")
  public boolean addWatermelonOrder(WatermelonOrder e) {
    if (!orders.contains(e))
      orders.add(e);
    
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

  @DOpt(type=DOpt.Type.LinkAdderNew)
  public boolean addNewWatermelonOrder(WatermelonOrder e) {
    orders.add(e);
    
    watermelonCount++;
    
    // v2.6.4.b
    computeAverageOrderPrice();
    
    // no other attributes changed (average mark is not serialisable!!!)
    return false; 
  }
  
  @DOpt(type=DOpt.Type.LinkAdder)
  //@MemberRef(name="orders")
  public boolean addWatermelonOrder(Collection<WatermelonOrder> o) {
    boolean added = false;
    for (WatermelonOrder e : o) {
      if (!orders.contains(e)) {
        if (!added) added = true;
        orders.add(e);
      }
    }
    // IMPORTANT: enrolment count must be updated separately by invoking setEnrolmentCount
    // otherwise computeAverageMark (below) can not be performed correctly
    // WHY? average mark is not serialisable
//    enrolmentCount += enrols.size();

//    if (added) {
//      // avg mark is not serialisable so we need to compute it here
//      computeAverageMark();
//    }

    // no other attributes changed
    return false; 
  }
//
  @DOpt(type=DOpt.Type.LinkAdderNew)
  public boolean addNewWatermelonOrder(Collection<WatermelonOrder> o) {
    orders.addAll(o);
    watermelonCount+=o.size();
    
    // v2.6.4.b
    computeAverageOrderPrice();

    // no other attributes changed (average mark is not serialisable!!!)
    return false; 
  }
  
  @DOpt(type=DOpt.Type.LinkRemover)
  //@MemberRef(name="orders")
  public boolean removeWatermelonOrder(WatermelonOrder e) {
    boolean removed = orders.remove(e);
    
    if (removed) {
      watermelonCount--;
      
      // v2.6.4.b
      computeAverageOrderPrice();
    }
    // no other attributes changed
    return false; 
  }
//
  @DOpt(type=DOpt.Type.LinkUpdater)
  //@MemberRef(name="orders")
  public boolean updateWatermelonOrder(WatermelonOrder e)  throws IllegalStateException {
    // recompute using just the affected enrolment
    double totalPrice = averageOrderPrice * watermelonCount;
    
    int oldFinalMark = e.getTotalPrice(true);
    
    int diff = e.getTotalPrice() - oldFinalMark;
    
    // TODO: cache totalMark if needed 
    
    totalPrice += diff;
    
    averageOrderPrice = totalPrice / watermelonCount;
    
    // no other attributes changed
    return true; 
  }

  public void setWatermelonOrders(Collection<WatermelonOrder> en) {
    this.orders = en;
    watermelonCount = en.size();
    
    // v2.6.4.b
    computeAverageOrderPrice();
  }
//  
  // v2.6.4.b
  /**
   * @effects 
   *  computes {@link #averageMark} of all the {@link Enrolment#getFinalMark()}s 
   *  (in {@link #enrolments}.  
   */
  private void computeAverageOrderPrice() {
    if (watermelonCount > 0) {
      double totalPrice = 0d;
      for (WatermelonOrder e : orders) {
        totalPrice += e.getTotalPrice();
      }
      
      averageOrderPrice = totalPrice / watermelonCount;
    } else {
      averageOrderPrice = 0;
    }
  }
  
  // v2.6.4.b
  public double getAverageOrderPrice() {
    return averageOrderPrice;
  }
  
  // getter methods
  public String getId() {
    return id;
  }

  public Double getWeightByKg() {
    return weightByKg;
  }

  public Origin getOrigin() {
    return origin;
  }

  public Double getPricePerKg() {
    return pricePerKg;
  }

  public WType getWtype() {
    return wtype;
  }
  public Warehouse getWarehouse() {
	    return warehouse;
	  }
  
  public Collection<WatermelonOrder> getWatermelonOrders() {
    return orders;
  }

  @DOpt(type=DOpt.Type.LinkCountGetter)
  public Integer getWatermelonOrdersCount() {
    return watermelonCount;
    //return enrolments.size();
  }

  @DOpt(type=DOpt.Type.LinkCountSetter)
  public void setWatermelonOrdersCount(int count) {
    watermelonCount = count;
  }

  /**
   * @effects return rptWatermelonByPrice
   */
  public WatermelonsByPriceReport getRptWatermelonByPrice() {
    return rptWatermelonByPrice;
  }
  
  /**
   * @effects return rptWatermelonOrigin
   */
  public WatermelonByOriginReport getRptWatermelonByOrigin() {
    return rptWatermelonByOrigin;
  }

  // override toString
  /**
   * @effects returns <code>this.id</code>
   */
  @Override
  public String toString() {
    return toString(true);
  }

  /**
   * @effects returns <code>Watermelon(id,weightByKg,price,origin,type,warehouse)</code>.
   */
  public String toString(boolean full) {
    if (full)
      return "Watermelon(" + id + "," + weightByKg + "," + origin + ","
          + pricePerKg + ((wtype != null) ? "," + wtype.getName() : "")+ ((warehouse != null) ? "," + warehouse.getName() : "") + ")";
    else
      return "Watermelon(" + id + ")";
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    Watermelon other = (Watermelon) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  // automatically generate the next watermelon id
  private String nextID(String id) throws ConstraintViolationException {
    if (id == null) { // generate a new id
      if (idCounter == 0) {
        idCounter = Calendar.getInstance().get(Calendar.YEAR);
        
        x = String.valueOf(idCounter-2000);
        
      } else {
        idCounter++;
      }
      return "W" +idCounter;
    } else {
      // update id
      int num;
      try {
        num = Integer.parseInt(id.substring(1));
      } catch (RuntimeException e) {
        throw new ConstraintViolationException(
            ConstraintViolationException.Code.INVALID_VALUE, e, new Object[] { id });
      }
      
      if (num > idCounter) {
        idCounter = num;
      }
      
      return id;
    }
  }

  /**
   * @requires 
   *  minVal != null /\ maxVal != null
   * @effects 
   *  update the auto-generated value of attribute <tt>attrib</tt>, specified for <tt>derivingValue</tt>, using <tt>minVal, maxVal</tt>
   */
  @DOpt(type=DOpt.Type.AutoAttributeValueSynchroniser)
  public static void updateAutoGeneratedValue(
      DAttr attrib,
      Tuple derivingValue, 
      Object minVal, 
      Object maxVal) throws ConstraintViolationException {
    
    if (minVal != null && maxVal != null) {
      //TODO: update this for the correct attribute if there are more than one auto attributes of this class 

      String maxId = (String) maxVal;
      
      try {
        int maxIdNum = Integer.parseInt(maxId.substring(1));
        
        if (maxIdNum > idCounter) // extra check
          idCounter = maxIdNum;
        
      } catch (RuntimeException e) {
        throw new ConstraintViolationException(
            ConstraintViolationException.Code.INVALID_VALUE, e, new Object[] {maxId});
      }
    }
  }
}
