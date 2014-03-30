/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.base.Joiner;
import ru.ssau.graphplus.api.Node;

import java.util.Collections;

/**
 * process = '*' ident [params] ['=' ((ports [';' actions]) | actions) ] '.'.
 */
public class ProcessCode extends NodeCodeBase {


    /*
        channel = '~' ident [params] ['=' state {';' state}] '.'.
        state = ['+'] ident [ ('?'|'!') [rules] ].
        rules = rule { ',' rule }.
        rule = ident { ',' ident } '->' ident.
        process = '*' ident [params] ['=' ((ports [';' actions]) | actions) ] '.'.
        ports = port {';' port}.
        port = ident ':' ident ('?'|'!')[(rules ['|' '->' ident])|( '->' ident)].
        actions = action {';' action}.
        action = ['+'] [ident ':'] disjunction ['->' ([ident] '|' ident) | ident].
        disjunction = conjunction { '|' conjunction}.
        conjunction = call {'&' call}.
        call = ident '(' [args] ')'.
        args = ident ('?'|'!') ident {',' ident ('?'|'!') ident}.
        params = '<' ident {',' ident} '>'
    */


//    *Parent =
//    p1 : Link ! sin2 -> join; p2 : Link ! cos2 -> join;
//    +fork(p1!argSin,p2!argCos); join(p1?sin2,p2?cos2).


//    *Child =
//    p : Link ? argSin -> sin | argCos -> cos;
//    sin2(p?argSin,p!sin2); cos2(p?argCos,p!cos2).



    private String ident;

    public ProcessCode(Node node) {
        super(node);
    }

    @Override
    public String getCode() {
        String s = "*" + getIdent() + (needParameters() ? '<' + Joiner.on(',').join(getParameters()) + '>' : "");

        if (needPorts() || (needPorts() && needActions())) {
            s += Joiner.on(',').join(getPorts());
            if (needActions()) {
                s += Joiner.on(',').join(getActions());
            }
        }
        else {
            if (needActions()){
                s+= Joiner.on(',').join(getActions());
            }
        }

        s+=".";
        return s;
    }

    private boolean needActions() {
        return false;
    }

    private boolean needPorts() {
        return false;
    }

    public String getIdent() {
        return node.getName();
    }

    public Iterable<?> getParameters() {
        return Collections.emptyList();
    }

    private boolean needParameters() {

        // TODO
        return false;
    }

    public Iterable<?> getPorts() {
        return Collections.emptyList();
    }

    public Iterable<?> getActions() {
        return Collections.emptyList();
    }
}
