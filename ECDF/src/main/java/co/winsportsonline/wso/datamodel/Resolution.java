package co.winsportsonline.wso.datamodel;

/**
 * Created by Esteban- on 23-04-14.
 */
public class Resolution {
    private String width;
    private String height;

    public Resolution() {
    }
    @DataMember(member = "width")
    public String getWidth() {
        return width;
    }
    @DataMember(member = "width")
    public void setWidth(String width) {
        this.width = width;
    }
    @DataMember(member = "height")
    public String getHeight() {
        return height;
    }
    @DataMember(member = "height")
    public void setHeight(String height) {
        this.height = height;
    }
}
