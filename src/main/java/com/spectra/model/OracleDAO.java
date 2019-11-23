package com.spectra.model;


import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import connections.IDao;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


public class OracleDAO {

	private CallableStatement sp_trackresults;
	private CallableStatement sp_process_insert;
	private PreparedStatement selStateMaster;
	private PreparedStatement selDistributor;
	private PreparedStatement sel_activity;

	private PreparedStatement sel_getactivityresults; //IH_DW.DW_ODS_ACTIVITY
    private PreparedStatement sel_getresultwithreqid; // Get result with activity requisition_id




	private OracleListener listener;
	
	private static LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

	private static Logger logger = lc.getLogger("OracleSQL Dao");

	private final Set<String> nemSet = new HashSet<String>();
	
	private Connection con;
	
	private final IDao dao;

	
	private PreparedStatement geteastactivity;
	

	private List<String> month = new LinkedList<String>();
	
	
	public void setListener(OracleListener listener){

		this.listener = listener;
		if(listener != null){
			listener.ListenerSet();
		}
	}

	public OracleDAO(IDao dao) {
		nemSet.add("ERTAPENEM");
		nemSet.add("IMIPENEM");
		nemSet.add("MEROPENEM");

		this.dao = dao;
	}

	public void getTrackResults(){

		try{
			sp_trackresults.setString(1,"NJ");
			sp_trackresults.registerOutParameter(2, Types.INTEGER);

			sp_trackresults.execute();

			System.out.println(sp_trackresults.getInt(2));

		} catch (SQLException se){
			se.printStackTrace();
		}


	}

	public void m_spProcessInsert(String state_abbrev)  {
		ResultSet rst = null;
		ResultSetMetaData rsmd = null;

		try
		{
			sp_process_insert.setString(1,state_abbrev);
			sp_process_insert.execute();


		} catch (SQLException se){
			se.printStackTrace();
		} finally {

		}

	}

    private static java.sql.Timestamp getCurrentDate(String strDate) {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.MONTH,10);
        cal.set(Calendar.DAY_OF_MONTH,1);

		System.out.println(cal.getTime());


