package com.enums;

import java.util.ArrayList;
import java.util.List;

public enum Language {
    en_US("English (US)"),
    en_GB("English (UK)"),
    en_AU("English (Australia)"),
    en_IN("English (India)"),
    ar_XA("Arabic"),
    bn_IN("Bengali (India)"),
    cmn_CN("Chinese (Mandarin)"),
    yue_HK("Chinese (Yue)"),
    cs_CZ("Czech (Czech Republic)"),
    da_DK("Danish (Denmark)"),
    nl_NL("Dutch (Netherlands)"),
    fil_PH("Filipino (Philippines)"),
    fi_FI("Finnish (Finland)"),
    fr_CA("French (Canada)"),
    fr_FR("French (France)"),
    de_DE("German (Germany)"),
    el_GR("Greek (Greece)"),
    gu_IN("Gujarati (India)"),
    hi_IN("Hindi (India)"),
    hu_HU("Hungarian (Hungary)"),
    id_ID("Indonesian (Indonesia)"),
    it_IT("Italian (Italy)"),
    ja_JP("Japanese (Japan)"),
    kn_IN("Kannada (India)"),
    ko_KR("Korean (South Korea)"),
    ml_IN("Malayalam (India)"),
    nb_NO("Norwegian (Norway)"),
    pl_PL("Polish (Poland)"),
    pt_BR("Portuguese (Brazil)"),
    pt_PT("Portuguese (Portugal)"),
    ru_RU("Russian (Russia)"),
    sk_SK("Slovak (Slovakia)"),
    es_ES("Spanish (Spain)"),
    sv_SE("Swedish (Sweden)"),
    ta_IN("Tamil (India)"),
    te_IN("Telugu (India)"),
    th_TH("Thai (Thailand)"),
    tr_TR("Turkish (Turkey)"),
    uk_UA("Ukrainian (Ukraine)"),
    vi_VN("Vietnamese (Vietnam)");

    private String langName;

    Language(String langName) {
        this.langName = langName;
    }

    private String getLangName() {
        return this.langName;
    }

    /**
     * This method returns the language code of given language name if it exists, otherwise returns null.
     * @param targetName  Language name.
     * @return  Language enum or null.
     */
    public static Language codeOf(String targetName) {
        for(Language langCode : Language.values()) {
            if(langCode.getLangName().equals(targetName)) {
                return langCode;
            }
        }
        return null;
    }

    /**
     * This method lists all the language names defined in Language enum.
     * @return  List of language names.
     */
    public static String[] langNames() {
        Language[] arrLangs = Language.values();
        List<String> listLangs = new ArrayList<String>();
        String name;
        for (Language arrLang : arrLangs) {
            name = arrLang.getLangName();
            listLangs.add(name);
        }
        String[] returnArray = new String[listLangs.size()];
        return listLangs.toArray(returnArray);
    }

    /**
     * This method returns true if no male voice is available for the given language name.
     * @param choice  Language name to be evaluated for male voice availability.
     * @return  Boolean indicating availability.
     */
    public static boolean noMaleVoice(String choice) {
        Language targetName = codeOf(choice);
        return targetName == cs_CZ || targetName == fi_FI || targetName == el_GR || targetName == hu_HU ||
                targetName == pt_BR || targetName == sk_SK || targetName == sv_SE || targetName == th_TH ||
                targetName == uk_UA;
    }
}
