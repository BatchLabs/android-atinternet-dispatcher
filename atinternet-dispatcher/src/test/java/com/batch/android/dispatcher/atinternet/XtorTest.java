package com.batch.android.dispatcher.atinternet;

import android.os.Build;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Test the AT Internet XTOR parser
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)
public class XtorTest {

    public void compareXtor(String[] expectedParts, Xtor xtor) {
        Assert.assertEquals(expectedParts.length, xtor.getParts().length);
        for (int i = 0; i < expectedParts.length; ++i) {
            Assert.assertEquals(expectedParts[i], xtor.getParts()[i]);
            Assert.assertEquals(expectedParts[i], xtor.getPart(i));
        }
    }

    @Test
    public void testValidXtor() {
        String xtorTag = "EPR-50-[BA_notification_2019_09_24]-20190924-[WEB_BA_notification]";
        Xtor xtor = Xtor.parse(xtorTag);

        String[] expectedParts = new String[]{"EPR", "50", "[BA_notification_2019_09_24]", "20190924", "[WEB_BA_notification]"};

        compareXtor(expectedParts, xtor);
        Assert.assertTrue(xtor.isValidXtor());
    }

    @Test
    public void testValidXtor2() {
        String xtorTag = "CS1-[mylabeltest]-test-15[sef]";
        Xtor xtor = Xtor.parse(xtorTag);

        String[] expectedParts = new String[]{"CS1", "[mylabeltest]", "test", "15[sef]"};

        compareXtor(expectedParts, xtor);
        Assert.assertTrue(xtor.isValidXtor());
    }

    @Test
    public void testValidPartialXtor() {
        String xtorTag = "EPR-2413";
        Xtor xtor = Xtor.parse(xtorTag);

        String[] expectedParts = new String[]{"EPR", "2413"};

        compareXtor(expectedParts, xtor);
        Assert.assertTrue(xtor.isValidXtor());
    }

    @Test
    public void testValidXtorWithEmptyLabel() {
        String xtorTag = "EPR-50-[BA-notification-2019-09-23]-20190923-[WEB_BA_notification]-[]-[]-";
        Xtor xtor = Xtor.parse(xtorTag);

        String[] expectedParts = new String[]{"EPR", "50", "[BA-notification-2019-09-23]", "20190923", "[WEB_BA_notification]", "[]", "[]"};

        compareXtor(expectedParts, xtor);
        Assert.assertTrue(xtor.isValidXtor());
    }

    @Test
    public void testInvalidXtor() {
        String xtorTag = "---";
        Xtor xtor = Xtor.parse(xtorTag);

        String[] expectedParts = new String[]{"", "", ""};

        compareXtor(expectedParts, xtor);
        Assert.assertFalse(xtor.isValidXtor());
    }

    @Test
    public void testInvalidXtor2() {
        String xtorTag = "salut salut";
        Xtor xtor = Xtor.parse(xtorTag);

        String[] expectedParts = new String[]{"salut salut"};

        compareXtor(expectedParts, xtor);
        Assert.assertFalse(xtor.isValidXtor());
    }

    @Test
    public void testInvalidXtor3() {
        String xtorTag = "-test-15[sefsef]--";
        Xtor xtor = Xtor.parse(xtorTag);

        String[] expectedParts = new String[]{"", "test", "15[sefsef]", ""};

        compareXtor(expectedParts, xtor);
        Assert.assertFalse(xtor.isValidXtor());
    }

}
