package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.*;
import com.bossymr.rapid.language.flow.data.snapshots.Snapshot;
import com.bossymr.rapid.language.flow.expression.*;
import com.bossymr.rapid.language.flow.expression.FunctionCallExpression.Entry;
import com.bossymr.rapid.language.symbol.RapidComponent;
import com.bossymr.rapid.language.symbol.RapidRecord;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidVariable;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.microsoft.z3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConditionAnalyzer extends ControlFlowVisitor<Expr<?>> {

    private static final @NotNull Expression EMPTY_EXPRESSION = new SnapshotExpression(Snapshot.createSnapshot(RapidPrimitiveType.NUMBER));
    private final @NotNull Context context;

    private final @NotNull Map<Snapshot, Expr<?>> symbols = new HashMap<>();
    private final @NotNull Map<Snapshot, Expr<?>> pointers = new HashMap<>();
    private final @NotNull Map<Integer, AtomicInteger> suffixes = new HashMap<>();

    private final @NotNull Deque<Expression> stack = new ArrayDeque<>();
    private final @NotNull List<BoolExpr> queue;

    private ConditionAnalyzer(@NotNull Context context, @NotNull List<BoolExpr> queue) {
        this.context = context;
        this.queue = queue;
    }

    public static boolean isSatisfiable(@NotNull DataFlowState state, @NotNull Set<Snapshot> targets) {
        if (targets.isEmpty()) {
            return true;
        }
        try (Context context = new Context()) {
            Solver solver = context.mkSolver();
            getSolver(context, state, solver, targets);
            return switch (solver.check()) {
                case UNSATISFIABLE -> false;
                case UNKNOWN, SATISFIABLE -> true;
            };
        }
    }

    public static @NotNull Constraint getBooleanValue(@NotNull DataFlowState state, @NotNull Expression expression) {
        try (Context context = new Context()) {
            Set<Snapshot> targets = new HashSet<>();
            expression.iterate(expr -> {
                if (expr instanceof SnapshotExpression target) {
                    targets.add(target.getSnapshot());
                }
                return false;
            });
            Solver solver = context.mkSolver();
            ConditionAnalyzer conditionAnalyzer = getSolver(context, state, solver, targets);
            boolean isTrue = solver.check(conditionAnalyzer.getAsBoolean(new BinaryExpression(BinaryOperator.EQUAL_TO, expression, new LiteralExpression(true)).accept(conditionAnalyzer))) != Status.UNSATISFIABLE;
            boolean isFalse = solver.check(conditionAnalyzer.getAsBoolean(new BinaryExpression(BinaryOperator.EQUAL_TO, expression, new LiteralExpression(false)).accept(conditionAnalyzer))) != Status.UNSATISFIABLE;
            if (isTrue && isFalse) {
                return Constraint.ANY_VALUE;
            }
            if (isTrue) {
                return Constraint.ALWAYS_TRUE;
            }
            if (isFalse) {
                return Constraint.ALWAYS_FALSE;
            }
            return Constraint.NO_VALUE;
        }
    }

    public static @NotNull Optionality getOptionality(@NotNull DataFlowState state, @NotNull ReferenceExpression variable) {
        if (!(variable instanceof SnapshotExpression snapshotExpression)) {
            throw new IllegalArgumentException("Cannot compute optionality for variable: " + variable);
        }
        Constraint constraint = getBooleanValue(state, FunctionCallExpression.present(snapshotExpression.getSnapshot()));
        return switch (constraint) {
            case ANY_VALUE -> Optionality.UNKNOWN;
            case ALWAYS_TRUE -> Optionality.PRESENT;
            case ALWAYS_FALSE -> Optionality.MISSING;
            case NO_VALUE -> Optionality.NO_VALUE;
        };
    }

    private static @NotNull ConditionAnalyzer getSolver(@NotNull Context context, @NotNull DataFlowState state, @NotNull Solver solver, @NotNull Set<Snapshot> targets) {
        List<BoolExpr> queue = new ArrayList<>();
        ConditionAnalyzer conditionAnalyzer = new ConditionAnalyzer(context, queue);
        computeArgumentOptionality(state, conditionAnalyzer);
        DataFlowState compactState = state.createCompactState(targets);
        for (Expression condition : compactState.getConditions()) {
            Expr<?> expression = condition.accept(conditionAnalyzer);
            if (expression.getSort().equals(context.getBoolSort())) {
                queue.add(conditionAnalyzer.getAsBoolean(expression));
            }
            if (queue.contains(context.mkFalse())) {
                solver.add(context.mkFalse());
                return conditionAnalyzer;
            }
        }
        solver.add(queue.toArray(BoolExpr[]::new));
        return conditionAnalyzer;
    }

    private static void computeArgumentOptionality(@NotNull DataFlowState state, @NotNull ConditionAnalyzer conditionAnalyzer) {
        Block functionBlock = state.getInstruction().getBlock();
        List<ArgumentGroup> argumentGroups = functionBlock.getArgumentGroups();
        List<DataFlowState> chain = state.getPredecessorChain();
        DataFlowState root = chain.get(chain.size() - 1);
        for (ArgumentGroup argumentGroup : argumentGroups) {
            if (!(argumentGroup.isOptional()) || argumentGroup.arguments().size() <= 1) {
                continue;
            }
            List<Expr<BoolSort>> isOptional = new ArrayList<>();
            for (Argument argument : argumentGroup.arguments()) {
                Snapshot snapshot = root.getRoots().get(argument);
                BinaryExpression expression = new BinaryExpression(BinaryOperator.EQUAL_TO, FunctionCallExpression.present(snapshot), new LiteralExpression(true));
                isOptional.add(conditionAnalyzer.getAsBoolean(expression.accept(conditionAnalyzer)));
            }
            @SuppressWarnings("unchecked")
            Expr<BoolSort>[] array = isOptional.toArray(Expr[]::new);
            conditionAnalyzer.queue.add(conditionAnalyzer.context.mkAtMost(array, 1));
        }
    }

    private @NotNull Expr<?> getExpression(@Nullable Expression expression, @NotNull Expression child) {
        stack.add(Objects.requireNonNullElse(expression, EMPTY_EXPRESSION));
        Expr<?> expr = child.accept(this);
        stack.removeLast();
        return expr;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Expr<?> visitFunctionCallExpression(@NotNull FunctionCallExpression expression) {
        Expr[] arguments = getArguments(expression);
        return switch (expression.getName()) {
            case ":ConstantA" -> context.mkConstArray(arguments[0].getSort(), arguments[1]);
            case ":SelectA" -> context.mkSelect(arguments[0], arguments[1]);
            case ":StoreA" -> context.mkStore(arguments[0], arguments[1], arguments[2]);
            case ":SelectS", ":StoreS" -> {
                Entry.ValueEntry referenceEntry = ((Entry.ValueEntry) expression.getArguments().get(0));
                RapidRecord record = ((RapidRecord) referenceEntry.expression().getType().getRootStructure());
                Objects.requireNonNull(record);
                Entry.ValueEntry valueEntry = (Entry.ValueEntry) expression.getArguments().get(1);
                LiteralExpression literalExpression = (LiteralExpression) valueEntry.expression();
                String componentName = (String) literalExpression.getValue();
                FuncDecl<?> field = getField(record, componentName);
                if (expression.getName().equals(":SelectS")) {
                    System.out.println("field = " + field);
                    System.out.println("arguments[0] = " + arguments[0]);
                    yield context.mkApp(field, arguments[0]);
                } else {
                    // TODO: Fix this method...
                    System.out.println("field = " + field);
                    System.out.println("arguments[0] = " + arguments[0]);
                    System.out.println("arguments[2] = " + arguments[2]);
                    yield context.mkUpdateField(field, arguments[0], arguments[2]);
                }
            }
            default -> {
                Sort sort = Objects.requireNonNullElseGet(getSort(expression.getType()), context::getRealSort);
                Sort[] sorts = Arrays.stream(arguments)
                                     .map(Expr::getSort)
                                     .toList()
                                     .toArray(new Sort[0]);
                FuncDecl<?> funcDecl = context.mkFuncDecl(expression.getName(), sorts, sort);
                yield funcDecl.apply(arguments);
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private @NotNull FuncDecl<?> getField(@NotNull RapidRecord record, @NotNull String componentName) {
        DatatypeSort sort = (DatatypeSort) getSort(record.createType());
        Objects.requireNonNull(sort);
        return sort.getAccessors()[0][getComponentIndex(record, componentName)];
    }

    private int getComponentIndex(@NotNull RapidRecord record, @NotNull String componentName) {
        List<? extends RapidComponent> components = record.getComponents();
        for (int i = 0; i < components.size(); i++) {
            if (componentName.equalsIgnoreCase(components.get(i).getName())) {
                return i;
            }
        }
        return -1;
    }

    private Expr<?> @NotNull [] getArguments(@NotNull FunctionCallExpression expression) {
        List<Entry> arguments = expression.getArguments();
        Expr<?>[] expressions = new Expr<?>[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            Entry entry = arguments.get(i);
            if (entry instanceof Entry.ReferenceEntry referenceEntry) {
                expressions[i] = createPointer(referenceEntry.snapshot());
            } else if (entry instanceof Entry.ValueEntry valueEntry) {
                expressions[i] = getExpression(null, valueEntry.expression());
            }
        }
        return expressions;
    }

    @Override
    public Expr<?> visitBinaryExpression(@NotNull BinaryExpression expression) {
        Expr<?> left = getExpression(expression, expression.getLeft());
        Expr<?> right = getExpression(expression, expression.getRight());
        return switch (expression.getOperator()) {
            case ADD -> {
                if (expression.getLeft().getType().isAssignable(RapidPrimitiveType.STRING) || expression.getRight().getType().isAssignable(RapidPrimitiveType.STRING)) {
                    yield context.mkConcat(getAsString(left), getAsString(right));
                } else {
                    yield context.mkAdd(getAsNumber(left), getAsNumber(right));
                }
            }
            case SUBTRACT -> context.mkSub(getAsNumber(left), getAsNumber(right));
            case MULTIPLY -> context.mkMul(getAsNumber(left), getAsNumber(right));
            case DIVIDE -> context.mkDiv(getAsNumber(left), getAsNumber(right));
            case INTEGER_DIVIDE -> context.mkDiv(getAsInt(left), getAsInt(right));
            case MODULO -> context.mkMod(getAsInt(left), getAsInt(right));
            case LESS_THAN -> context.mkLt(getAsNumber(left), getAsNumber(right));
            case LESS_THAN_OR_EQUAL -> context.mkLe(getAsNumber(left), getAsNumber(right));
            case EQUAL_TO -> {
                if (isEqualSort(left, right)) {
                    if (expression.getLeft() instanceof SnapshotExpression leftSnapshot && expression.getRight() instanceof SnapshotExpression rightSnapshot) {
                        yield context.mkAnd(context.mkEq(left, right), context.mkEq(createPointer(leftSnapshot.getSnapshot()), createPointer(rightSnapshot.getSnapshot())));
                    }
                    yield context.mkEq(left, right);
                } else {
                    yield context.mkBool(false);
                }
            }
            case NOT_EQUAL_TO -> getExpression(expression, new UnaryExpression(UnaryOperator.NOT, expression));
            case GREATER_THAN -> context.mkGt(getAsNumber(left), getAsNumber(right));
            case GREATER_THAN_OR_EQUAL -> context.mkGe(getAsNumber(left), getAsNumber(right));
            case AND -> context.mkAnd(getAsBoolean(left), getAsBoolean(right));
            case XOR -> context.mkXor(getAsBoolean(left), getAsBoolean(right));
            case OR -> context.mkOr(getAsBoolean(left), getAsBoolean(right));
        };
    }

    private boolean isEqualSort(Expr<?> left, Expr<?> right) {
        if (left.getSort().equals(context.getIntSort()) || left.getSort().equals(context.getRealSort())) {
            if (right.getSort().equals(context.getIntSort()) || right.getSort().equals(context.getRealSort())) {
                return true;
            }
        }
        return left.getSort().equals(right.getSort());
    }

    @SuppressWarnings("unchecked")
    private @NotNull Expr<SeqSort<CharSort>> getAsString(@NotNull Expr<?> expr) {
        Sort sort = expr.getSort();
        if (sort.equals(context.getStringSort())) {
            return (Expr<SeqSort<CharSort>>) expr;
        }
        throw new IllegalArgumentException("Unexpected sort: " + sort + " for expression: " + expr);
    }

    private @NotNull BoolExpr getAsBoolean(@NotNull Expr<?> expr) {
        Sort sort = expr.getSort();
        if (sort.equals(context.getBoolSort())) {
            return (BoolExpr) expr;
        }
        throw new IllegalArgumentException("Unexpected sort: " + sort + " for expression: " + expr);
    }

    @SuppressWarnings("unchecked")
    private @NotNull Expr<? extends ArithSort> getAsNumber(@NotNull Expr<?> expr) {
        Sort sort = expr.getSort();
        if (sort.equals(context.getIntSort()) || sort.equals(context.getRealSort())) {
            return (Expr<? extends ArithSort>) expr;
        }
        throw new IllegalArgumentException("Unexpected sort: " + sort + " for expression: " + expr);
    }

    @SuppressWarnings("unchecked")
    private @NotNull Expr<IntSort> getAsInt(@NotNull Expr<?> expr) {
        Sort sort = expr.getSort();
        if (sort.equals(context.getIntSort())) {
            return (Expr<IntSort>) expr;
        }
        if (sort.equals(context.getRealSort())) {
            return context.mkReal2Int((Expr<RealSort>) expr);
        }
        throw new IllegalArgumentException("Unexpected sort: " + sort + " for expression: " + expr);
    }

    @Override
    public Expr<?> visitUnaryExpression(@NotNull UnaryExpression expression) {
        Expression component = expression.getExpression();
        Expr<?> expr = getExpression(expression, component);
        return switch (expression.getOperator()) {
            case NOT -> context.mkNot(getAsBoolean(expr));
            case NEGATE -> context.mkMul(getAsNumber(expr), context.mkReal(-1));
        };
    }

    private @NotNull RapidType getCorrectType(@NotNull Expression current) {
        if (!(current.getType().equals(RapidPrimitiveType.ANYTYPE))) {
            return current.getType();
        }
        Expression expression = stack.peekLast();
        if (expression == null || expression.equals(EMPTY_EXPRESSION)) {
            return RapidPrimitiveType.ANYTYPE;
        }
        if (expression instanceof UnaryExpression unaryExpression) {
            return switch (unaryExpression.getOperator()) {
                case NOT -> RapidPrimitiveType.BOOLEAN;
                case NEGATE -> RapidPrimitiveType.NUMBER;
            };
        }
        if (expression instanceof BinaryExpression binaryExpression) {
            Expression alternative = binaryExpression.getLeft().equals(current) ? binaryExpression.getRight() : binaryExpression.getLeft();
            RapidType type = getCorrectType(alternative);
            return switch (binaryExpression.getOperator()) {
                case ADD -> {
                    if (RapidPrimitiveType.STRING.isAssignable(type)) {
                        yield RapidPrimitiveType.STRING;
                    }
                    yield RapidPrimitiveType.NUMBER;
                }
                case SUBTRACT, DIVIDE, MULTIPLY, MODULO, LESS_THAN, LESS_THAN_OR_EQUAL, INTEGER_DIVIDE, GREATER_THAN, GREATER_THAN_OR_EQUAL ->
                        RapidPrimitiveType.NUMBER;
                case EQUAL_TO, NOT_EQUAL_TO -> type;
                case AND, XOR, OR -> RapidPrimitiveType.BOOLEAN;
            };
        }
        if (expression instanceof AggregateExpression aggregateExpression) {
            RapidType type = getCorrectType(expression);
            if (type.getDimensions() > 0) {
                return type.createArrayType(type.getDimensions() - 1);
            }
            if (type.getRootStructure() instanceof RapidRecord record) {
                int index = aggregateExpression.getExpressions().indexOf(current);
                List<? extends RapidComponent> components = record.getComponents();
                if (index < 0 || index >= components.size()) {
                    return RapidPrimitiveType.BOOLEAN;
                }
                RapidType componentType = components.get(index).getType();
                return Objects.requireNonNullElse(componentType, RapidPrimitiveType.BOOLEAN);
            }
        }
        if (expression instanceof IndexExpression indexExpression) {
            if (current.equals(indexExpression.getIndex())) {
                return RapidPrimitiveType.NUMBER;
            }
            if (current.equals(indexExpression.getVariable())) {
                RapidType correctType = getCorrectType(indexExpression);
                return correctType.createArrayType(correctType.getDimensions() + 1);
            }
        }
        return RapidPrimitiveType.ANYTYPE;
    }

    @SuppressWarnings("unchecked")
    private @Nullable Sort getSort(@NotNull RapidType type) {
        if (type.equals(RapidPrimitiveType.ANYTYPE)) {
            return null;
        }
        if (type.isAssignable(RapidPrimitiveType.STRING)) {
            return context.mkStringSort();
        }
        if (type.isAssignable(RapidPrimitiveType.BOOLEAN)) {
            return context.mkBoolSort();
        }
        if (type.isAssignable(RapidPrimitiveType.NUMBER)) {
            return context.mkRealSort();
        }
        if (type.isArray()) {
            Sort sort = Objects.requireNonNullElseGet(getSort(type.createArrayType(type.getDimensions() - 1)), context::getRealSort);
            return context.mkArraySort(new Sort[]{context.getRealSort()}, sort);
        }
        if (type.isRecord() && type.getRootStructure() instanceof RapidRecord record) {
            Sort[] sorts = record.getComponents().stream()
                                 .map(RapidVariable::getType)
                                 .filter(Objects::nonNull)
                                 .map(componentType -> Objects.requireNonNullElseGet(getSort(componentType), context::getRealSort))
                                 .toList()
                                 .toArray(new Sort[0]);
            String qualifiedName = Objects.requireNonNullElseGet(record.getQualifiedName(), () -> ":" + RapidSymbol.getDefaultText());
            String[] componentNames = record.getComponents().stream()
                                            .map(RapidSymbol::getName)
                                            .filter(Objects::nonNull)
                                            .toList()
                                            .toArray(new String[0]);
            Constructor<Object> constructor = context.mkConstructor("create:" + qualifiedName, "is:" + qualifiedName, componentNames, sorts, null);
            return context.mkDatatypeSort(qualifiedName, new Constructor[]{constructor});
        }
        return null;
    }

    private @NotNull Expr<?> createPointer(@NotNull Snapshot expression) {
        return pointers.computeIfAbsent(expression, snapshot -> context.mkRealConst(getName(snapshot) + "*"));
    }

    @Override
    public Expr<?> visitSnapshotExpression(@NotNull SnapshotExpression expression) {
        Snapshot snapshot = expression.getSnapshot();
        if (symbols.containsKey(snapshot)) {
            return symbols.get(snapshot);
        }
        RapidType correctType = getCorrectType(expression);
        Sort sort = getSort(correctType);
        if (sort == null) {
            return createPointer(snapshot);
        }
        String name = getName(snapshot);
        Expr<?> expr = context.mkConst(name, sort);
        symbols.put(snapshot, expr);
        switch (snapshot.getOptionality()) {
            case PRESENT -> queue.add(getAsBoolean(FunctionCallExpression.present(snapshot).accept(this)));
            case MISSING -> {
                UnaryExpression unaryExpression = new UnaryExpression(UnaryOperator.NOT, FunctionCallExpression.present(snapshot));
                queue.add(getAsBoolean(unaryExpression.accept(this)));
            }
            case NO_VALUE -> queue.add(context.mkFalse());
            case UNKNOWN -> {}
        }
        if (!(correctType.equals(expression.getType()))) {
            symbols.remove(snapshot);
            if (suffixes.get(snapshot.hashCode()).decrementAndGet() == 0) {
                suffixes.remove(snapshot.hashCode());
            }
        }
        return expr;
    }

    private @NotNull String getName(@NotNull Snapshot snapshot) {
        int hashCode = snapshot.hashCode();
        if (suffixes.containsKey(hashCode)) {
            return hashCode + ":" + suffixes.get(hashCode).getAndIncrement();
        } else {
            suffixes.put(hashCode, new AtomicInteger());
            return String.valueOf(hashCode);
        }
    }

    @Override
    public Expr<?> visitConstantExpression(@NotNull LiteralExpression expression) {
        Object object = expression.getValue();
        if (object instanceof String value) {
            return context.mkString(value);
        }
        if (object instanceof Boolean value) {
            return context.mkBool(value);
        }
        if (object instanceof Number value) {
            RapidType type = expression.getType();
            if (type.isAssignable(RapidPrimitiveType.NUMBER)) {
                double v = value.doubleValue();
                if ((long) v == v) {
                    return context.mkReal((long) v);
                }
                return context.mkFPToReal(context.mkFP(value.doubleValue(), context.mkFPSort(8, 24)));
            }
        }
        throw new IllegalArgumentException("Cannot create expression for: " + expression);
    }

    @Override
    public Expr<?> visitExpression(@NotNull Expression expression) {
        throw new IllegalArgumentException("Cannot create expression for: " + expression);
    }
}
