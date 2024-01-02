package com.bossymr.rapid.ide.execution.debugger.frame;

import com.bossymr.network.NetworkManager;
import com.bossymr.rapid.ide.execution.debugger.RapidSourcePosition;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.language.type.RapidType;
import com.bossymr.rapid.robot.network.robotware.rapid.RapidService;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.QueryableSymbol;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolModel;
import com.bossymr.rapid.robot.network.robotware.rapid.symbol.SymbolValue;
import com.bossymr.rapid.robot.network.robotware.rapid.task.StackFrame;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.frame.presentation.XErrorValuePresentation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class RapidSymbolValue extends XNamedValue {

    private final @NotNull RapidVariable symbol;
    private final @NotNull StackFrame stackFrame;
    private final @NotNull NetworkManager manager;

    public RapidSymbolValue(@NotNull NetworkManager manager, @NotNull RapidVariable symbol, @NotNull StackFrame stackFrame) {
        super(symbol.getName() != null ? symbol.getName() : "");
        this.manager = manager;
        this.symbol = symbol;
        this.stackFrame = stackFrame;
    }

    protected static @NotNull String getCanonicalName(@NotNull RapidVariable symbol, @NotNull StackFrame stackFrame) {
        StringJoiner stringJoiner = new StringJoiner("/");
        stringJoiner.add("RAPID");
        RapidSymbol parent = symbol;
        if (symbol instanceof RapidComponent component) {
            parent = component.getRecord();
        }
        if (symbol instanceof RapidParameter parameter) {
            parent = parameter.getParameterGroup().getRoutine();
        }
        if (parent instanceof VirtualSymbol virtualSymbol) {
            stringJoiner.add(virtualSymbol.getName());
        } else if (parent instanceof PhysicalSymbol physicalSymbol) {
            String taskName = stackFrame.getRoutine().split("/")[1];
            stringJoiner.add(taskName);
            PhysicalModule module = PsiTreeUtil.getParentOfType(physicalSymbol, PhysicalModule.class, false);
            if (module == null) throw new IllegalStateException();
            stringJoiner.add(module.getName());
            stringJoiner.add(physicalSymbol.getName());
        } else {
            throw new IllegalStateException();
        }
        if (parent != symbol) {
            stringJoiner.add(symbol.getName());
        }
        return stringJoiner.toString();
    }

    public static @NotNull QueryableSymbol findSymbol(@NotNull NetworkManager manager, @NotNull RapidVariable symbol, @NotNull StackFrame stackFrame) throws IOException, InterruptedException {
        SymbolModel symbolModel = manager.createService(RapidService.class)
                .findSymbol(getCanonicalName(symbol, stackFrame)).get();
        if (!(symbolModel instanceof QueryableSymbol queryableSymbol)) {
            throw new IllegalStateException();
        }
        return queryableSymbol;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void computePresentation(@NotNull XValueNode node, @NotNull XValuePlace place) {
        try {
            String value = getValue();
            RapidType dataType = symbol.getType();
            boolean hasChildren = dataType != null && dataType.getRootStructure() instanceof RapidRecord record && !(record.getComponents().isEmpty());
            node.setPresentation(symbol.getTargetPresentation().getIcon(), new RapidValuePresentation(dataType, value), hasChildren);
        } catch (IOException e) {
            node.setPresentation(null, new XErrorValuePresentation(e.getLocalizedMessage()), false);
        } catch (InterruptedException ignored) {}
    }

    protected @NotNull String getValue() throws IOException, InterruptedException {
        QueryableSymbol queryableSymbol = findSymbol(manager, symbol, stackFrame);
        SymbolValue value = queryableSymbol.getValue().get();
        return value.getValue();
    }

    @Override
    public boolean canNavigateToSource() {
        return symbol instanceof PhysicalSymbol;
    }

    @Override
    public boolean canNavigateToTypeSource() {
        RapidType dataType = symbol.getType();
        return dataType != null && dataType.getStructure() instanceof PhysicalSymbol;
    }

    @Override
    public void computeSourcePosition(@NotNull XNavigatable navigatable) {
        if (symbol instanceof PhysicalSymbol physicalSymbol) {
            navigatable.setSourcePosition(RapidSourcePosition.create(physicalSymbol));
        } else {
            navigatable.setSourcePosition(null);
        }
    }

    @Override
    public void computeTypeSourcePosition(@NotNull XNavigatable navigatable) {
        PsiElement typeStructure = getTypeStructure();
        if (typeStructure != null) {
            navigatable.setSourcePosition(RapidSourcePosition.create(typeStructure));
        } else {
            navigatable.setSourcePosition(null);
        }
    }

    private @Nullable PsiElement getTypeStructure() {
        RapidType dataType = symbol.getType();
        if (dataType == null) return null;
        RapidStructure structure = dataType.getStructure();
        if (!(structure instanceof PhysicalSymbol physicalSymbol)) return null;
        return physicalSymbol;
    }

    @Override
    public void computeChildren(@NotNull XCompositeNode node) {
        if (symbol.getType() != null && symbol.getType().getRootStructure() instanceof RapidRecord record) {
            XValueChildrenList childrenList = new XValueChildrenList();
            List<? extends RapidComponent> components = record.getComponents();
            for (int i = 0; i < components.size(); i++) {
                childrenList.add(new RapidComponentValue(manager, components.get(i), i));
            }
            node.addChildren(childrenList, true);
        }
    }

    public class RapidComponentValue extends RapidSymbolValue {

        private final int index;

        public RapidComponentValue(@NotNull NetworkManager manager, @NotNull RapidComponent symbol, int index) {
            super(manager, symbol, stackFrame);
            this.index = index;
        }

        public static @NotNull String getValue(@NotNull String value, int index) {
            List<Integer> stack = new ArrayList<>();
            StringBuilder stringBuilder = null;
            for (int i = 0; i < value.length(); i++) {
                char character = value.charAt(i);
                switch (character) {
                    case '[' -> stack.add(0);
                    case ']' -> {
                        assert !(stack.isEmpty());
                        stack.remove(stack.size() - 1);
                    }
                    case ',' -> {
                        assert !(stack.isEmpty());
                        stack.set(stack.size() - 1, stack.get(stack.size() - 1) + 1);
                    }
                    default -> {}
                }
                switch (character) {
                    case '[', ']', ',' -> {
                        if (isAtIndex(stack, index)) {
                            if (stringBuilder == null) {
                                stringBuilder = new StringBuilder();
                            } else {
                                stringBuilder.append(character);
                            }
                        } else {
                            if (stringBuilder != null) {
                                return stringBuilder.toString();
                            }
                        }
                    }
                    default -> {
                        if (stringBuilder != null) {
                            stringBuilder.append(character);
                        }
                    }
                }
            }
            throw new IllegalStateException();
        }

        @Contract(pure = true)
        private static boolean isAtIndex(@NotNull List<Integer> stack, int index) {
            if (stack.isEmpty()) {
                return false;
            }
            return stack.get(0) == index;
        }

        @Override
        protected @NotNull String getValue() throws IOException, InterruptedException {
            String value = RapidSymbolValue.this.getValue();
            return getValue(value, index);
        }
    }
}
