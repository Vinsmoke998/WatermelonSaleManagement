package vn.com.courseman.model;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;

@DClass(schema="courseman")
public class DomesticCustomer extends Customer {
	@DOpt(type=DOpt.Type.ObjectFormConstructor)
	public DomesticCustomer(@AttrRef("name") String name,
					   @AttrRef("address") String address,
					   @AttrRef("phone") String phone,
					   @AttrRef("type") TypeOfCustomer type,
					   @AttrRef("email") String email,
					   @AttrRef("note") String note) {
	this(null, name, address,phone,type,email,note);	
	}
	@DOpt(type=DOpt.Type.DataSourceConstructor)
	public DomesticCustomer(String id, String name, 
					   String address, String phone,
					   TypeOfCustomer type, String email, String note ) throws ConstraintViolationException {
		super(id, name, address, phone, type, email, note);
	}
}
