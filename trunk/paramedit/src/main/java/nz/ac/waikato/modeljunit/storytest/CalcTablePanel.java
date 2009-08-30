package nz.ac.waikato.modeljunit.storytest;

import nz.ac.waikato.modeljunit.command.RemoveCalcTableCommand;
import nz.ac.waikato.modeljunit.command.ToggleResultColumnCommand;
import nz.ac.waikato.modeljunit.command.UndoInterface;
import nz.ac.waikato.modeljunit.command.AddColumnCommand;
import nz.ac.waikato.modeljunit.command.AddRowCommand;
import nz.ac.waikato.modeljunit.command.DeleteColumnCommand;
import nz.ac.waikato.modeljunit.command.DeleteRowCommand;
import nz.ac.waikato.modeljunit.command.SetColumnNameCommand;
import nz.ac.waikato.modeljunit.command.SetValueCommand;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import javax.swing.JFrame;
import java.awt.Component;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import javax.swing.JComponent;
import nz.ac.waikato.modeljunit.command.Command;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.WindowConstants;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import javax.swing.event.ChangeEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class CalcTablePanel
   extends JPanel
   implements StoryTestGUIInterface
{
   public static final long serialVersionUID = 1;
   
   // Eclipse JUnit colours are: green=0x5fbf5f, red=0xc91625/0xde1f2f/0x9f3f3f
   // JUnit Swingui colours were blatant: green=0x04fc04 red=0xff0000
   private static final Color NORMALCOLOR = Color.WHITE;
   /** This colour is used for the background colour of output columns. */
   private static final Color RESULTCOLOR = new Color(0xcdffcb); // very pale green
   private static final Color ERRORCOLOR = new Color(0xde1f2f); // Eclipse JUnit red
   private static final Color HIGHLIGHTEDCOLOR = new Color(0x04fc04); // Eclipse JUnit green   
   private final Action mUA;
   private final Action mRA;
   private final Action mARE;
   private final Action mTR;
   private final Action mAR;
   private final Action mAC;
   private final Action mDR;
   private final Action mDC;
   private final Action mRC;
   
   private final CalcTable mCalc;
   private final JTable mTable;
   private final JPopupMenu mPopup;
   
   private int mColumn;
   private int mRow;
   
   private final StoryTestGUIInterface mParent;
   private final StoryTest mStory;
   
   public CalcTablePanel(CalcTable calc, StoryTestGUIInterface parent)
   {
      mStory = (StoryTest)parent.getStoryTestInterface();
      mParent = parent;
      mCalc = calc;
      mUA = mParent.getUndoInterface().getUndoAction();
      mRA = mParent.getUndoInterface().getRedoAction();
      mAR = new AddRowAction();
      mARE = new AddRowEnterAction();
      mAC = new AddColumnAction();
      mDR = new DeleteRowAction();
      mDC = new DeleteColumnAction();
      mTR = new ToggleResultAction();
      mRC = new RemoveCalcTableAction();
      CalcTableModel model = new CalcTableModel(mCalc);
      mTable = new JTable(model);
      mTable.setDefaultRenderer(String.class,
                                new TextCellRenderer(mTable.getDefaultRenderer(String.class)));
      mTable.setDefaultEditor(String.class, new TextCellEditor());
      mTable.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), mARE);
      mTable.getActionMap().put(mARE, mARE);
      mTable.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK), mAC);
      mTable.getActionMap().put(mAC, mAC);
      mTable.addMouseListener(new MyMouseListener());
      mPopup = new JPopupMenu();
      mPopup.add(new JMenuItem(mAR));
      mPopup.add(new JMenuItem(mAC));
      mPopup.add(new JMenuItem(mDR));
      mPopup.add(new JMenuItem(mDC));
      mPopup.add(new JMenuItem(mUA));
      mPopup.add(new JMenuItem(mRA));
      mPopup.add(new JMenuItem(mTR));
      mPopup.add(new JMenuItem(mRC));
      add(mTable);
      FocusListener focus = new MYFocusListener();
      mTable.addFocusListener(focus);
      mRow = mCalc.rows();
      mColumn = mCalc.columns() - 1;
   }

   /** Create a slightly different colour.  eg. darker. */
   public static Color subtleDifference(Color c) {
	   float[] hsb = new float[3];
	   Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
	   return Color.getHSBColor(hsb[0], hsb[1], hsb[2]*0.95F);
   }
	
   private class MYFocusListener
      extends FocusAdapter
   {
      public void focusGained(FocusEvent event)
      {
         requestSuggestions(mCalc);
      }
   }
   
   public StoryTestInterface getStoryTestInterface()
   {
      return mCalc;
   }

   public StoryTestGUIInterface getStoryParent()
   {
      return mParent;
   }
   
   public void requestSuggestions(StoryTestInterface sti)
   {
      mParent.requestSuggestions(sti);
   }
   
   public UndoInterface getUndoInterface()
   {
      return mParent.getUndoInterface();
   }
   
   private class MyMouseListener
      extends MouseAdapter
   {
      public void mousePressed( MouseEvent e )
      {
         mColumn = mTable.columnAtPoint(e.getPoint());
         mRow = mTable.rowAtPoint(e.getPoint());
         checkForTriggerEvent( e );
      }

      public void mouseReleased( MouseEvent e )
      { checkForTriggerEvent( e ); } 

      private void checkForTriggerEvent( MouseEvent e )
      {
         if ( e.isPopupTrigger() ) 
            mPopup.show( e.getComponent(),
                            e.getX(), e.getY() );
      }
   }
   
   private class TextCellRenderer
      implements TableCellRenderer
   {
      private final TableCellRenderer mRenderer;
      
      public TextCellRenderer(TableCellRenderer renderer)
      {
         mRenderer = renderer;
      }
      
      public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row, int column)
      {
         Component comp = mRenderer.getTableCellRendererComponent(table, value,
                                                                  isSelected,
                                                                  hasFocus, row,
                                                                  column);
         Color background = mCalc.isResult(column) ? RESULTCOLOR : NORMALCOLOR;
         if (row > 0) {
            background = mCalc.isContradiction(row - 1) ? ERRORCOLOR : background;
            background = mCalc.isHighlighted(row - 1) ? HIGHLIGHTEDCOLOR : background;
         }
         background = row % 2 == 0 ? background : subtleDifference(background);
         comp.setBackground(background);
         return comp;
      }
   }
   
   private class TextCellEditor
      extends AbstractCellEditor
      implements TableCellEditor
   {
      public static final long serialVersionUID = 1;
      
      final private JTextField mComponent = new JTextField();
      private int mRow = 0;
      private int mColumn = 0;
      private String mPrevious = "";
      
      public Component getTableCellEditorComponent(JTable table, Object value,
             boolean isSelected, int rowIndex, int vColIndex) {
 
         mComponent.setText((String)value);
         System.out.println(value);
         mRow = rowIndex;
         mColumn = vColIndex;
         mTable.changeSelection(mRow, mColumn, false, false);
         mPrevious = (String)value;
         return mComponent;
     }
 
     public Object getCellEditorValue() {
        if (mPrevious.equals(mComponent.getText())) {return mPrevious;}
        System.out.println(mComponent.getText());
        Command command = mRow == 0 ?
                                    new SetColumnNameCommand(mCalc, mColumn,
                                                             mComponent.getText(),
                                                             mPrevious)
                                  : new SetValueCommand(mCalc, mRow - 1, mColumn,
                                                        mComponent.getText(),
                                                        mPrevious);
        getUndoInterface().execute(command);
        return mComponent.getText();
     }
   }
   
   private abstract class MyAbstractAction
      extends AbstractAction
   {
      public static final long serialVersionUID = 1;
      
      public MyAbstractAction(String name)
      {
         super(name);
      }
      
      public void actionPerformed(ActionEvent e)
      {
         if (mTable.isEditing()) {
            mTable.editingStopped(new ChangeEvent(e.getSource()));
         }
      }
   }
   
   private class AddRowAction
      extends MyAbstractAction
      implements Action
   {
      public static final long serialVersionUID = 1;
      
      public AddRowAction()
      {
         super("Add Row");
      }
      
      public void actionPerformed(ActionEvent e)
      {
         super.actionPerformed(e);
         int srow = mTable.getSelectedRow();
         int scol = mTable.getSelectedColumn();
         int row = srow == -1 ? mRow : srow;
         row = row == -1 ? mCalc.rows() : row;
         
         Command command = new AddRowCommand(mCalc, row);
         getUndoInterface().execute(command);
         if (srow != -1 && scol != -1) {
            mTable.changeSelection(srow + 1, scol, false, false);
         }
      }
   }
   
   private class AddRowEnterAction
     extends MyAbstractAction
     implements Action
   {
     public static final long serialVersionUID = 1;
     
     public AddRowEnterAction()
     {
        super("Add Row");
     }
     
     public void actionPerformed(ActionEvent e)
     {
        super.actionPerformed(e);
        int srow = mTable.getSelectedRow();
        int scol = mTable.getSelectedColumn();
        int row = mCalc.rows();
        System.out.println(srow + ":" + row);
        if (srow == row) {
          Command command = new AddRowCommand(mCalc, row);
          getUndoInterface().execute(command);
        }
        mTable.changeSelection(srow + 1, scol, false, false);
     }
   }
   
   private class DeleteColumnAction
      extends MyAbstractAction
      implements Action
   {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public DeleteColumnAction()
      {
         super("Delete Column");
      }
      
      public void actionPerformed(ActionEvent e)
      {
         super.actionPerformed(e);
         int srow = mTable.getSelectedRow();
         int scol = mTable.getSelectedColumn();
         int column = scol == -1 ? mColumn : scol;
         column = column == -1 ? mCalc.columns() : column;
         Command command = new DeleteColumnCommand(mCalc, column);
         getUndoInterface().execute(command);
         if (srow != -1 && scol != -1) {
            mTable.changeSelection(srow, scol + 1, false, false);
         }
      }
   }
   
   private class ToggleResultAction
     extends MyAbstractAction
     implements Action
  {
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
  
     public ToggleResultAction()
     {
        super("Toggle Result");
     }
     
     public void actionPerformed(ActionEvent e)
     {
        super.actionPerformed(e);
        int scol = mTable.getSelectedColumn();
        int column = scol == -1 ? mColumn : scol;
        column = column == -1 ? mCalc.columns() - 1: column;
        Command command = new ToggleResultColumnCommand(mCalc, mColumn);
        getUndoInterface().execute(command);
     }
  }
   
   private class DeleteRowAction
      extends MyAbstractAction
      implements Action
   {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public DeleteRowAction()
      {
         super("Delete Row");
      }
      
      public void actionPerformed(ActionEvent e)
      {
         super.actionPerformed(e);
         int srow = mTable.getSelectedRow();
         int scol = mTable.getSelectedColumn();
         int row = srow == -1 ? mRow : srow;
         row = row == -1 ? mCalc.columns() : row;
         row--;
         if (row == -1) {System.out.println("can't delete header"); return;}
         Command command = new DeleteRowCommand(mCalc, row);
         getUndoInterface().execute(command);
         if (srow != -1 && scol != -1) {
            mTable.changeSelection(srow, scol, false, false);
         }
      }
   }
   
   private class AddColumnAction
      extends MyAbstractAction
      implements Action
   {
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public AddColumnAction()
      {
         super("Add Column");
      }
      
      public void actionPerformed(ActionEvent e)
      {
         super.actionPerformed(e);
         int srow = mTable.getSelectedRow();
         int scol = mTable.getSelectedColumn();
         int column = scol == -1 ? mColumn : scol;
         column = column == -1 ? mCalc.columns() : column;
         column++;
         Command command = new AddColumnCommand(mCalc, column);
         getUndoInterface().execute(command);
         if (srow != -1 && scol != -1) {
            mTable.changeSelection(srow, scol + 1, false, false);
         }
      }
   }
   
   private class RemoveCalcTableAction
     extends MyAbstractAction
     implements Action
  {
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
  
     public RemoveCalcTableAction()
     {
        super("Remove CalcTable");
     }
     
     public void actionPerformed(ActionEvent e)
     {
        super.actionPerformed(e);
        Command command = new RemoveCalcTableCommand(mStory, mCalc);
        getUndoInterface().execute(command);
     }
  }  
   
   public static void main(String args[])
   {
      JFrame frame = new JFrame("frame");
      String[] strings = new String[] {"a","b","c"};
      CalcTable tab = new CalcTable("Table1", Arrays.asList(strings));
      tab.setValue(0,0,"1");
      tab.setValue(0,1,"2");
      tab.setValue(0,2,"3");
      tab.addRow();
      frame.add(new CalcTablePanel(tab, null));
      frame.pack();
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
}
