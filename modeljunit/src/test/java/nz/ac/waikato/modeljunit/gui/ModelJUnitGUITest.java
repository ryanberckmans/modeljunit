package nz.ac.waikato.modeljunit.gui;

import java.awt.Component;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.uispec4j.Trigger;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.finder.StringMatcher;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.utils.ComponentUtils;

import edu.uci.ics.jung.graph.Graph;

/**
 * ModelJUnitGUITest for testing a few of the GUI use cases.
 * 
 * Note that this uses the UISpec4J library, which currently requires Java 1.6.
 * So these tests may fail if run with Java 1.7.
 *
 * @author Celia Lai <wl31@waikato.ac.nz>
 */
public class ModelJUnitGUITest extends UISpecTestCase {
    private ModelJUnitGUI modelJUnitGUI;
    private Window window;

    static {
        // Should be done first to ensure that dialogs are handled by UISpec4J.
        UISpec4J.init();
    }

    @Before
    public void setUp() {
        window = WindowInterceptor.run(new Trigger() {
            public void run() {
                modelJUnitGUI = new ModelJUnitGUI(false);
            }
        });
        assertTrue(window.titleEquals("ModelJUnit - Untitled*"));
    }

    @After
    public void tearDown() {
        window = null;
        modelJUnitGUI = null;
    }

    @Test
    public void testInitialControlPanel() {
        String[] labelPositions = new String[]{"AUTO","CNTR","N","NE","E","SE","S","SW","W","NW"};
        String[] layoutTypes = new String[]{"FR","CIRCLE","ISOM","KK","SPRING"};

        /* Show or hide labels */
        assertTrue(window.getCheckBox(displayedNameIdentity("Show vertex labels")).isSelected());
        window.getComboBox("vertLabelPosComboBox").contentEquals(labelPositions);
        assertFalse(window.getCheckBox(displayedNameIdentity("Show edge labels")).isSelected());
        assertTrue(window.getCheckBox(displayedNameIdentity("Show unexplored states/actions")).isSelected());

        /* Layout type selection */
        window.getComboBox("layoutTypeComboBox").contentEquals(layoutTypes);

        /* Merge buttons */
        assertNotNull(window.getButton(displayedNameIdentity("Merge states")));
        assertNotNull(window.getButton(displayedNameIdentity("Expand states")));
        assertNotNull(window.getButton(displayedNameIdentity("Reset")));
        assertNotNull(window.getButton(displayedNameIdentity("Save as image")));

        /* Animation controls */
        assertTrue(window.getCheckBox(displayedNameIdentity("Show animation")).isSelected());
        assertNotNull(window.getToggleButton(displayedNameIdentity("Pause")));
        assertNotNull(window.getButton(displayedNameIdentity("Stop")));
        window.getSlider().positionEquals("1000");
        window.getSlider().relativePositionEquals(100);
    }

    @Test
    public void testSimpleSetRandomTester() {
        /* Choose the SimpleSet model from menu */
        window.getMenuBar().getMenu("Run").getSubMenu("SimpleSet").click();
        assertTrue(window.titleEquals("ModelJUnit: nz.ac.waikato.modeljunit.examples.SimpleSet"));

        /* Choose the Generate Tests menu item to run the Random Test */
        window.getMenuBar().getMenu("Run").getSubMenu("Generate Tests").click();
        assertTrue(window.getTree().contentEquals(
                        "All test sequences\n" + 
                                        "  Test sequence 1\n" +
                                        "    (FF, addS2, FT)\n" +
                                        "    (FT, addS1, TT)\n" +
                                        "    (TT, removeS1, FT)\n" +
                                        "    (FT, addS2, FT)\n" +
                                        "    (FT, addS2, FT)\n" +
                                        "    (FT, addS1, TT)\n" +
                                        "    (TT, addS2, TT)\n" +
                                        "    (TT, addS2, TT)\n" +
                                        "    (TT, addS2, TT)\n" +
                                        "    (TT, addS2, TT)"
                        ));

        /* Verify the number of vertices and edges  */
        Graph<Object, Object> graph = modelJUnitGUI.getVisualisation().getJUNGHelper().getGraph();
        assertEquals(4, graph.getVertexCount());
        assertEquals(16, graph.getEdgeCount());
    }

    @Test
    public void testViewTests() {
        window.getMenuBar().getMenu("Run").getSubMenu("SimpleSet").click();
        WindowInterceptor.init(window.getMenuBar().getMenu("View").getSubMenu("View Tests Window").triggerClick())
        .process(new WindowHandler() {
            public Trigger process(Window configurationDialog) {
                assertTrue(configurationDialog.titleEquals("Results - ModelJUnit"));
                assertTrue(configurationDialog.getTextBox("resultsOutput").textEquals(
                                "done (FF, addS2, FT)\n" + 
                                                "done (FT, addS1, TT)\n" +
                                                "done (TT, removeS1, FT)\n" +
                                                "done (FT, addS2, FT)\n" +
                                                "done (FT, addS2, FT)\n" +
                                                "done (FT, addS1, TT)\n" +
                                                "done (TT, addS2, TT)\n" +
                                                "done (TT, addS2, TT)\n" +
                                                "done (TT, addS2, TT)\n" +
                                                "done (TT, addS2, TT)\n" + 
                                                "State coverage = 2/4\n" + 
                                                "Transition coverage = 5/16\n"
                                ));

                // no action can be performed after the results window is shown,
                // so return a new trigger with empty run() implementation.
                return new Trigger() {
                    @Override
                    public void run() throws Exception {}
                };
            }
        })
        .run();
    }

