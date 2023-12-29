package org.gautelis.gedcom2latex.model.gedcom;

import java.util.HashMap;
import java.util.Map;

// A code that indicates the sex of the individual
public enum SEX {
    Male("M"),
    Female("F"),
    Undetermined("U");

    private final String code;

    SEX(String s) {
        this.code = s;
    }

    private static Map<String, SEX> index = new HashMap<>();
    static {
        for (SEX sex : SEX.values()) {
            index.put(sex.code, sex);
        }
    }

    public static SEX from(String code) {
        SEX sex = index.get(code.toUpperCase());
        if (null == sex) {
            return Undetermined;
        }
        return sex;
    }
}
