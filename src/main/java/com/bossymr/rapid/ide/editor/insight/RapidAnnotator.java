package com.bossymr.rapid.ide.editor.insight;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.fix.*;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidFile;
import com.bossymr.rapid.language.symbol.ModuleType;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.resolve.RapidResolveService;
import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class RapidAnnotator extends RapidElementVisitor implements Annotator {

    private AnnotationHolder annotationHolder;

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder annotationHolder) {
        try {
            this.annotationHolder = annotationHolder;
            element.accept(this);
        } finally {
            this.annotationHolder = null;
        }
    }

    @Override
    public void visitSymbol(@NotNull PhysicalSymbol symbol) {
        checkIdentifierLength(symbol);
        checkDuplicateSymbol(symbol);
        super.visitSymbol(symbol);
    }

    private void checkIdentifierLength(@NotNull PhysicalSymbol symbol) {
        PsiElement identifier = symbol.getNameIdentifier();
        if (identifier == null) {
            return;
        }
        if (identifier.getTextLength() > 32) {
            annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.identifier.length"))
                            .range(identifier)
                            .withFix(new InvokeRenameElementFix(symbol))
                            .create();
        }
    }

    private void checkDuplicateSymbol(@NotNull PhysicalSymbol symbol) {
        PsiElement identifier = symbol.getNameIdentifier();
        String name = symbol.getName();
        if (identifier == null || name == null) {
            return;
        }
        RapidResolveService service = RapidResolveService.getInstance(symbol.getProject());
        List<RapidSymbol> symbols = service.findSymbols(symbol, name);
        if (symbols.isEmpty() || symbols.indexOf(symbol) == 0) {
            return;
        }
        RapidSymbol duplicateSymbol = symbols.get(0);
        AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.declaration.duplicate.symbol", name))
                                                              .range(identifier);
        if (duplicateSymbol instanceof PhysicalSymbol physicalSymbol) {
            annotationBuilder = annotationBuilder.withFix(new NavigateToAlreadyDeclaredSymbolFix(physicalSymbol));
        }
        annotationBuilder.create();
    }

    @Override
    public void visitModule(@NotNull PhysicalModule module) {
        checkModuleName(module);
        super.visitModule(module);
    }

    private void checkModuleName(@NotNull PhysicalModule module) {
        PsiElement identifier = module.getNameIdentifier();
        String name = module.getName();
        if (identifier == null || name == null) {
            return;
        }
        PsiFile containingFile = module.getContainingFile();
        String fileName = containingFile.getViewProvider().getVirtualFile().getNameWithoutExtension();
        if (name.equals(fileName)) {
            return;
        }
        AnnotationBuilder annotationBuilder = annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.name", name))
                                                              .range(identifier);
        if (containingFile instanceof RapidFile file) {
            List<PhysicalModule> modules = file.getModules();
            if (modules.size() > 1) {
                PsiDirectory containingDirectory = containingFile.getContainingDirectory();
                PsiFile correctFile = containingDirectory.findFile(name + RapidFileType.DEFAULT_DOT_EXTENSION);
                if (correctFile == null) {
                    annotationBuilder = annotationBuilder.withFix(new MoveModuleToSeparateFileFix(module));
                }
            }
            boolean canRenameFile = modules.stream().noneMatch(otherModule -> fileName.equals(otherModule.getName()));
            if (canRenameFile) {
                annotationBuilder = annotationBuilder.withFix(new RenameElementFix(containingFile, name + RapidFileType.DEFAULT_DOT_EXTENSION))
                                                     .withFix(new RenameElementFix(module, fileName));
            }
        }
        annotationBuilder.create();
    }

    @Override
    public void visitAttributeList(@NotNull RapidAttributeList attributeList) {
        checkAttributeList(attributeList);
        super.visitAttributeList(attributeList);
    }

    private void checkAttributeList(@NotNull RapidAttributeList attributeList) {
        List<ASTNode> unsorted = List.of(attributeList.getNode().getChildren(ModuleType.TOKEN_SET));
        List<ASTNode> sorted = new ArrayList<>(unsorted);
        List<ModuleType> attributes = unsorted.stream().map(node -> ModuleType.getAttribute(node.getElementType())).toList();
        sorted.sort(Comparator.comparing(element -> ModuleType.getAttribute(element.getElementType()), Comparator.comparing(Enum::ordinal)));
        for (ASTNode element : unsorted) {
            ModuleType moduleType = ModuleType.getAttribute(element.getElementType());
            int index = unsorted.indexOf(element);
            if (sorted.indexOf(element) > index) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.order"))
                                .range(element)
                                .withFix(new ReorderModuleAttributeFix(attributeList))
                                .create();
            }
            int firstIndex = attributes.indexOf(moduleType);
            if (firstIndex != attributes.lastIndexOf(moduleType) && index == firstIndex) {
                annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.duplicate"))
                                .range(element)
                                .withFix(new RemoveModuleAttributeFix(element.getPsi()))
                                .create();
            }
            if (ModuleType.MUTUALLY_EXCLUSIVE.containsKey(moduleType)) {
                List<ModuleType> exlusiveList = ModuleType.MUTUALLY_EXCLUSIVE.get(moduleType);
                Optional<ModuleType> exlusiveType = attributes.stream().filter(exlusiveList::contains).findFirst();
                if (exlusiveType.isPresent()) {
                    ModuleType otherType = exlusiveType.orElseThrow();
                    annotationHolder.newAnnotation(HighlightSeverity.ERROR, RapidBundle.message("annotation.module.attribute.mutually.exclusive", otherType.getText(), moduleType.getText()))
                                    .range(element)
                                    .withFix(new RemoveModuleAttributeFix(element.getPsi()))
                                    .withFix(new RemoveModuleAttributeFix(unsorted.get(attributes.indexOf(otherType)).getPsi()))
                                    .create();
                }
            }
        }
    }
}