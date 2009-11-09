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
import nz.ac.waikato.modeljunit.examples.FSM;

/**
 * @author root
 *
 */
public class AddDelColumnModel
    extends FSM
{
  private static final int MAXDEPTH = 4;

  private final List<String> mHeaders;
  private final List<List<String>> mColumns;
  
  /** The commands issued in this automata
   *  They are of the format {0 if add|1 if del, the row index in mRows,
   *                          where in the calctable the action is taking place}*/
  private final List<int[]> mCommands;
  
  private final List<Integer> mDefaultTable;
  
  private int mDepth;
  
  private CalcTablePanel mSUT;
  
  public AddDelColumnModel()
  {
    super();
    mColumns = new ArrayList<List<String>>();
    mHeaders = Arrays.asList(new String[]{"a", "b", ""});
    mColumns.add(Arrays.asList(new String[] {"3", "6", "4"}));
    mColumns.add(Arrays.asList(new String[] {"2", "4", "5"}));
    mColumns.add(Arrays.asList(new String[] {"", "", ""}));
    mCommands = new ArrayList<int[]>();
    mDefaultTable = Arrays.asList(new Integer[] {1, 0, 1});
    setup();
  }
  
  private void setup()
  {
    CalcTable calc = new CalcTable("table", Arrays.asList(new String[] {"a", "b", "c"}));
    calc.removeRow(0);
    mSUT = new CalcTablePanel(calc, new DummyStoryTestInterface());
    for (int r = 0; r < mColumns.get(0).size(); r ++) {
      calc.addRow();
    }
    for (int c = 0; c < mDefaultTable.size(); c++) {
      int ic = mDefaultTable.get(c);
      List<String> col = mColumns.get(ic);
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
  
  public void addColumn(int column)
  {
    AddColumnCommand arc = new AddColumnCommand(mSUT.getStoryTestInterface(), column);
    mSUT.getUndoInterface().execute(arc);
    while (mDepth < mCommands.size()) {
      mCommands.remove(mDepth);
    }
    mCommands.add(new int[] {0, 2, column});
    mDepth++;
    check();
  }
  
  public boolean addColumn0Guard()
  {
    return mCommands.size() < MAXDEPTH;
  }
  
  @Action
  public void addColumn0()
  {
    addColumn(0);
  }
  
  public boolean addColumn1Guard()
  {
    return mCommands.size() < MAXDEPTH && mSUT.getStoryTestInterface().columns() >= 1;
  }
  
  @Action
  public void addColumn1()
  {
    addColumn(1);
  }
  
  public boolean addColumn2Guard()
  {
    return mCommands.size() < MAXDEPTH && mSUT.getStoryTestInterface().columns() >= 2;
  }
  
  @Action
  public void addColumn2()
  {
    addColumn(2);
  }
  
  public boolean addColumn3Guard()
  {
    return mCommands.size() < MAXDEPTH && mSUT.getStoryTestInterface().columns() >= 3;
  }
  
  @Action
  public void addColumn3()
  {
    addColumn(3);
  }
  
  public void delColumn(int column)
  {
    DeleteColumnCommand drc = new DeleteColumnCommand(mSUT.getStoryTestInterface(), column);
    mSUT.getUndoInterface().execute(drc);
    while (mDepth < mCommands.size()) {
      mCommands.remove(mDepth);
    }
    mCommands.add(new int[] {1, 2, column});
    mDepth++;
    check();
  }
  
  public boolean delColumn0Guard()
  {
    return mCommands.size() < MAXDEPTH && 1 <= mSUT.getStoryTestInterface().columns();
  }
  
  @Action
  public void delColumn0()
  {
    delColumn(0);
  }
  
  public boolean delColumn1Guard()
  {
    return mCommands.size() < MAXDEPTH && 2 <= mSUT.getStoryTestInterface().columns();
  }
  
  @Action
  public void delColumn1()
  {
    delColumn(1);
  }
  
  public boolean delColumn2Guard()
  {
    return mCommands.size() < MAXDEPTH && 3 <= mSUT.getStoryTestInterface().columns();
  }
  
  @Action
  public void delColumn2()
  {
    delColumn(2);
  }
  
  public boolean delColumn3Guard()
  {
    return mCommands.size() < MAXDEPTH && 4 <= mSUT.getStoryTestInterface().columns();
  }
  
  @Action
  public void delColumn3()
  {
    delColumn(3);
  }
  
  public void reset(boolean arg0)
  {
    mDepth = 0;
    mCommands.clear();
    setup();
  }
  
  public void check()
  {
    List<Integer> expected = new ArrayList<Integer>(mDefaultTable);
    for (int c = 0; c < mDepth; c++) {
      int[] command = mCommands.get(c);
      if (command[0] == 0) {
        int column = command[1];
        expected.add(command[2], column);
      } else {
        expected.remove(command[2]);
      }
    }
    CalcTable calc = mSUT.getStoryTestInterface();
    Assert.assertEquals("ExpectedSize: ", expected.size(), calc.columns());
    for (int c = 0; c < expected.size(); c++) {
      int indirectcolumn = expected.get(c);
      for (int r = 0; r < mColumns.get(indirectcolumn).size(); r++) {
        Assert.assertEquals("Values: at" + c  + "," + r, calc.getValue(r, c),
                            mColumns.get(indirectcolumn).get(r));
      }
    }
    Assert.assertEquals("canUndo: ", mDepth > 0, mSUT.getUndoInterface().canUndo());
    Assert.assertEquals("canRedo: ", mDepth < mCommands.size(), mSUT.getUndoInterface().canRedo());
  }
}
