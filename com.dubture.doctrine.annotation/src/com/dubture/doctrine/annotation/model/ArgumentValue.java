package com.dubture.doctrine.annotation.model;

import com.dubture.doctrine.annotation.parser.antlr.SourcePosition;

abstract public class ArgumentValue implements IArgumentValue {

    protected SourcePosition sourcePosition = new SourcePosition();

    @Override
    public SourcePosition getSourcePosition() {
        return sourcePosition;
    }

    @Override
    public void setSourcePosition(SourcePosition position) {
        sourcePosition = position;
    }
}
