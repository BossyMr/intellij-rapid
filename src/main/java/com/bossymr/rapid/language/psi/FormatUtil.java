package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.*;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;

/**
 * Utility methods used to format symbols.
 */
public class FormatUtil {

    public static final int MAX_PARAMETERS = 7;

    /**
     * Formats the specified atomic, using the specified options.
     * <p>
     * The options which can be used together with this method are: {@link Option#SHOW_TYPE SHOW_TYPE},
     * {@link Option#SHOW_NAME SHOW_NAME}, and {@link Option#SHOW_TYPE_AFTER SHOW_TYPE_AFTER}.
     *
     * @param atomic the atomic to format.
     * @param options the options defining how to format the specified atomic.
     * @return a string representing the specified atomic.
     */
    public static @NotNull String format(@NotNull RapidAtomic atomic, @NotNull EnumSet<Option> options) {
        StringBuilder buffer = new StringBuilder();
        if (options.contains(Option.SHOW_TYPE) && !(options.contains(Option.SHOW_TYPE_AFTER))) {
            if (atomic.getAssociatedType() != null) {
                buffer.append(atomic.getAssociatedType().getPresentableText()).append(" ");
            }
        }
        if (options.contains(Option.SHOW_NAME)) {
            buffer.append(atomic.getName());
        }
        if (options.contains(Option.SHOW_TYPE) && options.contains(Option.SHOW_TYPE_AFTER)) {
            if (atomic.getAssociatedType() != null) {
                buffer.append(":").append(atomic.getAssociatedType().getPresentableText()).append(" ");
            }
        }
        return buffer.toString();
    }

    /**
     * Formats the specified record, using the specified options.
     * <p>
     * The options which can be used together with this method are: {@link Option#SHOW_NAME SHOW_NAME}, and
     * {@link Option#SHOW_COMPONENTS SHOW_COMPONENTS}}.
     *
     * @param record the atomic to format.
     * @param options the options defining how to format the specified atomic.
     * @return a string representing the specified atomic.
     */
    public static @NotNull String format(@NotNull RapidRecord record, @NotNull EnumSet<Option> options,
                                         @NotNull EnumSet<Option> components) {
        StringBuilder buffer = new StringBuilder();
        if (options.contains(Option.SHOW_NAME)) {
            buffer.append(record.getName());
        }
        if (options.contains(Option.SHOW_COMPONENTS)) {
            buffer.append('(');
            List<? extends RapidComponent> groups = record.getComponents();
            for (int i = 0; i < groups.size(); i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                buffer.append(format(groups.get(i), components));
            }
            buffer.append(')');
        }
        return buffer.toString();
    }

    public static @NotNull String format(@NotNull RapidComponent component, @NotNull EnumSet<Option> options) {
        StringBuilder buffer = new StringBuilder();
        if (options.contains(Option.SHOW_TYPE) && !(options.contains(Option.SHOW_TYPE_AFTER))) {
            if (component.getType() != null) {
                buffer.append(component.getType().getPresentableText()).append(" ");
            }
        }
        if (options.contains(Option.SHOW_NAME)) {
            buffer.append(component.getName());
        }
        if (options.contains(Option.SHOW_TYPE) && options.contains(Option.SHOW_TYPE_AFTER)) {
            if (component.getType() != null) {
                buffer.append(":").append(component.getType().getPresentableText()).append(" ");
            }
        }
        return buffer.toString();
    }

    /**
     * Formats the specified alias, using the specified options.
     * <p>
     * The options which can be used together with this method are: {@link Option#SHOW_TYPE SHOW_TYPE},
     * {@link Option#SHOW_NAME SHOW_NAME}, and {@link Option#SHOW_TYPE_AFTER SHOW_TYPE_AFTER}.
     *
     * @param alias the alias to format.
     * @param options the options defining how to format the specified alias.
     * @return a string representing the specified alias.
     */
    public static @NotNull String format(@NotNull RapidAlias alias, @NotNull EnumSet<Option> options) {
        StringBuilder buffer = new StringBuilder();
        if (options.contains(Option.SHOW_TYPE) && !(options.contains(Option.SHOW_TYPE_AFTER))) {
            if (alias.getType() != null) {
                buffer.append(alias.getType().getPresentableText()).append(" ");
            }
        }
        if (options.contains(Option.SHOW_NAME)) {
            buffer.append(alias.getName());
        }
        if (options.contains(Option.SHOW_TYPE) && options.contains(Option.SHOW_TYPE_AFTER)) {
            if (alias.getType() != null) {
                buffer.append(":").append(alias.getType().getPresentableText()).append(" ");
            }
        }
        return buffer.toString();
    }

