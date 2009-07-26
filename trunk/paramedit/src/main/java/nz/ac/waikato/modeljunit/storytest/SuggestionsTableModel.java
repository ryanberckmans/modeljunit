package nz.ac.waikato.modeljunit.storytest;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper around a CalcTable to use it in JTable
 */
public class SuggestionsTableModel
   extends AbstractTableModel
   implements TableModel, Observer
{
   public static final long serialVersionUID = 1;
   
   private static final int MAX = 4;
   /** The suggestion strategies */
   private final List<SuggestionStrategy> mStrategies;
   /** The suggestions */
   private final List<Suggestion> mSuggestions;
   
   private final CalcTable mTable;
   
   /**
    * Constructs a basic CalcTableModel using table
    */
   public SuggestionsTableModel(List<SuggestionStrategy> strategies, CalcTable table)
   {
      mTable = table;
      mStrategies = new ArrayList<SuggestionStrategy>(strategies);
      mSuggestions = new ArrayList<Suggestion>();
      for (SuggestionStrategy strat : mStrategies) {
         strat.registerObserver(this);
         mSuggestions.addAll(strat.getSuggestions());
      }
   }
   
   public Class<String> getColumnClass(int column)
   {
      return String.class;
   }
   
   public int getRowCount()
   {
      return mSuggestions.size() < MAX + 1? mSuggestions.size() : MAX + 1;
   }
   
   public int getColumnCount()
   {
      return mTable.columns();
   }
   
   public boolean isCellEditable(int row, int column)
   {
      return false;
   }
   
   public String getValueAt(int row, int column)
   {
      return mSuggestions.get(row).getFields().get(column);
   }
   
   public Suggestion getSuggestion(int row)
   {
      return mSuggestions.get(row);
   }
   
   public String getColumnName(int column)
   {
      return mTable.getColumnHeader(column);
   }
   
   public void update()
   {
      //TODO this is innefficient do it better
      mSuggestions.clear();
      for (SuggestionStrategy strat : mStrategies) {
         mSuggestions.addAll(strat.getSuggestions());
      }
      fireTableStructureChanged();
   }
}
