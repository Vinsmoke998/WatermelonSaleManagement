package vn.com.courseman.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import domainapp.basics.util.Tuple;
import vn.com.courseman.model.reports.CustomerByNameReport;
import vn.com.courseman.model.reports.CustomerByTypeReport;

/**
 * Represents a student. The student ID is auto-incremented from the current
 * year.
 * 
 * @author dmle
 * @version 2.0
 */
@DClass(schema = "courseman")
public class Customer implements Serializable {
	public static final String A_name = "name";
	public static final String A_id = "id";
	public static final String A_address = "address";
	public static final String A_phone = "phone";
	public static final String A_type = "type";
	public static final String A_email = "email";
	public static final String A_note = "note";
	public static final String A_rptCustomerByName = "rptCustomerByName";
	public static final String A_rptCustomerByType = "rptCustomerByType";
	// public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
	// Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
	// Pattern.CASE_INSENSITIVE);

	// attributes of students
	@DAttr(name = A_id, id = true, type = Type.String, auto = true, length = 10, mutable = false, optional = false)
	private String id;
	// static variable to keep track of student id
	private static int idCounter = 0;
	private static String y;

	@DAttr(name = A_name, type = Type.String, length = 30, optional = false)
	private String name;

	@DAttr(name = A_address, type = Type.String, length = 25, optional = false)
	private String address;

	@DAttr(name = A_phone, type = Type.String, length = 12, optional = false)
	private String phone;

	@DAttr(name = A_type, type = Type.Domain, length = 10, optional = true)
	@DAssoc(ascName = "customer-has-type", role = "customer", ascType = AssocType.One2Many, endType = AssocEndType.One, associate = @Associate(type = TypeOfCustomer.class, cardMin = 1, cardMax = MetaConstants.CARD_MORE))
	private TypeOfCustomer type;

	@DAttr(name = A_email, type = Type.String, length = 25, optional = false)
	private String email;

	@DAttr(name = A_note, type = Type.String, length = 50, optional = true)
	private String note;

//	@DAttr(name = "orders", type = Type.Collection, optional = false, serialisable = false, filter = @Select(clazz = Order.class))
//	@DAssoc(ascName = "student-has-enrolments", role = "student", ascType = AssocType.One2Many, endType = AssocEndType.One, associate = @Associate(type = Order.class, cardMin = 0, cardMax = 30))
//	private Collection<Order> orders;

	// derived
	private int orderCount;

	// v2.6.4b: derived: average of the final mark of all enrolments

	// v5.0: to realise link to report
	@DAttr(name = A_rptCustomerByName, type = Type.Domain, serialisable = false,
			// IMPORTANT: set virtual=true to exclude this attribute from the object state
			// (avoiding the view having to load this attribute's value from data source)
			virtual = true)
	private CustomerByNameReport rptCustomerByName;

	@DAttr(name = A_rptCustomerByType, type = Type.Domain, serialisable = false,
			// IMPORTANT: set virtual=true to exclude this attribute from the object state
			// (avoiding the view having to load this attribute's value from data source)
			virtual = true)
	private CustomerByTypeReport rptCustomerByType;

	// constructor methods
	// for creating in the application
	// without SClass
	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	@DOpt(type = DOpt.Type.RequiredConstructor)
	public Customer(@AttrRef("name") String name, @AttrRef("address") String address, @AttrRef("phone") String phone,
			@AttrRef("type") TypeOfCustomer type, @AttrRef("email") String email, @AttrRef("note") String note) {
		this(null, name, address, phone, type, email, note);
	}

