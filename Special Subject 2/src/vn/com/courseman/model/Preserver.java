package vn.com.courseman.model;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr.Type;

/**
 * Represents a compulsory module (a subclass of Module)
 * 
 * @author dmle
 * 
 */
@DClass(schema="watermelon")
public class Preserver extends Employee {
	  @DAttr(name="warehouse",type=Type.Domain,length = 6)
	  @DAssoc(ascName="warehouse-has-preserver",role="preserver",
	      ascType=AssocType.One2Many,endType=AssocEndType.Many,
	      associate=@Associate(type=Warehouse.class,cardMin=1,cardMax=1))
	  private Warehouse warehouse;

  // constructor method
  // the order of the arguments must be this: 
  // - super-class arguments first, then sub-class
//  @DOpt(type=DOpt.Type.ObjectFormConstructor)
//  public CompulsoryModule(@AttrRef("name") String name, 
//      @AttrRef("semester") int semester, @AttrRef("credits") int credits) {
//    this(null, null, name, semester, credits);
//  }

  // the order of the arguments must be this: 
  // - super-class arguments first, then sub-class
  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  public Preserver( @AttrRef("name") String name, @AttrRef("phone") String phone,
      @AttrRef("address") String address,@AttrRef("dob") String dob, 
      @AttrRef("warehouse") Warehouse warehouse) {
    this(null,"Preserver", name,  phone, address, dob, warehouse);
  }

  @DOpt(type=DOpt.Type.DataSourceConstructor)
  public Preserver(Integer id, String position, String name,  
		  String phone, String address, String dob,Warehouse warehouse) 
    throws ConstraintViolationException {
    super(id, position, name, phone, address, dob);
    this.warehouse=warehouse;
  }
  public void setWarehouse(Warehouse warehouse) {
	    this.warehouse = warehouse;
	  }
	  
	  // getter method
	  public Warehouse getWarehouse() {
	    return warehouse;
	  }

}
