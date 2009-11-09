/**
 * 
 */
package nz.ac.waikato.modeljunit.storytest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.command.AddRowCommand;
import nz.ac.waikato.modeljunit.command.DeleteRowCommand;
import nz.ac.waikato.modeljunit.examples.FSM;

/**
 * @author root
 *
 */
public class AddDelRowModel
    extends FSM
{
  private static final int MAXDEPTH = 4;
  
  private final List<List<String>> mRows;
  
  /** The commands issued in this automata
   *  They are of the format {0 if add|1 if del, the row index in mRows,
   *                          where in the calctable the action is taking place}*/
  private final List<int[]> mCommands;
  
  private final List<Integer> mDefaultTable;
  
  private int mDepth;
  
  private CalcTablePanel mSUT;
  
  public AddDelRowModel()
  {
    super();
    mRows = new ArrayList<List<String>>();
    mRows.add(Arrays.asList(new String[] {"3", "6", "4"}));
    mRows.add(Arrays.asList(new String[] {"2", "4", "5"}));
    mRows.add(Arrays.asList(new String[] {"", "", ""}));
    mCommands = new ArrayList<int[]>();
    mDefaultTable = Arrays.asList(new Integer[] {1, 0, 1});
    setup();
  }
  
  private void setup()
  {
    CalcTable calc = new CalcTable("table", Arrays.asList(new String[] {"a", "b", "c"}));
    calc.removeRow(0);
    mSUT = new CalcTablePanel(calc, new DummyStoryTestInterface());
    for (Integer i : mDefaultTable) {
      calc.addRow(mRows.get(i));
    }
    mDepth = 0;
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
  
  public void addRow(int row)
  {
    AddRowCommand arc = new AddRowCommand(mSUT.getStoryTestInterface(), row);
    mSUT.getUndoInterface().execute(arc);
    while (mDepth < mCommands.size()) {
      mCommands.remove(mDepth);
    }
    mCommands.add(new int[] {0, 2, row});
    mDepth++;
    check();
  }
  
  public boolean addRow0Guard()
  {
    return mCommands.size() < MAXDEPTH;
  }
  
  @Action
  public void addRow0()
  {
    addRow(0);
  }
  
  public boolean addRow1Guard()
  {
    return mCommands.size() < MAXDEPTH && mSUT.getStoryTestInterface().rows() >= 1;
  }
  
  @Action
  public void addRow1()
  {
    addRow(1);
  }
  
  public boolean addRow2Guard()
  {
    return mCommands.size() < MAXDEPTH && mSUT.getStoryTestInterface().rows() >= 2;
  }
  
  @Action
  public void addRow2()
  {
    addRow(2);
  }
  
  public boolean addRow3Guard()
  {
    return mCommands.size() < MAXDEPTH && mSUT.getStoryTestInterface().rows() >= 3;
  }
  
  @Action
  public void addRow3()
  {
    addRow(3);
  }
  
  public void delRow(int row)
  {
    DeleteRowCommand drc = new DeleteRowCommand(mSUT.getStoryTestInterface(), row);
    mSUT.getUndoInterface().execute(drc);
    while (mDepth < mCommands.size()) {
      mCommands.remove(mDepth);
    }
    mCommands.add(new int[] {1, 2, row});
    mDepth++;
    check();
  }
  
  public boolean delRow0Guard()
  {
    return mCommands.size() < MAXDEPTH && 1 <= mSUT.getStoryTestInterface().rows();
  }
  
  @Action
  public void delRow0()
  {
    delRow(0);
  }
  
  public boolean delRow1Guard()
  {
    return mCommands.size() < MAXDEPTH && 2 <= mSUT.getStoryTestInterface().rows();
  }
  
  @Action
  public void delRow1()
  {
    delRow(1);
  }
  
  public boolean delRow2Guard()
  {
    return mCommands.size() < MAXDEPTH && 3 <= mSUT.getStoryTestInterface().rows();
  }
  
  @Action
  public void delRow2()
  {
    delRow(2);
  }
  
  public boolean delRow3Guard()
  {
    return mCommands.size() < MAXDEPTH && 4 <= mSUT.getStoryTestInterface().rows();
  }
  
  @Action
  public void delRow3()
  {
    delRow(3);
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
        int row = command[1];
        expected.add(command[2], row);
      } else {
        expected.remove(command[2]);
      }
    }
    CalcTable calc = mSUT.getStoryTestInterface();
    Assert.assertEquals(expected.size(), calc.rows());
    for (int r = 0; r < expected.size(); r++) {
      int indirectrow = expected.get(r);
      for (int c = 0; c < mRows.get(indirectrow).size(); c++) {
        Assert.assertEquals(calc.getValue(r, c),
                            mRows.get(indirectrow).get(c));
      }
    }
    Assert.assertEquals(mDepth > 0, mSUT.getUndoInterface().canUndo());
    Assert.assertEquals(mDepth < mCommands.size(), mSUT.getUndoInterface().canRedo());
  }
}