	// a shared constructor that is invoked by other constructors
	@DOpt(type = DOpt.Type.DataSourceConstructor)
	public Customer(@AttrRef("id") String id, @AttrRef("name") String name, @AttrRef("address") String address,
			@AttrRef("phone") String phone, @AttrRef("type") TypeOfCustomer type, @AttrRef("email") String email,
			@AttrRef("note") String note) throws ConstraintViolationException {
		// generate an id
		this.id = nextID(id);

		// assign other values
		this.name = name;
		this.address = address;
		if(validatePhone(phone)) {
			this.phone = phone;
			}
		this.type = type;
		if (email.indexOf("@") < 0) {
			throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE,
					new Object[] { "'" + email + "'(Does not have '@')" });
		} else {
		this.email = email;
	}
		this.note = note;

//    orders = new ArrayList<>();
		orderCount = 0;

	}

	// setter methods
	public void setName(String name) {
		this.name = name;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setPhone(String phone) {
		if(validatePhone(phone)) {
		this.phone = phone;
		}
	}

	public void setType(TypeOfCustomer type) {
		this.type = type;
	}

	// v2.7.3
	public void setNewType(TypeOfCustomer type) {
		// change this invocation if need to perform other tasks (e.g. updating value of
		// a derived attribtes)
		setType(type);
	}

	public void setEmail(String email) throws ConstraintViolationException {
		if (email.indexOf("@") < 0) {
			throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE,
					new Object[] { "'" + email + "'(Does not have '@')" });
		} else {
		this.email = email;
	}}

	public void setNote(String note) {
		this.note = note;
	}

	// getter methods
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String getPhone() {
		return phone;
	}

	public TypeOfCustomer getType() {
		return type;
	}

	public String getEmail() {
		return email;
	}

	public String getNote() {
		return note;
	}

//	public Collection<Order> getOrders() {
//		return orders;
//	}

	@DOpt(type = DOpt.Type.LinkCountGetter)
	public Integer getOrderCount() {
		return orderCount;
		// return enrolments.size();
	}

	@DOpt(type = DOpt.Type.LinkCountSetter)
	public void setOrderCount(int count) {
		orderCount = count;
	}
	private boolean validatePhone(String phone) throws ConstraintViolationException {
		if (!phone.matches("^[0-9]*$")) {
			throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE,
					new Object[] { phone + ": Phone is invalid!" });
		} else {
			return true;
		}
	}

	/**
	 * @effects return rptStudentByName
	 */
	public CustomerByNameReport getRptCustomerByName() {
		return rptCustomerByName;
	}

	public CustomerByTypeReport getRptCustomerByType() {
		return rptCustomerByType;
	}

//	public static boolean validateEmail(String email) {
//		Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
//		return matcher.find();
//	}

	@Override
	public String toString() {
		return "Customer [id=" + id + ", name=" + name + ", address=" + address + ", phone=" + phone + ", type=" + type
				+ ", email=" + email + ", note=" + note + "]";
	}

	/**
	 * @effects returns <code>Student(id,name,dob,address,email)</code>.
	 */
//	public String toString(boolean full) {
//		if (full)
//			return "Customer(" + id + "," + name + "," + address + "," +  "," +typeOfCustomer+","+ email
//				 + ")";
//		else
//			return "Customer(" + id + ")";
//	}

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
		Customer other = (Customer) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	// automatically generate the next student id
	private String nextID(String id) throws ConstraintViolationException {
		if (id == null) { // generate a new id
			if (idCounter == 0) {
				idCounter = Calendar.getInstance().get(Calendar.YEAR);
				int x = idCounter - 2000;
				y = String.valueOf(x);
			} else {
				idCounter++;
			}
			return "C" + idCounter;
		} else {
			// update id
			int num;
			try {
				num = Integer.parseInt(id.substring(1));
			} catch (RuntimeException e) {
				throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE, e,
						new Object[] { id });
			}

			if (num > idCounter) {
				idCounter = num;
			}

			return id;
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
			// TODO: update this for the correct attribute if there are more than one auto
			// attributes of this class

			String maxId = (String) maxVal;

			try {
				int maxIdNum = Integer.parseInt(maxId.substring(1));

				if (maxIdNum > idCounter) // extra check
					idCounter = maxIdNum;

			} catch (RuntimeException e) {
				throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE, e,
						new Object[] { maxId });
			}
		}
	}
}
