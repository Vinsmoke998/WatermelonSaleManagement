package vn.com.courseman.model;

import java.util.LinkedHashMap;
import java.util.Map;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.util.Tuple;
import vn.com.courseman.model.reports.EmployeesByNameReport;
import vn.com.courseman.model.reports.EmployeesByPositionReport;

/**
 * Represents a course module. The module id is auto-incremented from a base
 * calculated by "M" + semester-value * 100.
 * 
 * @author dmle
 * @version 2.0
 */
@DClass(schema = "watermelon")
public abstract class Employee {

	public final static String A_id = "id";
	public final static String A_position = "position";
	public final static String A_name = "name";
	public final static String A_warehouse = "warehouse";
	public final static String A_phone = "phone";
	public final static String A_address = "address";
	public final static String A_dob = "dob";
	public final static String A_rptEmployeesByName = "rptEmployeesByName";
	public final static String A_rptEmployeesByPosition = "rptEmployeesByPosition";

	// attributes

	private static int idCounter;
	@DAttr(name = A_id, id = true, auto = true, type = Type.Integer, length = 3, mutable = false, optional = false)
	private int id;
	@DAttr(name = A_position, type = Type.String,auto= true, length = 10, mutable = false, optional = true)
	private String position;
	@DAttr(name = A_name, type = Type.String, length = 30, optional = false)
	private String name;

	@DAttr(name = A_phone, type = Type.String, length = 11, optional = false, min = 1)
	private String phone;
	@DAttr(name = A_address, type = Type.String, length = 30, optional = false, min = 1)
	private String address;
	@DAttr(name = A_dob, type = Type.String, length = 10, optional = false, min = 1)
	private String dob;
	@DAttr(name = A_rptEmployeesByName, type = Type.Domain, serialisable = false,
			// IMPORTANT: set virtual=true to exclude this attribute from the object state
			// (avoiding the view having to load this attribute's value from data source)
			virtual = true)
	private EmployeesByNameReport rptEmployeesByName;

	@DAttr(name = A_rptEmployeesByPosition, type = Type.Domain, serialisable = false,
			// IMPORTANT: set virtual=true to exclude this attribute from the object state
			// (avoiding the view having to load this attribute's value from data source)
			virtual = true)
	private EmployeesByPositionReport rptEmployeesByPosition;

	// static variable to keep track of module code
	private static Map<Tuple, Integer> currNums = new LinkedHashMap<Tuple, Integer>();

	// constructor method: create objects from data source
	@DOpt(type = DOpt.Type.DataSourceConstructor)
	protected Employee(Integer id, String position, String name, String phone, String address, String dob)
			throws ConstraintViolationException {
		this.id = nextID(id);
		// automatically generate a code

		// assign other values
		this.position = position;
		this.name = name;
	
	
		if(validatePhone(phone)) {		
		this.phone = phone;
		}
		
		
		
		this.address = address;
		if (!dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
			throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE,
					new Object[] { "'" + dob + "'(the date should follow the format 'dd/mm/yyyy')" });
		} else {
			this.dob = dob;
		}
	}

//  @DOpt(type=DOpt.Type.ObjectFormConstructor)
//  protected CourseModule(@AttrRef("name") String name, 
//      @AttrRef("semester") int semester, @AttrRef("credits") int credits) {
//    this(null, null, name, semester, credits);
//  }

	// overloading constructor to support object type values
	// @version 2.0
	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	protected Employee( @AttrRef("name") String name,
			@AttrRef("phone") String phone, @AttrRef("address") String address, @AttrRef("dob") String dob) {
		this(null, null, name, phone, address, dob);
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

	public int getId() {
		return id;
	}

	// setter methods
	public void setName(String name) {
		this.name = name;
	}

	public void setPhone(String phone) {
		if(validatePhone(phone)) {		
			this.phone = phone;
			}			
			
		}
		
	
	
	private boolean validatePhone(String phone) throws ConstraintViolationException {
		if (!phone.matches("^[0-9]*$")) {
			throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE,
					new Object[] { phone + ": Phone is invalid!" });
		} else {
			return true;
		}
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setDob(String dob) throws ConstraintViolationException {
		if (!dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
			throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE,
					new Object[] { "'" + dob + "'(the date should follow the format 'dd/mm/yyyy')" });
		} else {
			this.dob = dob;
		}
	}

	// getter methods

	public String getPosition() {
		return position;
	}

	public String getName() {
		return name;
	}

	public String getPhone() {
		return phone;
	}

	public String getAddress() {
		return address;
	}

	public String getDob() {
		return dob;
	}

	public EmployeesByNameReport getRptEmployeesByName() {
		return rptEmployeesByName;
	}

	public EmployeesByPositionReport getRptEmployeesByPosition() {
		return rptEmployeesByPosition;
	}

	// override toString
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + getName() + ")";
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
			if (attrib.name().equals("id")) {
				int maxIdVal = (Integer) maxVal;
				if (maxIdVal > idCounter)
					idCounter = maxIdVal;

			}
//      else if (attrib.name().equals("code")) {
//        String maxCode = (String) maxVal;
//        
//        try {
//          int maxCodeNum = Integer.parseInt(maxCode.substring(1));
//          
//          // current max num for the semester
//          Integer currNum = currNums.get(derivingValue);
//          
//          if (currNum == null || maxCodeNum > currNum) {
//            currNums.put(derivingValue, maxCodeNum);
//          }
//          
//        } catch (RuntimeException e) {
//          throw new ConstraintViolationException(
//              ConstraintViolationException.Code.INVALID_VALUE, e, new Object[] {maxCode});
//        }
//      }
		}
	}
}
