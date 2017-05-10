package talktranslator.app.ivanasen.talktranslator;


import android.content.ContextWrapper;
import android.support.test.runner.AndroidJUnit4;
import android.test.mock.MockContext;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import talktranslator.app.ivanasen.talktranslator.utils.InterviewMaker;

/**
 * Created by ivan on 3/4/2017.
 */
@RunWith(AndroidJUnit4.class)
public class InterviewMakerTest {
    @Test
    public void test() {
        InterviewMaker maker = new InterviewMaker(new MockContext());

        String s = "Hello";
        maker.addInterviewerText(s);
        maker.addIntervieweeText(s);
        String exp = s + InterviewMaker.INTERVIEW_TEXT_TO_SPEECH_PAUSE + s;
        String actual = maker.getCurrentInterviewText();
        assertEquals(exp, actual);
    }
}
