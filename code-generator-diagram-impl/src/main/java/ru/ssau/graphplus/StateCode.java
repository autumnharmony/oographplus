/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.base.Joiner;

import java.util.Collections;

/**
  * state = ['+'] ident [ ('?'|'!') [rules] ].
  */

public class StateCode implements CodeProvider {

    private boolean isInitial(){
        return false;//TODO
    }

    @Override
    public String getCode() {
        return "" + (isInitial() ? "+" : "") + getIdent() + ( needOptional()?  getMark()+ (needRules() ? Joiner.on(',').join(getRules()) : "") : "");
    }

    private boolean needOptional() {
        return false;//TODO
    }

    private boolean needRules() {
        return false;//TODO
    }


    public String getIdent() {
        return "ident";//TODO
    }

    public Iterable<RuleCode> getRules() {
        return Collections.emptyList();//TODO
    }

    public String getMark() {
        return "?";
    }
}
