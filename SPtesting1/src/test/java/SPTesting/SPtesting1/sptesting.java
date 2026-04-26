package SPTesting.SPtesting1;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;



public class sptesting
{
	Connection con = null;
	Statement stmt=null;
	ResultSet rs;
	CallableStatement cstmt = null;
	ResultSet rs1;
	ResultSet rs2;
	
	@BeforeClass
	void setup() throws SQLException
	{
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/classicmodels","root","12345");
	}
	@AfterClass
	void tearDown() throws SQLException
	{
		con.close();
	}
	//@Test(priority=1)
	void test_StoreProcedure_Exists() throws SQLException
	{
		stmt=con.createStatement();
		rs = stmt.executeQuery("show procedure status where name = 'SelectALLCustomers'");
		rs.next();//read data moves to first row
		Assert.assertEquals(rs.getString("Name"),"SelectALLCustomers");
	}
	//@Test(priority=2)
	void selectAllCustomers() throws SQLException
	{
		cstmt = con.prepareCall("{CALL SelectALLCustomers()}");
		rs1 = cstmt.executeQuery();
		
		Statement stmt = con.createStatement();
		rs2 = stmt.executeQuery("select*from customers");
		Assert.assertEquals(compareResultSets(rs1,rs2),true);
	}
	
	//@Test(priority=3)
	void selectAllCustomersBycity() throws SQLException
	{
		cstmt = con.prepareCall("{CALL SelectCity(?)}");
		cstmt.setString(1,"Singapore");
		rs1 = cstmt.executeQuery();
		
		Statement stmt = con.createStatement();
		rs2 = stmt.executeQuery("Select * from customers where city = 'Singapore'");
		Assert.assertEquals(compareResultSets(rs1,rs2),true);
	}
	//Does not exists in db;
	//@Test(priority=4) 
	void selectAllCustomersBycityandPincode() throws SQLException
	{
		cstmt = con.prepareCall("{CALL SelectALLCustomersByCityAndPin(?,?)}");
		cstmt.setString(1,"Singapore");
		cstmt.setString(2,"079903");
		rs1 = cstmt.executeQuery();
		
		Statement stmt = con.createStatement();
		rs2 = stmt.executeQuery("Select * from customers where city = 'Singapore' and postalCode='079903'");
		Assert.assertEquals(compareResultSets(rs1,rs2),true);
	}
	
	//@Test(priority=5)
	void get_order_by_cust_id() throws SQLException
	{
		cstmt = con.prepareCall("{CALL get_order_by_cust_id(?,?,?,?,?)}");
		cstmt.setInt(1,141);
		cstmt.registerOutParameter(2,Types.INTEGER);
		cstmt.registerOutParameter(3,Types.INTEGER);
		cstmt.registerOutParameter(4,Types.INTEGER);
		cstmt.registerOutParameter(5,Types.INTEGER);
		
		cstmt.executeQuery();
		int shipped = cstmt.getInt(2);
		int canceled = cstmt.getInt(3);
		int resolved = cstmt.getInt(4);
		int disputed = cstmt.getInt(5);
	
//		System.out.println(shipped+" "+canceled+" "+" "+resolved+" "+disputed);
		Statement stmt = con.createStatement();
		rs = stmt.executeQuery(
			    "SELECT " +
			    "(SELECT COUNT(*) FROM orders WHERE customerNumber = 141 AND status = 'Shipped') AS Shipped, " +
			    "(SELECT COUNT(*) FROM orders WHERE customerNumber = 141 AND status = 'Canceled') AS Canceled, " +
			    "(SELECT COUNT(*) FROM orders WHERE customerNumber = 141 AND status = 'Resolved') AS Resolved, " +
			    "(SELECT COUNT(*) FROM orders WHERE customerNumber = 141 AND status = 'Disputed') AS Disputed"
			);
		rs.next();
		
		int exp_shipped = rs.getInt("Shipped");
		int exp_canceled =rs.getInt("Canceled");
		int exp_resolved =rs.getInt("Resolved");
		int exp_disputed =rs.getInt("Disputed");
		
		if(shipped==exp_shipped && canceled==exp_canceled&&exp_resolved==resolved&&exp_disputed==disputed)
		{
			Assert.assertTrue(true);
			
		}
		else
		{
			Assert.assertTrue(false);
		}
	}
	
	@Test(priority=5)
	void getcustomershipping() throws SQLException
	{
		cstmt = con.prepareCall("{CALL getcustomershipping(?,?)}");
		cstmt.setInt(1,112);
		cstmt.registerOutParameter(2,Types.VARCHAR);
		
		cstmt.executeQuery();
		
		String shippingTime = cstmt.getString(2);
		
	
//		System.out.println(shipped+" "+canceled+" "+" "+resolved+" "+disputed);
		Statement stmt = con.createStatement();
		rs = stmt.executeQuery(
			    "SELECT CASE " +
			    "WHEN country = 'USA' THEN '2-day shipping' " +
			    "WHEN country = 'CANADA' THEN '3-day shipping' " +
			    "ELSE '5-day shipping' END AS ShippingTime " +
			    "FROM customers WHERE customerNumber = 112"
			);
		rs.next();
		
		String exp_shippingTime = rs.getString("ShippingTime");
		
		Assert.assertEquals(shippingTime,exp_shippingTime);
	}
	
	
	public boolean compareResultSets(ResultSet resultset1, ResultSet resultset2) throws SQLException
	{
		while(resultset1.next())
		{
			resultset2.next();
			
			int count = resultset1.getMetaData().getColumnCount();
			for(int i=1;i<=count;i++)
			{
				if(!StringUtils.equals(resultset1.getString(i),resultset2.getString(i)))
				{
					return false;
				}
				return true;
			}
		}
		
		return true;
		
	}
}
