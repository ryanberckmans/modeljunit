/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest.parse;

import nz.ac.waikato.modeljunit.storytest.CalcTable;
import nz.ac.waikato.modeljunit.storytest.ColumnName;

/**
 * @author root
 *
 */
public class Variable
  implements Function
{
  private final ColumnName mColumn;
  private final CalcTable mTable;
  
  Variable(String column, CalcTable table)
  {
    this(table.getColumnName(column), table);
  }
  
  Variable(ColumnName column, CalcTable table)
  {
    mColumn = column;
    mTable = table;
  }
  
  public double calculate(int row)
  {
    System.out.println(mColumn + "\tnum = " + mTable.getColumnNum(mColumn));
    String val = mTable.getValue(row, mTable.getColumnNum(mColumn));
    if (val == "") {
      return 0;
    } else {
      return Double.parseDouble(val);
    }
  }
}