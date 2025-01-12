package com.catchylabs.model;

import lombok.Getter;

@Getter
public class ElementInfo {
    public String keyword;
    public String locatorValue;
    public String locatorType;

    @Override
    public String toString() {
        return "Elements[" + "keyword=" + keyword + ",locatorType=" + locatorType + ",locatorValue=" + locatorValue + "]";
    }


}