    /**
     * Formats the specified field, using the specified options.
     * <p>
     * The options which can be used together with this method are: {@link Option#SHOW_VISIBILITY SHOW_VISIBILITY},
     * {@link Option#SHOW_MODE SHOW_MODE}, {@link Option#SHOW_TYPE SHOW_TYPE},
     * {@link Option#SHOW_TYPE_AFTER SHOW_TYPE_AFTER}, {@link Option#SHOW_NAME SHOW_NAME}, and
     * {@link Option#SHOW_INITIALIZER SHOW_INITIALIZER}.
     *
     * @param field the field to format.
     * @param options the options defining how to format the specified field.
     * @return a string representing the specified field.
     */
    public static @NotNull String format(@NotNull RapidField field, @NotNull EnumSet<Option> options) {
        StringBuilder buffer = new StringBuilder();
        if (options.contains(Option.SHOW_VISIBILITY)) {
            buffer.append(switch (field.getVisibility()) {
                case LOCAL -> "LOCAL ";
                case TASK -> "TASK ";
                case GLOBAL -> "";
            });
        }
        if (options.contains(Option.SHOW_MODE)) {
            buffer.append(switch (field.getFieldType()) {
                case VARIABLE -> "VAR ";
                case CONSTANT -> "PERS ";
                case PERSISTENT -> "CONST ";
            });
        }
        if (options.contains(Option.SHOW_TYPE) && !(options.contains(Option.SHOW_TYPE_AFTER))) {
            if (field.getType() != null) {
                buffer.append(field.getType().getPresentableText()).append(" ");
            }
        }
        if (options.contains(Option.SHOW_NAME)) {
            buffer.append(field.getName());
        }
        if (options.contains(Option.SHOW_TYPE) && options.contains(Option.SHOW_TYPE_AFTER)) {
            if (field.getType() != null) {
                buffer.append(":").append(field.getType().getPresentableText());
            }
        }
        if (options.contains(Option.SHOW_INITIALIZER)) {
            RapidExpression expression = field.getInitializer();
            if (expression != null) {
                buffer.append(" := ");
                String text = format(expression);
                buffer.append(text);
            }
        }
        return buffer.toString();
    }

    /**
     * Formats the specified routine, using the specified options. This method specifies a maximum number of parameters,
     * after which the parameter list will be appended with ellipsis ("..."). To manually configure a maximum number of
     * parameters, use {@link #format(RapidRoutine, EnumSet, EnumSet, int)} instead.
     * <p>
     * The options which can be used as options are: {@link Option#SHOW_VISIBILITY SHOW_VISIBILITY},
     * {@link Option#SHOW_MODE SHOW_MODE}, {@link Option#SHOW_TYPE SHOW_TYPE},
     * {@link Option#SHOW_TYPE_AFTER SHOW_TYPE_AFTER}, {@link Option#SHOW_NAME SHOW_NAME}, and
     * {@link Option#SHOW_PARAMETERS SHOW_PARAMETERS}.
     * <p>
     * The options which can be used as options to configure the formatting of routine parameters are:
     * {@link Option#SHOW_MODE SHOW_MODE}, {@link Option#SHOW_TYPE SHOW_TYPE}, {@link Option#SHOW_NAME SHOW_NAME}.
     *
     * @param routine the routine to format.
     * @param options the options defining how to format the specified routine.
     * @param parameters the options defining how to format parameters to the specified routine.
     * @return a string representing the specified routine.
     */
    public static @NotNull String format(@NotNull RapidRoutine routine, @NotNull EnumSet<Option> options,
                                         @NotNull EnumSet<Option> parameters) {
        return format(routine, options, parameters, MAX_PARAMETERS);
    }

