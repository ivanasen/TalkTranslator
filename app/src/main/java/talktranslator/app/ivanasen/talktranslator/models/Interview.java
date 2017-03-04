package talktranslator.app.ivanasen.talktranslator.models;

import com.orm.SugarRecord;

/**
 * Created by ivan on 3/3/2017.
 */

public class Interview extends SugarRecord {
    private String internalPath;
    private String length;

    public Interview(String internalPath, String length) {
        this.internalPath = internalPath;
        this.length = length;
    }

    public String getInternalPath() {
        return internalPath;
    }

    public void setInternalPath(String internalPath) {
        this.internalPath = internalPath;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }
}
