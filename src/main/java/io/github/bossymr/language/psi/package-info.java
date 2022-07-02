/**
 * Contains all functionality coupled to the structure of the language. Although parsing is automatically generated,
 * elements are handcrafted, allowing features such as documentation, and for handcrafted element accessors. A goal for
 * the design of these elements is to encapsulate as much of the access to PsiElement's as possible. This includes
 * modifying the element through setters instead of manually replacing child elements.
 * <p>
 * Additionally, some elements are not made accessible, as they are used to structure nodes, but do not need to be
 * accessed by others. This includes the attribute list of a module, which can be accessed directly through the module.
 */
package io.github.bossymr.language.psi;