    /**
     * Formats the specified routine, using the specified options.
     * <p>
     * The options which can be used as options are: {@link Option#SHOW_VISIBILITY SHOW_VISIBILITY},
     * {@link Option#SHOW_MODE SHOW_MODE}, {@link Option#SHOW_TYPE SHOW_TYPE},
     * {@link Option#SHOW_TYPE_AFTER SHOW_TYPE_AFTER}, {@link Option#SHOW_NAME SHOW_NAME}, and
     * {@link Option#SHOW_PARAMETERS SHOW_PARAMETERS}.
     * <p>
     * The options which can be used as options to configure the formatting of routine parameters are:
     * {@link Option#SHOW_VISIBILITY SHOW_VISIBILITY}, {@link Option#SHOW_MODE SHOW_MODE},
     * {@link Option#SHOW_TYPE SHOW_TYPE}, {@link Option#SHOW_NAME SHOW_NAME}.
     *
     * @param routine the routine to format.
     * @param options the options defining how to format the specified routine.
     * @param parameters the options defining hwo to format parameters to the specified routine.
     * @param length the maximum number of parameters, after which, the parameter list will be appended with an ellipsis
     * ("...").
     * @return a string representing the specified routine.
     */
    public static @NotNull String format(@NotNull RapidRoutine routine, @NotNull EnumSet<Option> options,
                                         @NotNull EnumSet<Option> parameters, int length) {
        StringBuilder buffer = new StringBuilder();
        if (options.contains(Option.SHOW_VISIBILITY)) {
            buffer.append(switch (routine.getVisibility()) {
                case LOCAL -> "LOCAL ";
                case TASK -> "TASK ";
                case GLOBAL -> "";
            });
        }
        if (options.contains(Option.SHOW_MODE)) {
            buffer.append(switch (routine.getRoutineType()) {
                case FUNCTION -> "FUNC ";
                case PROCEDURE -> "PROC ";
                case TRAP -> "TRAP ";
            });
        }
        if (options.contains(Option.SHOW_TYPE) && !(options.contains(Option.SHOW_TYPE_AFTER))) {
            if (routine.getType() != null) {
                buffer.append(routine.getType().getPresentableText()).append(" ");
            }
        }
        if (options.contains(Option.SHOW_NAME)) {
            buffer.append(routine.getName());
        }
        if (options.contains(Option.SHOW_PARAMETERS)) {
            if (routine.getParameters() != null) {
                buffer.append('(');
                List<? extends RapidParameterGroup> groups = routine.getParameters();
                for (int i = 0; i < Math.min(groups.size(), length); i++) {
                    if (i > 0) {
                        buffer.append(", ");
                    }
                    buffer.append(format(groups.get(i), parameters));
                }
                if (groups.size() > length) {
                    buffer.append(", ...");
                }
                buffer.append(')');
            }
        }
        if (options.contains(Option.SHOW_TYPE) && options.contains(Option.SHOW_TYPE_AFTER)) {
            if (routine.getType() != null) {
                buffer.append(": ").append(routine.getType().getPresentableText());
            }
        }
        return fix(buffer);
    }

