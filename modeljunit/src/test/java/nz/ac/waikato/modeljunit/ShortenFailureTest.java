
package nz.ac.waikato.modeljunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;
import nz.ac.waikato.modeljunit.examples.SimpleSet;

/**
 * Test the basic methods used in the ShortenFailure shortening algorithms.
 * @author Nadin J. Wadi
 *
 */
public class ShortenFailureTest extends TestCase
{

  public static void testGetRandomNumber()
  {
    TestFailureException exception = new TestFailureException();
    exception.setSequence(new ArrayList<Transition>());
    ShortenFailure sut = new ShortenFailure(new RandomTester(new SimpleSet()),
        exception);

    assertTrue(sut.getRandomNumber(0) == 0); //modified

    for (int i = 0; i < 10; i++) //check number is within the range
    {
      assertTrue(sut.getRandomNumber(80) >= 0 && sut.getRandomNumber(80) < 80);
    }

  }

  public static void testGetRefinedSequence()
  {
    TestFailureException exception = new TestFailureException();
    exception.setSequence(new ArrayList<Transition>());
    ShortenFailure sut = new ShortenFailure(new RandomTester(new SimpleSet()),
        exception);
    List<Transition> list = new ArrayList<Transition>();
    list.add(new Transition("1", "a1", "2")); //0
    list.add(new Transition("2", "a2", "3")); //1
    list.add(new Transition("3", "a1", "3")); //2
    list.add(new Transition("3", "a2", "3")); //3
    list.add(new Transition("3", "a3", "4")); //4

    assertTrue("test1", list.size() == 5);
    List<Transition> newList = sut.getRefinedSequence(list, 2, 4);

    assertTrue("test2", newList.size() == 3);
    assertEquals(new Transition("1", "a1", "2"), newList.get(0));
    assertEquals(new Transition("2", "a2", "3"), newList.get(1));
    assertEquals(new Transition("3", "a3", "4"), newList.get(2));
  }

  public static void testAddToHashTable()
  {
    TestFailureException exception = new TestFailureException();
    exception.setSequence(new ArrayList<Transition>());
    ShortenFailure sut = new ShortenFailure(new RandomTester(new SimpleSet()),
        exception);
    HashMap<String, ArrayList<Integer>> table = new HashMap<String, ArrayList<Integer>>();
    sut.addToHashTable(table, "1", 0);
    sut.addToHashTable(table, "3", 2);
    sut.addToHashTable(table, "3", 3);

    ArrayList<Integer> listS1 = table.get("1");
    assertTrue(listS1.size() == 1 && listS1.get(0) == 0);

    ArrayList<Integer> listS3 = table.get("3");
    assertTrue(listS3.size() == 2);
    assertTrue(listS3.get(0) == 2 && listS3.get(1) == 3);
  }

  public static void testFindAllSequenceCycles()
  {
    TestFailureException exception = new TestFailureException();
    exception.setSequence(new ArrayList<Transition>());
    ShortenFailure sut = new ShortenFailure(new RandomTester(new SimpleSet()),
        exception);
    List<Transition> list = new ArrayList<Transition>();
    list.add(new Transition("1", "a1", "2")); //0
    list.add(new Transition("2", "a2", "3")); //1
    list.add(new Transition("3", "a1", "3")); //2
    list.add(new Transition("3", "a2", "3")); //3
    list.add(new Transition("3", "a3", "4")); //4

    HashMap<String, ArrayList<Integer>> table = sut.findAllSequenceCycles(list);

    ArrayList<Integer> listS1 = table.get("1");
    assertTrue(listS1.size() == 1 && listS1.get(0) == 0);

    ArrayList<Integer> listS2 = table.get("2");
    assertTrue(listS2.size() == 1 && listS2.get(0) == 1);

    ArrayList<Integer> listS3 = table.get("3");
    assertTrue(listS3.size() == 3);
    assertTrue(listS3.get(0) == 2 && listS3.get(1) == 3 && listS3.get(2) == 4);

  }

  public void testGetShortest()
  {
    TestFailureException exception = new TestFailureException();
    exception.setSequence(new ArrayList<Transition>());
    ShortenFailure sut = new ShortenFailure(new RandomTester(new SimpleSet()),
        exception);
    List<Transition> list = new ArrayList<Transition>();
    list.add(new Transition("1", "a1", "2")); //0
    list.add(new Transition("2", "a2", "3")); //1
    list.add(new Transition("3", "a1", "3")); //2
    list.add(new Transition("3", "a2", "3")); //3
    list.add(new Transition("3", "a3", "4")); //4

    List<Transition> subList = new ArrayList<Transition>();
    subList.add(new Transition("2", "a2", "3")); //refer shortest path found by the pathFinder

    List<Transition> newList = sut.getRefinedSequence(list, subList, 1, 3);

    assertTrue(newList.size() == 3);
  }

  public void testAreListsEqual()
  {
    TestFailureException exception = new TestFailureException();
    exception.setSequence(new ArrayList<Transition>());
    ShortenFailure sut = new ShortenFailure(new RandomTester(new SimpleSet()),
        exception);
    List<Transition> list1 = new ArrayList<Transition>();
    list1.add(new Transition("1", "a1", "2")); //0
    list1.add(new Transition("2", "a2", "3")); //1
    list1.add(new Transition("3", "a1", "3")); //2
    list1.add(new Transition("3", "a2", "3")); //3
    list1.add(new Transition("3", "a3", "4")); //4

    assertFalse(sut.areListsEqual(list1, null));
    assertFalse(sut.areListsEqual(null, list1));

    List<Transition> list2 = new ArrayList<Transition>();
    list2.add(new Transition("1", "a1", "2")); //0
    list2.add(new Transition("2", "a2", "3")); //1
    list2.add(new Transition("3", "a1", "3")); //2

    assertFalse(sut.areListsEqual(list1, list2));

    list2.add(new Transition("3", "a2", "3")); //3
    list2.add(new Transition("3", "a2", "4")); //4

    assertFalse(sut.areListsEqual(list1, list2));

    list2.remove(4);
    list2.add(new Transition("3", "a3", "4")); //4

    assertTrue(sut.areListsEqual(list1, list2));
  }
}
