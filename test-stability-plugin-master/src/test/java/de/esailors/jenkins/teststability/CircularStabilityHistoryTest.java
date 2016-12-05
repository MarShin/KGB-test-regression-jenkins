package de.esailors.jenkins.teststability;

import org.junit.Test;

import java.util.logging.*;

import org.junit.Assert;
import de.esailors.jenkins.teststability.StabilityTestData.Result;

public class CircularStabilityHistoryTest {

    private static final Logger myLog = Logger.getLogger(CircularStabilityHistoryTest.class.getName());

    private CircularStabilityHistory init() {
        Result passed1 = new Result(1, true);
        Result passed2 = new Result(2, true);
        Result passed3 = new Result(3, true);
        Result failed3 = new Result(3, false);

        CircularStabilityHistory childHistory1 = new CircularStabilityHistory(5);
        CircularStabilityHistory childHistory2 = new CircularStabilityHistory(5);
        CircularStabilityHistory childHistory3 = new CircularStabilityHistory(5);

        CircularStabilityHistory parentHistory = new CircularStabilityHistory(5);

        childHistory1.add(passed1);
        childHistory1.add(passed2);
        childHistory1.add(failed3);
        childHistory1.setName("Filter this test");

        childHistory2.add(passed1);
        childHistory2.add(passed2);
        childHistory2.add(passed3);
        childHistory2.setName("Test2");

        childHistory3.add(passed1);
        childHistory3.add(passed2);
        childHistory3.add(passed3);
        childHistory3.setName("Test3");

        parentHistory.add(passed1);
        parentHistory.add(passed2);
        parentHistory.add(failed3);
        parentHistory.setName("Root History");

        parentHistory.addChild(childHistory1);
        parentHistory.addChild(childHistory2);
        parentHistory.addChild(childHistory3);

        return parentHistory;
    }

    @Test
    public void updateChildrenTest() {
        CircularStabilityHistory parentHistory = init();

        Result[] unfilteredResults = parentHistory.getData();

        Assert.assertFalse(unfilteredResults[unfilteredResults.length - 1].passed);

        parentHistory.addToHiddenTests("Filter this test");
        parentHistory.updateResultForChildren();

        Result[] filteredResults = parentHistory.getData();
        Assert.assertTrue(filteredResults[filteredResults.length - 1].passed);
    }

    @Test
    public void checkStabilityIsStillHundredPercent() {
        CircularStabilityHistory parentHistory = init();

        parentHistory.addToHiddenTests("Filter this test");
        parentHistory.updateResultForChildren();

        Assert.assertEquals(100, parentHistory.getStability());
    }
}
