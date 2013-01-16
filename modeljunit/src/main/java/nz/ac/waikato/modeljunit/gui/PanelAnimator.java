
package nz.ac.waikato.modeljunit.gui;

import nz.ac.waikato.modeljunit.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.BitSet;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/** This gets the current Model object and builds a GUI based upon the actions
 *  it contains.
 * 
 * @author Gian Perrone <gdp3@cs.waikato.ac.nz>
**/
@SuppressWarnings("serial")
public class PanelAnimator extends PanelAbstract implements ActionListener {
   private static PanelAnimator mInstance;
   private Model mModel;
   private JList mActionHistoryList;
   private DefaultListModel mActionHistory;
   private JButton mResetButton;
   private Map<JButton,Integer> mButtons;
   private JLabel mStateLabel;
 

   public static PanelAnimator getInstance() {
      if(mInstance == null) {
         mInstance = new PanelAnimator();
      }
  
      return mInstance;
   }

   public PanelAnimator()
   {
      mModel = ModelJUnitGUI.getModel();

      Comparator<JButton> sort = new Comparator<JButton>() {
        @Override
        public int compare(JButton b1, JButton b2) {
          return b1.getText().compareTo(b2.getText());
        }
      };
      
      mButtons = new TreeMap<JButton,Integer>(sort);

      mActionHistory = new DefaultListModel();

      mActionHistoryList = new JList(mActionHistory);

      mResetButton = new JButton("Reset");
      // mButtons maps buttons to actions - reset is always "-1"
      mButtons.put(mResetButton, new Integer(-1));
      mResetButton.addActionListener(this);
  
      mStateLabel = new JLabel("(uninitialised model)");
      mStateLabel.setName("stateLabel");
   }

   public void newModel() {
      mModel = ModelJUnitGUI.getModel();
      removeAll();
      mButtons.clear();
      buildGUI();
      mButtons.put(mResetButton, new Integer(-1));

      mActionHistory.clear();
      mModel.doReset();
      mActionHistory.addElement("*** RESET *** (" + mModel.getCurrentState() + ")");
   }

   /** Construct an animator GUI based on the current state of the Model structure. 
    *
    * Call this only once, followed by multiple calls to updateGUI()
    *
    * Requires a non-null Model returned by ModelJUnitGUI.getModel()
    */
   public void buildGUI() {
      GridLayout gl = new GridLayout(0,3);

      setLayout(gl);

      // Build a map from action numbers to buttons representing those actions, suitable for display.

      if(mModel == null) return;
      mModel.doReset();
      
      BitSet enabledActions = mModel.enabledGuards();
      for(int i = 0; i < mModel.getNumActions(); i++) {
         System.out.println("GUI build action: " + i);
         JButton btn = new JButton(mModel.getActionName(i));
         btn.setEnabled(enabledActions.get(i));
         btn.addActionListener(this);
         add(btn);
         mButtons.put(btn, new Integer(i));
      }
   }

   public void updateGUI() {
      BitSet enabledActions = mModel.enabledGuards();

      for(JButton j : mButtons.keySet()) {
         int action = mButtons.get(j);
         if(action == -1) continue; // Reset button
         j.setEnabled(enabledActions.get(action));
      }

      mActionHistoryList.ensureIndexIsVisible(mActionHistory.size()-1);  

      mStateLabel.setText("Current State: "+mModel.getCurrentState());
   }

   public JList getActionHistoryList() {
      return mActionHistoryList;
   }

   public JButton getResetButton() {
      return mResetButton;
   }

   public JLabel getStateLabel() {
      return mStateLabel;
   }

   public void actionPerformed(ActionEvent e) {
      if(!(e.getSource() instanceof JButton)) return;

      int i = -1;

      try {
         i = mButtons.get((JButton)e.getSource());
      } catch (Exception ex) {
         ex.printStackTrace();
      }

      // -1 is the special "reset" button flag
      if(i == -1) {
         mModel.doReset();
         mActionHistory.clear();
         mActionHistory.addElement("*** RESET *** (" + mModel.getCurrentState() + ")");
         updateGUI();
         return;
      }

      mModel.doAction(i);
      mActionHistory.addElement(mModel.getActionName(i) + "(" + mModel.getCurrentState() + ")");
      
      updateGUI();
   }

}



