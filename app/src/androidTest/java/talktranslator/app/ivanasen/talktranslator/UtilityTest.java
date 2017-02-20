package talktranslator.app.ivanasen.talktranslator;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import talktranslator.app.ivanasen.talktranslator.utils.Utility;

/**
 * Created by ivan on 2/15/2017.
 */
@RunWith(AndroidJUnit4.class)
public class UtilityTest {

    @Test
    public void testBulgarianTextForReadingConversion() {
        assertEquals("Нощ",
                Utility.editBulgarianTextForRussianReading("Нощ"), "Нощт");
        assertEquals("Гъба",
                Utility.editBulgarianTextForRussianReading("Гъба"), "Гэба");

    }
}
