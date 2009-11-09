/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.command.AddColumnCommand;
import nz.ac.waikato.modeljunit.command.AddRowCommand;
import nz.ac.waikato.modeljunit.command.DeleteColumnCommand;
import nz.ac.waikato.modeljunit.command.DeleteRowCommand;
import nz.ac.waikato.modeljunit.command.SetColumnNameCommand;
import nz.ac.waikato.modeljunit.command.SetValueCommand;
import nz.ac.waikato.modeljunit.examples.FSM;

/**
 * @author root
 *
 */
public class SetValueModel
    extends FSM
{
  private static final int MAXDEPTH = 4;

  private final List<String> mHeaders;
  private final List<String> mAltHeaders;
  private final List<List<String>> mColumns;
  private final List<List<String>> mAltColumns;
  
  /** The commands issued in this automata
   *  They are of the format {0 if add|1 if del, the row index in mRows,
   *                          where in the calctable the action is taking place}*/
  private final List<int[]> mCommands;
  
  private int mDepth;
  
  private CalcTablePanel mSUT;
  
  public SetValueModel()
  {
    super();
    mColumns = new ArrayList<List<String>>();
    mAltColumns = new ArrayList<List<String>>();
    mHeaders = Arrays.asList(new String[]{"a", "b", ""});
    mAltHeaders = Arrays.asList(new String[] {"c", "d", "e"});
    mColumns.add(Arrays.asList(new String[] {"3", "6", "4"}));
    mColumns.add(Arrays.asList(new String[] {"2", "4", "5"}));
    mColumns.add(Arrays.asList(new String[] {"", "", ""}));
    mAltColumns.add(Arrays.asList(new String[] {"5", "8", "6"}));
    mAltColumns.add(Arrays.asList(new String[] {"4", "6", "7"}));
    mAltColumns.add(Arrays.asList(new String[] {"8", "3", "4"}));
    mCommands = new ArrayList<int[]>();    
    setup();
  }
  
  private void setup()
  {
    CalcTable calc = new CalcTable("table", mHeaders);
    calc.removeRow(0);
    mSUT = new CalcTablePanel(calc, new DummyStoryTestInterface());
    for (int r = 0; r < mColumns.get(0).size(); r ++) {
      calc.addRow();
    }
    for (int c = 0; c < mColumns.size(); c++) {
      List<String> col = mColumns.get(c);
      for (int r = 0; r < calc.rows(); r++) {
        calc.setValue(r, c, col.get(r));
      }
    }
    mDepth = 0;
    check();
  }
  
  public String getState()
  {
    StringBuffer sbuf = new StringBuffer();
    for (int[] com : mCommands) {
      sbuf.append(Arrays.toString(com));
      sbuf.append(", ");
    }
    return mCommands.toString() + "Depth = " + mDepth;
  }
  
  public boolean undoGuard()
  {
    return mDepth > 0;
  }
  
  @Action
  public void undo()
  {
    mDepth--;
    mSUT.getUndoInterface().undo();
  }
  
  public boolean redoGuard()
  {
    return mDepth < mCommands.size();
  }
  
  @Action
  public void redo()
  {
    mDepth++;
    mSUT.getUndoInterface().redo();
  }
  
  public void setColumnHeader(int column, int alt)
  {
    String value = alt == 1 ? mAltHeaders.get(column) : mHeaders.get(column);
    SetColumnNameCommand shc = new SetColumnNameCommand(mSUT.getStoryTestInterface(), column, value);
    mSUT.getUndoInterface().execute(shc);
    while (mDepth < mCommands.size()) {
      mCommands.remove(mDepth);
    }
    mCommands.add(new int[] {2, alt, column});
    mDepth++;
    check();
  }
  
  public void setValue(int column, int row, int alt)
  {
    String value = alt == 1 ? mAltColumns.get(column).get(row) : mColumns.get(column).get(row);
    SetValueCommand svc = new SetValueCommand(mSUT.getStoryTestInterface(), row, column, value);
    mSUT.getUndoInterface().execute(svc);
    while (mDepth < mCommands.size()) {
      mCommands.remove(mDepth);
    }
    mCommands.add(new int[] {3, alt, column, row});
    mDepth++;
    check();
  }
  
  @Action
  public void setColumnHeader0_0()
  {
    setColumnHeader(0, 0);
  }
  
  @Action
  public void setColumnHeader1_0()
  {
    setColumnHeader(1, 0);
  }
  
  @Action
  public void setColumnHeader2_0()
  {
    setColumnHeader(2, 0);
  }
  
  @Action
  public void setColumnHeader0_1()
  {
    setColumnHeader(0, 1);
  }
  
  @Action
  public void setColumnHeader1_1()
  {
    setColumnHeader(1, 1);
  }
  
  @Action
  public void setColumnHeader2_1()
  {
    setColumnHeader(2, 1);
  }
  
  @Action
  public void setValue0_0_0()
  {
    setValue(0, 0, 0);
  }
  
  @Action
  public void setValue1_0_0()
  {
    setValue(1, 0, 0);
  }
  
  @Action
  public void setValue2_0_0()
  {
    setValue(2, 0, 0);
  }
  
  @Action
  public void setValue0_1_0()
  {
    setValue(0, 1, 0);
  }
  
  @Action
  public void setValue1_1_0()
  {
    setValue(1, 1, 0);
  }
  
  @Action
  public void setValue2_1_0()
  {
    setValue(2, 1, 0);
  }
  
  @Action
  public void setValue0_2_0()
  {
    setValue(0, 2, 0);
  }
  
  @Action
  public void setValue1_2_0()
  {
    setValue(1, 2, 0);
  }
  
  @Action
  public void setValue2_2_0()
  {
    setValue(2, 2, 0);
  }
  
  @Action
  public void setValue0_0_1()
  {
    setValue(0, 0, 1);
  }
  
  @Action
  public void setValue1_0_1()
  {
    setValue(1, 0, 1);
  }
  
  @Action
  public void setValue2_0_1()
  {
    setValue(2, 0, 1);
  }
  
  @Action
  public void setValue0_1_1()
  {
    setValue(0, 1, 1);
  }
  
  @Action
  public void setValue1_1_1()
  {
    setValue(1, 1, 1);
  }
  
  @Action
  public void setValue2_1_1()
  {
    setValue(2, 1, 1);
  }
  
  @Action
  public void setValue0_2_1()
  {
    setValue(0, 2, 1);
  }
  
  @Action
  public void setValue1_2_1()
  {
    setValue(1, 2, 1);
  }
  
  @Action
  public void setValue2_2_1()
  {
    setValue(2, 2, 1);
  }
  
  public void reset(boolean arg0)
  {
    mDepth = 0;
    mCommands.clear();
    setup();
  }
  
  public void check()
  {
    boolean[] header = new boolean[3];
    boolean[][] expected = new boolean[3][3];
    for (int d = 0; d < mDepth; d++) {
      int[] command = mCommands.get(d);
      if (command[0] == 3) {
        boolean value = command[1] == 1;
        int c = command[2];
        int r = command[3];
        expected[c][r] = value;
      } else {
        boolean value = command[1] == 1;
        int c = command[2];
        header[c] = value;
      }
    }
    CalcTable calc = mSUT.getStoryTestInterface();
    Assert.assertEquals("ExpectedSize: ", expected.length, calc.columns());
    for (int c = 0; c < expected.length; c++) {
      String expheader = header[c] ? mAltHeaders.get(c) : mHeaders.get(c);
      Assert.assertEquals(expheader, calc.getColumnHeader(c));
      for (int r = 0; r < expected[c].length; r++) {
        boolean alt = expected[c][r];
        String exp = alt ? mAltColumns.get(c).get(r) : mColumns.get(c).get(r); 
        Assert.assertEquals(exp, calc.getValue(r, c));
      }
    }
    Assert.assertEquals("canUndo: ", mDepth > 0, mSUT.getUndoInterface().canUndo());
    Assert.assertEquals("canRedo: ", mDepth < mCommands.size(), mSUT.getUndoInterface().canRedo());
  }
}