    public static @NotNull String format(@NotNull RapidParameterGroup group, @NotNull EnumSet<Option> options) {
        StringBuilder buffer = new StringBuilder();
        if (options.contains(Option.SHOW_VISIBILITY)) {
            if (group.isOptional()) buffer.append('\\');
        }
        List<? extends RapidParameter> parameters = group.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) buffer.append(" | ");
            RapidParameter parameter = parameters.get(i);
            buffer.append(format(parameter, options));
        }
        return buffer.toString();
    }

    public static @NotNull String format(@NotNull RapidParameter parameter, @NotNull EnumSet<Option> options) {
        StringBuilder buffer = new StringBuilder();
        if (options.contains(Option.SHOW_MODE)) {
            buffer.append(switch (parameter.getParameterType()) {
                case INPUT -> "";
                case VARIABLE -> "VAR ";
                case PERSISTENT -> "PERS ";
                case INOUT -> "INOUT ";
                case REFERENCE -> "REF ";
            });
        }
        if (options.contains(Option.SHOW_TYPE)) {
            if (parameter.getType() != null) {
                buffer.append(parameter.getType().getPresentableText()).append(" ");
            }
        }
        if (options.contains(Option.SHOW_NAME)) {
            buffer.append(parameter.getName()).append(" ");
        }
        return fix(buffer);
    }

    /**
     * Formats the specified expression. This method specifies a maximum length for the formatted string. To manually
     * configure a maximum length, use {@link #format(RapidExpression, int)} instead.
     *
     * @param expression the expression to format.
     * @return a string representing the specified expression.
     */
    public static @NotNull String format(@NotNull RapidExpression expression) {
        return format(expression, 2 << 6);
    }

    /**
     * Formats the specified expression.
     *
     * @param expression the expression to format.
     * @param length the maximum length of the formatted string, after which, the formatted string will be appended with
     * an ellipsis ("...").
     * @return a string representing the specified expression.
     */
    public static @NotNull String format(@NotNull RapidExpression expression, int length) {
        StringBuilder buffer = new StringBuilder();
        expression.accept(new ExpressionVisitor(buffer));
        final String text = buffer.toString();
        int index = text.indexOf('\n');
        String trimmed = text.substring(0, index != -1 ? index : Math.min(length, text.length()));
        if (trimmed.length() != text.length()) {
            trimmed += "...";
        }
        return trimmed;
    }

    private static @NotNull String fix(@NotNull StringBuilder buffer) {
        String value = buffer.toString();
        value = value.replaceAll(" {2}", " ");
        value = value.replaceAll(" ,", ",");
        value = value.replaceAll(" \\)", ")");
        return value;
    }

    /**
     * Options are used to configure how to format a symbol. The same symbols are used to configure both fields and
     * routines.
     */
    public enum Option {
        /**
         * Defines whether the visibility ("LOCAL" or "TASK") of the symbol should be displayed at the beginning of the
         * formatted value.
         */
        SHOW_VISIBILITY,

        /**
         * Defines whether the mode of the symbol should be displayed at the beginning of the formatted value. The mode
         * of a symbol is either "VAR", "CONST", or "PERS" for fields or "INOUT", "PERS" or "VAR" for parameters.
         */
        SHOW_MODE,

        /**
         * Defines whether the type of the symbol should be displayed in the formatted value. The type will by default
         * be displayed at the correct position (succeeding the mode and preceding the name) in the formatted value.
         * This behaviour can be changed by using {@link #SHOW_TYPE_AFTER} in addition to this option, in which case it
         * will be displayed succeeding the name.
         */
        SHOW_TYPE,

        /**
         * Defines whether the type of the symbol should be displayed after the name of the symbol. This option has to
         * be selected in addition to {@link #SHOW_TYPE}.
         */
        SHOW_TYPE_AFTER,

        /**
         * Defines whether the name of the symbol should be displayed.
         */
        SHOW_NAME,

        /**
         * Defines whether the initializer of the field should be displayed. This option only functions when used to
         * format a {@link RapidField}.
         */
        SHOW_INITIALIZER,

        SHOW_COMPONENTS,
        /**
         * Defines whether the parameters of a routine should be displayed. This option only functions when used with to
         * format a {@link RapidRoutine}.
         */
        SHOW_PARAMETERS
    }

    /**
     *
     */
    private static class ExpressionVisitor extends RapidRecursiveElementWalkingVisitor {

        private final StringBuilder buffer;

        public ExpressionVisitor(@NotNull StringBuilder buffer) {
            this.buffer = buffer;
        }

        @Override
        public void visitExpression(@NotNull RapidExpression o) {
            buffer.append(o.getText());
        }

        @Override
        public void visitAggregateExpression(@NotNull RapidAggregateExpression o) {
            buffer.append("[");
            List<RapidExpression> expressions = o.getExpressions();
            for (int i = 0; i < expressions.size(); i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                expressions.get(i).accept(this);
            }
            buffer.append("]");
        }

        @Override
        public void visitIndexExpression(@NotNull RapidIndexExpression o) {
            o.getExpression().accept(this);
            buffer.append("{");
            List<RapidExpression> expressions = o.getArray().getDimensions();
            for (int i = 0; i < expressions.size(); i++) {
                if (i > 0) {
                    buffer.append(",");
                }
                expressions.get(i).accept(this);
            }
            buffer.append("}");
        }

        @Override
        public void visitBinaryExpression(@NotNull RapidBinaryExpression o) {
            RapidExpression left = o.getLeft();
            RapidExpression right = o.getRight();
            left.accept(this);
            buffer.append(o.getSign().getText());
            if (right != null) {
                right.accept(this);
            }
        }

        @Override
        public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression o) {
            o.getReferenceExpression().accept(this);
            o.getArgumentList().accept(this);
        }

        @Override
        public void visitArgumentList(@NotNull RapidArgumentList o) {
            buffer.append(o.getArguments().isEmpty() ? "()" : "(...)");
        }

        @Override
        public void visitParenthesisedExpression(@NotNull RapidParenthesisedExpression o) {
            buffer.append("(");
            final RapidExpression expression = o.getExpression();
            if (expression != null) {
                expression.accept(this);
            }
            buffer.append(")");
        }

        @Override
        public void visitUnaryExpression(@NotNull RapidUnaryExpression o) {
            buffer.append(o.getSign().getText());
            final RapidExpression expression = o.getExpression();
            if (expression != null) {
                expression.accept(this);
            }
        }

        @Override
        public void visitReferenceExpression(@NotNull RapidReferenceExpression o) {
            final RapidExpression expression = o.getQualifier();
            if (expression != null) {
                expression.accept(this);
                buffer.append(".");
            }
            buffer.append(o.getIdentifier());
        }
    }
}
