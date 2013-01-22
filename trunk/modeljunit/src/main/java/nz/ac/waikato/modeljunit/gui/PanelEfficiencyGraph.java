package nz.ac.waikato.modeljunit.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;

import nz.ac.waikato.modeljunit.Action;
import nz.ac.waikato.modeljunit.GreedyTester;
import nz.ac.waikato.modeljunit.LookaheadTester;
import nz.ac.waikato.modeljunit.Model;
import nz.ac.waikato.modeljunit.QuickTester;
import nz.ac.waikato.modeljunit.RandomTester;
import nz.ac.waikato.modeljunit.Tester;
import nz.ac.waikato.modeljunit.coverage.CoverageHistory;
import nz.ac.waikato.modeljunit.coverage.CoverageMetric;
import nz.ac.waikato.modeljunit.coverage.TransitionCoverage;

/**
 * Panel for displaying the efficiency graph for Transition Coverage History of RandomTester, GreedyTester,
 * LookaheadTester, QuickTester
 */
public class PanelEfficiencyGraph extends PanelAbstract {

    private static final long serialVersionUID = -6413684642746369029L;

    // Minimum dimension
    private static final Dimension MIN_COORD_AXIS = new Dimension(760, 300);

    //height of pixels
    private static final int TOP_SPACE = 30;

    private static final int BOTTOM_SPACE = 30;

    private static final int LEFT_SPACE = 50;

    private static final int RIGHT_SPACE = 30;

    // Coordinate arrow parameters
    private static final int ARROW_HALF_WIDTH = 6;

    private static final int ARROW_LENGTH = 10;

    // Scale numbers
    private static int m_nScaleNumber = 20;

    private static final int SCALE_LARGE_LENGTH = 6;

    private static final int SCALE_SHORT_LENGTH = 3;

    //The length of the test
    private static final int WALK_LENGTH = 1000;

    private int[] m_arrayStages = null;

    // String position
    private static final int STRING_Y_AXIS_LEFT_PADDING = 16;

    // Points to draw
    // Random tester coverage array
    private List<Integer> mRandomTesterCoverage;

    // Greedy tester coverage array
    private List<Integer> mGreedyTesterCoverage;

    // Lookahead tester coverage array
    private List<Integer> mLookaheadTesterCoverage;

    // Quick tester coverage array
    private List<Integer> mQuickTesterCoverage;

    // The maximum coverage
    private int mMaximum;

    // Panel object
    private static PanelEfficiencyGraph m_panel;

    private JProgressBar mProgress;

    public static PanelEfficiencyGraph getInstance() {
        if (m_panel == null)
            m_panel = new PanelEfficiencyGraph();
        return m_panel;
    }

