package assignment2;

public class STC {

    private String name;
    private String type;
    private Object dataType;

    STC(String name, String type, Object dataType) {
        this.name = name;
        this.type = type;
        this.dataType = dataType;
    }

    String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    Object getDataType() {
        return dataType;
    }

}
