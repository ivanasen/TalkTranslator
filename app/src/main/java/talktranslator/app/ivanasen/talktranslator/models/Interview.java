package talktranslator.app.ivanasen.talktranslator.models;

import com.orm.SugarRecord;

/**
 * Created by ivan on 3/3/2017.
 */

public class Interview extends SugarRecord {
    private String title;
    private String text;
    private long length;

    public Interview(String title, String text, long length) {
        this.title = title;
        this.text = text;
        this.length = length;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
