package nz.ac.waikato.modeljunit.storytest;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Arrays;
import java.lang.IndexOutOfBoundsException;

/**
 * Tests the CalcTable
 * @author Simon Ware
 */
public class CalcTableTest extends TestCase {
   
    private CalcTable mTable = null;
    private String[] mColumns = new String[]{"a", "b", "c"};
    private String[][] mRows = new String[][] {{"1","2","3"},{"4","2","6"},{"5","1","6"}};
    
    /**
     * Prepare for the test by creating some simulation objects.
     */
    protected void setUp() { 
       mTable = new CalcTable("Test", Arrays.asList(mColumns));
       try {
          for (int i = 0; i < mRows.length; i++) {
             for (int j = 0; j < mRows.length; j++) {
                mTable.setValue(i, j, mRows[i][j] + "");
             }
             if (i + 1 < mRows.length) {
                mTable.addRow();
             }
          }
       } catch (IndexOutOfBoundsException e) {
          assertTrue(false);
       }
    }
    
    /**
     * Help java collect the garbage
     */
    protected void tearDown() {
        mTable = null;
    }
    
    /**
     * Tests if the initial mTable is setup correctly
     */
    public void testSetup() {
        String string = "|a|b|c|\n|1|2|3|\n|4|2|6|\n|5|1|6|\n";
        assertEquals(string, mTable.toString());
    }
    
    private void setValueCommon(int row, int column, String value)
    {
       mTable.setValue(row, column, value);
       getValueCommon(row, column, value);
    }
    
    private void getValueCommon(int row, int column, String value)
    {
       assertEquals(value, mTable.getValue(row, column));
    }
    
    /**
     * Test adding a row
     */
    public void testAddRow1() {
        try {
           int rows = mTable.rows();
           mTable.addRow();
           assertEquals(rows +1, mTable.rows());
           for (int i = 0; i < mTable.columns(); i++) {
              getValueCommon(rows, i, "");
              setValueCommon(rows, i, "test");
           }
        } catch (IndexOutOfBoundsException e) {
            assertTrue(false);
        }
    }
    
    public void testAddRow2()
    {
       int rowtoadd = 1;
       try {
           int rows = mTable.rows();
           mTable.addRow(rowtoadd);
           assertEquals(rows +1, mTable.rows());
           for (int i = 0; i < mTable.columns(); i++) {
              getValueCommon(rowtoadd,i,"");
              setValueCommon(rowtoadd,i,"test");
              getValueCommon(rowtoadd + 1, i, mRows[rowtoadd][i] + "");
           }
        } catch (IndexOutOfBoundsException e) {
            assertTrue(false);
        } 
    }
    
    public void testAddRow3()
    {
       int rowtoadd = mTable.rows() + 1;
       try {
           mTable.addRow(rowtoadd);
        } catch (IndexOutOfBoundsException e) {
           return;
        } 
        assertTrue(false);
    }
    
    public void testRemoveRow()
    {
       int rowtodel = 1;
       try {
           int rows = mTable.rows();
           mTable.removeRow(rowtodel);
           assertEquals(rows - 1, mTable.rows());
           for (int i = 0; i < mTable.columns(); i++) {
              getValueCommon(rowtodel,i,mRows[rowtodel + 1][i]);
              setValueCommon(rowtodel,i,"test");
           }
        } catch (IndexOutOfBoundsException e) {
            assertTrue(false);
        } 
    }
    
    public void testAddColumn1()
    {
       int columnToAdd = mTable.columns() + 1;
       try {
           mTable.addColumn("d", columnToAdd);
        } catch (IndexOutOfBoundsException e) {
           return;
        }
        assertTrue(false);
    }
    
    public void testAddColumn2()
    {
       String colname = "d";
       try {
           int columns = mTable.columns();
           mTable.addColumn(colname);
           assertEquals(mTable.getColumnHeader(columns), colname);
           assertEquals(columns +1, mTable.columns());
           for (int i = 0; i < mTable.rows(); i++) {
              getValueCommon(i, columns, "");
              setValueCommon(i, columns, "test");
           }
        } catch (IndexOutOfBoundsException e) {
            assertTrue(false);
        }
    }
    
    public void testAddColumn3()
    {
       int columnToAdd = 2;
       String colname = "d";
       String def = "def";
       try {
           int columns = mTable.columns();
           mTable.addColumn(colname, columnToAdd, def);
           assertEquals(mTable.getColumnHeader(columnToAdd), colname);
           assertEquals(mTable.getColumnHeader(columnToAdd + 1),
                        mColumns[columnToAdd]);
           assertEquals(columns + 1, mTable.columns());
           for (int i = 0; i < mTable.rows(); i++) {
              getValueCommon(i, columnToAdd, def);
              setValueCommon(i, columnToAdd, "test");
              getValueCommon(i, columnToAdd + 1, mRows[i][columnToAdd]);
           }
        } catch (IndexOutOfBoundsException e) {
            assertTrue(false);
        }
    }
    
    public void testRemoveColumn()
    {
       int columnToDel = 1;
       try {
           int columns = mTable.columns();
           mTable.removeColumn(columnToDel);
           assertEquals(mTable.getColumnHeader(columnToDel),
                        mColumns[columnToDel + 1]);
           assertEquals(columns - 1, mTable.columns());
           for (int i = 0; i < mTable.rows(); i++) {
              getValueCommon(i, columnToDel, mRows[i][columnToDel + 1]);
              setValueCommon(i, columnToDel, "test");
           }
        } catch (IndexOutOfBoundsException e) {
            assertTrue(false);
        }
    }
    
    /**
     * Returns the test suite for the close door command.
     * @return The test suite.
     */
    public static TestSuite suite() {
        return new TestSuite(CalcTableTest.class);
    }
    
    /**
     * Run the test for the close door command.
     * @param args The ignored arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
