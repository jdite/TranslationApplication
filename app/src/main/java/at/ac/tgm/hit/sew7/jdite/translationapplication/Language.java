package at.ac.tgm.hit.sew7.jdite.translationapplication;

import java.util.Locale;

public class Language {
    public String code;

    public Language(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return new Locale(code).getDisplayLanguage();
    }
    @Override
    public boolean equals(Object o) {
        return ((Language) o).code.equals(code);
    }
}
