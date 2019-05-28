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
import domainapp.basics.model.meta.MetaConstants;
import domainapp.basics.model.meta.Select;
import domainapp.basics.util.Tuple;

/**
 * Represents a warehouse class.
 * 
 * @author thangnd
 *
 */
@DClass(schema="watermelon")
public class Warehouse {
  @DAttr(name="id",id=true,auto=true,length=6,mutable=false,type=Type.Integer)
  private int id;
  private static int idCounter;
  
  @DAttr(name="name",length=20,type=Type.String,optional=false)
  private String name;
  
  @DAttr(name="watermelons",type=Type.Collection,
      serialisable=false,optional=false,
      filter=@Select(clazz=Watermelon.class))
  @DAssoc(ascName="warehouse-has-watermelon",role="warehouse",
      ascType=AssocType.One2Many,endType=AssocEndType.One,
      associate=@Associate(type=Watermelon.class,
      cardMin=1,cardMax=MetaConstants.CARD_MORE, determinant = true))  
  private Collection<Watermelon> watermelons;
  
  // derived attributes
  private int watermelonsCount;
  
  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  @DOpt(type=DOpt.Type.RequiredConstructor)
  public Warehouse(@AttrRef("name") String name) {
    this(null, name);
  }

  // constructor to create objects from data source
  @DOpt(type=DOpt.Type.DataSourceConstructor)
  public Warehouse(@AttrRef("id") Integer id,@AttrRef("name") String name) {
    this.id = nextID(id);
    this.name = name;
    
    watermelons = new ArrayList<>();
    watermelonsCount = 0;
  }

  @DOpt(type=DOpt.Type.Setter)
  public void setName(String name) {
    this.name = name;
  }

  @DOpt(type=DOpt.Type.LinkAdder)
  //only need to do this for reflexive association: @MemberRef(name="watermelon")  
  public boolean addStudent(Watermelon s) {
    if (!this.watermelons.contains(s)) {
    	watermelons.add(s);
    }
    
    // no other attributes changed
    return false; 
  }

  @DOpt(type=DOpt.Type.LinkAdderNew)
  public boolean addNewStudent(Watermelon s) {
	  watermelons.add(s);
	  watermelonsCount++;
    
    // no other attributes changed
    return false; 
  }
  
  @DOpt(type=DOpt.Type.LinkAdder)
  public boolean addWatermelon(Collection<Watermelon> watermelons) {
    for (Watermelon s : watermelons) {
      if (!this.watermelons.contains(s)) {
        this.watermelons.add(s);
      }
    }
    
    // no other attributes changed
    return false; 
  }

  @DOpt(type=DOpt.Type.LinkAdderNew)
  public boolean addNewWatermelon(Collection<Watermelon> watermelons) {
    this.watermelons.addAll(watermelons);
    watermelonsCount += watermelons.size();

    // no other attributes changed
    return false; 
  }

  @DOpt(type=DOpt.Type.LinkRemover)
  //only need to do this for reflexive association: @MemberRef(name="watermelons")
  public boolean removeWatermelon(Watermelon s) {
    boolean removed = watermelons.remove(s);
    
    if (removed) {
    	watermelonsCount--;
    }
    
    // no other attributes changed
    return false; 
  }
  
  @DOpt(type=DOpt.Type.Setter)
  public void setWatermelons(Collection<Watermelon> watermelons) {
    this.watermelons = watermelons;
    
    watermelonsCount = watermelons.size();
  }
    
  /**
   * @effects 
   *  return <tt>studentsCount</tt>
   */
  @DOpt(type=DOpt.Type.LinkCountGetter)
  public Integer getWatermelonsCount() {
    return watermelonsCount;
  }

  @DOpt(type=DOpt.Type.LinkCountSetter)
  public void setWatermelonsCount(int count) {
	  watermelonsCount = count;
  }
  
  @DOpt(type=DOpt.Type.Getter)
  public String getName() {
    return name;
  }
  
  @DOpt(type=DOpt.Type.Getter)
  public Collection<Watermelon> getWatermelons() {
    return watermelons;
  }
  
  @DOpt(type=DOpt.Type.Getter)
  public int getId() {
    return id;
  }
  
  @Override
  public String toString() {
    return "Warehouse("+getId()+","+getName()+")";
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
    Warehouse other = (Warehouse) obj;
    if (id != other.id)
      return false;
    return true;
  }

  private static int nextID(Integer currID) {
    if (currID == null) {
      idCounter++;
      return idCounter;
    } else {
      int num = currID.intValue();
      if (num > idCounter)
        idCounter = num;
      
      return currID;
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
      if (attrib.name().equals("id")) {
        int maxIdVal = (Integer) maxVal;
        if (maxIdVal > idCounter)  
          idCounter = maxIdVal;
      }
    }
  }
}
