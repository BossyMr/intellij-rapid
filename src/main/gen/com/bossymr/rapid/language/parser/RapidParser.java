// This is a generated file. Not intended for manual editing.
package com.bossymr.rapid.language.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.bossymr.rapid.language.psi.RapidElementTypes.*;
import static com.bossymr.rapid.language.parser.RapidParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static com.bossymr.rapid.language.psi.RapidTokenTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class RapidParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, EXTENDS_SETS_);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return file(builder_, level_ + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(AGGREGATE_EXPRESSION, BINARY_EXPRESSION, EMPTY_EXPRESSION, FUNCTION_CALL_EXPRESSION,
      INDEX_EXPRESSION, LITERAL_EXPRESSION, PARENTHESISED_EXPRESSION, REFERENCE_EXPRESSION,
      UNARY_EXPRESSION),
  };

  /* ********************************************************** */
  // visibility? 'ALIAS' type_element identifier ';'
  public static boolean alias(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alias")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ALIAS, "<alias>");
    result_ = alias_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ALIAS_KEYWORD);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, type_element(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, identifier(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // visibility?
  private static boolean alias_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "alias_0")) return false;
    visibility(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // [first_argument (next_argument)*]
  static boolean argument_list_body(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argument_list_body")) return false;
    argument_list_body_0(builder_, level_ + 1);
    return true;
  }

  // first_argument (next_argument)*
  private static boolean argument_list_body_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argument_list_body_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = first_argument(builder_, level_ + 1);
    result_ = result_ && argument_list_body_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (next_argument)*
  private static boolean argument_list_body_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argument_list_body_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!argument_list_body_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "argument_list_body_0_1", pos_)) break;
    }
    return true;
  }

  // (next_argument)
  private static boolean argument_list_body_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argument_list_body_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = next_argument(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '{' array_expression (array_tail)* '}'
  public static boolean array(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARRAY, null);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, array_expression(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, array_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (array_tail)*
  private static boolean array_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!array_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "array_2", pos_)) break;
    }
    return true;
  }

  // (array_tail)
  private static boolean array_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = array_tail(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // () empty_expression
  static boolean array_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_expression")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = array_expression_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RapidParser::array_expression_recovery);
    return result_ || pinned_;
  }

  // ()
  private static boolean array_expression_0(PsiBuilder builder_, int level_) {
    return true;
  }

  /* ********************************************************** */
  // !(',' | '}' | ':=' | ';') symbol_recovery
  static boolean array_expression_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_expression_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = array_expression_recovery_0(builder_, level_ + 1);
    result_ = result_ && symbol_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(',' | '}' | ':=' | ';')
  private static boolean array_expression_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_expression_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !array_expression_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ',' | '}' | ':=' | ';'
  private static boolean array_expression_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_expression_recovery_0_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, COMMA);
    if (!result_) result_ = consumeTokenFast(builder_, RBRACE);
    if (!result_) result_ = consumeTokenFast(builder_, CEQ);
    if (!result_) result_ = consumeTokenFast(builder_, SEMICOLON);
    return result_;
  }

  /* ********************************************************** */
  // ',' array_expression
  static boolean array_tail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "array_tail")) return false;
    if (!nextTokenIs(builder_, COMMA)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && array_expression(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // variable_target ':=' empty_expression ';'
  public static boolean assignment_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignment_statement")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ASSIGNMENT_STATEMENT, "<assignment statement>");
    result_ = variable_target(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CEQ);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, empty_expression(builder_, level_ + 1, -1));
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // () ('SYSMODULE' | 'NOVIEW' | 'NOSTEPIN' | 'VIEWONLY' | 'READONLY')
  static boolean attribute(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = attribute_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && attribute_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RapidParser::attribute_recovery);
    return result_ || pinned_;
  }

  // ()
  private static boolean attribute_0(PsiBuilder builder_, int level_) {
    return true;
  }

  // 'SYSMODULE' | 'NOVIEW' | 'NOSTEPIN' | 'VIEWONLY' | 'READONLY'
  private static boolean attribute_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, SYSMODULE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, NOVIEW_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, NOSTEPIN_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, VIEWONLY_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, READONLY_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // [!(<<eof>> | 'ENDMODULE') '(' attribute (attribute_list_tail)* ')']
  public static boolean attribute_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_list")) return false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ATTRIBUTE_LIST, "<attribute list>");
    attribute_list_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, true, false, null);
    return true;
  }

  // !(<<eof>> | 'ENDMODULE') '(' attribute (attribute_list_tail)* ')'
  private static boolean attribute_list_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_list_0")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = attribute_list_0_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LPARENTH);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, attribute(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, attribute_list_0_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RPARENTH) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // !(<<eof>> | 'ENDMODULE')
  private static boolean attribute_list_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_list_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !attribute_list_0_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // <<eof>> | 'ENDMODULE'
  private static boolean attribute_list_0_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_list_0_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = eof(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, ENDMODULE_KEYWORD);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (attribute_list_tail)*
  private static boolean attribute_list_0_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_list_0_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!attribute_list_0_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "attribute_list_0_3", pos_)) break;
    }
    return true;
  }

  // (attribute_list_tail)
  private static boolean attribute_list_0_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_list_0_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = attribute_list_tail(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ',' attribute
  static boolean attribute_list_tail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_list_tail")) return false;
    if (!nextTokenIs(builder_, COMMA)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && attribute(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // !(',' | ')') symbol_recovery
  static boolean attribute_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = attribute_recovery_0(builder_, level_ + 1);
    result_ = result_ && symbol_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(',' | ')')
  private static boolean attribute_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !attribute_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ',' | ')'
  private static boolean attribute_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attribute_recovery_0_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, COMMA);
    if (!result_) result_ = consumeTokenFast(builder_, RPARENTH);
    return result_;
  }

  /* ********************************************************** */
  // 'BACKWARD' statement_list
  public static boolean backward_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "backward_clause")) return false;
    if (!nextTokenIs(builder_, BACKWARD_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT_LIST, null);
    result_ = consumeToken(builder_, BACKWARD_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && statement_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // !('ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'DEFAULT' | '<CSE>')
  static boolean case_section_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "case_section_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !case_section_recovery_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'DEFAULT' | '<CSE>'
  private static boolean case_section_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "case_section_recovery_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, ENDFUNC_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDPROC_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDTRAP_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDIF_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ELSEIF_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ELSE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDFOR_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDWHILE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDTEST_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, CASE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, DEFAULT_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, CSE_PLACEHOLDER);
    return result_;
  }

  /* ********************************************************** */
  // type_element identifier ';'
  public static boolean component(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "component")) return false;
    if (!nextTokenIsFast(builder_, IDENTIFIER)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, COMPONENT, null);
    result_ = type_element(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, identifier(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // (component)*
  static boolean component_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "component_list")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!component_list_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "component_list", pos_)) break;
    }
    return true;
  }

  // (component)
  private static boolean component_list_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "component_list_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = component(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'THEN' statement_list (else_if_statement)* else_statement? 'ENDIF'
  static boolean compound_if_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compound_if_statement")) return false;
    if (!nextTokenIs(builder_, THEN_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, THEN_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, statement_list(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, compound_if_statement_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, compound_if_statement_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ENDIF_KEYWORD) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (else_if_statement)*
  private static boolean compound_if_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compound_if_statement_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!compound_if_statement_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "compound_if_statement_2", pos_)) break;
    }
    return true;
  }

  // (else_if_statement)
  private static boolean compound_if_statement_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compound_if_statement_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = else_if_statement(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // else_statement?
  private static boolean compound_if_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compound_if_statement_3")) return false;
    else_statement(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // if_statement |
  //                                for_statement |
  //                                while_statement |
  //                                test_statement
  static boolean compound_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "compound_statement")) return false;
    boolean result_;
    result_ = if_statement(builder_, level_ + 1);
    if (!result_) result_ = for_statement(builder_, level_ + 1);
    if (!result_) result_ = while_statement(builder_, level_ + 1);
    if (!result_) result_ = test_statement(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // '\' unqualified_reference_expression '?' ('<VAR>' | empty_expression)
  public static boolean conditional_argument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_argument")) return false;
    if (!nextTokenIs(builder_, BACKSLASH)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BACKSLASH);
    result_ = result_ && unqualified_reference_expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, QUESTION);
    result_ = result_ && conditional_argument_3(builder_, level_ + 1);
    exit_section_(builder_, marker_, CONDITIONAL_ARGUMENT, result_);
    return result_;
  }

  // '<VAR>' | empty_expression
  private static boolean conditional_argument_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "conditional_argument_3")) return false;
    boolean result_;
    result_ = consumeToken(builder_, VAR_PLACEHOLDER);
    if (!result_) result_ = empty_expression(builder_, level_ + 1, -1);
    return result_;
  }

  /* ********************************************************** */
  // 'CONNECT' connect_target 'WITH' connect_with_target ';'
  public static boolean connect_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "connect_statement")) return false;
    if (!nextTokenIs(builder_, CONNECT_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, CONNECT_STATEMENT, null);
    result_ = consumeToken(builder_, CONNECT_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, connect_target(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, WITH_KEYWORD)) && result_;
    result_ = pinned_ && report_error_(builder_, connect_with_target(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // '<VAR>' | empty_expression
  static boolean connect_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "connect_target")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, VAR_PLACEHOLDER);
    if (!result_) result_ = empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, result_, false, RapidParser::connect_target_recovery);
    return result_;
  }

  /* ********************************************************** */
  // !('WITH' | ';') statement_recovery
  static boolean connect_target_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "connect_target_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = connect_target_recovery_0(builder_, level_ + 1);
    result_ = result_ && statement_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !('WITH' | ';')
  private static boolean connect_target_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "connect_target_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !connect_target_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'WITH' | ';'
  private static boolean connect_target_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "connect_target_recovery_0_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, WITH_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, SEMICOLON);
    return result_;
  }

  /* ********************************************************** */
  // empty_expression
  static boolean connect_with_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "connect_with_target")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, result_, false, RapidParser::connect_with_target_recovery);
    return result_;
  }

  /* ********************************************************** */
  // !(';') statement_recovery
  static boolean connect_with_target_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "connect_with_target_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = connect_with_target_recovery_0(builder_, level_ + 1);
    result_ = result_ && statement_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(';')
  private static boolean connect_with_target_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "connect_with_target_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !connect_with_target_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (';')
  private static boolean connect_with_target_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "connect_with_target_recovery_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenFast(builder_, SEMICOLON);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'ELSEIF' empty_expression 'THEN' (statement_list)
  public static boolean else_if_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "else_if_statement")) return false;
    if (!nextTokenIs(builder_, ELSEIF_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IF_STATEMENT, null);
    result_ = consumeToken(builder_, ELSEIF_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, empty_expression(builder_, level_ + 1, -1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, THEN_KEYWORD)) && result_;
    result_ = pinned_ && else_if_statement_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (statement_list)
  private static boolean else_if_statement_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "else_if_statement_3")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = statement_list(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'ELSE' statement_list
  static boolean else_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "else_statement")) return false;
    if (!nextTokenIs(builder_, ELSE_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, ELSE_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && statement_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // () (empty_expression)
  static boolean error(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = error_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && error_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RapidParser::error_recovery);
    return result_ || pinned_;
  }

  // ()
  private static boolean error_0(PsiBuilder builder_, int level_) {
    return true;
  }

  // (empty_expression)
  private static boolean error_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'ERROR' error_list? statement_list
  public static boolean error_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error_clause")) return false;
    if (!nextTokenIs(builder_, ERROR_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT_LIST, null);
    result_ = consumeToken(builder_, ERROR_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, error_clause_1(builder_, level_ + 1));
    result_ = pinned_ && statement_list(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // error_list?
  private static boolean error_clause_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error_clause_1")) return false;
    error_list(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' error (error_tail)* ')'
  public static boolean error_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error_list")) return false;
    if (!nextTokenIs(builder_, LPARENTH)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION_LIST, null);
    result_ = consumeToken(builder_, LPARENTH);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, error(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, error_list_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RPARENTH) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (error_tail)*
  private static boolean error_list_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error_list_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!error_list_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "error_list_2", pos_)) break;
    }
    return true;
  }

  // (error_tail)
  private static boolean error_list_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error_list_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = error_tail(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(',' | ')') statement_recovery
  static boolean error_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = error_recovery_0(builder_, level_ + 1);
    result_ = result_ && statement_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(',' | ')')
  private static boolean error_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !error_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ',' | ')'
  private static boolean error_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error_recovery_0_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, COMMA);
    if (!result_) result_ = consumeTokenFast(builder_, RPARENTH);
    return result_;
  }

  /* ********************************************************** */
  // ',' error
  static boolean error_tail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "error_tail")) return false;
    if (!nextTokenIs(builder_, COMMA)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && error(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // 'EXIT' ';'
  public static boolean exit_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "exit_statement")) return false;
    if (!nextTokenIs(builder_, EXIT_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXIT_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, EXIT_KEYWORD, SEMICOLON);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // visibility? ('VAR' | 'PERS' | 'CONST') type_element identifier array? [':=' optional_expression] ';'
  public static boolean field(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "field")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FIELD, "<field>");
    result_ = field_0(builder_, level_ + 1);
    result_ = result_ && field_1(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, type_element(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, identifier(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, field_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, field_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // visibility?
  private static boolean field_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "field_0")) return false;
    visibility(builder_, level_ + 1);
    return true;
  }

  // 'VAR' | 'PERS' | 'CONST'
  private static boolean field_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "field_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, VAR_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, PERS_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, CONST_KEYWORD);
    return result_;
  }

  // array?
  private static boolean field_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "field_4")) return false;
    array(builder_, level_ + 1);
    return true;
  }

  // [':=' optional_expression]
  private static boolean field_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "field_5")) return false;
    field_5_0(builder_, level_ + 1);
    return true;
  }

  // ':=' optional_expression
  private static boolean field_5_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "field_5_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CEQ);
    result_ = result_ && optional_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(<<eof>>) (module_with_recovery)*
  static boolean file(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "file")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = file_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && file_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // !(<<eof>>)
  private static boolean file_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "file_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !file_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // <<eof>>
  private static boolean file_0_0(PsiBuilder builder_, int level_) {
    return eof(builder_, level_ + 1);
  }

  // (module_with_recovery)*
  private static boolean file_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "file_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!file_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "file_1", pos_)) break;
    }
    return true;
  }

  // (module_with_recovery)
  private static boolean file_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "file_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = module_with_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '<ARG>' | optional_argument | conditional_argument | required_argument
  static boolean first_argument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "first_argument")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ARG_PLACEHOLDER);
    if (!result_) result_ = optional_argument(builder_, level_ + 1);
    if (!result_) result_ = conditional_argument(builder_, level_ + 1);
    if (!result_) result_ = required_argument(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // (!(')') parameter_statement_recovery) parameter_group
  static boolean first_parameter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "first_parameter")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = first_parameter_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && parameter_group(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // !(')') parameter_statement_recovery
  private static boolean first_parameter_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "first_parameter_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = first_parameter_0_0(builder_, level_ + 1);
    result_ = result_ && parameter_statement_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(')')
  private static boolean first_parameter_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "first_parameter_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !consumeToken(builder_, RPARENTH);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'FOR' target_variable 'FROM' empty_expression 'TO' empty_expression ['STEP' empty_expression] 'DO' statement_list 'ENDFOR'
  public static boolean for_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "for_statement")) return false;
    if (!nextTokenIs(builder_, FOR_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FOR_STATEMENT, null);
    result_ = consumeToken(builder_, FOR_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, target_variable(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, FROM_KEYWORD)) && result_;
    result_ = pinned_ && report_error_(builder_, empty_expression(builder_, level_ + 1, -1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, TO_KEYWORD)) && result_;
    result_ = pinned_ && report_error_(builder_, empty_expression(builder_, level_ + 1, -1)) && result_;
    result_ = pinned_ && report_error_(builder_, for_statement_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, DO_KEYWORD)) && result_;
    result_ = pinned_ && report_error_(builder_, statement_list(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ENDFOR_KEYWORD) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ['STEP' empty_expression]
  private static boolean for_statement_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "for_statement_6")) return false;
    for_statement_6_0(builder_, level_ + 1);
    return true;
  }

  // 'STEP' empty_expression
  private static boolean for_statement_6_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "for_statement_6_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, STEP_KEYWORD);
    result_ = result_ && empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // visibility? 'FUNC' type_element identifier parameter_list? statement_list [backward_clause] [error_clause] [undo_clause] 'ENDFUNC'
  static boolean function(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = function_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, FUNC_KEYWORD);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, type_element(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, identifier(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, function_4(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, statement_list(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, function_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, function_7(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, function_8(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ENDFUNC_KEYWORD) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // visibility?
  private static boolean function_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_0")) return false;
    visibility(builder_, level_ + 1);
    return true;
  }

  // parameter_list?
  private static boolean function_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_4")) return false;
    parameter_list(builder_, level_ + 1);
    return true;
  }

  // [backward_clause]
  private static boolean function_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_6")) return false;
    backward_clause(builder_, level_ + 1);
    return true;
  }

  // [error_clause]
  private static boolean function_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_7")) return false;
    error_clause(builder_, level_ + 1);
    return true;
  }

  // [undo_clause]
  private static boolean function_8(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_8")) return false;
    undo_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' argument_list_body ')'
  public static boolean function_call_argument_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "function_call_argument_list")) return false;
    if (!nextTokenIs(builder_, LPARENTH)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARGUMENT_LIST, null);
    result_ = consumeToken(builder_, LPARENTH);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, argument_list_body(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPARENTH) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // !('ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST') ('<CSE>' | test_case_statement | test_default_statement)
  static boolean general_case_section(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "general_case_section")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = general_case_section_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && general_case_section_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RapidParser::case_section_recovery);
    return result_ || pinned_;
  }

  // !('ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST')
  private static boolean general_case_section_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "general_case_section_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !general_case_section_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST'
  private static boolean general_case_section_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "general_case_section_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ENDFUNC_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, ENDPROC_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, ENDTRAP_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, ENDIF_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, ELSEIF_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, ELSE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, ENDFOR_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, ENDWHILE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, ENDTEST_KEYWORD);
    return result_;
  }

  // '<CSE>' | test_case_statement | test_default_statement
  private static boolean general_case_section_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "general_case_section_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, CSE_PLACEHOLDER);
    if (!result_) result_ = test_case_statement(builder_, level_ + 1);
    if (!result_) result_ = test_default_statement(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // 'GOTO' unqualified_reference_expression ';'
  public static boolean goto_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "goto_statement")) return false;
    if (!nextTokenIs(builder_, GOTO_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, GOTO_STATEMENT, null);
    result_ = consumeToken(builder_, GOTO_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, unqualified_reference_expression(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // IDENTIFIER | '<ID>'
  static boolean identifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "identifier")) return false;
    if (!nextTokenIs(builder_, "", IDENTIFIER, ID_PLACEHOLDER)) return false;
    boolean result_;
    result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, ID_PLACEHOLDER);
    return result_;
  }

  /* ********************************************************** */
  // 'IF' empty_expression (simple_if_statement_list | compound_if_statement)
  public static boolean if_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_statement")) return false;
    if (!nextTokenIs(builder_, IF_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IF_STATEMENT, null);
    result_ = consumeToken(builder_, IF_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, empty_expression(builder_, level_ + 1, -1));
    result_ = pinned_ && if_statement_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // simple_if_statement_list | compound_if_statement
  private static boolean if_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "if_statement_2")) return false;
    boolean result_;
    result_ = simple_if_statement_list(builder_, level_ + 1);
    if (!result_) result_ = compound_if_statement(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER ':'
  public static boolean label_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "label_statement")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 2, IDENTIFIER, COLON);
    exit_section_(builder_, marker_, LABEL_STATEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // field
  static boolean local_variable(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "local_variable")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = field(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, RapidParser::local_variable_recovery);
    return result_;
  }

  /* ********************************************************** */
  // !('LOCAL' | 'TASK' | 'VAR' | 'PERS' | 'CONST') statement_recovery
  static boolean local_variable_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "local_variable_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = local_variable_recovery_0(builder_, level_ + 1);
    result_ = result_ && statement_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !('LOCAL' | 'TASK' | 'VAR' | 'PERS' | 'CONST')
  private static boolean local_variable_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "local_variable_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !local_variable_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'LOCAL' | 'TASK' | 'VAR' | 'PERS' | 'CONST'
  private static boolean local_variable_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "local_variable_recovery_0_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, LOCAL_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, TASK_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, VAR_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, PERS_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, CONST_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // 'MODULE' identifier attribute_list module_body 'ENDMODULE'
  public static boolean module(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "module")) return false;
    if (!nextTokenIs(builder_, MODULE_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MODULE, null);
    result_ = consumeToken(builder_, MODULE_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, identifier(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, attribute_list(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, module_body(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ENDMODULE_KEYWORD) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // (symbol_entry)*
  static boolean module_body(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "module_body")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!module_body_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "module_body", pos_)) break;
    }
    return true;
  }

  // (symbol_entry)
  private static boolean module_body_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "module_body_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = symbol_entry(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !('MODULE')
  static boolean module_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "module_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !module_recovery_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ('MODULE')
  private static boolean module_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "module_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenFast(builder_, MODULE_KEYWORD);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(<<eof>>) module
  static boolean module_with_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "module_with_recovery")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = module_with_recovery_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && module(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RapidParser::module_recovery);
    return result_ || pinned_;
  }

  // !(<<eof>>)
  private static boolean module_with_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "module_with_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !module_with_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // <<eof>>
  private static boolean module_with_recovery_0_0(PsiBuilder builder_, int level_) {
    return eof(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // (optional_argument | conditional_argument) | (',' ('<ARG>' | optional_argument | conditional_argument | required_argument))
  static boolean next_argument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "next_argument")) return false;
    if (!nextTokenIs(builder_, "", BACKSLASH, COMMA)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = next_argument_0(builder_, level_ + 1);
    if (!result_) result_ = next_argument_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // optional_argument | conditional_argument
  private static boolean next_argument_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "next_argument_0")) return false;
    boolean result_;
    result_ = optional_argument(builder_, level_ + 1);
    if (!result_) result_ = conditional_argument(builder_, level_ + 1);
    return result_;
  }

  // ',' ('<ARG>' | optional_argument | conditional_argument | required_argument)
  private static boolean next_argument_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "next_argument_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && next_argument_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '<ARG>' | optional_argument | conditional_argument | required_argument
  private static boolean next_argument_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "next_argument_1_1")) return false;
    boolean result_;
    result_ = consumeToken(builder_, ARG_PLACEHOLDER);
    if (!result_) result_ = optional_argument(builder_, level_ + 1);
    if (!result_) result_ = conditional_argument(builder_, level_ + 1);
    if (!result_) result_ = required_argument(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // (',' parameter_group) | (&('\') parameter_group)
  static boolean next_parameter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "next_parameter")) return false;
    if (!nextTokenIs(builder_, "", BACKSLASH, COMMA)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = next_parameter_0(builder_, level_ + 1);
    if (!result_) result_ = next_parameter_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ',' parameter_group
  private static boolean next_parameter_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "next_parameter_0")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, COMMA);
    pinned_ = result_; // pin = 1
    result_ = result_ && parameter_group(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // &('\') parameter_group
  private static boolean next_parameter_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "next_parameter_1")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = next_parameter_1_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && parameter_group(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // &('\')
  private static boolean next_parameter_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "next_parameter_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _AND_);
    result_ = consumeToken(builder_, BACKSLASH);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // '\' unqualified_reference_expression [':=' empty_expression]
  public static boolean optional_argument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_argument")) return false;
    if (!nextTokenIs(builder_, BACKSLASH)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, BACKSLASH);
    result_ = result_ && unqualified_reference_expression(builder_, level_ + 1);
    result_ = result_ && optional_argument_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, OPTIONAL_ARGUMENT, result_);
    return result_;
  }

  // [':=' empty_expression]
  private static boolean optional_argument_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_argument_2")) return false;
    optional_argument_2_0(builder_, level_ + 1);
    return true;
  }

  // ':=' empty_expression
  private static boolean optional_argument_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_argument_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CEQ);
    result_ = result_ && empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '<EXP>' | empty_expression
  public static boolean optional_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "optional_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EMPTY_EXPRESSION, "<optional expression>");
    result_ = consumeTokenFast(builder_, EXP_PLACEHOLDER);
    if (!result_) result_ = empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ['VAR' | 'PERS' | 'INOUT'] type_element identifier ['{' '*' (',' '*')* '}']
  public static boolean parameter(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER, "<parameter>");
    result_ = parameter_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, type_element(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, identifier(builder_, level_ + 1)) && result_;
    result_ = pinned_ && parameter_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, RapidParser::parameter_recovery);
    return result_ || pinned_;
  }

  // ['VAR' | 'PERS' | 'INOUT']
  private static boolean parameter_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_0")) return false;
    parameter_0_0(builder_, level_ + 1);
    return true;
  }

  // 'VAR' | 'PERS' | 'INOUT'
  private static boolean parameter_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, VAR_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, PERS_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, INOUT_KEYWORD);
    return result_;
  }

  // ['{' '*' (',' '*')* '}']
  private static boolean parameter_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_3")) return false;
    parameter_3_0(builder_, level_ + 1);
    return true;
  }

  // '{' '*' (',' '*')* '}'
  private static boolean parameter_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, LBRACE, ASTERISK);
    result_ = result_ && parameter_3_0_2(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACE);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (',' '*')*
  private static boolean parameter_3_0_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_3_0_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parameter_3_0_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parameter_3_0_2", pos_)) break;
    }
    return true;
  }

  // ',' '*'
  private static boolean parameter_3_0_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_3_0_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokens(builder_, 0, COMMA, ASTERISK);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // ['\'] ('<DIM>' | parameter) (parameter_group_tail)*
  public static boolean parameter_group(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_group")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER_GROUP, "<parameter group>");
    result_ = parameter_group_0(builder_, level_ + 1);
    result_ = result_ && parameter_group_1(builder_, level_ + 1);
    result_ = result_ && parameter_group_2(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ['\']
  private static boolean parameter_group_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_group_0")) return false;
    consumeToken(builder_, BACKSLASH);
    return true;
  }

  // '<DIM>' | parameter
  private static boolean parameter_group_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_group_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DIM_PLACEHOLDER);
    if (!result_) result_ = parameter(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (parameter_group_tail)*
  private static boolean parameter_group_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_group_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parameter_group_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parameter_group_2", pos_)) break;
    }
    return true;
  }

  // (parameter_group_tail)
  private static boolean parameter_group_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_group_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = parameter_group_tail(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '|' ('<DIM>' | parameter)
  static boolean parameter_group_tail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_group_tail")) return false;
    if (!nextTokenIs(builder_, LINE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, LINE);
    pinned_ = result_; // pin = 1
    result_ = result_ && parameter_group_tail_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // '<DIM>' | parameter
  private static boolean parameter_group_tail_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_group_tail_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DIM_PLACEHOLDER);
    if (!result_) result_ = parameter(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // '(' (first_parameter (next_parameter)*)? ')'
  public static boolean parameter_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_list")) return false;
    if (!nextTokenIs(builder_, LPARENTH)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAMETER_LIST, null);
    result_ = consumeToken(builder_, LPARENTH);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, parameter_list_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPARENTH) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (first_parameter (next_parameter)*)?
  private static boolean parameter_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_list_1")) return false;
    parameter_list_1_0(builder_, level_ + 1);
    return true;
  }

  // first_parameter (next_parameter)*
  private static boolean parameter_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = first_parameter(builder_, level_ + 1);
    result_ = result_ && parameter_list_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (next_parameter)*
  private static boolean parameter_list_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_list_1_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parameter_list_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parameter_list_1_0_1", pos_)) break;
    }
    return true;
  }

  // (next_parameter)
  private static boolean parameter_list_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_list_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = next_parameter(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(')' | '|' | ',' | '\') parameter_statement_recovery
  static boolean parameter_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = parameter_recovery_0(builder_, level_ + 1);
    result_ = result_ && parameter_statement_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(')' | '|' | ',' | '\')
  private static boolean parameter_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !parameter_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ')' | '|' | ',' | '\'
  private static boolean parameter_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_recovery_0_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, RPARENTH);
    if (!result_) result_ = consumeTokenFast(builder_, LINE);
    if (!result_) result_ = consumeTokenFast(builder_, COMMA);
    if (!result_) result_ = consumeTokenFast(builder_, BACKSLASH);
    return result_;
  }

  /* ********************************************************** */
  // !('<SMT>' | '<DDN>' | 'GOTO' | 'RETURN' |'RAISE' | 'EXIT' | 'RETRY' | 'CONNECT' | 'TRYNEXT' | '%' | 'IF' | 'FOR' | 'WHILE' | 'TEST' | 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'DEFAULT' | 'UNDO' | 'ERROR' | 'BACKWARD' | 'LOCAL' | 'TASK' |  'CONST') symbol_recovery
  static boolean parameter_statement_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_statement_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = parameter_statement_recovery_0(builder_, level_ + 1);
    result_ = result_ && symbol_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !('<SMT>' | '<DDN>' | 'GOTO' | 'RETURN' |'RAISE' | 'EXIT' | 'RETRY' | 'CONNECT' | 'TRYNEXT' | '%' | 'IF' | 'FOR' | 'WHILE' | 'TEST' | 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'DEFAULT' | 'UNDO' | 'ERROR' | 'BACKWARD' | 'LOCAL' | 'TASK' |  'CONST')
  private static boolean parameter_statement_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_statement_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !parameter_statement_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // '<SMT>' | '<DDN>' | 'GOTO' | 'RETURN' |'RAISE' | 'EXIT' | 'RETRY' | 'CONNECT' | 'TRYNEXT' | '%' | 'IF' | 'FOR' | 'WHILE' | 'TEST' | 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'DEFAULT' | 'UNDO' | 'ERROR' | 'BACKWARD' | 'LOCAL' | 'TASK' |  'CONST'
  private static boolean parameter_statement_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parameter_statement_recovery_0_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, SMT_PLACEHOLDER);
    if (!result_) result_ = consumeTokenFast(builder_, DDN_PLACEHOLDER);
    if (!result_) result_ = consumeTokenFast(builder_, GOTO_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, RETURN_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, RAISE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, EXIT_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, RETRY_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, CONNECT_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, TRYNEXT_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, PERCENT);
    if (!result_) result_ = consumeTokenFast(builder_, IF_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, FOR_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, WHILE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, TEST_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDFUNC_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDPROC_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDTRAP_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDIF_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ELSEIF_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ELSE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDFOR_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDWHILE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDTEST_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, CASE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, DEFAULT_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, UNDO_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ERROR_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, BACKWARD_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, LOCAL_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, TASK_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, CONST_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // visibility? 'PROC' identifier parameter_list? statement_list [backward_clause] [error_clause] [undo_clause] 'ENDPROC'
  static boolean procedure(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "procedure")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = procedure_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PROC_KEYWORD);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, identifier(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, procedure_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, statement_list(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, procedure_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, procedure_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, procedure_7(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ENDPROC_KEYWORD) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // visibility?
  private static boolean procedure_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "procedure_0")) return false;
    visibility(builder_, level_ + 1);
    return true;
  }

  // parameter_list?
  private static boolean procedure_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "procedure_3")) return false;
    parameter_list(builder_, level_ + 1);
    return true;
  }

  // [backward_clause]
  private static boolean procedure_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "procedure_5")) return false;
    backward_clause(builder_, level_ + 1);
    return true;
  }

  // [error_clause]
  private static boolean procedure_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "procedure_6")) return false;
    error_clause(builder_, level_ + 1);
    return true;
  }

  // [undo_clause]
  private static boolean procedure_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "procedure_7")) return false;
    undo_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // argument_list_body
  public static boolean procedure_call_argument_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "procedure_call_argument_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARGUMENT_LIST, "<procedure call argument list>");
    result_ = argument_list_body(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // procedure_target procedure_call_argument_list ';'
  public static boolean procedure_call_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "procedure_call_statement")) return false;
    if (!nextTokenIsFast(builder_, IDENTIFIER) &&
        !nextTokenIs(builder_, "<procedure call statement>", PERCENT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PROCEDURE_CALL_STATEMENT, "<procedure call statement>");
    result_ = procedure_target(builder_, level_ + 1);
    result_ = result_ && procedure_call_argument_list(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, SEMICOLON);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // unqualified_reference_expression | '%' empty_expression '%'
  static boolean procedure_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "procedure_target")) return false;
    if (!nextTokenIsFast(builder_, IDENTIFIER) &&
        !nextTokenIs(builder_, "", PERCENT)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = unqualified_reference_expression(builder_, level_ + 1);
    if (!result_) result_ = procedure_target_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '%' empty_expression '%'
  private static boolean procedure_target_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "procedure_target_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PERCENT);
    result_ = result_ && empty_expression(builder_, level_ + 1, -1);
    result_ = result_ && consumeToken(builder_, PERCENT);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // () IDENTIFIER
  static boolean qualified_tail(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualified_tail")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = qualified_tail_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ()
  private static boolean qualified_tail_0(PsiBuilder builder_, int level_) {
    return true;
  }

  /* ********************************************************** */
  // 'RAISE' empty_expression? ';'
  public static boolean raise_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "raise_statement")) return false;
    if (!nextTokenIs(builder_, RAISE_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RAISE_STATEMENT, null);
    result_ = consumeToken(builder_, RAISE_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, raise_statement_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // empty_expression?
  private static boolean raise_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "raise_statement_1")) return false;
    empty_expression(builder_, level_ + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // visibility? 'RECORD' identifier component_list 'ENDRECORD'
  public static boolean record(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "record")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RECORD, "<record>");
    result_ = record_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RECORD_KEYWORD);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, identifier(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, component_list(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ENDRECORD_KEYWORD) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // visibility?
  private static boolean record_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "record_0")) return false;
    visibility(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // [unqualified_reference_expression ':='] empty_expression
  public static boolean required_argument(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_argument")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, REQUIRED_ARGUMENT, "<required argument>");
    result_ = required_argument_0(builder_, level_ + 1);
    result_ = result_ && empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // [unqualified_reference_expression ':=']
  private static boolean required_argument_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_argument_0")) return false;
    required_argument_0_0(builder_, level_ + 1);
    return true;
  }

  // unqualified_reference_expression ':='
  private static boolean required_argument_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "required_argument_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = unqualified_reference_expression(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, CEQ);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'RETRY' ';'
  public static boolean retry_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "retry_statement")) return false;
    if (!nextTokenIs(builder_, RETRY_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RETRY_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, RETRY_KEYWORD, SEMICOLON);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // 'RETURN' empty_expression? ';'
  public static boolean return_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "return_statement")) return false;
    if (!nextTokenIs(builder_, RETURN_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, RETURN_STATEMENT, null);
    result_ = consumeToken(builder_, RETURN_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, return_statement_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, SEMICOLON) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // empty_expression?
  private static boolean return_statement_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "return_statement_1")) return false;
    empty_expression(builder_, level_ + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // procedure | function | trap
  public static boolean routine(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "routine")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ROUTINE, "<routine>");
    result_ = procedure(builder_, level_ + 1);
    if (!result_) result_ = function(builder_, level_ + 1);
    if (!result_) result_ = trap(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // simple_statement | '<SMT>'
  public static boolean simple_if_statement_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_if_statement_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT_LIST, "<simple if statement list>");
    result_ = simple_statement(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, SMT_PLACEHOLDER);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // procedure_call_statement |
  //                              assignment_statement |
  //                              goto_statement |
  //                              return_statement |
  //                              raise_statement |
  //                              exit_statement |
  //                              retry_statement |
  //                              try_next_statement |
  //                              connect_statement
  static boolean simple_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "simple_statement")) return false;
    boolean result_;
    result_ = procedure_call_statement(builder_, level_ + 1);
    if (!result_) result_ = assignment_statement(builder_, level_ + 1);
    if (!result_) result_ = goto_statement(builder_, level_ + 1);
    if (!result_) result_ = return_statement(builder_, level_ + 1);
    if (!result_) result_ = raise_statement(builder_, level_ + 1);
    if (!result_) result_ = exit_statement(builder_, level_ + 1);
    if (!result_) result_ = retry_statement(builder_, level_ + 1);
    if (!result_) result_ = try_next_statement(builder_, level_ + 1);
    if (!result_) result_ = connect_statement(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // '<SMT>' | '<DDN>' | local_variable | simple_statement | compound_statement | label_statement
  static boolean statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, SMT_PLACEHOLDER);
    if (!result_) result_ = consumeToken(builder_, DDN_PLACEHOLDER);
    if (!result_) result_ = local_variable(builder_, level_ + 1);
    if (!result_) result_ = simple_statement(builder_, level_ + 1);
    if (!result_) result_ = compound_statement(builder_, level_ + 1);
    if (!result_) result_ = label_statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, RapidParser::statement_recovery);
    return result_;
  }

  /* ********************************************************** */
  // (statement_with_recovery)*
  public static boolean statement_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_list")) return false;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT_LIST, "<statement list>");
    while (true) {
      int pos_ = current_position_(builder_);
      if (!statement_list_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "statement_list", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, true, false, null);
    return true;
  }

  // (statement_with_recovery)
  private static boolean statement_list_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_list_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = statement_with_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(IDENTIFIER) parameter_statement_recovery
  static boolean statement_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = statement_recovery_0(builder_, level_ + 1);
    result_ = result_ && parameter_statement_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(IDENTIFIER)
  private static boolean statement_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !statement_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (IDENTIFIER)
  private static boolean statement_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_recovery_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenFast(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !('ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'UNDO' | 'ERROR' | 'BACKWARD' | 'DEFAULT') statement
  static boolean statement_with_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_with_recovery")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = statement_with_recovery_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && statement(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RapidParser::statement_recovery);
    return result_ || pinned_;
  }

  // !('ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'UNDO' | 'ERROR' | 'BACKWARD' | 'DEFAULT')
  private static boolean statement_with_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_with_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !statement_with_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'UNDO' | 'ERROR' | 'BACKWARD' | 'DEFAULT'
  private static boolean statement_with_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "statement_with_recovery_0_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, ENDFUNC_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDPROC_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDTRAP_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDIF_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ELSEIF_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ELSE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDFOR_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDWHILE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ENDTEST_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, CASE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, UNDO_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ERROR_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, BACKWARD_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, DEFAULT_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // '<TDN>' | '<DDN>' | '<RDN>' | alias | record | field | routine
  static boolean symbol(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol")) return false;
    boolean result_;
    result_ = consumeToken(builder_, TDN_PLACEHOLDER);
    if (!result_) result_ = consumeToken(builder_, DDN_PLACEHOLDER);
    if (!result_) result_ = consumeToken(builder_, RDN_PLACEHOLDER);
    if (!result_) result_ = alias(builder_, level_ + 1);
    if (!result_) result_ = record(builder_, level_ + 1);
    if (!result_) result_ = field(builder_, level_ + 1);
    if (!result_) result_ = routine(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // !(<<eof>> | 'ENDMODULE') symbol
  static boolean symbol_entry(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_entry")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = symbol_entry_0(builder_, level_ + 1);
    pinned_ = result_; // pin = 1
    result_ = result_ && symbol(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, RapidParser::symbol_recovery);
    return result_ || pinned_;
  }

  // !(<<eof>> | 'ENDMODULE')
  private static boolean symbol_entry_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_entry_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !symbol_entry_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // <<eof>> | 'ENDMODULE'
  private static boolean symbol_entry_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_entry_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = eof(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, ENDMODULE_KEYWORD);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !('ENDMODULE' | '<TDN>' | '<DDN>' | '<RDN>' | 'LOCAL' | 'TASK' | 'ALIAS' | 'RECORD' | 'VAR' | 'PERS' | 'CONST' | 'FUNC' | 'PROC' | 'TRAP')
  static boolean symbol_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !symbol_recovery_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // 'ENDMODULE' | '<TDN>' | '<DDN>' | '<RDN>' | 'LOCAL' | 'TASK' | 'ALIAS' | 'RECORD' | 'VAR' | 'PERS' | 'CONST' | 'FUNC' | 'PROC' | 'TRAP'
  private static boolean symbol_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "symbol_recovery_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, ENDMODULE_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, TDN_PLACEHOLDER);
    if (!result_) result_ = consumeTokenFast(builder_, DDN_PLACEHOLDER);
    if (!result_) result_ = consumeTokenFast(builder_, RDN_PLACEHOLDER);
    if (!result_) result_ = consumeTokenFast(builder_, LOCAL_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, TASK_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, ALIAS_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, RECORD_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, VAR_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, PERS_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, CONST_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, FUNC_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, PROC_KEYWORD);
    if (!result_) result_ = consumeTokenFast(builder_, TRAP_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  public static boolean target_variable(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "target_variable")) return false;
    if (!nextTokenIs(builder_, "<target variable>", IDENTIFIER, ID_PLACEHOLDER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TARGET_VARIABLE, "<target variable>");
    result_ = identifier(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // 'CASE' test_expression_list ':' statement_list
  public static boolean test_case_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "test_case_statement")) return false;
    if (!nextTokenIs(builder_, CASE_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TEST_CASE_STATEMENT, null);
    result_ = consumeToken(builder_, CASE_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, test_expression_list(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, COLON)) && result_;
    result_ = pinned_ && statement_list(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // 'DEFAULT' ':' statement_list
  public static boolean test_default_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "test_default_statement")) return false;
    if (!nextTokenIs(builder_, DEFAULT_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TEST_CASE_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, DEFAULT_KEYWORD, COLON);
    pinned_ = result_; // pin = 1
    result_ = result_ && statement_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // empty_expression (',' empty_expression)*
  public static boolean test_expression_list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "test_expression_list")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPRESSION_LIST, "<test expression list>");
    result_ = empty_expression(builder_, level_ + 1, -1);
    result_ = result_ && test_expression_list_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (',' empty_expression)*
  private static boolean test_expression_list_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "test_expression_list_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!test_expression_list_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "test_expression_list_1", pos_)) break;
    }
    return true;
  }

  // ',' empty_expression
  private static boolean test_expression_list_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "test_expression_list_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'TEST' empty_expression (general_case_section)* 'ENDTEST'
  public static boolean test_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "test_statement")) return false;
    if (!nextTokenIs(builder_, TEST_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TEST_STATEMENT, null);
    result_ = consumeToken(builder_, TEST_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, empty_expression(builder_, level_ + 1, -1));
    result_ = pinned_ && report_error_(builder_, test_statement_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ENDTEST_KEYWORD) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (general_case_section)*
  private static boolean test_statement_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "test_statement_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!test_statement_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "test_statement_2", pos_)) break;
    }
    return true;
  }

  // (general_case_section)
  private static boolean test_statement_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "test_statement_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = general_case_section(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // visibility? 'TRAP' identifier parameter_list? statement_list [backward_clause] [error_clause] [undo_clause] 'ENDTRAP'
  static boolean trap(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "trap")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = trap_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, TRAP_KEYWORD);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, identifier(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, trap_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, statement_list(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, trap_5(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, trap_6(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, trap_7(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ENDTRAP_KEYWORD) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // visibility?
  private static boolean trap_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "trap_0")) return false;
    visibility(builder_, level_ + 1);
    return true;
  }

  // parameter_list?
  private static boolean trap_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "trap_3")) return false;
    parameter_list(builder_, level_ + 1);
    return true;
  }

  // [backward_clause]
  private static boolean trap_5(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "trap_5")) return false;
    backward_clause(builder_, level_ + 1);
    return true;
  }

  // [error_clause]
  private static boolean trap_6(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "trap_6")) return false;
    error_clause(builder_, level_ + 1);
    return true;
  }

  // [undo_clause]
  private static boolean trap_7(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "trap_7")) return false;
    undo_clause(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // 'TRYNEXT' ';'
  public static boolean try_next_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "try_next_statement")) return false;
    if (!nextTokenIs(builder_, TRYNEXT_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, TRY_NEXT_STATEMENT, null);
    result_ = consumeTokens(builder_, 1, TRYNEXT_KEYWORD, SEMICOLON);
    pinned_ = result_; // pin = 1
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // unqualified_reference_expression
  public static boolean type_element(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "type_element")) return false;
    if (!nextTokenIsFast(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = unqualified_reference_expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, TYPE_ELEMENT, result_);
    return result_;
  }

  /* ********************************************************** */
  // 'UNDO' statement_list
  public static boolean undo_clause(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "undo_clause")) return false;
    if (!nextTokenIs(builder_, UNDO_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STATEMENT_LIST, null);
    result_ = consumeToken(builder_, UNDO_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && statement_list(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // '<VAR>' | empty_expression
  static boolean variable_target(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "variable_target")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, VAR_PLACEHOLDER);
    if (!result_) result_ = empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, level_, marker_, result_, false, RapidParser::variable_target_recovery);
    return result_;
  }

  /* ********************************************************** */
  // !(':=' | ';') statement_recovery
  static boolean variable_target_recovery(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "variable_target_recovery")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = variable_target_recovery_0(builder_, level_ + 1);
    result_ = result_ && statement_recovery(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(':=' | ';')
  private static boolean variable_target_recovery_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "variable_target_recovery_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !variable_target_recovery_0_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ':=' | ';'
  private static boolean variable_target_recovery_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "variable_target_recovery_0_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, CEQ);
    if (!result_) result_ = consumeTokenFast(builder_, SEMICOLON);
    return result_;
  }

  /* ********************************************************** */
  // 'LOCAL' | 'TASK'
  static boolean visibility(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "visibility")) return false;
    if (!nextTokenIs(builder_, "", LOCAL_KEYWORD, TASK_KEYWORD)) return false;
    boolean result_;
    result_ = consumeToken(builder_, LOCAL_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, TASK_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // 'WHILE' empty_expression 'DO' statement_list 'ENDWHILE'
  public static boolean while_statement(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "while_statement")) return false;
    if (!nextTokenIs(builder_, WHILE_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, WHILE_STATEMENT, null);
    result_ = consumeToken(builder_, WHILE_KEYWORD);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, empty_expression(builder_, level_ + 1, -1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, DO_KEYWORD)) && result_;
    result_ = pinned_ && report_error_(builder_, statement_list(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, ENDWHILE_KEYWORD) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // Expression root: empty_expression
  // Operator priority table:
  // 0: BINARY(binary_or_expression) BINARY(binary_xor_expression) PREFIX(unary_not_expression)
  // 1: BINARY(binary_and_expression)
  // 2: BINARY(binary_relational_expression)
  // 3: BINARY(binary_additive_expression)
  // 4: BINARY(binary_multiplicative_expression)
  // 5: PREFIX(unary_additive_expression)
  // 6: ATOM(literal_expression) ATOM(unqualified_reference_expression) POSTFIX(qualified_reference_expression) POSTFIX(index_expression)
  //    POSTFIX(function_call_expression) ATOM(aggregate_expression) ATOM(parenthesised_expression)
  public static boolean empty_expression(PsiBuilder builder_, int level_, int priority_) {
    if (!recursion_guard_(builder_, level_, "empty_expression")) return false;
    addVariant(builder_, "<empty expression>");
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<empty expression>");
    result_ = unary_not_expression(builder_, level_ + 1);
    if (!result_) result_ = unary_additive_expression(builder_, level_ + 1);
    if (!result_) result_ = literal_expression(builder_, level_ + 1);
    if (!result_) result_ = unqualified_reference_expression(builder_, level_ + 1);
    if (!result_) result_ = aggregate_expression(builder_, level_ + 1);
    if (!result_) result_ = parenthesised_expression(builder_, level_ + 1);
    pinned_ = result_;
    result_ = result_ && empty_expression_0(builder_, level_ + 1, priority_);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean empty_expression_0(PsiBuilder builder_, int level_, int priority_) {
    if (!recursion_guard_(builder_, level_, "empty_expression_0")) return false;
    boolean result_ = true;
    while (true) {
      Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
      if (priority_ < 0 && consumeTokenSmart(builder_, OR_KEYWORD)) {
        result_ = empty_expression(builder_, level_, 0);
        exit_section_(builder_, level_, marker_, BINARY_EXPRESSION, result_, true, null);
      }
      else if (priority_ < 0 && consumeTokenSmart(builder_, XOR_KEYWORD)) {
        result_ = empty_expression(builder_, level_, 0);
        exit_section_(builder_, level_, marker_, BINARY_EXPRESSION, result_, true, null);
      }
      else if (priority_ < 1 && consumeTokenSmart(builder_, AND_KEYWORD)) {
        result_ = empty_expression(builder_, level_, 1);
        exit_section_(builder_, level_, marker_, BINARY_EXPRESSION, result_, true, null);
      }
      else if (priority_ < 2 && binary_relational_expression_0(builder_, level_ + 1)) {
        result_ = empty_expression(builder_, level_, 2);
        exit_section_(builder_, level_, marker_, BINARY_EXPRESSION, result_, true, null);
      }
      else if (priority_ < 3 && binary_additive_expression_0(builder_, level_ + 1)) {
        result_ = empty_expression(builder_, level_, 3);
        exit_section_(builder_, level_, marker_, BINARY_EXPRESSION, result_, true, null);
      }
      else if (priority_ < 4 && binary_multiplicative_expression_0(builder_, level_ + 1)) {
        result_ = empty_expression(builder_, level_, 4);
        exit_section_(builder_, level_, marker_, BINARY_EXPRESSION, result_, true, null);
      }
      else if (priority_ < 6 && qualified_reference_expression_0(builder_, level_ + 1)) {
        result_ = true;
        exit_section_(builder_, level_, marker_, REFERENCE_EXPRESSION, result_, true, null);
      }
      else if (priority_ < 6 && array(builder_, level_ + 1)) {
        result_ = true;
        exit_section_(builder_, level_, marker_, INDEX_EXPRESSION, result_, true, null);
      }
      else if (priority_ < 6 && leftMarkerIs(builder_, REFERENCE_EXPRESSION) && function_call_argument_list(builder_, level_ + 1)) {
        result_ = true;
        exit_section_(builder_, level_, marker_, FUNCTION_CALL_EXPRESSION, result_, true, null);
      }
      else {
        exit_section_(builder_, level_, marker_, null, false, false, null);
        break;
      }
    }
    return result_;
  }

  public static boolean unary_not_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_not_expression")) return false;
    if (!nextTokenIsSmart(builder_, NOT_KEYWORD)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, NOT_KEYWORD);
    pinned_ = result_;
    result_ = pinned_ && empty_expression(builder_, level_, 0);
    exit_section_(builder_, level_, marker_, UNARY_EXPRESSION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // '<' | '<=' | '=' | '>' | '>=' | '<>'
  private static boolean binary_relational_expression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "binary_relational_expression_0")) return false;
    boolean result_;
    result_ = consumeTokenSmart(builder_, LT);
    if (!result_) result_ = consumeTokenSmart(builder_, LE);
    if (!result_) result_ = consumeTokenSmart(builder_, EQ);
    if (!result_) result_ = consumeTokenSmart(builder_, GT);
    if (!result_) result_ = consumeTokenSmart(builder_, GE);
    if (!result_) result_ = consumeTokenSmart(builder_, LTGT);
    return result_;
  }

  // '+' | '-'
  private static boolean binary_additive_expression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "binary_additive_expression_0")) return false;
    boolean result_;
    result_ = consumeTokenSmart(builder_, PLUS);
    if (!result_) result_ = consumeTokenSmart(builder_, MINUS);
    return result_;
  }

  // '*' | '/' | 'DIV' | 'MOD'
  private static boolean binary_multiplicative_expression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "binary_multiplicative_expression_0")) return false;
    boolean result_;
    result_ = consumeTokenSmart(builder_, ASTERISK);
    if (!result_) result_ = consumeTokenSmart(builder_, DIV);
    if (!result_) result_ = consumeTokenSmart(builder_, DIV_KEYWORD);
    if (!result_) result_ = consumeTokenSmart(builder_, MOD_KEYWORD);
    return result_;
  }

  public static boolean unary_additive_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_additive_expression")) return false;
    if (!nextTokenIsSmart(builder_, MINUS, PLUS)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = unary_additive_expression_0(builder_, level_ + 1);
    pinned_ = result_;
    result_ = pinned_ && empty_expression(builder_, level_, 5);
    exit_section_(builder_, level_, marker_, UNARY_EXPRESSION, result_, pinned_, null);
    return result_ || pinned_;
  }

  // '+' | '-'
  private static boolean unary_additive_expression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unary_additive_expression_0")) return false;
    boolean result_;
    result_ = consumeTokenSmart(builder_, PLUS);
    if (!result_) result_ = consumeTokenSmart(builder_, MINUS);
    return result_;
  }

  // 'TRUE' | 'FALSE' | INTEGER_LITERAL | STRING_LITERAL
  public static boolean literal_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "literal_expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LITERAL_EXPRESSION, "<literal expression>");
    result_ = consumeTokenSmart(builder_, TRUE_KEYWORD);
    if (!result_) result_ = consumeTokenSmart(builder_, FALSE_KEYWORD);
    if (!result_) result_ = consumeTokenSmart(builder_, INTEGER_LITERAL);
    if (!result_) result_ = consumeTokenSmart(builder_, STRING_LITERAL);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // IDENTIFIER
  public static boolean unqualified_reference_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unqualified_reference_expression")) return false;
    if (!nextTokenIsSmart(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenSmart(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, REFERENCE_EXPRESSION, result_);
    return result_;
  }

  // '.' qualified_tail
  private static boolean qualified_reference_expression_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "qualified_reference_expression_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenSmart(builder_, DOT);
    result_ = result_ && qualified_tail(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '[' (empty_expression (',' empty_expression)*)? ']'
  public static boolean aggregate_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregate_expression")) return false;
    if (!nextTokenIsSmart(builder_, LBRACKET)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenSmart(builder_, LBRACKET);
    result_ = result_ && aggregate_expression_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RBRACKET);
    exit_section_(builder_, marker_, AGGREGATE_EXPRESSION, result_);
    return result_;
  }

  // (empty_expression (',' empty_expression)*)?
  private static boolean aggregate_expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregate_expression_1")) return false;
    aggregate_expression_1_0(builder_, level_ + 1);
    return true;
  }

  // empty_expression (',' empty_expression)*
  private static boolean aggregate_expression_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregate_expression_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = empty_expression(builder_, level_ + 1, -1);
    result_ = result_ && aggregate_expression_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (',' empty_expression)*
  private static boolean aggregate_expression_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregate_expression_1_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!aggregate_expression_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "aggregate_expression_1_0_1", pos_)) break;
    }
    return true;
  }

  // ',' empty_expression
  private static boolean aggregate_expression_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "aggregate_expression_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenSmart(builder_, COMMA);
    result_ = result_ && empty_expression(builder_, level_ + 1, -1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // '(' empty_expression ')'
  public static boolean parenthesised_expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenthesised_expression")) return false;
    if (!nextTokenIsSmart(builder_, LPARENTH)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARENTHESISED_EXPRESSION, null);
    result_ = consumeTokenSmart(builder_, LPARENTH);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, empty_expression(builder_, level_ + 1, -1));
    result_ = pinned_ && consumeToken(builder_, RPARENTH) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

}
