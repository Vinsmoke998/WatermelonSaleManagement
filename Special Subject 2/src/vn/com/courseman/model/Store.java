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
import domainapp.basics.model.meta.Select;
import domainapp.basics.util.Tuple;

/**
 * Represents a student class.
 * 
 * @author dmle
 *
 */
@DClass(schema="watermelon")
public class Store {
  @DAttr(name="id",id=true,auto=true,length=6,mutable=false,type=Type.Integer)
  private int id;
  private static int idCounter;
  
  @DAttr(name="name",length=20,type=Type.String,optional=false)
  private String name;
  
  @DAttr(name="sellers",type=Type.Collection,
      serialisable=false,optional=false,
      filter=@Select(clazz=Seller.class))
  @DAssoc(ascName="store-has-seller",role="store",
      ascType=AssocType.One2Many,endType=AssocEndType.One,
      associate=@Associate(type=Seller.class,
      cardMin=1,cardMax=25))  
  private Collection<Seller> sellers;
  
  // derived attributes
  private int sellersCount;
  
  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  @DOpt(type=DOpt.Type.RequiredConstructor)
  public Store(@AttrRef("name") String name) {
    this(null, name);
  }

  // constructor to create objects from data source
  @DOpt(type=DOpt.Type.DataSourceConstructor)
  public Store(@AttrRef("id") Integer id,@AttrRef("name") String name) {
    this.id = nextID(id);
    this.name = name;
    
    sellers = new ArrayList<>();
    sellersCount = 0;
  }

  @DOpt(type=DOpt.Type.Setter)
  public void setName(String name) {
    this.name = name;
  }

  @DOpt(type=DOpt.Type.LinkAdder)
  //only need to do this for reflexive association: @MemberRef(name="students")  
  public boolean addSeller(Seller s) {
    if (!this.sellers.contains(s)) {
    	sellers.add(s);
    }
    
    // no other attributes changed
    return false; 
  }

  @DOpt(type=DOpt.Type.LinkAdderNew)
  public boolean addNewSeller(Seller s) {
	  sellers.add(s);
	  sellersCount++;
    
    // no other attributes changed
    return false; 
  }
  
  @DOpt(type=DOpt.Type.LinkAdder)
  public boolean addSeller(Collection<Seller> sellers) {
    for (Seller s : sellers) {
      if (!this.sellers.contains(s)) {
        this.sellers.add(s);
      }
    }
    
    // no other attributes changed
    return false; 
  }

  @DOpt(type=DOpt.Type.LinkAdderNew)
  public boolean addNewSeller(Collection<Seller> sellers) {
    this.sellers.addAll(sellers);
    sellersCount += sellers.size();

    // no other attributes changed
    return false; 
  }

  @DOpt(type=DOpt.Type.LinkRemover)
  //only need to do this for reflexive association: @MemberRef(name="students")
  public boolean removeSeller(Seller s) {
    boolean removed = sellers.remove(s);
    
    if (removed) {
    	sellersCount--;
    }
    
    // no other attributes changed
    return false; 
  }
  
  @DOpt(type=DOpt.Type.Setter)
  public void setSeller(Collection<Seller> sellers) {
    this.sellers = sellers;
    
    sellersCount = sellers.size();
  }
    
  /**
   * @effects 
   *  return <tt>studentsCount</tt>
   */
  @DOpt(type=DOpt.Type.LinkCountGetter)
  public Integer getSellersCount() {
    return sellersCount;
  }

  @DOpt(type=DOpt.Type.LinkCountSetter)
  public void setSellersCount(int count) {
	  sellersCount = count;
  }
  
  @DOpt(type=DOpt.Type.Getter)
  public String getName() {
    return name;
  }
  
  @DOpt(type=DOpt.Type.Getter)
  public Collection<Seller> getSellers() {
    return sellers;
  }
  
  @DOpt(type=DOpt.Type.Getter)
  public int getId() {
    return id;
  }
  
  @Override
  public String toString() {
    return "Seller("+getId()+","+getName()+")";
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
    Store other = (Store) obj;
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
