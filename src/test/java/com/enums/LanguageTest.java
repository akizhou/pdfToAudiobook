package com.enums;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class LanguageTest {
    @Test
    @Tag("codeOf")
    void codeOfReturn_LanguageCode() {
        assertEquals(Language.en_US, Language.codeOf("English (US)"));
        assertEquals(Language.en_GB, Language.codeOf("English (UK)"));
        assertEquals(Language.en_AU, Language.codeOf("English (Australia)"));
        assertEquals(Language.en_IN, Language.codeOf("English (India)"));
        assertEquals(Language.ar_XA, Language.codeOf("Arabic"));
        assertEquals(Language.bn_IN, Language.codeOf("Bengali (India)"));
        assertEquals(Language.cmn_CN, Language.codeOf("Chinese (Mandarin)"));
        assertEquals(Language.yue_HK, Language.codeOf("Chinese (Yue)"));
        assertEquals(Language.cs_CZ, Language.codeOf("Czech (Czech Republic)"));
        assertEquals(Language.da_DK, Language.codeOf("Danish (Denmark)"));
        assertEquals(Language.nl_NL, Language.codeOf("Dutch (Netherlands)"));
        assertEquals(Language.fil_PH, Language.codeOf("Filipino (Philippines)"));
        assertEquals(Language.fi_FI, Language.codeOf("Finnish (Finland)"));
        assertEquals(Language.fr_CA, Language.codeOf("French (Canada)"));
        assertEquals(Language.fr_FR, Language.codeOf("French (France)"));
        assertEquals(Language.de_DE, Language.codeOf("German (Germany)"));
        assertEquals(Language.el_GR, Language.codeOf("Greek (Greece)"));
        assertEquals(Language.gu_IN, Language.codeOf("Gujarati (India)"));
        assertEquals(Language.hi_IN, Language.codeOf("Hindi (India)"));
        assertEquals(Language.hu_HU, Language.codeOf("Hungarian (Hungary)"));
        assertEquals(Language.id_ID, Language.codeOf("Indonesian (Indonesia)"));
        assertEquals(Language.it_IT, Language.codeOf("Italian (Italy)"));
        assertEquals(Language.ja_JP, Language.codeOf("Japanese (Japan)"));
        assertEquals(Language.kn_IN, Language.codeOf("Kannada (India)"));
        assertEquals(Language.ko_KR, Language.codeOf("Korean (South Korea)"));
        assertEquals(Language.ml_IN, Language.codeOf("Malayalam (India)"));
        assertEquals(Language.nb_NO, Language.codeOf("Norwegian (Norway)"));
        assertEquals(Language.pl_PL, Language.codeOf("Polish (Poland)"));
        assertEquals(Language.pt_BR, Language.codeOf("Portuguese (Brazil)"));
        assertEquals(Language.pt_PT, Language.codeOf("Portuguese (Portugal)"));
        assertEquals(Language.ru_RU, Language.codeOf("Russian (Russia)"));
        assertEquals(Language.sk_SK, Language.codeOf("Slovak (Slovakia)"));
        assertEquals(Language.es_ES, Language.codeOf("Spanish (Spain)"));
        assertEquals(Language.sv_SE, Language.codeOf("Swedish (Sweden)"));
        assertEquals(Language.ta_IN, Language.codeOf("Tamil (India)"));
        assertEquals(Language.te_IN, Language.codeOf("Telugu (India)"));
        assertEquals(Language.th_TH, Language.codeOf("Thai (Thailand)"));
        assertEquals(Language.tr_TR, Language.codeOf("Turkish (Turkey)"));
        assertEquals(Language.uk_UA, Language.codeOf("Ukrainian (Ukraine)"));
        assertEquals(Language.vi_VN, Language.codeOf("Vietnamese (Vietnam)"));
    }

    @Test
    @Tag("codeOf")
    void noMatchingCode_ReturnNull() {
        assertNull(Language.codeOf("This language option doesn't exist"));
    }

    @Test
    void langNames_ReturnListOfNames() {
        String[] listOfNames = {
                "English (US)",
                "English (UK)",
                "English (Australia)",
                "English (India)",
                "Arabic",
                "Bengali (India)",
                "Chinese (Mandarin)",
                "Chinese (Yue)",
                "Czech (Czech Republic)",
                "Danish (Denmark)",
                "Dutch (Netherlands)",
                "Filipino (Philippines)",
                "Finnish (Finland)",
                "French (Canada)",
                "French (France)",
                "German (Germany)",
                "Greek (Greece)",
                "Gujarati (India)",
                "Hindi (India)",
                "Hungarian (Hungary)",
                "Indonesian (Indonesia)",
                "Italian (Italy)",
                "Japanese (Japan)",
                "Kannada (India)",
                "Korean (South Korea)",
                "Malayalam (India)",
                "Norwegian (Norway)",
                "Polish (Poland)",
                "Portuguese (Brazil)",
                "Portuguese (Portugal)",
                "Russian (Russia)",
                "Slovak (Slovakia)",
                "Spanish (Spain)",
                "Swedish (Sweden)",
                "Tamil (India)",
                "Telugu (India)",
                "Thai (Thailand)",
                "Turkish (Turkey)",
                "Ukrainian (Ukraine)",
                "Vietnamese (Vietnam)"
        };
        String[] namesReturned = Language.langNames();

        assertEquals(40, Language.langNames().length);
        for (int i = 0; i < Language.langNames().length; i++) {
            assertEquals(listOfNames[i], namesReturned[i]);
        }
    }

    @Test
    void noMaleVoice_forLanguageWithoutMaleVoice_ReturnTrue() {
        assertTrue(Language.noMaleVoice("Czech (Czech Republic)"));
        assertTrue(Language.noMaleVoice("Finnish (Finland)"));
        assertTrue(Language.noMaleVoice("Greek (Greece)"));
        assertTrue(Language.noMaleVoice("Hungarian (Hungary)"));
        assertTrue(Language.noMaleVoice("Portuguese (Brazil)"));
        assertTrue(Language.noMaleVoice("Slovak (Slovakia)"));
        assertTrue(Language.noMaleVoice("Swedish (Sweden)"));
        assertTrue(Language.noMaleVoice("Thai (Thailand)"));
        assertTrue(Language.noMaleVoice("Ukrainian (Ukraine)"));
    }
}