    private PanelEfficiencyGraph() {
        this.setBackground(Color.WHITE);
        this.setDoubleBuffered(true);
        mProgress = new JProgressBar(0, 100);
        mProgress.setStringPainted(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension size = getSize();
        // Limit the minimum size of coordinate system
        if (size.height < MIN_COORD_AXIS.height)
            size.height = MIN_COORD_AXIS.height;
        if (size.width < MIN_COORD_AXIS.width)
            size.width = MIN_COORD_AXIS.width;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Draw coordinate
        g2.setPaint(Color.BLACK);
        final int AXIS_HEIGHT = size.height - TOP_SPACE - BOTTOM_SPACE;
        final int AXIS_WIDTH = size.width - RIGHT_SPACE - LEFT_SPACE;
        // Draw axis Y
        g2.draw(new Line2D.Float(LEFT_SPACE, TOP_SPACE, LEFT_SPACE, AXIS_HEIGHT));
        // Draw axis X
        g2.draw(new Line2D.Float(LEFT_SPACE, AXIS_HEIGHT, AXIS_WIDTH, AXIS_HEIGHT));
        // Draw coordinate arrows
        // Y coordinate arrow
        g2.draw(new Line2D.Float(LEFT_SPACE, TOP_SPACE, LEFT_SPACE + ARROW_HALF_WIDTH, TOP_SPACE + ARROW_LENGTH));
        g2.draw(new Line2D.Float(LEFT_SPACE, TOP_SPACE, LEFT_SPACE - ARROW_HALF_WIDTH, TOP_SPACE + ARROW_LENGTH));
        // X coordinate arrow
        g2.draw(new Line2D.Float(AXIS_WIDTH, AXIS_HEIGHT, AXIS_WIDTH - ARROW_LENGTH, AXIS_HEIGHT + ARROW_HALF_WIDTH));
        g2.draw(new Line2D.Float(AXIS_WIDTH, AXIS_HEIGHT, AXIS_WIDTH - ARROW_LENGTH, AXIS_HEIGHT - ARROW_HALF_WIDTH));
        // Draw scale
        // Scale Y
        final int nInternalSpaceY = (AXIS_HEIGHT - ARROW_LENGTH) / (PanelEfficiencyGraph.m_nScaleNumber + 2);
        for (int i = 1; i < PanelEfficiencyGraph.m_nScaleNumber + 1; i++) {
            int nScalePos = AXIS_HEIGHT - i * nInternalSpaceY;
            if (i % 2 == 0) {
                g2.draw(new Line2D.Float(LEFT_SPACE, nScalePos, LEFT_SPACE + SCALE_LARGE_LENGTH, nScalePos));
                g2.drawString(Integer.toString(i / 2 * 10) + "%", STRING_Y_AXIS_LEFT_PADDING, nScalePos);
            } else
                g2.draw(new Line2D.Float(LEFT_SPACE, nScalePos, LEFT_SPACE + SCALE_SHORT_LENGTH, nScalePos));
        }
        // Scale X
        final int nWalkLength = WALK_LENGTH;

        if (this.m_arrayStages == null)
            computeStages(nWalkLength);

        final int internalSpaceX = (AXIS_WIDTH - ARROW_LENGTH) / (PanelEfficiencyGraph.m_nScaleNumber + 1);
        int[] arrayScaleXPos = new int[m_arrayStages.length];
        int[] arraySScaleYPos = new int[m_arrayStages.length];
        int[] arrayTScaleYPos = new int[m_arrayStages.length];
        int[] arrayTPScaleYPos = new int[m_arrayStages.length];
        int[] arrayAScaleYPos = new int[m_arrayStages.length];
        final int AXIS_Y_LENGTH = AXIS_HEIGHT - TOP_SPACE - ARROW_LENGTH;
        for (int i = 0; i < m_arrayStages.length; i++) {
            int nScaleposY = AXIS_HEIGHT;
            int nScaleposX = LEFT_SPACE + (i + 1) * internalSpaceX;
            if (i % 2 != 0)
                g2.draw(new Line2D.Float(nScaleposX, nScaleposY - SCALE_LARGE_LENGTH, nScaleposX, nScaleposY));
            else
                g2.draw(new Line2D.Float(nScaleposX, nScaleposY - SCALE_SHORT_LENGTH, nScaleposX, nScaleposY));
            arrayScaleXPos[i] = nScaleposX;
            if (isCoverageDrawable(i)) {
                arraySScaleYPos[i] = (int) ((double) AXIS_HEIGHT - ((double) AXIS_Y_LENGTH
                                * mRandomTesterCoverage.get(i) / mMaximum));
                arrayTScaleYPos[i] = (int) ((double) AXIS_HEIGHT - ((double) AXIS_Y_LENGTH
                                * mGreedyTesterCoverage.get(i) / mMaximum));
                arrayTPScaleYPos[i] = (int) ((double) AXIS_HEIGHT - ((double) AXIS_Y_LENGTH
                                * mLookaheadTesterCoverage.get(i) / mMaximum));
                arrayAScaleYPos[i] = (int) ((double) AXIS_HEIGHT - ((double) AXIS_Y_LENGTH
                                * mQuickTesterCoverage.get(i) / mMaximum));
            }
            // Draw scale text
            g2.drawString(Integer.toString(m_arrayStages[i]), nScaleposX, nScaleposY + 16);
        }

        //System.out.println(isCoverageDrawable(i));
        if (isCoverageDrawable(1)) {
            arraySScaleYPos[0] = arrayTScaleYPos[0] = arrayTPScaleYPos[0] = arrayAScaleYPos[0] = AXIS_HEIGHT;
            arrayScaleXPos[0] = LEFT_SPACE;
            // Draw line chart 

            // Random tester coverage history
            g2.setColor(Color.BLACK);
            g2.drawPolyline(arrayScaleXPos, arraySScaleYPos, mRandomTesterCoverage.size());

            // Greedy tester coverage history
            g2.setColor(Color.ORANGE);
            g2.drawPolyline(arrayScaleXPos, arrayTScaleYPos, mGreedyTesterCoverage.size());

            // Lookahead tester coverage history
            g2.setColor(Color.CYAN);
            g2.drawPolyline(arrayScaleXPos, arrayTPScaleYPos, mLookaheadTesterCoverage.size());

            // Quick tester coverage history
            g2.setColor(Color.MAGENTA);
            g2.drawPolyline(arrayScaleXPos, arrayAScaleYPos, mQuickTesterCoverage.size());
        }
    }

    public int[] computeStages(final int nWalkLength) {
        int nScaleNum = 0;
        // To set number scales for x coordinate according to random walk length
        double dScaleSpan = 1f;
        if (nWalkLength > m_nScaleNumber) {
            nScaleNum = PanelEfficiencyGraph.m_nScaleNumber;
            dScaleSpan = (double) nWalkLength / (m_nScaleNumber);
        } else
            nScaleNum = nWalkLength;
        m_arrayStages = new int[nScaleNum];
        double j = 0;
        for (int i = 0; i < m_arrayStages.length; i++) {
            j += dScaleSpan;
            m_arrayStages[i] = (int) j;
            // System.out.print(+m_arrayStages[i]+",");
        }
        // System.out.println();
        return m_arrayStages;
    }

    private boolean isCoverageDrawable(int i) {
        return (mRandomTesterCoverage != null && mRandomTesterCoverage.size() > 0 && mGreedyTesterCoverage != null
                        && mGreedyTesterCoverage.size() > 0 && mLookaheadTesterCoverage != null
                        && mLookaheadTesterCoverage.size() > 0 && mQuickTesterCoverage != null
                        && mQuickTesterCoverage.size() > 0 && mRandomTesterCoverage.size() > i
                        && mGreedyTesterCoverage.size() > i && mLookaheadTesterCoverage.size() > i && mQuickTesterCoverage
                        .size() > i) ? true : false;
    }

    public void clearCoverages() {
        mRandomTesterCoverage = new ArrayList<Integer>();
        mGreedyTesterCoverage = new ArrayList<Integer>();
        mLookaheadTesterCoverage = new ArrayList<Integer>();
        mQuickTesterCoverage = new ArrayList<Integer>();
    }

    public JProgressBar getProgress() {
        return mProgress;
    }

    public void setProgress(int complete, int target) {
        double progress = (((double) complete) / ((double) target)) * 100.0;
        mProgress.setValue((int) progress);
        mProgress.setString("(" + complete + "/" + target + ")");
    }

    public void redrawGraph() {
        this.paintImmediately(0, 0, this.getWidth(), this.getHeight());
    }

    public void newModel() {
        clearCoverages();
        redrawGraph();
    }

    public void runClass() {
        CoverageHistory[] coverage = new CoverageHistory[4];
        coverage[0] = new CoverageHistory(new TransitionCoverage(), 1);
        coverage[1] = new CoverageHistory(new TransitionCoverage(), 1);
        coverage[2] = new CoverageHistory(new TransitionCoverage(), 1);
        coverage[3] = new CoverageHistory(new TransitionCoverage(), 1);

        Tester randomTester = constructRandomTester(coverage[0]);
        Tester greedyTester = constructGreedyTester(coverage[1]);
        Tester lookaheadTester = constructLookaheadTester(coverage[2]);
        Tester quickTester = constructQuickTester(coverage[3]);

        setMaximumCoverage();

        // Run test several times to draw line chart
        int[] stages = computeStages(WALK_LENGTH);
        for (int i = 0; i < stages.length; i++) {
            randomTester.generate(stages[i]);
            greedyTester.generate(stages[i]);
            lookaheadTester.generate(stages[i]);
            quickTester.generate(stages[i]);

            System.out.println("Progress: " + stages[i] + "/" + WALK_LENGTH);
            setProgress(stages[i], WALK_LENGTH);
            // Update the line chart and repaint
            mRandomTesterCoverage.add((int) coverage[0].getCoverage());
            mGreedyTesterCoverage.add((int) coverage[1].getCoverage());
            mLookaheadTesterCoverage.add((int) coverage[2].getCoverage());
            mQuickTesterCoverage.add((int) coverage[3].getCoverage());
            redrawGraph();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        writeHistoryToFile(coverage);
    }

    public Tester constructRandomTester(CoverageMetric metric) {
        RandomTester tester = new RandomTester(loadModelClass());
        tester.addCoverageMetric(metric);
        return tester;
    }

    public Tester constructGreedyTester(CoverageMetric metric) {
        GreedyTester tester = new GreedyTester(loadModelClass());
        tester.addCoverageMetric(metric);
        return tester;
    }

    public Tester constructLookaheadTester(CoverageMetric metric) {
        LookaheadTester tester = new LookaheadTester(loadModelClass());
        tester.addCoverageMetric(metric);
        //    tester.setDepth(100);
        tester.setMaxLength(WALK_LENGTH);
        return tester;
    }

    public Tester constructQuickTester(CoverageMetric metric) {
        QuickTester tester = new QuickTester(loadModelClass());
        tester.addCoverageMetric(metric);
        return tester;
    }

    private Model loadModelClass() {
        TestExeModel.reset();

        if (TestExeModel.loadModelClassFromFile()) {
            System.out.println("SUCCESS: loaded model " + Parameter.getClassName());
        } else {
            throw new RuntimeException("Error Loading Model - No @Action annotations!");
        }
        return new Model(TestExeModel.getModelObject());
    }

    private void setMaximumCoverage() {
        GreedyTester tester = new GreedyTester(loadModelClass());
        CoverageMetric metric = new TransitionCoverage();
        tester.addCoverageMetric(metric);
        int i = 1;
        while (metric.getMaximum() == -1) {
            tester.generate(i);
            i++;
        }
        mMaximum = metric.getMaximum();
    }

    public void writeHistoryToFile(CoverageHistory[] coverage) {
        try {
            File f = new File("EfficiencyOutput.csv");
            PrintWriter w = new PrintWriter(new FileOutputStream(f));

            w.println(Parameter.getClassName() + " Transition Coverage History");
            w.println();
            w.println("Random Tester: ");
            w.println(coverage[0].toCSV());
            w.println("Greedy Tester: ");
            w.println(coverage[1].toCSV());
            w.println("Lookahead Tester: ");
            w.println(coverage[2].toCSV());
            w.println("Quick Tester: ");
            w.println(coverage[3].toCSV());

            w.close();
        } catch (Exception ex) {
            System.err.println("IO error occurance");
            ex.printStackTrace();
        }
    }
}
