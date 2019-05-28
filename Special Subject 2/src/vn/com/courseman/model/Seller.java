package vn.com.courseman.model;

import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;

/**
 * Represents an elective module (a subclass of Module)
 * 
 * @author dmle
 *
 */
@DClass(schema = "watermelon")
public class Seller extends Employee {
	// extra attribute of elective module
	@DAttr(name = "store", type = Type.Domain, length = 6)
	@DAssoc(ascName = "store-has-watermelon", role = "watermelon", ascType = AssocType.One2Many, endType = AssocEndType.Many, associate = @Associate(type = Store.class, cardMin = 1, cardMax = 1))
	private Store store;

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
	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	public Seller(

			@AttrRef("name") String name, @AttrRef("phone") String phone,
			@AttrRef("address") String address, @AttrRef("dob") String dob, @AttrRef("store") Store store) {
		this(null, "Seller", name, phone, address, dob, store);
	}

	@DOpt(type = DOpt.Type.DataSourceConstructor)
	public Seller(Integer id, String position, String name, String phone, String address, String dob, Store store) {
		super(id, position, name, phone, address, dob);
		this.store = store;
	}

	// setter method
	public void setStore(Store store) {
		this.store = store;
	}

	// getter method
	public Store getStore() {
		return store;
	}
}
