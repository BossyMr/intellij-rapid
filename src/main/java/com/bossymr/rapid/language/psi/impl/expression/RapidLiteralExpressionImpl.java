package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiLiteralUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidLiteralExpressionImpl extends RapidExpressionElement implements RapidLiteralExpression {

    public RapidLiteralExpressionImpl() {
        super(RapidElementTypes.LITERAL_EXPRESSION);
    }

    @Override
    public @Nullable RapidType getType() {
        IElementType elementType = getNode().getFirstChildNode().getElementType();
        if (RapidTokenTypes.TRUE_KEYWORD.equals(elementType) || RapidTokenTypes.FALSE_KEYWORD.equals(elementType)) {
            return RapidType.BOOLEAN;
        }
        if (RapidTokenTypes.INTEGER_LITERAL.equals(elementType)) {
            return RapidType.NUMBER;
        }
        if (RapidTokenTypes.STRING_LITERAL.equals(elementType)) {
            return RapidType.STRING;
        }
        return null;
    }

    @Override
    public @Nullable Object getValue() {
        IElementType elementType = getNode().getFirstChildNode().getElementType();
        if (elementType.equals(RapidTokenTypes.TRUE_KEYWORD)) {
            return true;
        }
        if (elementType.equals(RapidTokenTypes.FALSE_KEYWORD)) {
            return false;
        }
        if (elementType.equals(RapidTokenTypes.STRING_LITERAL)) {
            String text = getText();
            text = text.substring(1, text.length() - 1);
            text = text.replaceAll("\"\"", "\"");
            text = text.replaceAll("\\\\", "\\");
            return text;
        }
        if (elementType.equals(RapidTokenTypes.INTEGER_LITERAL)) {
            String text = getText();
            if (text.startsWith("0D") || text.startsWith("0d")) {
                return Long.parseLong(text.substring(2), 10);
            }
            if (text.startsWith("0X") || text.startsWith("0x")) {
                return Long.parseLong(text.substring(2), 16);
            }
            if (text.startsWith("0O") || text.startsWith("0o")) {
                return Long.parseLong(text.substring(2), 8);
            }
            if (text.startsWith("0B") || text.startsWith("0b")) {
                return Long.parseLong(text.substring(2), 2);
            }
            return PsiLiteralUtil.parseDouble(text);
        }
        throw new IllegalStateException();
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitLiteralExpression(this);
    }
}
