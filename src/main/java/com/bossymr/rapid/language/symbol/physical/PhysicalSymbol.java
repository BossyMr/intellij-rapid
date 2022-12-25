package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

public interface PhysicalSymbol extends RapidSymbol, RapidElement, PsiNameIdentifierOwner, NavigatablePsiElement {}
