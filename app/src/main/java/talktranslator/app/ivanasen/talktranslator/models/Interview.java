package talktranslator.app.ivanasen.talktranslator.models;

import com.orm.SugarRecord;

/**
 * Created by ivan on 3/3/2017.
 */

public class Interview extends SugarRecord {
    private String title;
    private String text;
    private String language;
    private long length;

    public Interview() {
    }

    public Interview(String title, String text, String language, long length) {
        this.title = title;
        this.text = text;
        this.language = language;
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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