    @Test
    public void testAnimateModel() {
        window.getMenuBar().getMenu("Run").getSubMenu("SimpleSet").click();
        WindowInterceptor.init(window.getMenuBar().getMenu("View").getSubMenu("Animate Window").triggerClick())
        .process(new WindowHandler() {
            public Trigger process(Window configurationDialog) {
                assertTrue(configurationDialog.titleEquals("Animator - ModelJUnit"));

                assertTrue(configurationDialog.getButton(displayedNameIdentity("addS1")).isEnabled());
                assertTrue(configurationDialog.getButton(displayedNameIdentity("addS2")).isEnabled());
                assertTrue(configurationDialog.getButton(displayedNameIdentity("removeS1")).isEnabled());
                assertTrue(configurationDialog.getButton(displayedNameIdentity("removeS2")).isEnabled());

                configurationDialog.getListBox().contentEquals("*** RESET *** (FF)");
                configurationDialog.getButton(displayedNameIdentity("addS1")).click();
                configurationDialog.getListBox().contentEquals(
                                "*** RESET *** (FF)\n" +
                                                "addS1(TF)"
                                );
                configurationDialog.getButton(displayedNameIdentity("addS2")).click();
                configurationDialog.getListBox().contentEquals(
                                "*** RESET *** (FF)\n" +
                                                "addS1(TF)\n" +
                                                "addS2(TT)"
                                );
                configurationDialog.getButton(displayedNameIdentity("removeS1")).click();
                configurationDialog.getListBox().contentEquals(
                                "*** RESET *** (FF)" +
                                                "addS1(TF)\n" +
                                                "addS2(TT)\n" +
                                                "removeS1(FT)"
                                );
                configurationDialog.getButton(displayedNameIdentity("removeS2")).click();
                configurationDialog.getListBox().contentEquals(
                                "*** RESET *** (FF)" +
                                                "addS1(TF)\n" +
                                                "addS2(TT)\n" +
                                                "removeS1(FT)\n" +
                                                "removeS2(FF)"
                                );

                return configurationDialog.getButton(displayedNameIdentity("reset")).triggerClick();
            }
        })
        .run();
    }

    @Test
    public void testTestConfigurationWithGreedyTester() {
        window.getMenuBar().getMenu("Run").getSubMenu("SimpleSet").click();
        WindowInterceptor.init(window.getMenuBar().getMenu("Run").getSubMenu("Test Configuration...").triggerClick())
        .process(new WindowHandler() {
            public Trigger process(Window configurationDialog) {
                assertTrue(configurationDialog.titleEquals("Edit Configuration"));
                /* Set the test length to 20 */
                configurationDialog.getTextBox("totalTestLength").setText("20");
                /* Choose Greedy Walk algorithm*/
                configurationDialog.getComboBox().select("Greedy Walk");
                return configurationDialog.getButton("OK").triggerClick();
            }
        })
        .run();

        /* Choose the Generate Tests menu item to run the Greedy Test */
        window.getMenuBar().getMenu("Run").getSubMenu("Generate Tests").click();
        assertTrue(window.getTree().contentEquals(
                        "All test sequences\n" + 
                                        "  Test sequence 1\n" +
                                        "    (FF, addS2, FT)\n" +
                                        "    (FT, addS1, TT)\n" +
                                        "    (TT, removeS1, FT)\n" +
                                        "    (FT, removeS2, FF)\n" +
                                        "    (FF, removeS2, FF)\n" +
                                        "    (FF, addS1, TF)\n" +
                                        "    (TF, addS2, TT)\n" +
                                        "    (TT, addS2, TT)\n" +
                                        "    (TT, addS1, TT)\n" +
                                        "    (TT, removeS2, TF)\n" +
                                        "    (TF, removeS1, FF)\n" +
                                        "    (FF, removeS1, FF)\n" +
                                        "    (FF, removeS1, FF)\n" +
                                        "    (FF, addS1, TF)\n" +
                                        "    (TF, removeS2, TF)\n" +
                                        "    (TF, addS1, TF)\n" +
                                        "    (TF, removeS1, FF)\n" +
                                        "  Test sequence 2 (Random reset)\n" +
                                        "    (FF, addS2, FT)\n" +
                                        "    (FT, addS2, FT)"
                        ));
    }

    @Test
    public void testViewCoverageWindow() {
        window.getMenuBar().getMenu("Run").getSubMenu("SimpleSet").click();
        WindowInterceptor.init(window.getMenuBar().getMenu("View").getSubMenu("Coverage Window").triggerClick())
        .process(new WindowHandler() {
            public Trigger process(Window configurationDialog) {
                assertTrue(configurationDialog.titleEquals("Coverage - ModelJUnit"));

                // no action can be performed after the coverage graphs window is shown,
                // so return a new trigger with empty run() implementation.
                return new Trigger() {
                    @Override
                    public void run() throws Exception {}
                };
            }
        })
        .run();
    }

    /**
     * Matches components whose displayed name is exactly the same as the reference.
     */
    public static ComponentMatcher displayedNameIdentity(String reference) {
        return new DisplayedNameComponentMatcher(StringMatcher.identity(reference));
    }

    /**
     * This class implements the {@link ComponentMatcher} that returns True if the display name
     * of the component matches the string matcher.
     */
    private static class DisplayedNameComponentMatcher implements ComponentMatcher {
        private final StringMatcher stringMatcher;

        public DisplayedNameComponentMatcher(StringMatcher stringMatcher) {
            this.stringMatcher = stringMatcher;
        }

        public boolean matches(Component component) {
            if (!ComponentUtils.hasDisplayedName(component.getClass())) {
                return false;
            }
            String displayedName = ComponentUtils.getDisplayedName(component);
            return stringMatcher.matches(displayedName);
        }
    }
}
