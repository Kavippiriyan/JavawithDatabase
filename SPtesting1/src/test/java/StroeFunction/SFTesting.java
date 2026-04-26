package StroeFunction;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SFTesting 
{

	Connection con =null;
	Statement stmt;
	ResultSet rs;
	ResultSet rs1;
	ResultSet rs2;
	CallableStatement cstmt;
	
	@BeforeClass
	void setup() throws SQLException
	{
		con = DriverManager.getConnection("jdbc:mysql://localhost:3306/classicmodels","root","12345");
		
	}
	
	void tearDown() throws SQLException
	{
		con.close();
	}
	
	@Test(priority=1)
	void test_storeFunction_exist() throws SQLException
	{
		stmt = con.createStatement();
		rs = stmt.executeQuery("show function status where Name='customerlevel'");
		rs.next();
		Assert.assertEquals(rs.getString("Name"),"customerlevel");
		
	}
	
	@Test(priority=2)
	void test_customerlvl_withSqlStmts() throws SQLException
	{
		stmt = con.createStatement();
		rs1 = stmt.executeQuery("select customerName, customerlevel(creditLimit)from customers");
		
		
		stmt = con.createStatement();
		rs2 = stmt.executeQuery("SELECT customerName, creditLimit, CASE WHEN creditLimit > 50000 THEN 'Platinum' WHEN creditLimit >= 10000 AND creditLimit <= 50000 THEN 'Gold' ELSE 'Silver' END AS customerlevel FROM customers");
		Assert.assertEquals(compareResultSets(rs1,rs2), true);
		
	}
	
	@Test(priority=3)
	void customerLevel_WithStoreProcedure() throws SQLException
	{
		cstmt = con.prepareCall("{CALL getcustomerlevel(?,?)}");
		cstmt.setInt(1, 131);
		cstmt.registerOutParameter(2, Types.VARCHAR);
		cstmt.executeQuery();
		String CustomerLevel = cstmt.getString(2);
		
		rs = con.createStatement().executeQuery("SELECT customerName, creditLimit, CASE WHEN creditLimit > 50000 THEN 'Platinum' WHEN creditLimit >= 10000 AND creditLimit <= 50000 THEN 'Gold' ELSE 'Silver' END AS customerlevel FROM customers WHERE customerNumber = 131");
		rs.next();
		String exp_customerlvl = rs.getString("customerlevel");
		Assert.assertEquals(CustomerLevel, exp_customerlvl);
		
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
