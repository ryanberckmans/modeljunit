
package nz.ac.waikato.modeljunit.gui;

import nz.ac.waikato.modeljunit.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

import javax.swing.event.ListDataListener;

/** This gets the current Model object and builds a GUI based upon the actions
 *  it contains.
 * 
 * @author Gian Perrone <gdp3@cs.waikato.ac.nz>
**/
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

      mButtons = new HashMap<JButton,Integer>();

      mActionHistory = new DefaultListModel();

      mActionHistoryList = new JList(mActionHistory);
      mActionHistoryList.setEnabled(false);

      mResetButton = new JButton("Reset");
      // mButtons maps buttons to actions - reset is always "-1"
      mButtons.put(mResetButton, new Integer(-1));
      mResetButton.addActionListener(this);
  
      mStateLabel = new JLabel("(uninitialised model)");
   }

   public void newModel() {
      mModel = ModelJUnitGUI.getModel();
      removeAll();
      mButtons.clear();
      buildGUI();
      mButtons.put(mResetButton, new Integer(-1));

      mModel.doReset();
      mActionHistory.addElement("*** RESET ***");
   }

   /** Construct an animator GUI based on the current state of the Model structure. 
    *
    * Call this only once, followed by multiple calls to updateGUI()
    *
    * Requires a non-null Model returned by ModelJUnitGUI.getModel()
    */
   public void buildGUI() {
      GridLayout gl = new GridLayout(0,2);

      setLayout(gl);

      // Build a map from action numbers to buttons representing those actions, suitable for display.

      if(mModel == null) return;

      BitSet enabledActions = mModel.enabledGuards();
      for(int i = 0; i<enabledActions.length(); i++) {
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
         mActionHistory.addElement("*** RESET ***");
         updateGUI();
         return;
      }

      mModel.doAction(i);
      mActionHistory.addElement(mModel.getActionName(i));
    
      updateGUI();
   }

}



