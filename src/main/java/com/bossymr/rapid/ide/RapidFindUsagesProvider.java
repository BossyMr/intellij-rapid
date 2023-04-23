package com.bossymr.rapid.ide;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.lexer.RapidLexer;
import com.bossymr.rapid.language.psi.FormatUtil;
import com.bossymr.rapid.language.psi.FormatUtil.Option;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.psi.RapidTargetVariable;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.symbol.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalRecord;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class RapidFindUsagesProvider implements FindUsagesProvider {

    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new DefaultWordsScanner(new RapidLexer(),
                TokenSet.create(RapidTokenTypes.IDENTIFIER),
                TokenSet.create(RapidTokenTypes.COMMENT),
                TokenSet.create(RapidTokenTypes.INTEGER_LITERAL, RapidTokenTypes.STRING_LITERAL, RapidTokenTypes.TRUE_KEYWORD, RapidTokenTypes.FALSE_KEYWORD));
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement element) {
        return element instanceof RapidSymbol;
    }

    @Override
    public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public @Nls @NotNull String getType(@NotNull PsiElement element) {
        if (element instanceof RapidFile) return RapidBundle.message("element.type.file");
        if (element instanceof RapidModule) return RapidBundle.message("element.type.module");
        if (element instanceof RapidAtomic) return RapidBundle.message("element.type.atomic");
        if (element instanceof RapidAlias) return RapidBundle.message("element.type.alias");
        if (element instanceof RapidRecord) return RapidBundle.message("element.type.alias");
        if (element instanceof RapidComponent) return RapidBundle.message("element.type.alias");
        if (element instanceof RapidRoutine routine) {
            return switch (routine.getRoutineType()) {
                case FUNCTION -> RapidBundle.message("element.type.function");
                case PROCEDURE -> RapidBundle.message("element.type.procedure");
                case TRAP -> RapidBundle.message("element.type.trap");
            };
        }
        if (element instanceof RapidField field) {
            return switch (field.getFieldType()) {
                case VARIABLE -> RapidBundle.message("element.type.variable");
                case CONSTANT -> RapidBundle.message("element.type.constant");
                case PERSISTENT -> RapidBundle.message("element.type.persistent");
            };
        }
        if (element instanceof RapidTargetVariable) return RapidBundle.message("element.type.variable");
        if (element instanceof RapidLabelStatement) return RapidBundle.message("element.type.label");
        if (element instanceof RapidParameter) return RapidBundle.message("element.type.parameter");
        return "";
    }

    @Override
    public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof RapidFile) {
            return ((RapidFile) element).getVirtualFile().getPresentableUrl();
        }
        if (element instanceof RapidAtomic atomic) {
            return FormatUtil.format(atomic,
                    EnumSet.of(Option.SHOW_NAME));
        }
        if (element instanceof RapidAlias alias) {
            return FormatUtil.format(alias, EnumSet.of(Option.SHOW_NAME));
        }
        if (element instanceof RapidRecord record) {
            return FormatUtil.format(record,
                    EnumSet.of(Option.SHOW_NAME, Option.SHOW_COMPONENTS),
                    EnumSet.of(Option.SHOW_TYPE));
        }
        if (element instanceof RapidComponent component) {
            return FormatUtil.format(component, EnumSet.of(Option.SHOW_NAME));
        }
        if (element instanceof RapidRoutine routine) {
            return FormatUtil.format(routine,
                    EnumSet.of(Option.SHOW_NAME, Option.SHOW_PARAMETERS),
                    EnumSet.of(Option.SHOW_TYPE));
        }
        if (element instanceof RapidField field) {
            return FormatUtil.format(field, EnumSet.of(Option.SHOW_NAME));
        }
        if (element instanceof RapidSymbol symbol) {
            String name = symbol.getName();
            return name != null ? name : "";
        }
        return "";
    }

    @Override
    public @Nls @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof RapidFile file) {
            return useFullName ? file.getVirtualFile().getPresentableUrl() : file.getName();
        }
        if (element instanceof RapidAtomic atomic) {
            return FormatUtil.format(atomic,
                    EnumSet.of(Option.SHOW_TYPE_AFTER, Option.SHOW_TYPE, Option.SHOW_NAME));
        }
        if (element instanceof RapidAlias alias) {
            return FormatUtil.format(alias,
                    EnumSet.of(Option.SHOW_TYPE_AFTER, Option.SHOW_TYPE, Option.SHOW_NAME));
        }
        if (element instanceof RapidRecord record) {
            if (useFullName) {
                return FormatUtil.format(record,
                        EnumSet.of(Option.SHOW_TYPE_AFTER, Option.SHOW_TYPE, Option.SHOW_NAME, Option.SHOW_COMPONENTS),
                        EnumSet.of(Option.SHOW_TYPE, Option.SHOW_NAME));
            } else {
                return FormatUtil.format(record,
                        EnumSet.of(Option.SHOW_NAME, Option.SHOW_COMPONENTS),
                        EnumSet.of(Option.SHOW_TYPE));
            }
        }
        if (element instanceof RapidComponent component) {
            RapidRecord record = PsiTreeUtil.getParentOfType(element, PhysicalRecord.class);
            assert record != null;
            String componentFormat = FormatUtil.format(component, EnumSet.of(Option.SHOW_TYPE_AFTER, Option.SHOW_TYPE, Option.SHOW_NAME));
            String recordFormat = FormatUtil.format(record, EnumSet.of(Option.SHOW_NAME, Option.SHOW_COMPONENTS), EnumSet.of(Option.SHOW_TYPE));
            return RapidBundle.message("element.node.component.of.record", componentFormat, recordFormat);
        }
        if (element instanceof RapidRoutine routine) {
            if (useFullName) {
                return FormatUtil.format(routine,
                        EnumSet.of(Option.SHOW_TYPE_AFTER, Option.SHOW_TYPE, Option.SHOW_NAME, Option.SHOW_PARAMETERS),
                        EnumSet.of(Option.SHOW_TYPE, Option.SHOW_NAME));
            } else {
                return FormatUtil.format(routine,
                        EnumSet.of(Option.SHOW_NAME, Option.SHOW_PARAMETERS),
                        EnumSet.of(Option.SHOW_TYPE));
            }
        }
        if (element instanceof RapidField field) {
            return FormatUtil.format(field, EnumSet.of(Option.SHOW_NAME, Option.SHOW_TYPE, Option.SHOW_TYPE_AFTER));
        }
        if (element instanceof RapidParameter parameter) {
            return FormatUtil.format(parameter, EnumSet.of(Option.SHOW_NAME, Option.SHOW_TYPE, Option.SHOW_TYPE_AFTER));
        }
        if (element instanceof RapidSymbol symbol) {
            String name = symbol.getName();
            return name != null ? name : "";
        }
        return "";
    }
}
