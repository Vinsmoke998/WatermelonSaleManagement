package vn.com.courseman.model;

import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;

public class ForeignCustomer extends Customer {
	@DAttr(name="country", type = Type.String, length=15, optional= false)
	private String country;
	
	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	public ForeignCustomer(
					   @AttrRef("name") String name,
					   @AttrRef("address") String address,
					   @AttrRef("phone") String phone,
					   @AttrRef("type") TypeOfCustomer type,
					   @AttrRef("email") String email,
					   @AttrRef("note") String note,
					   @AttrRef("country") String country) {
		this(null,name, address, phone, type, email,note,country);
		
	}
	@DOpt(type = DOpt.Type.DataSourceConstructor)
	public ForeignCustomer(String id,String name, String address, String phone, TypeOfCustomer type, String email, String note, String country) {
		super(id, name, address, phone, type, email, note);
		this.country=country;
	}
	public void setCountry(String country) {
		this.country=country;
	}
	public String getCountry() {
		return country;
	}
	
}
