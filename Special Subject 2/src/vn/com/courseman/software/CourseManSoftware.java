package vn.com.courseman.software;

import domainapp.basics.exceptions.NotPossibleException;
import domainapp.basics.software.DomainAppToolSoftware;
import vn.com.courseman.model.Origin;
import vn.com.courseman.model.WType;
import vn.com.courseman.model.Warehouse;
import vn.com.courseman.model.Watermelon;
import vn.com.courseman.model.WatermelonOrder;
import vn.com.courseman.model.TypeOfCustomer;
import vn.com.courseman.model.DomesticCustomer;
import vn.com.courseman.model.Employee;
import vn.com.courseman.model.ForeignCustomer;

import vn.com.courseman.model.Deliverer;
import vn.com.courseman.model.Preserver;
import vn.com.courseman.model.Seller;
import vn.com.courseman.model.Store;
import vn.com.courseman.model.reports.CustomerByNameReport;
import vn.com.courseman.model.reports.EmployeesByNameReport;
import vn.com.courseman.model.reports.EmployeesByPositionReport;
import vn.com.courseman.model.reports.WatermelonByOriginReport;
import vn.com.courseman.model.reports.WatermelonsByPriceReport;


/**
 * @overview 
 *  Encapsulate the basic functions for setting up and running a software given its domain model.  
 *  
 * @author dmle
 *
 * @version 
 */
public class CourseManSoftware extends DomainAppToolSoftware {
  
  // the domain model of software
  private static final Class[] model = {
      WatermelonOrder.class,
      TypeOfCustomer.class,
      ForeignCustomer.class,
      DomesticCustomer.class,
      Watermelon.class, 
      Origin.class, 
      WType.class,
      Warehouse.class,
      Employee.class,
      Store.class,
      Seller.class,
      Preserver.class,
      Deliverer.class,
      // reports
      WatermelonsByPriceReport.class,
      WatermelonByOriginReport.class,
      CustomerByNameReport.class,
      EmployeesByPositionReport.class,
      EmployeesByNameReport.class
  };
  
  /* (non-Javadoc)
   * @see vn.com.courseman.software.Software#getModel()
   */
  /**
   * @effects 
   *  return {@link #model}.
   */
  @Override
  protected Class[] getModel() {
    return model;
  }

  /**
   * The main method
   * @effects 
   *  run software with a command specified in args[0] and with the model 
   *  specified by {@link #getModel()}. 
   *  
   *  <br>Throws NotPossibleException if failed for some reasons.
   */
  public static void main(String[] args) throws NotPossibleException {
    new CourseManSoftware().exec(args);
  }
}
