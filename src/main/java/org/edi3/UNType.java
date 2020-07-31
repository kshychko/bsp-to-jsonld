
package org.edi3;

public enum UNType {
    INDICATOR("Indicator"),
    IDENTIFIER("Identifier"),
    CODE("Code"),
    TEXT("Text"),
    DATETIME("DateTime"),
    AMOUNT("Amount"),
    PERCENT("Percent"),//Numeric
    RATE("Rate"),//Numeric
    DATE("Date"),//DateTime
    QUANTITY("Quantity"),//Numeric
    VALUE("Value"),//Text
    BINARYOBJECT("BinaryObject"),
    NUMERIC("Numeric"),
    MEASURE("Measure"),
    TYPE("Type"),//Text
    TIME("Time"),//DateTime
    GRAPHIC("Graphic"),//DateTime
    PICTURE("Picture"),//DateTime
    VIDEO("Video"),//DateTime
    SOUND("Sound");//DateTime


    private final String name;

    private UNType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        // (otherName == null) check is not needed because name.equals(null) returns false
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