        java.util.Date today = cal.getTime();
        return new java.sql.Timestamp(cal.getTimeInMillis());
    }

    // Get Results by requisition_id
    private void psel_getReqResult(String reqid){
        ResultSet rst = null;
        ResultSetMetaData rsmd = null;
        //final String testCategory = "IMMUNOIMMUNPCRARUPMICRO";
        //final String testCategory = "MICRO";

        try
        {

            sel_getresultwithreqid.setString(1,reqid);



            rst = sel_getresultwithreqid.executeQuery();

            rsmd = rst.getMetaData();

            int colCount = rsmd.getColumnCount() + 1;
            int cnt = 0;

            for (int i = 1; i < colCount; i++){
                //System.out.println(i + " " + rsmd.getColumnName(i) + " " + rsmd.getColumnType(i) + " " + rsmd.getColumnTypeName(i));
            }

            /* Result Metadata
            7  - Accession_Number
            11 - Order_Test_Code
            12 - Order_Test_Name

			*/


            while(rst.next()){
                //System.out.println();
//                if(rst.getString(15).equals("MICRO")) {
//					if(nemSet.contains(rst.getString(14)) && rst.getString(18).equals("R"))
                   		System.out.println(cnt++ + " " + rst.getString(6) + " " + rst.getString(11) + " " + rst.getString(14) + " " + rst.getString(29));
//                            " " + rst.getString(14));
                    //System.out.println(cnt++ + " " + rst.getString(13));
                    for(int i = 1; i < colCount;i++){
                        System.out.print(i + " " + rst.getString(i) + " " );
                    }
                    System.out.println();
//                }

            }

            rst.close();

        } catch (SQLException  se){
            se.printStackTrace();
        } finally {
            if (rst != null) {
                try {
                    rst.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }

            }
        }
    }

    public void m_selActivityByDate(String startDate)  {
        ResultSet rst = null;
        ResultSetMetaData rsmd = null;


        try
        {

            System.out.println(getCurrentDate(""));
            sel_getactivityresults.setTimestamp(1,getCurrentDate(""));



            rst = sel_getactivityresults.executeQuery();

            rsmd = rst.getMetaData();

            int colCount = rsmd.getColumnCount() + 1;
            int cnt = 0;

            for (int i = 1; i < colCount; i++){
                //System.out.println(rsmd.getColumnName(i));
            }

            while(rst.next()){

				if(rst.getString(1).equals("28828AH")) {
					System.out.println(cnt++ + " " + rst.getString(1) + " " + rst.getString(2)  );
					psel_getReqResult(rst.getString(1));
					//System.exit(0);
				}
            }

            rst.close();

        } catch (SQLException  se){
            se.printStackTrace();
        } finally {
            if (rst != null) {
                try {
                    rst.close();
                } catch (SQLException se) {
                    se.printStackTrace();
                }

            }
        }

    }

	public void m_selStateMaster(String state_abbrev, String status)  {
		ResultSet rst = null;
		ResultSetMetaData rsmd = null;

		try
		{
			selStateMaster.setString(1,state_abbrev);
			selStateMaster.setString(2,status);

			rst = selStateMaster.executeQuery();
			rsmd = rst.getMetaData();

			int colCount = rsmd.getColumnCount() + 1;

			for (int i = 1; i < colCount; i++){
				System.out.println(rsmd.getColumnName(i));
			}

			rst.close();

		} catch (SQLException se){

		} finally {
			if (rst != null) {
				try {
					rst.close();
				} catch (SQLException se) {

				}

			}
		}

	}

	public void selActivity()  {

		logger.error("OracleDAO model selActivity *************");
		ResultSet rst = null;
		ResultSetMetaData rsmd = null;

		try
		{

			rst = sel_activity.executeQuery();
			rsmd = rst.getMetaData();

			int colCount = rsmd.getColumnCount() + 1;

			for (int i = 1; i < colCount; i++){
				System.out.println(rsmd.getColumnName(i));
			}
			while(rst.next()){
				for (int i = 1; i < rsmd.getColumnCount() + 1; i++){
					System.out.print(rst.getString(i) + " - ");
				}
				System.out.println();
			}

			rst.close();

		} catch (SQLException se){
			se.printStackTrace();
		} finally {
			if (rst != null) {
				try {
					rst.close();
				} catch (SQLException se) {
					se.printStackTrace();
				}

			}
		}

	}
	

	
	private void prepareStatements() {
		logger.warn("Preparing statements for connection: " + con);
		try {
			if(con == null){

				throw new SQLException("Connection not available");
			}

			// Get results from IH_DW.DW_ODS_Activity
            sel_getactivityresults = con.prepareStatement("select " +
                    "requisition_id " +
                    "from " +
                    "IH_DW.DW_ODS_ACTIVITY " +
                    "where " +
                    "LAST_UPDATED_DATE >= ?");

			// Get Result with Requisition_ID
            sel_getresultwithreqid = con.prepareStatement("select * from IH_DW.RESULTS where requisition_id = ? ");




			// Insert row into ASR_Process_Tracking table
			//Parameters: NJ(String)
			sp_process_insert = con.prepareCall("{call SP_ASR_PROCESS_TRACKING_INSERT( ?)} ");

			// Select all record from State_Master
			// Parameters: NJ(String), active(String)
			selStateMaster = con.prepareStatement("select * from STATERPT_OWNER.STATE_MASTER " +
					"where state_master_pk is not null and state_abbreviation = ? and status = ? ");

			sp_trackresults = con.prepareCall("{call SP_ASR_PROC_TRACK_RESULTS( ?, ?)} ");

			selDistributor = con.prepareStatement("select s.state, s.state_abbreviation, di.DISTRIBUTOR_ITEM_PK, d.distributor_pk, di.DISTRIBUTOR_ITEM, \n" +
					"                 di.DISTRIBUTOR_ITEM_TYPE, di.CREATED_DATE, di.CREATED_BY, di.LAST_UPDATED_DATE, di.LAST_UPDATED_BY, \n" +
					"                 di.DISTRIBUTOR_ITEMS_GROUP, di.DISTRIBUTOR_ITEM_VALUE from STATERPT_OWNER.DISTRIBUTOR_ITEMS di, STATERPT_OWNER.DISTRIBUTOR_ITEMS_MAP dim, \n" +
					"                 STATERPT_OWNER.DISTRIBUTOR d, STATERPT_OWNER.GENERATOR g, STATERPT_OWNER.STATE_MASTER s \n" +
					"           where di.distributor_item_pk is not null and dim.distributor_fk = d.distributor_pk \n" +
					"           and dim.distributor_items_group = di.distributor_items_group and d.state_fk = s.state_master_pk \n" +
					"           and d.generator_fk = g.generator_pk and g.state_fk = s.state_master_pk \n" +
					"           and s.state = ? and s.state_abbreviation = ? and di.status = ? ");


			sel_activity = con.prepareStatement("select " +
					"distinct(act.requisition_id) , f.PHYS_STATE,r.micro_isolate,r.MICRO_ORGANISM_NAME,r.result_test_name, lod.test_category,lo.requisition_status " +
					",lod.RESULT_RPT_CHNG_DATE_TIME,r.TEXTUAL_RESULT, " +
                    "                        r.TEXTUAL_RESULT_FULL, " +
                    "                        r.NUMERIC_RESULT " +
					"from " +
					"IH_DW.RESULTS r, " +
					"IH_DW.DW_ODS_ACTIVITY act, " +
					"IH_DW.DIM_LAB_ORDER lo, IH_DW.DIM_ACCOUNT a," +
					"IH_DW.DIM_LAB_ORDER_DETAILS lod, " +
					"IH_DW.DIM_FACILITY f " +
					"where " +
							"lo.requisition_id = act.requisition_id " +
					"and lo.account_fk = a.account_pk  " +

					"AND f.phys_state = 'NJ' " +
					"AND act.requisition_id = '2606GWH' " +
					//"and act.last_updated_date < '30-JUN-19 12.00.00.000000000 PM' " +
					"and r.LAB_ORDER_FK = lo.LAB_ORDER_PK  " +
					"and r.LAB_ORDER_DETAILS_FK = lod.LAB_ORDER_DETAILS_PK and lo.LAB_ORDER_PK = lod.LAB_ORDER_FK ");
					//"and lod.test_category = 'MICRO' and r.result_test_name = 'MEROPENEM'");

			//sel_Requistion = con.prepareStatement("");



		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

}
	
	
	
	
	

public void disconnect(){
	if(con != null){
		try {
			con.close();
		} catch (SQLException e) {
			
		}
		
	}
	
	dao.disconnect();
	con = null;
	
}



public void Connect() {
	
	
	
	if(con == null){
		logger.warn("Connecting SQLProdDAO..." + con); 
		
		con = (Connection) dao.getConnection();
		prepareStatements();
	}
	
}


}
