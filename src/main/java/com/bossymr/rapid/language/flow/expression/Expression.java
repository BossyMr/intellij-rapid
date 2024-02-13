package com.bossymr.rapid.language.flow.expression;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.expression.FunctionCallExpression.Entry;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An expression represents an expression.
 */
public interface Expression {

    /**
     * Returns the type for this expression.
     *
     * @return the type for this expression.
     */
    @NotNull RapidType getType();

    <R> R accept(@NotNull ControlFlowVisitor<R> visitor);

    @Nullable RapidExpression getElement();

    default void iterate(@NotNull Predicate<Expression> consumer) {
        AtomicBoolean cancelled = new AtomicBoolean();
        accept(new ControlFlowVisitor<>() {
            @Override
            public Object visitFunctionCallExpression(@NotNull FunctionCallExpression expression) {
                for (Entry value : expression.getArguments()) {
                    if (value instanceof Entry.ValueEntry valueEntry) {
                        valueEntry.expression().accept(this);
                    }
                    if (value instanceof Entry.ReferenceEntry referenceEntry) {
                        new SnapshotExpression(referenceEntry.snapshot()).accept(this);
                    }
                    if (cancelled.get()) {
                        return null;
                    }
                }
                return null;
            }

            @Override
            public Void visitAggregateExpression(@NotNull AggregateExpression expression) {
                for (Expression component : expression.getExpressions()) {
                    component.accept(this);
                    if (cancelled.get()) {
                        return null;
                    }
                }
                cancelled.set(cancelled.get() || consumer.test(expression));
                return null;
            }

            @Override
            public Void visitBinaryExpression(@NotNull BinaryExpression expression) {
                expression.getLeft().accept(this);
                if (cancelled.get()) {
                    return null;
                }
                expression.getRight().accept(this);
                cancelled.set(cancelled.get() || consumer.test(expression));
                return null;
            }

            @Override
            public Void visitUnaryExpression(@NotNull UnaryExpression expression) {
                expression.getExpression().accept(this);
                cancelled.set(cancelled.get() || consumer.test(expression));
                return null;
            }

            @Override
            public Void visitIndexExpression(@NotNull IndexExpression expression) {
                expression.getVariable().accept(this);
                if (cancelled.get()) {
                    return null;
                }
                expression.getIndex().accept(this);
                cancelled.set(cancelled.get() || consumer.test(expression));
                return null;
            }

            @Override
            public Void visitComponentExpression(@NotNull ComponentExpression expression) {
                expression.getVariable().accept(this);
                cancelled.set(cancelled.get() || consumer.test(expression));
                return null;
            }

            @Override
            public Void visitExpression(@NotNull Expression expression) {
                cancelled.set(cancelled.get() || consumer.test(expression));
                return null;
            }
        });
    }

    default @NotNull Expression replace(@NotNull Function<Expression, Expression> mapper) {
        return accept(new ControlFlowVisitor<>() {
            @Override
            public Expression visitFunctionCallExpression(@NotNull FunctionCallExpression expression) {
                List<Entry> arguments = expression.getArguments().stream()
                                                  .map(entry -> {
                                                      if (entry instanceof Entry.ValueEntry valueEntry) {
                                                          return new Entry.ValueEntry(valueEntry.expression().accept(this));
                                                      }
                                                      if (entry instanceof Entry.ReferenceEntry referenceEntry) {
                                                          SnapshotExpression snapshot = new SnapshotExpression(referenceEntry.snapshot());
                                                          Expression replaceValue = snapshot.accept(this);
                                                          if (!(replaceValue instanceof SnapshotExpression replaceSnapshot)) {
                                                              throw new IllegalArgumentException("Could not replace snapshot: " + referenceEntry.snapshot() + " with: " + replaceValue);
                                                          }
                                                          return new Entry.ReferenceEntry(replaceSnapshot.getSnapshot());
                                                      }
                                                      return entry;
                                                  })
                                                  .toList();
                return new FunctionCallExpression(expression.getElement(), expression.getType(), expression.getName(), arguments);
            }

            @Override
            public Expression visitAggregateExpression(@NotNull AggregateExpression expression) {
                List<Expression> expressions = expression.getExpressions().stream()
                                                         .map(component -> component.accept(this))
                                                         .toList();
                AggregateExpression aggregateExpression = new AggregateExpression(expression.getElement(), expression.getType(), expressions);
                return mapper.apply(aggregateExpression);
            }

            @Override
            public Expression visitBinaryExpression(@NotNull BinaryExpression expression) {
                Expression left = expression.getLeft().accept(this);
                Expression right = expression.getRight().accept(this);
                BinaryExpression binaryExpression = new BinaryExpression(expression.getElement(), expression.getOperator(), left, right);
                return mapper.apply(binaryExpression);
            }

            @Override
            public Expression visitUnaryExpression(@NotNull UnaryExpression expression) {
                Expression component = expression.getExpression().accept(this);
                UnaryExpression unaryExpression = new UnaryExpression(expression.getElement(), expression.getOperator(), component);
                return mapper.apply(unaryExpression);
            }

            @Override
            public Expression visitComponentExpression(@NotNull ComponentExpression expression) {
                ReferenceExpression variable = ((ReferenceExpression) expression.getVariable().accept(this));
                ComponentExpression componentExpression = new ComponentExpression(expression.getElement(), expression.getType(), variable, expression.getComponent());
                return super.visitComponentExpression(componentExpression);
            }

            @Override
            public Expression visitIndexExpression(@NotNull IndexExpression expression) {
                ReferenceExpression variable = ((ReferenceExpression) expression.getVariable().accept(this));
                Expression index = expression.getIndex().accept(this);
                IndexExpression indexExpression = new IndexExpression(expression.getElement(), variable, index);
                return super.visitIndexExpression(indexExpression);
            }

            @Override
            public Expression visitExpression(@NotNull Expression expression) {
                return mapper.apply(expression);
            }
        });
    }

    default @NotNull Collection<Expression> getComponents() {
        Set<Expression> expressions = new HashSet<>();
        replace(expression -> {
            expressions.add(expression);
            return expression;
        });
        return expressions;
    }

}
