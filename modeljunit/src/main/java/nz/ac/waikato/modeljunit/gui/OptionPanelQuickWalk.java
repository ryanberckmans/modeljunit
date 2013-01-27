package nz.ac.waikato.modeljunit.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.lang.reflect.Constructor;

import javax.swing.JLabel;
import javax.swing.JTextField;

import nz.ac.waikato.modeljunit.QuickTester;

public class OptionPanelQuickWalk extends OptionPanelAdapter implements IAlgorithmParameter {

    private static final long serialVersionUID = -1996525954175847354L;

    private JTextField m_quickWalkMaxDepth;

    private int m_defaultMaxDepth = 10;

    private JTextField m_resetProb;

    private double m_defaultResetProb = 0.05;

    public OptionPanelQuickWalk(String name, String explain, String imgPath) {
        super(name, explain, imgPath);

        this.setLayout(new GridLayout(2, 2));
        add(new JLabel("Maximum Depth:"));
        m_quickWalkMaxDepth = new JTextField(Integer.toString(m_defaultMaxDepth));
        m_quickWalkMaxDepth.setPreferredSize(new Dimension(100, 20));
        m_quickWalkMaxDepth.setMaximumSize(new Dimension(300, 30));
        add(m_quickWalkMaxDepth);
        add(new JLabel("Reset probability:"));
        m_resetProb = new JTextField(Double.toString(m_defaultResetProb));
        m_resetProb.setPreferredSize(new Dimension(100, 20));
        m_resetProb.setMaximumSize(new Dimension(300, 30));
        add(m_resetProb);
    }

    /**
     * Converts a string into an integer value. Returns default quick walk max depth if the string is not legal.
     * 
     * @param str
     *            A string to convert
     * @return The parameter value, or m_defaultMaxDepth on error.
     */
    private int getIntValue(String str) {
        int value = 0;
        try {
            value = Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            // TODO: report errors to the user somehow/somewhere
            value = m_defaultMaxDepth;
        }
        if (value < 1) {
            value = m_defaultMaxDepth;
        }
        return value;
    }

    /**
     * Converts a string into a double value. Returns default reset probability if the string is not legal.
     * 
     * @param str
     *            A string to convert
     * @return The parameter value, or m_defaultResetProb on error.
     */
    private double getDoubleValue(String str) {
        double value = 0;
        try {
            value = Double.parseDouble(str);
        } catch (NumberFormatException ex) {
            // TODO: report errors to the user somehow/somewhere
            value = m_defaultResetProb;
        }
        if (value < 1) {
            value = m_defaultResetProb;
        }
        return value;
    }

    @Override
    public String generateCode(Project project) {
        StringBuffer result = new StringBuffer();

        // Initialize test model
        result.append(Indentation.indent(project.getClassName() + " model = new " + project.getClassName() + "();"));
        result.append(Indentation.indent("QuickTester tester = new QuickTester(model);"));

        // Calculate the Quick Walk max depth
        int depth = getIntValue(m_quickWalkMaxDepth.getText());
        if (depth >= 1 && depth != m_defaultMaxDepth) {
            result.append(Indentation.indent("tester.setDepth(" + depth + ");"));
        }

        // Calculate the reset probability
        double resetProb = getDoubleValue(m_resetProb.getText());
        if (resetProb >= 1.0 && resetProb != m_defaultResetProb) {
            result.append(Indentation.indent("tester.setMaxLength(" + resetProb + ");"));
        }

        return result.toString();
    }

    @Override
    public void initialize(Project project, int idx) {
        try {
            // Initialize model test case by using the loaded model
            Class<?> testerClass = Class.forName("nz.ac.waikato.modeljunit.QuickTester");
            Constructor<?> con = testerClass.getConstructor(new Class[] { Class
                            .forName("nz.ac.waikato.modeljunit.FsmModel") });
            m_tester[idx] = (QuickTester) con.newInstance(new Object[] { project.getModelObject() });
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    @Override
    public String generateImportLab() {
        StringBuffer result = new StringBuffer();
        result.append(Indentation.indent("import nz.ac.waikato.modeljunit.QuickTester;"));
        return result.toString();
    }

    @Override
    public void runAlgorithm(int idx) {
        // Set the Quick Walk max depth
        int depth = getIntValue(m_quickWalkMaxDepth.getText());
        if (depth >= 1 && depth != m_defaultMaxDepth) {
            ((QuickTester) m_tester[idx]).setMaxDepth(depth);
        }

        // Set the reset probability
        double resetProb = getDoubleValue(m_resetProb.getText());
        if (resetProb >= 1 && resetProb != m_defaultResetProb) {
            ((QuickTester) m_tester[idx]).setResetProbability(resetProb);
        }
    }
}
