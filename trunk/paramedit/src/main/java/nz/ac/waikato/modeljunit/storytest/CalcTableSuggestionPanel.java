package nz.ac.waikato.modeljunit.storytest;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import java.util.List;
import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import nz.ac.waikato.modeljunit.command.Command;
import nz.ac.waikato.modeljunit.command.AddSuggestionCommand;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class CalcTableSuggestionPanel
   extends JPanel
{
   public static final long serialVersionUID = 1;
   
   private final JTable mTable;
   private final CalcTable mCalc;
   private final JPopupMenu mPopup;
   private final SuggestionsTableModel mModel;
   //private int mColumn;
   private int mRow;
   
   private final StoryTestGUIInterface mParent;
   
   public CalcTableSuggestionPanel(CalcTable calc,
                                   List<SuggestionStrategy> strats,
                                   StoryTestGUIInterface parent)
   {
      mCalc = calc;
      mParent = parent;
      mModel = new SuggestionsTableModel(strats, mCalc);
      mTable = new JTable(mModel);
      mPopup = new JPopupMenu();
      mPopup.add(new AddSuggestionAction());
      mTable.addMouseListener(new MyMouseListener());
      add(mTable);
      mTable.getSelectionModel().addListSelectionListener(new MySelectionListener());
   }
   
   private class MySelectionListener
      implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {
         int index = e.getFirstIndex();
         if (mTable.getSelectionModel().isSelectedIndex(index)) {
            Suggestion sug = mModel.getSuggestion(index);
            sug.selected();
         }
      }
   }
   
   private class AddSuggestionAction
      extends AbstractAction
      implements Action
   {
      public static final long serialVersionUID = 1;
      
      public AddSuggestionAction()
      {
         super("Add Suggestion");
      }
      
      public void actionPerformed(ActionEvent e)
      {
         Command command = new AddSuggestionCommand(mCalc,
                                                    mModel.getSuggestion(mRow));
         mParent.getUndoInterface().execute(command);
      }
   }
   
   private class MyMouseListener
      extends MouseAdapter
   {
      public void mousePressed(MouseEvent e)
      {
         //mColumn = mTable.columnAtPoint(e.getPoint());
         mRow = mTable.rowAtPoint(e.getPoint());
         checkForTriggerEvent(e);
      }

      public void mouseReleased(MouseEvent e)
      { checkForTriggerEvent(e); } 

      private void checkForTriggerEvent(MouseEvent e)
      {
         if (e.isPopupTrigger()) {
            mPopup.show(e.getComponent(),
                        e.getX(), e.getY());
         }
      }
   }
}
