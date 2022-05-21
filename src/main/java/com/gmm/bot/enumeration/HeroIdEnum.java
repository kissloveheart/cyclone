package com.gmm.bot.enumeration;

public enum HeroIdEnum {
    THUNDER_GOD,
    MONK,
    AIR_SPIRIT,
    SEA_GOD,
    MERMAID,
    SEA_SPIRIT,
    FIRE_SPIRIT,
    CERBERUS,
    DISPATER,
    ELIZAH,
    TALOS,
    SKELETON,
    GUTS,
    MONKEY,
    LIFE_STEALER,
    SHIELD_BEARER,
    LOCKE,
    SILENCER,
    NEICHUS,
    SHOW_STOPPER,
    JINN,
    DRUNKEN_FIST,
    HEART_BREAKER,
    BABY_DRAGON,
    GRIFFIN,
    ;

    public static final int SIZE = values().length;


    public static HeroIdEnum from(String value) {
        for (HeroIdEnum type : values()) {
            if (value.equals(type.toString())) return type;
        }
        return null;
    }
}

