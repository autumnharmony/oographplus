package ru.ssau.graphplus.validation.impl;

import com.google.common.base.Strings;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.validation.RuleResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PortNameRule extends NodeNameRule {
    @Override
    public RuleResult<Node> check(Node node) {
        if ((node.getType().equals(Node.NodeType.ServerPort) || node.getType().equals(Node.NodeType.ClientPort)) && !Strings.isNullOrEmpty(node.getName().trim())){
            Pattern pattern = Pattern.compile("\\w+:\\w+");
            Matcher matcher = pattern.matcher(node.getName());
            return matcher.matches()? new ResultOk() : error(node);
        }
        else {
            return new ResultOk();
        }
    }
}
