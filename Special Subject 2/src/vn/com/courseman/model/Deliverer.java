package vn.com.courseman.model;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;


/**
 * Represents an elective module (a subclass of Module)
 * @author dmle
 *
 */
@DClass(schema="watermelon")
public class Deliverer extends Employee {
  // extra attribute of elective module
  @DAttr(name="vehicle",type=Type.String,length=50,optional=false)
  private String vehicle;
  
  // constructor method
  // the order of the arguments must be this: 
  // - super-class arguments first, then sub-class
//  @DOpt(type=DOpt.Type.ObjectFormConstructor)
//  public ElectiveModule(@AttrRef("name") String name, 
//      @AttrRef("semester") int semester, @AttrRef("credits") int credits, 
//      @AttrRef("deptName") String deptName) {
//    this(null, null, name, semester, credits, deptName);
//  }
  
  // the order of the arguments must be this: 
  // - super-class arguments first, then sub-class
  @DOpt(type=DOpt.Type.ObjectFormConstructor)
  public Deliverer(
      @AttrRef("vehicle") String vehicle,
       @AttrRef("name") String name, 
      @AttrRef("phone") String phone,
     @AttrRef("address") String address,@AttrRef("dob") String dob
      ) {
    this(null, "Deliverer", name, phone, address,dob, vehicle);
  }
  
  @DOpt(type=DOpt.Type.DataSourceConstructor)
  public Deliverer(Integer id, String position, String name,
		  String phone, String address, String dob, String vehicle) {
    super(id, position, name,  phone, address, dob);
    this.vehicle = vehicle;
  }
  
  // setter method 
  public void setVihicle(String vehicle) {
    this.vehicle = vehicle;
    }
  
  // getter method
  public String getVehicle() {
    return vehicle;
  }
}
