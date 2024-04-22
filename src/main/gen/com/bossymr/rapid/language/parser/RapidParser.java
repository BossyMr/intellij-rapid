// This is a generated file. Not intended for manual editing.
package com.bossymr.rapid.language.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LightPsiParser;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

import static com.bossymr.rapid.language.parser.RapidParserUtil.*;
import static com.bossymr.rapid.language.psi.RapidElementTypes.*;
import static com.bossymr.rapid.language.psi.RapidTokenTypes.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class RapidParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return file(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(AGGREGATE_EXPRESSION, BINARY_EXPRESSION, EMPTY_EXPRESSION, FUNCTION_CALL_EXPRESSION,
      INDEX_EXPRESSION, LITERAL_EXPRESSION, PARENTHESISED_EXPRESSION, REFERENCE_EXPRESSION,
      UNARY_EXPRESSION),
  };

  /* ********************************************************** */
  // visibility? 'ALIAS' type_element identifier ';'
  public static boolean alias(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "alias")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ALIAS, "<alias>");
    r = alias_0(b, l + 1);
    r = r && consumeToken(b, ALIAS_KEYWORD);
    p = r; // pin = 2
    r = r && report_error_(b, type_element(b, l + 1));
    r = p && report_error_(b, identifier(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    register_hook_(b, LEFT_BINDER, ADJACENT_LINE_COMMENTS);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // visibility?
  private static boolean alias_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "alias_0")) return false;
    visibility(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // [first_argument (next_argument)*]
  static boolean argument_list_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_body")) return false;
    argument_list_body_0(b, l + 1);
    return true;
  }

  // first_argument (next_argument)*
  private static boolean argument_list_body_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_body_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = first_argument(b, l + 1);
    r = r && argument_list_body_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (next_argument)*
  private static boolean argument_list_body_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_body_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!argument_list_body_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "argument_list_body_0_1", c)) break;
    }
    return true;
  }

  // (next_argument)
  private static boolean argument_list_body_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "argument_list_body_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = next_argument(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '{' array_expression (array_tail)* '}'
  public static boolean array(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ARRAY, null);
    r = consumeToken(b, LBRACE);
    p = r; // pin = 1
    r = r && report_error_(b, array_expression(b, l + 1));
    r = p && report_error_(b, array_2(b, l + 1)) && r;
    r = p && consumeToken(b, RBRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (array_tail)*
  private static boolean array_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!array_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "array_2", c)) break;
    }
    return true;
  }

  // (array_tail)
  private static boolean array_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = array_tail(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // () empty_expression
  static boolean array_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_expression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = array_expression_0(b, l + 1);
    p = r; // pin = 1
    r = r && empty_expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, RapidParser::array_expression_recovery);
    return r || p;
  }

  // ()
  private static boolean array_expression_0(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // !(',' | '}' | ':=' | ';') symbol_recovery
  static boolean array_expression_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_expression_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = array_expression_recovery_0(b, l + 1);
    r = r && symbol_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(',' | '}' | ':=' | ';')
  private static boolean array_expression_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_expression_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !array_expression_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ',' | '}' | ':=' | ';'
  private static boolean array_expression_recovery_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_expression_recovery_0_0")) return false;
    boolean r;
    r = consumeTokenFast(b, COMMA);
    if (!r) r = consumeTokenFast(b, RBRACE);
    if (!r) r = consumeTokenFast(b, CEQ);
    if (!r) r = consumeTokenFast(b, SEMICOLON);
    return r;
  }

  /* ********************************************************** */
  // ',' array_expression
  static boolean array_tail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "array_tail")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && array_expression(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // variable_target ':=' empty_expression ';'
  public static boolean assignment_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "assignment_statement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ASSIGNMENT_STATEMENT, "<assignment statement>");
    r = variable_target(b, l + 1);
    r = r && consumeToken(b, CEQ);
    p = r; // pin = 2
    r = r && report_error_(b, empty_expression(b, l + 1, -1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // () ('SYSMODULE' | 'NOVIEW' | 'NOSTEPIN' | 'VIEWONLY' | 'READONLY')
  static boolean attribute(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = attribute_0(b, l + 1);
    p = r; // pin = 1
    r = r && attribute_1(b, l + 1);
    exit_section_(b, l, m, r, p, RapidParser::attribute_recovery);
    return r || p;
  }

  // ()
  private static boolean attribute_0(PsiBuilder b, int l) {
    return true;
  }

  // 'SYSMODULE' | 'NOVIEW' | 'NOSTEPIN' | 'VIEWONLY' | 'READONLY'
  private static boolean attribute_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_1")) return false;
    boolean r;
    r = consumeToken(b, SYSMODULE_KEYWORD);
    if (!r) r = consumeToken(b, NOVIEW_KEYWORD);
    if (!r) r = consumeToken(b, NOSTEPIN_KEYWORD);
    if (!r) r = consumeToken(b, VIEWONLY_KEYWORD);
    if (!r) r = consumeToken(b, READONLY_KEYWORD);
    return r;
  }

  /* ********************************************************** */
  // !(<<eof>> | 'ENDMODULE') '(' attribute (attribute_list_tail)* ')'
  public static boolean attribute_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_list")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ATTRIBUTE_LIST, "<attribute list>");
    r = attribute_list_0(b, l + 1);
    r = r && consumeToken(b, LPARENTH);
    p = r; // pin = 2
    r = r && report_error_(b, attribute(b, l + 1));
    r = p && report_error_(b, attribute_list_3(b, l + 1)) && r;
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !(<<eof>> | 'ENDMODULE')
  private static boolean attribute_list_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_list_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !attribute_list_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>> | 'ENDMODULE'
  private static boolean attribute_list_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_list_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eof(b, l + 1);
    if (!r) r = consumeToken(b, ENDMODULE_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  // (attribute_list_tail)*
  private static boolean attribute_list_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_list_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!attribute_list_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "attribute_list_3", c)) break;
    }
    return true;
  }

  // (attribute_list_tail)
  private static boolean attribute_list_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_list_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attribute_list_tail(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ',' attribute
  static boolean attribute_list_tail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_list_tail")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && attribute(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // !(',' | ')') symbol_recovery
  static boolean attribute_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attribute_recovery_0(b, l + 1);
    r = r && symbol_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(',' | ')')
  private static boolean attribute_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !attribute_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ',' | ')'
  private static boolean attribute_recovery_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attribute_recovery_0_0")) return false;
    boolean r;
    r = consumeTokenFast(b, COMMA);
    if (!r) r = consumeTokenFast(b, RPARENTH);
    return r;
  }

  /* ********************************************************** */
  // 'BACKWARD' inner_statement_list
  public static boolean backward_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "backward_clause")) return false;
    if (!nextTokenIs(b, BACKWARD_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT_LIST, null);
    r = consumeToken(b, BACKWARD_KEYWORD);
    p = r; // pin = 1
    r = r && inner_statement_list(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // !('ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'DEFAULT' | '<CSE>')
  static boolean case_section_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "case_section_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !case_section_recovery_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'DEFAULT' | '<CSE>'
  private static boolean case_section_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "case_section_recovery_0")) return false;
    boolean r;
    r = consumeTokenFast(b, ENDFUNC_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDPROC_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDTRAP_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDIF_KEYWORD);
    if (!r) r = consumeTokenFast(b, ELSEIF_KEYWORD);
    if (!r) r = consumeTokenFast(b, ELSE_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDFOR_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDWHILE_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDTEST_KEYWORD);
    if (!r) r = consumeTokenFast(b, CASE_KEYWORD);
    if (!r) r = consumeTokenFast(b, DEFAULT_KEYWORD);
    if (!r) r = consumeTokenFast(b, CSE_PLACEHOLDER);
    return r;
  }

  /* ********************************************************** */
  // ',' ('<ARG>' | conditional_or_optional_argument | required_argument)
  static boolean comma_argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comma_argument")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && comma_argument_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // '<ARG>' | conditional_or_optional_argument | required_argument
  private static boolean comma_argument_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comma_argument_1")) return false;
    boolean r;
    r = consumeToken(b, ARG_PLACEHOLDER);
    if (!r) r = conditional_or_optional_argument(b, l + 1);
    if (!r) r = required_argument(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // type_element identifier ';'
  public static boolean component(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "component")) return false;
    if (!nextTokenIsFast(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMPONENT, null);
    r = type_element(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, identifier(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    register_hook_(b, LEFT_BINDER, ADJACENT_LINE_COMMENTS);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (component)*
  static boolean component_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "component_list")) return false;
    while (true) {
      int c = current_position_(b);
      if (!component_list_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "component_list", c)) break;
    }
    return true;
  }

  // (component)
  private static boolean component_list_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "component_list_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = component(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // compound_if_statement_inner 'ENDIF'
  static boolean compound_if_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_if_statement")) return false;
    if (!nextTokenIs(b, THEN_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = compound_if_statement_inner(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, ENDIF_KEYWORD);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'THEN' statement_list (outer_else_if_statement|else_statement)?
  static boolean compound_if_statement_inner(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_if_statement_inner")) return false;
    if (!nextTokenIs(b, THEN_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, THEN_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, statement_list(b, l + 1));
    r = p && compound_if_statement_inner_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (outer_else_if_statement|else_statement)?
  private static boolean compound_if_statement_inner_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_if_statement_inner_2")) return false;
    compound_if_statement_inner_2_0(b, l + 1);
    return true;
  }

  // outer_else_if_statement|else_statement
  private static boolean compound_if_statement_inner_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_if_statement_inner_2_0")) return false;
    boolean r;
    r = outer_else_if_statement(b, l + 1);
    if (!r) r = else_statement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // if_statement |
  //                                for_statement |
  //                                while_statement |
  //                                test_statement
  static boolean compound_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "compound_statement")) return false;
    boolean r;
    r = if_statement(b, l + 1);
    if (!r) r = for_statement(b, l + 1);
    if (!r) r = while_statement(b, l + 1);
    if (!r) r = test_statement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // unqualified_reference_expression '?' ('<VAR>' | empty_expression)
  public static boolean conditional_argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_argument")) return false;
    if (!nextTokenIsFast(b, IDENTIFIER)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _UPPER_, CONDITIONAL_ARGUMENT, null);
    r = unqualified_reference_expression(b, l + 1);
    r = r && consumeToken(b, QUESTION);
    p = r; // pin = 2
    r = r && conditional_argument_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // '<VAR>' | empty_expression
  private static boolean conditional_argument_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_argument_2")) return false;
    boolean r;
    r = consumeToken(b, VAR_PLACEHOLDER);
    if (!r) r = empty_expression(b, l + 1, -1);
    return r;
  }

  /* ********************************************************** */
  // '\' (conditional_argument | optional_argument)
  public static boolean conditional_or_optional_argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_or_optional_argument")) return false;
    if (!nextTokenIs(b, BACKSLASH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OPTIONAL_ARGUMENT, null);
    r = consumeToken(b, BACKSLASH);
    p = r; // pin = 1
    r = r && conditional_or_optional_argument_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // conditional_argument | optional_argument
  private static boolean conditional_or_optional_argument_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_or_optional_argument_1")) return false;
    boolean r;
    r = conditional_argument(b, l + 1);
    if (!r) r = optional_argument(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // 'CONNECT' connect_target 'WITH' connect_with_target ';'
  public static boolean connect_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connect_statement")) return false;
    if (!nextTokenIs(b, CONNECT_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CONNECT_STATEMENT, null);
    r = consumeToken(b, CONNECT_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, connect_target(b, l + 1));
    r = p && report_error_(b, consumeToken(b, WITH_KEYWORD)) && r;
    r = p && report_error_(b, connect_with_target(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '<VAR>' | empty_expression
  static boolean connect_target(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connect_target")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, VAR_PLACEHOLDER);
    if (!r) r = empty_expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, RapidParser::connect_target_recovery);
    return r;
  }

  /* ********************************************************** */
  // !('WITH' | ';') statement_recovery
  static boolean connect_target_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connect_target_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = connect_target_recovery_0(b, l + 1);
    r = r && statement_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !('WITH' | ';')
  private static boolean connect_target_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connect_target_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !connect_target_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'WITH' | ';'
  private static boolean connect_target_recovery_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connect_target_recovery_0_0")) return false;
    boolean r;
    r = consumeTokenFast(b, WITH_KEYWORD);
    if (!r) r = consumeTokenFast(b, SEMICOLON);
    return r;
  }

  /* ********************************************************** */
  // empty_expression
  static boolean connect_with_target(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connect_with_target")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = empty_expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, RapidParser::connect_with_target_recovery);
    return r;
  }

  /* ********************************************************** */
  // !(';') statement_recovery
  static boolean connect_with_target_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connect_with_target_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = connect_with_target_recovery_0(b, l + 1);
    r = r && statement_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(';')
  private static boolean connect_with_target_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connect_with_target_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !connect_with_target_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (';')
  private static boolean connect_with_target_recovery_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "connect_with_target_recovery_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenFast(b, SEMICOLON);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // () empty_expression compound_if_statement_inner
  public static boolean else_if_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "else_if_statement")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_STATEMENT, "<else if statement>");
    r = else_if_statement_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, empty_expression(b, l + 1, -1));
    r = p && compound_if_statement_inner(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ()
  private static boolean else_if_statement_0(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // else_if_statement
  public static boolean else_if_statement_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "else_if_statement_list")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT_LIST, "<else if statement list>");
    r = else_if_statement(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'ELSE' statement_list
  static boolean else_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "else_statement")) return false;
    if (!nextTokenIs(b, ELSE_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ELSE_KEYWORD);
    p = r; // pin = 1
    r = r && statement_list(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // () (empty_expression)
  static boolean error(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = error_0(b, l + 1);
    p = r; // pin = 1
    r = r && error_1(b, l + 1);
    exit_section_(b, l, m, r, p, RapidParser::error_recovery);
    return r || p;
  }

  // ()
  private static boolean error_0(PsiBuilder b, int l) {
    return true;
  }

  // (empty_expression)
  private static boolean error_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = empty_expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'ERROR' error_list? inner_statement_list
  public static boolean error_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error_clause")) return false;
    if (!nextTokenIs(b, ERROR_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT_LIST, null);
    r = consumeToken(b, ERROR_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, error_clause_1(b, l + 1));
    r = p && inner_statement_list(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // error_list?
  private static boolean error_clause_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error_clause_1")) return false;
    error_list(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' error (error_tail)* ')'
  public static boolean error_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error_list")) return false;
    if (!nextTokenIs(b, LPARENTH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_LIST, null);
    r = consumeToken(b, LPARENTH);
    p = r; // pin = 1
    r = r && report_error_(b, error(b, l + 1));
    r = p && report_error_(b, error_list_2(b, l + 1)) && r;
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (error_tail)*
  private static boolean error_list_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error_list_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!error_list_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "error_list_2", c)) break;
    }
    return true;
  }

  // (error_tail)
  private static boolean error_list_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error_list_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = error_tail(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(',' | ')') statement_recovery
  static boolean error_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = error_recovery_0(b, l + 1);
    r = r && statement_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(',' | ')')
  private static boolean error_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !error_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ',' | ')'
  private static boolean error_recovery_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error_recovery_0_0")) return false;
    boolean r;
    r = consumeTokenFast(b, COMMA);
    if (!r) r = consumeTokenFast(b, RPARENTH);
    return r;
  }

  /* ********************************************************** */
  // ',' error
  static boolean error_tail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "error_tail")) return false;
    if (!nextTokenIs(b, COMMA)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && error(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'EXIT' ';'
  public static boolean exit_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "exit_statement")) return false;
    if (!nextTokenIs(b, EXIT_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, EXIT_STATEMENT, null);
    r = consumeTokens(b, 1, EXIT_KEYWORD, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // visibility? ('VAR' | 'PERS' | 'CONST') type_element identifier array? [':=' optional_expression] ';'
  public static boolean field(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FIELD, "<field>");
    r = field_0(b, l + 1);
    r = r && field_1(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, type_element(b, l + 1));
    r = p && report_error_(b, identifier(b, l + 1)) && r;
    r = p && report_error_(b, field_4(b, l + 1)) && r;
    r = p && report_error_(b, field_5(b, l + 1)) && r;
    r = p && consumeToken(b, SEMICOLON) && r;
    register_hook_(b, LEFT_BINDER, ADJACENT_LINE_COMMENTS);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // visibility?
  private static boolean field_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_0")) return false;
    visibility(b, l + 1);
    return true;
  }

  // 'VAR' | 'PERS' | 'CONST'
  private static boolean field_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_1")) return false;
    boolean r;
    r = consumeToken(b, VAR_KEYWORD);
    if (!r) r = consumeToken(b, PERS_KEYWORD);
    if (!r) r = consumeToken(b, CONST_KEYWORD);
    return r;
  }

  // array?
  private static boolean field_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_4")) return false;
    array(b, l + 1);
    return true;
  }

  // [':=' optional_expression]
  private static boolean field_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_5")) return false;
    field_5_0(b, l + 1);
    return true;
  }

  // ':=' optional_expression
  private static boolean field_5_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "field_5_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CEQ);
    r = r && optional_expression(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(<<eof>>) (module_with_recovery)*
  static boolean file(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = file_0(b, l + 1);
    p = r; // pin = 1
    r = r && file_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !(<<eof>>)
  private static boolean file_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !file_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>>
  private static boolean file_0_0(PsiBuilder b, int l) {
    return eof(b, l + 1);
  }

  // (module_with_recovery)*
  private static boolean file_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!file_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "file_1", c)) break;
    }
    return true;
  }

  // (module_with_recovery)
  private static boolean file_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "file_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = module_with_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '<ARG>' | conditional_or_optional_argument | required_first_argument
  static boolean first_argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "first_argument")) return false;
    boolean r;
    r = consumeToken(b, ARG_PLACEHOLDER);
    if (!r) r = conditional_or_optional_argument(b, l + 1);
    if (!r) r = required_first_argument(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // (!(')') parameter_statement_recovery) parameter_group
  static boolean first_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "first_parameter")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = first_parameter_0(b, l + 1);
    p = r; // pin = 1
    r = r && parameter_group(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !(')') parameter_statement_recovery
  private static boolean first_parameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "first_parameter_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = first_parameter_0_0(b, l + 1);
    r = r && parameter_statement_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(')')
  private static boolean first_parameter_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "first_parameter_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, RPARENTH);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'FOR' target_variable ('FROM' empty_expression) ('TO' empty_expression) ['STEP' empty_expression] ('DO' statement_list) 'ENDFOR'
  public static boolean for_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_statement")) return false;
    if (!nextTokenIs(b, FOR_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FOR_STATEMENT, null);
    r = consumeToken(b, FOR_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, target_variable(b, l + 1));
    r = p && report_error_(b, for_statement_2(b, l + 1)) && r;
    r = p && report_error_(b, for_statement_3(b, l + 1)) && r;
    r = p && report_error_(b, for_statement_4(b, l + 1)) && r;
    r = p && report_error_(b, for_statement_5(b, l + 1)) && r;
    r = p && consumeToken(b, ENDFOR_KEYWORD) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // 'FROM' empty_expression
  private static boolean for_statement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_statement_2")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, FROM_KEYWORD);
    p = r; // pin = 1
    r = r && empty_expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // 'TO' empty_expression
  private static boolean for_statement_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_statement_3")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, TO_KEYWORD);
    p = r; // pin = 1
    r = r && empty_expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ['STEP' empty_expression]
  private static boolean for_statement_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_statement_4")) return false;
    for_statement_4_0(b, l + 1);
    return true;
  }

  // 'STEP' empty_expression
  private static boolean for_statement_4_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_statement_4_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, STEP_KEYWORD);
    p = r; // pin = 1
    r = r && empty_expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // 'DO' statement_list
  private static boolean for_statement_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "for_statement_5")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, DO_KEYWORD);
    p = r; // pin = 1
    r = r && statement_list(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // visibility? 'FUNC' type_element identifier parameter_list? statement_list [backward_clause] [error_clause] [undo_clause] 'ENDFUNC'
  static boolean function(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = function_0(b, l + 1);
    r = r && consumeToken(b, FUNC_KEYWORD);
    p = r; // pin = 2
    r = r && report_error_(b, type_element(b, l + 1));
    r = p && report_error_(b, identifier(b, l + 1)) && r;
    r = p && report_error_(b, function_4(b, l + 1)) && r;
    r = p && report_error_(b, statement_list(b, l + 1)) && r;
    r = p && report_error_(b, function_6(b, l + 1)) && r;
    r = p && report_error_(b, function_7(b, l + 1)) && r;
    r = p && report_error_(b, function_8(b, l + 1)) && r;
    r = p && consumeToken(b, ENDFUNC_KEYWORD) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // visibility?
  private static boolean function_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_0")) return false;
    visibility(b, l + 1);
    return true;
  }

  // parameter_list?
  private static boolean function_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_4")) return false;
    parameter_list(b, l + 1);
    return true;
  }

  // [backward_clause]
  private static boolean function_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_6")) return false;
    backward_clause(b, l + 1);
    return true;
  }

  // [error_clause]
  private static boolean function_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_7")) return false;
    error_clause(b, l + 1);
    return true;
  }

  // [undo_clause]
  private static boolean function_8(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_8")) return false;
    undo_clause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '(' argument_list_body ')'
  public static boolean function_call_argument_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "function_call_argument_list")) return false;
    if (!nextTokenIs(b, LPARENTH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ARGUMENT_LIST, null);
    r = consumeToken(b, LPARENTH);
    p = r; // pin = 1
    r = r && report_error_(b, argument_list_body(b, l + 1));
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // !(IDENTIFIER | 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST') ('<CSE>' | test_case_statement | test_default_statement)
  static boolean general_case_section(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_case_section")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = general_case_section_0(b, l + 1);
    p = r; // pin = 1
    r = r && general_case_section_1(b, l + 1);
    exit_section_(b, l, m, r, p, RapidParser::case_section_recovery);
    return r || p;
  }

  // !(IDENTIFIER | 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST')
  private static boolean general_case_section_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_case_section_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !general_case_section_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // IDENTIFIER | 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST'
  private static boolean general_case_section_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_case_section_0_0")) return false;
    boolean r;
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, ENDFUNC_KEYWORD);
    if (!r) r = consumeToken(b, ENDPROC_KEYWORD);
    if (!r) r = consumeToken(b, ENDTRAP_KEYWORD);
    if (!r) r = consumeToken(b, ENDIF_KEYWORD);
    if (!r) r = consumeToken(b, ELSEIF_KEYWORD);
    if (!r) r = consumeToken(b, ELSE_KEYWORD);
    if (!r) r = consumeToken(b, ENDFOR_KEYWORD);
    if (!r) r = consumeToken(b, ENDWHILE_KEYWORD);
    if (!r) r = consumeToken(b, ENDTEST_KEYWORD);
    return r;
  }

  // '<CSE>' | test_case_statement | test_default_statement
  private static boolean general_case_section_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_case_section_1")) return false;
    boolean r;
    r = consumeToken(b, CSE_PLACEHOLDER);
    if (!r) r = test_case_statement(b, l + 1);
    if (!r) r = test_default_statement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // 'GOTO' unqualified_reference_expression ';'
  public static boolean goto_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "goto_statement")) return false;
    if (!nextTokenIs(b, GOTO_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, GOTO_STATEMENT, null);
    r = consumeToken(b, GOTO_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, unqualified_reference_expression(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // IDENTIFIER | '<ID>'
  static boolean identifier(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "identifier")) return false;
    if (!nextTokenIs(b, "", IDENTIFIER, ID_PLACEHOLDER)) return false;
    boolean r;
    r = consumeToken(b, IDENTIFIER);
    if (!r) r = consumeToken(b, ID_PLACEHOLDER);
    return r;
  }

  /* ********************************************************** */
  // 'IF' empty_expression (simple_if_statement_list | compound_if_statement)
  public static boolean if_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "if_statement")) return false;
    if (!nextTokenIs(b, IF_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, IF_STATEMENT, null);
    r = consumeToken(b, IF_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, empty_expression(b, l + 1, -1));
    r = p && if_statement_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // simple_if_statement_list | compound_if_statement
  private static boolean if_statement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "if_statement_2")) return false;
    boolean r;
    r = simple_if_statement_list(b, l + 1);
    if (!r) r = compound_if_statement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // (local_variable | statement_with_recovery)*
  static boolean inner_statement_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inner_statement_list")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inner_statement_list_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inner_statement_list", c)) break;
    }
    return true;
  }

  // local_variable | statement_with_recovery
  private static boolean inner_statement_list_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inner_statement_list_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = local_variable(b, l + 1);
    if (!r) r = statement_with_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IDENTIFIER ':'
  public static boolean label_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "label_statement")) return false;
    if (!nextTokenIs(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 2, IDENTIFIER, COLON);
    exit_section_(b, m, LABEL_STATEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // field
  static boolean local_variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "local_variable")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = field(b, l + 1);
    exit_section_(b, l, m, r, false, RapidParser::local_variable_recovery);
    return r;
  }

  /* ********************************************************** */
  // !('LOCAL' | 'TASK' | 'VAR' | 'PERS' | 'CONST') statement_recovery
  static boolean local_variable_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "local_variable_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = local_variable_recovery_0(b, l + 1);
    r = r && statement_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !('LOCAL' | 'TASK' | 'VAR' | 'PERS' | 'CONST')
  private static boolean local_variable_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "local_variable_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !local_variable_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'LOCAL' | 'TASK' | 'VAR' | 'PERS' | 'CONST'
  private static boolean local_variable_recovery_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "local_variable_recovery_0_0")) return false;
    boolean r;
    r = consumeTokenFast(b, LOCAL_KEYWORD);
    if (!r) r = consumeTokenFast(b, TASK_KEYWORD);
    if (!r) r = consumeTokenFast(b, VAR_KEYWORD);
    if (!r) r = consumeTokenFast(b, PERS_KEYWORD);
    if (!r) r = consumeTokenFast(b, CONST_KEYWORD);
    return r;
  }

  /* ********************************************************** */
  // 'MODULE' identifier attribute_list? module_body 'ENDMODULE'
  public static boolean module(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module")) return false;
    if (!nextTokenIs(b, MODULE_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, MODULE, null);
    r = consumeToken(b, MODULE_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, identifier(b, l + 1));
    r = p && report_error_(b, module_2(b, l + 1)) && r;
    r = p && report_error_(b, module_body(b, l + 1)) && r;
    r = p && consumeToken(b, ENDMODULE_KEYWORD) && r;
    register_hook_(b, LEFT_BINDER, ADJACENT_LINE_COMMENTS);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // attribute_list?
  private static boolean module_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_2")) return false;
    attribute_list(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (symbol_entry)*
  static boolean module_body(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_body")) return false;
    while (true) {
      int c = current_position_(b);
      if (!module_body_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "module_body", c)) break;
    }
    return true;
  }

  // (symbol_entry)
  private static boolean module_body_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_body_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = symbol_entry(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !('MODULE')
  static boolean module_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !module_recovery_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ('MODULE')
  private static boolean module_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenFast(b, MODULE_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(<<eof>>) module
  static boolean module_with_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_with_recovery")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = module_with_recovery_0(b, l + 1);
    p = r; // pin = 1
    r = r && module(b, l + 1);
    exit_section_(b, l, m, r, p, RapidParser::module_recovery);
    return r || p;
  }

  // !(<<eof>>)
  private static boolean module_with_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "module_with_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !module_with_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>>
  private static boolean module_with_recovery_0_0(PsiBuilder b, int l) {
    return eof(b, l + 1);
  }

  /* ********************************************************** */
  // conditional_or_optional_argument | comma_argument
  static boolean next_argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "next_argument")) return false;
    if (!nextTokenIs(b, "", BACKSLASH, COMMA)) return false;
    boolean r;
    r = conditional_or_optional_argument(b, l + 1);
    if (!r) r = comma_argument(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // (',' parameter_group) | (&('\') parameter_group)
  static boolean next_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "next_parameter")) return false;
    if (!nextTokenIs(b, "", BACKSLASH, COMMA)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = next_parameter_0(b, l + 1);
    if (!r) r = next_parameter_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ',' parameter_group
  private static boolean next_parameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "next_parameter_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMA);
    p = r; // pin = 1
    r = r && parameter_group(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // &('\') parameter_group
  private static boolean next_parameter_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "next_parameter_1")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = next_parameter_1_0(b, l + 1);
    p = r; // pin = 1
    r = r && parameter_group(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // &('\')
  private static boolean next_parameter_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "next_parameter_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _AND_);
    r = consumeToken(b, BACKSLASH);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // unqualified_reference_expression [optional_argument_value]
  public static boolean optional_argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_argument")) return false;
    if (!nextTokenIsFast(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _UPPER_, OPTIONAL_ARGUMENT, null);
    r = unqualified_reference_expression(b, l + 1);
    r = r && optional_argument_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [optional_argument_value]
  private static boolean optional_argument_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_argument_1")) return false;
    optional_argument_value(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ':=' empty_expression
  static boolean optional_argument_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_argument_value")) return false;
    if (!nextTokenIs(b, CEQ)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, CEQ);
    p = r; // pin = 1
    r = r && empty_expression(b, l + 1, -1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '<EXP>' | empty_expression
  public static boolean optional_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "optional_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, EMPTY_EXPRESSION, "<optional expression>");
    r = consumeTokenFast(b, EXP_PLACEHOLDER);
    if (!r) r = empty_expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'ELSEIF' else_if_statement_list
  static boolean outer_else_if_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "outer_else_if_statement")) return false;
    if (!nextTokenIs(b, ELSEIF_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, ELSEIF_KEYWORD);
    p = r; // pin = 1
    r = r && else_if_statement_list(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // ['VAR' | 'PERS' | 'INOUT'] type_element identifier parameter_array?
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, "<parameter>");
    r = parameter_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, type_element(b, l + 1));
    r = p && report_error_(b, identifier(b, l + 1)) && r;
    r = p && parameter_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, RapidParser::parameter_recovery);
    return r || p;
  }

  // ['VAR' | 'PERS' | 'INOUT']
  private static boolean parameter_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_0")) return false;
    parameter_0_0(b, l + 1);
    return true;
  }

  // 'VAR' | 'PERS' | 'INOUT'
  private static boolean parameter_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_0_0")) return false;
    boolean r;
    r = consumeToken(b, VAR_KEYWORD);
    if (!r) r = consumeToken(b, PERS_KEYWORD);
    if (!r) r = consumeToken(b, INOUT_KEYWORD);
    return r;
  }

  // parameter_array?
  private static boolean parameter_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_3")) return false;
    parameter_array(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // '{' '*' (',' '*')* '}'
  static boolean parameter_array(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_array")) return false;
    if (!nextTokenIs(b, LBRACE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, LBRACE, ASTERISK);
    r = r && parameter_array_2(b, l + 1);
    r = r && consumeToken(b, RBRACE);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' '*')*
  private static boolean parameter_array_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_array_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter_array_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameter_array_2", c)) break;
    }
    return true;
  }

  // ',' '*'
  private static boolean parameter_array_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_array_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, COMMA, ASTERISK);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ['\'] ('<DIM>' | parameter) (parameter_group_tail)*
  public static boolean parameter_group(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_group")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_GROUP, "<parameter group>");
    r = parameter_group_0(b, l + 1);
    r = r && parameter_group_1(b, l + 1);
    r = r && parameter_group_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ['\']
  private static boolean parameter_group_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_group_0")) return false;
    consumeToken(b, BACKSLASH);
    return true;
  }

  // '<DIM>' | parameter
  private static boolean parameter_group_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_group_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DIM_PLACEHOLDER);
    if (!r) r = parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (parameter_group_tail)*
  private static boolean parameter_group_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_group_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter_group_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameter_group_2", c)) break;
    }
    return true;
  }

  // (parameter_group_tail)
  private static boolean parameter_group_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_group_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter_group_tail(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '|' ('<DIM>' | parameter)
  static boolean parameter_group_tail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_group_tail")) return false;
    if (!nextTokenIs(b, LINE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LINE);
    p = r; // pin = 1
    r = r && parameter_group_tail_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // '<DIM>' | parameter
  private static boolean parameter_group_tail_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_group_tail_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DIM_PLACEHOLDER);
    if (!r) r = parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // '(' (first_parameter (next_parameter)*)? ')'
  public static boolean parameter_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_list")) return false;
    if (!nextTokenIs(b, LPARENTH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_LIST, null);
    r = consumeToken(b, LPARENTH);
    p = r; // pin = 1
    r = r && report_error_(b, parameter_list_1(b, l + 1));
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (first_parameter (next_parameter)*)?
  private static boolean parameter_list_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_list_1")) return false;
    parameter_list_1_0(b, l + 1);
    return true;
  }

  // first_parameter (next_parameter)*
  private static boolean parameter_list_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_list_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = first_parameter(b, l + 1);
    r = r && parameter_list_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (next_parameter)*
  private static boolean parameter_list_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_list_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter_list_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameter_list_1_0_1", c)) break;
    }
    return true;
  }

  // (next_parameter)
  private static boolean parameter_list_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_list_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = next_parameter(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(')' | '|' | ',' | '\') parameter_statement_recovery
  static boolean parameter_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter_recovery_0(b, l + 1);
    r = r && parameter_statement_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(')' | '|' | ',' | '\')
  private static boolean parameter_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !parameter_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ')' | '|' | ',' | '\'
  private static boolean parameter_recovery_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_recovery_0_0")) return false;
    boolean r;
    r = consumeTokenFast(b, RPARENTH);
    if (!r) r = consumeTokenFast(b, LINE);
    if (!r) r = consumeTokenFast(b, COMMA);
    if (!r) r = consumeTokenFast(b, BACKSLASH);
    return r;
  }

  /* ********************************************************** */
  // !('LOCAL' | 'TASK' | 'CONST' | '<SMT>' | 'GOTO' | 'RETURN' |'RAISE' | 'EXIT' | 'RETRY' | 'CONNECT' | 'TRYNEXT' | '%' | 'IF' | 'FOR' | 'WHILE' | 'TEST' | 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'DEFAULT' | 'UNDO' | 'ERROR' | 'BACKWARD' | "MODULE" | 'ENDMODULE' | '<TDN>' | '<DDN>' | '<RDN>' | 'ALIAS' | 'RECORD' | 'FUNC' | 'PROC' | 'TRAP')
  static boolean parameter_statement_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_statement_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !parameter_statement_recovery_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'LOCAL' | 'TASK' | 'CONST' | '<SMT>' | 'GOTO' | 'RETURN' |'RAISE' | 'EXIT' | 'RETRY' | 'CONNECT' | 'TRYNEXT' | '%' | 'IF' | 'FOR' | 'WHILE' | 'TEST' | 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'DEFAULT' | 'UNDO' | 'ERROR' | 'BACKWARD' | "MODULE" | 'ENDMODULE' | '<TDN>' | '<DDN>' | '<RDN>' | 'ALIAS' | 'RECORD' | 'FUNC' | 'PROC' | 'TRAP'
  private static boolean parameter_statement_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_statement_recovery_0")) return false;
    boolean r;
    r = consumeTokenFast(b, LOCAL_KEYWORD);
    if (!r) r = consumeTokenFast(b, TASK_KEYWORD);
    if (!r) r = consumeTokenFast(b, CONST_KEYWORD);
    if (!r) r = consumeTokenFast(b, SMT_PLACEHOLDER);
    if (!r) r = consumeTokenFast(b, GOTO_KEYWORD);
    if (!r) r = consumeTokenFast(b, RETURN_KEYWORD);
    if (!r) r = consumeTokenFast(b, RAISE_KEYWORD);
    if (!r) r = consumeTokenFast(b, EXIT_KEYWORD);
    if (!r) r = consumeTokenFast(b, RETRY_KEYWORD);
    if (!r) r = consumeTokenFast(b, CONNECT_KEYWORD);
    if (!r) r = consumeTokenFast(b, TRYNEXT_KEYWORD);
    if (!r) r = consumeTokenFast(b, PERCENT);
    if (!r) r = consumeTokenFast(b, IF_KEYWORD);
    if (!r) r = consumeTokenFast(b, FOR_KEYWORD);
    if (!r) r = consumeTokenFast(b, WHILE_KEYWORD);
    if (!r) r = consumeTokenFast(b, TEST_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDFUNC_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDPROC_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDTRAP_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDIF_KEYWORD);
    if (!r) r = consumeTokenFast(b, ELSEIF_KEYWORD);
    if (!r) r = consumeTokenFast(b, ELSE_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDFOR_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDWHILE_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDTEST_KEYWORD);
    if (!r) r = consumeTokenFast(b, CASE_KEYWORD);
    if (!r) r = consumeTokenFast(b, DEFAULT_KEYWORD);
    if (!r) r = consumeTokenFast(b, UNDO_KEYWORD);
    if (!r) r = consumeTokenFast(b, ERROR_KEYWORD);
    if (!r) r = consumeTokenFast(b, BACKWARD_KEYWORD);
    if (!r) r = consumeTokenFast(b, MODULE_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDMODULE_KEYWORD);
    if (!r) r = consumeTokenFast(b, TDN_PLACEHOLDER);
    if (!r) r = consumeTokenFast(b, DDN_PLACEHOLDER);
    if (!r) r = consumeTokenFast(b, RDN_PLACEHOLDER);
    if (!r) r = consumeTokenFast(b, ALIAS_KEYWORD);
    if (!r) r = consumeTokenFast(b, RECORD_KEYWORD);
    if (!r) r = consumeTokenFast(b, FUNC_KEYWORD);
    if (!r) r = consumeTokenFast(b, PROC_KEYWORD);
    if (!r) r = consumeTokenFast(b, TRAP_KEYWORD);
    return r;
  }

  /* ********************************************************** */
  // visibility? 'PROC' identifier parameter_list? statement_list [backward_clause] [error_clause] [undo_clause] 'ENDPROC'
  static boolean procedure(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = procedure_0(b, l + 1);
    r = r && consumeToken(b, PROC_KEYWORD);
    p = r; // pin = 2
    r = r && report_error_(b, identifier(b, l + 1));
    r = p && report_error_(b, procedure_3(b, l + 1)) && r;
    r = p && report_error_(b, statement_list(b, l + 1)) && r;
    r = p && report_error_(b, procedure_5(b, l + 1)) && r;
    r = p && report_error_(b, procedure_6(b, l + 1)) && r;
    r = p && report_error_(b, procedure_7(b, l + 1)) && r;
    r = p && consumeToken(b, ENDPROC_KEYWORD) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // visibility?
  private static boolean procedure_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure_0")) return false;
    visibility(b, l + 1);
    return true;
  }

  // parameter_list?
  private static boolean procedure_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure_3")) return false;
    parameter_list(b, l + 1);
    return true;
  }

  // [backward_clause]
  private static boolean procedure_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure_5")) return false;
    backward_clause(b, l + 1);
    return true;
  }

  // [error_clause]
  private static boolean procedure_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure_6")) return false;
    error_clause(b, l + 1);
    return true;
  }

  // [undo_clause]
  private static boolean procedure_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure_7")) return false;
    undo_clause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // argument_list_body
  public static boolean procedure_call_argument_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure_call_argument_list")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ARGUMENT_LIST, "<procedure call argument list>");
    r = argument_list_body(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // procedure_target () procedure_call_argument_list ';'
  public static boolean procedure_call_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure_call_statement")) return false;
    if (!nextTokenIsFast(b, IDENTIFIER) &&
        !nextTokenIs(b, "<procedure call statement>", PERCENT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROCEDURE_CALL_STATEMENT, "<procedure call statement>");
    r = procedure_target(b, l + 1);
    r = r && procedure_call_statement_1(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, procedure_call_argument_list(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ()
  private static boolean procedure_call_statement_1(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // unqualified_reference_expression | '%' empty_expression '%'
  static boolean procedure_target(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure_target")) return false;
    if (!nextTokenIsFast(b, IDENTIFIER) &&
        !nextTokenIs(b, "", PERCENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unqualified_reference_expression(b, l + 1);
    if (!r) r = procedure_target_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '%' empty_expression '%'
  private static boolean procedure_target_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "procedure_target_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PERCENT);
    r = r && empty_expression(b, l + 1, -1);
    r = r && consumeToken(b, PERCENT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // () IDENTIFIER
  static boolean qualified_tail(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualified_tail")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = qualified_tail_0(b, l + 1);
    p = r; // pin = 1
    r = r && consumeToken(b, IDENTIFIER);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ()
  private static boolean qualified_tail_0(PsiBuilder b, int l) {
    return true;
  }

  /* ********************************************************** */
  // 'RAISE' empty_expression? ';'
  public static boolean raise_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "raise_statement")) return false;
    if (!nextTokenIs(b, RAISE_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RAISE_STATEMENT, null);
    r = consumeToken(b, RAISE_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, raise_statement_1(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // empty_expression?
  private static boolean raise_statement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "raise_statement_1")) return false;
    empty_expression(b, l + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // visibility? 'RECORD' identifier component_list 'ENDRECORD'
  public static boolean record(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RECORD, "<record>");
    r = record_0(b, l + 1);
    r = r && consumeToken(b, RECORD_KEYWORD);
    p = r; // pin = 2
    r = r && report_error_(b, identifier(b, l + 1));
    r = p && report_error_(b, component_list(b, l + 1)) && r;
    r = p && consumeToken(b, ENDRECORD_KEYWORD) && r;
    register_hook_(b, LEFT_BINDER, ADJACENT_LINE_COMMENTS);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // visibility?
  private static boolean record_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "record_0")) return false;
    visibility(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // !('\') [unqualified_reference_expression ':='] empty_expression
  public static boolean required_argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_argument")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, REQUIRED_ARGUMENT, "<required argument>");
    r = required_argument_0(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, required_argument_1(b, l + 1));
    r = p && empty_expression(b, l + 1, -1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !('\')
  private static boolean required_argument_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_argument_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !consumeToken(b, BACKSLASH);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // [unqualified_reference_expression ':=']
  private static boolean required_argument_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_argument_1")) return false;
    required_argument_1_0(b, l + 1);
    return true;
  }

  // unqualified_reference_expression ':='
  private static boolean required_argument_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_argument_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unqualified_reference_expression(b, l + 1);
    r = r && consumeToken(b, CEQ);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(';' | ')') required_argument
  static boolean required_first_argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_first_argument")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = required_first_argument_0(b, l + 1);
    p = r; // pin = 1
    r = r && required_argument(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // !(';' | ')')
  private static boolean required_first_argument_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_first_argument_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !required_first_argument_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ';' | ')'
  private static boolean required_first_argument_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "required_first_argument_0_0")) return false;
    boolean r;
    r = consumeToken(b, SEMICOLON);
    if (!r) r = consumeToken(b, RPARENTH);
    return r;
  }

  /* ********************************************************** */
  // 'RETRY' ';'
  public static boolean retry_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "retry_statement")) return false;
    if (!nextTokenIs(b, RETRY_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETRY_STATEMENT, null);
    r = consumeTokens(b, 1, RETRY_KEYWORD, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'RETURN' empty_expression? ';'
  public static boolean return_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "return_statement")) return false;
    if (!nextTokenIs(b, RETURN_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, RETURN_STATEMENT, null);
    r = consumeToken(b, RETURN_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, return_statement_1(b, l + 1));
    r = p && consumeToken(b, SEMICOLON) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // empty_expression?
  private static boolean return_statement_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "return_statement_1")) return false;
    empty_expression(b, l + 1, -1);
    return true;
  }

  /* ********************************************************** */
  // procedure | function | trap
  public static boolean routine(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "routine")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ROUTINE, "<routine>");
    r = procedure(b, l + 1);
    if (!r) r = function(b, l + 1);
    if (!r) r = trap(b, l + 1);
    register_hook_(b, LEFT_BINDER, ADJACENT_LINE_COMMENTS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // simple_statement | '<SMT>'
  public static boolean simple_if_statement_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simple_if_statement_list")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT_LIST, "<simple if statement list>");
    r = simple_statement(b, l + 1);
    if (!r) r = consumeToken(b, SMT_PLACEHOLDER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // assignment_statement |
  //                              procedure_call_statement |
  //                              goto_statement |
  //                              return_statement |
  //                              raise_statement |
  //                              exit_statement |
  //                              retry_statement |
  //                              try_next_statement |
  //                              connect_statement
  static boolean simple_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "simple_statement")) return false;
    boolean r;
    r = assignment_statement(b, l + 1);
    if (!r) r = procedure_call_statement(b, l + 1);
    if (!r) r = goto_statement(b, l + 1);
    if (!r) r = return_statement(b, l + 1);
    if (!r) r = raise_statement(b, l + 1);
    if (!r) r = exit_statement(b, l + 1);
    if (!r) r = retry_statement(b, l + 1);
    if (!r) r = try_next_statement(b, l + 1);
    if (!r) r = connect_statement(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // '<SMT>' | label_statement | simple_statement | compound_statement
  static boolean statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, SMT_PLACEHOLDER);
    if (!r) r = label_statement(b, l + 1);
    if (!r) r = simple_statement(b, l + 1);
    if (!r) r = compound_statement(b, l + 1);
    exit_section_(b, l, m, r, false, RapidParser::statement_recovery);
    return r;
  }

  /* ********************************************************** */
  // inner_statement_list
  public static boolean statement_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_list")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT_LIST, "<statement list>");
    r = inner_statement_list(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // !(IDENTIFIER) parameter_statement_recovery
  static boolean statement_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = statement_recovery_0(b, l + 1);
    r = r && parameter_statement_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(IDENTIFIER)
  private static boolean statement_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !statement_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (IDENTIFIER)
  private static boolean statement_recovery_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_recovery_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenFast(b, IDENTIFIER);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !('ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'UNDO' | 'ERROR' | 'BACKWARD' | 'DEFAULT') statement
  static boolean statement_with_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_with_recovery")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = statement_with_recovery_0(b, l + 1);
    p = r; // pin = 1
    r = r && statement(b, l + 1);
    exit_section_(b, l, m, r, p, RapidParser::statement_recovery);
    return r || p;
  }

  // !('ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'UNDO' | 'ERROR' | 'BACKWARD' | 'DEFAULT')
  private static boolean statement_with_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_with_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !statement_with_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // 'ENDFUNC' | 'ENDPROC' | 'ENDTRAP' | 'ENDIF' | 'ELSEIF' | 'ELSE' | 'ENDFOR' | 'ENDWHILE' | 'ENDTEST' | 'CASE' | 'UNDO' | 'ERROR' | 'BACKWARD' | 'DEFAULT'
  private static boolean statement_with_recovery_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "statement_with_recovery_0_0")) return false;
    boolean r;
    r = consumeTokenFast(b, ENDFUNC_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDPROC_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDTRAP_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDIF_KEYWORD);
    if (!r) r = consumeTokenFast(b, ELSEIF_KEYWORD);
    if (!r) r = consumeTokenFast(b, ELSE_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDFOR_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDWHILE_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDTEST_KEYWORD);
    if (!r) r = consumeTokenFast(b, CASE_KEYWORD);
    if (!r) r = consumeTokenFast(b, UNDO_KEYWORD);
    if (!r) r = consumeTokenFast(b, ERROR_KEYWORD);
    if (!r) r = consumeTokenFast(b, BACKWARD_KEYWORD);
    if (!r) r = consumeTokenFast(b, DEFAULT_KEYWORD);
    return r;
  }

  /* ********************************************************** */
  // '<TDN>' | '<DDN>' | '<RDN>' | alias | record | field | routine
  static boolean symbol(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbol")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TDN_PLACEHOLDER);
    if (!r) r = consumeToken(b, DDN_PLACEHOLDER);
    if (!r) r = consumeToken(b, RDN_PLACEHOLDER);
    if (!r) r = alias(b, l + 1);
    if (!r) r = record(b, l + 1);
    if (!r) r = field(b, l + 1);
    if (!r) r = routine(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !(<<eof>> | 'ENDMODULE') symbol
  static boolean symbol_entry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbol_entry")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = symbol_entry_0(b, l + 1);
    p = r; // pin = 1
    r = r && symbol(b, l + 1);
    exit_section_(b, l, m, r, p, RapidParser::symbol_recovery);
    return r || p;
  }

  // !(<<eof>> | 'ENDMODULE')
  private static boolean symbol_entry_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbol_entry_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !symbol_entry_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // <<eof>> | 'ENDMODULE'
  private static boolean symbol_entry_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbol_entry_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eof(b, l + 1);
    if (!r) r = consumeToken(b, ENDMODULE_KEYWORD);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // !("MODULE" | 'ENDMODULE' | '<TDN>' | '<DDN>' | '<RDN>' | 'LOCAL' | 'TASK' | 'ALIAS' | 'RECORD' | 'VAR' | 'PERS' | 'CONST' | 'FUNC' | 'PROC' | 'TRAP')
  static boolean symbol_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbol_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !symbol_recovery_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // "MODULE" | 'ENDMODULE' | '<TDN>' | '<DDN>' | '<RDN>' | 'LOCAL' | 'TASK' | 'ALIAS' | 'RECORD' | 'VAR' | 'PERS' | 'CONST' | 'FUNC' | 'PROC' | 'TRAP'
  private static boolean symbol_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "symbol_recovery_0")) return false;
    boolean r;
    r = consumeTokenFast(b, MODULE_KEYWORD);
    if (!r) r = consumeTokenFast(b, ENDMODULE_KEYWORD);
    if (!r) r = consumeTokenFast(b, TDN_PLACEHOLDER);
    if (!r) r = consumeTokenFast(b, DDN_PLACEHOLDER);
    if (!r) r = consumeTokenFast(b, RDN_PLACEHOLDER);
    if (!r) r = consumeTokenFast(b, LOCAL_KEYWORD);
    if (!r) r = consumeTokenFast(b, TASK_KEYWORD);
    if (!r) r = consumeTokenFast(b, ALIAS_KEYWORD);
    if (!r) r = consumeTokenFast(b, RECORD_KEYWORD);
    if (!r) r = consumeTokenFast(b, VAR_KEYWORD);
    if (!r) r = consumeTokenFast(b, PERS_KEYWORD);
    if (!r) r = consumeTokenFast(b, CONST_KEYWORD);
    if (!r) r = consumeTokenFast(b, FUNC_KEYWORD);
    if (!r) r = consumeTokenFast(b, PROC_KEYWORD);
    if (!r) r = consumeTokenFast(b, TRAP_KEYWORD);
    return r;
  }

  /* ********************************************************** */
  // identifier
  public static boolean target_variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "target_variable")) return false;
    if (!nextTokenIs(b, "<target variable>", IDENTIFIER, ID_PLACEHOLDER)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, TARGET_VARIABLE, "<target variable>");
    r = identifier(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // 'CASE' test_expression_list ':' statement_list
  public static boolean test_case_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "test_case_statement")) return false;
    if (!nextTokenIs(b, CASE_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TEST_CASE_STATEMENT, null);
    r = consumeToken(b, CASE_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, test_expression_list(b, l + 1));
    r = p && report_error_(b, consumeToken(b, COLON)) && r;
    r = p && statement_list(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // 'DEFAULT' ':' statement_list
  public static boolean test_default_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "test_default_statement")) return false;
    if (!nextTokenIs(b, DEFAULT_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TEST_CASE_STATEMENT, null);
    r = consumeTokens(b, 1, DEFAULT_KEYWORD, COLON);
    p = r; // pin = 1
    r = r && statement_list(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // empty_expression (',' empty_expression)*
  public static boolean test_expression_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "test_expression_list")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPRESSION_LIST, "<test expression list>");
    r = empty_expression(b, l + 1, -1);
    r = r && test_expression_list_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (',' empty_expression)*
  private static boolean test_expression_list_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "test_expression_list_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!test_expression_list_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "test_expression_list_1", c)) break;
    }
    return true;
  }

  // ',' empty_expression
  private static boolean test_expression_list_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "test_expression_list_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && empty_expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // 'TEST' empty_expression (general_case_section)* 'ENDTEST'
  public static boolean test_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "test_statement")) return false;
    if (!nextTokenIs(b, TEST_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TEST_STATEMENT, null);
    r = consumeToken(b, TEST_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, empty_expression(b, l + 1, -1));
    r = p && report_error_(b, test_statement_2(b, l + 1)) && r;
    r = p && consumeToken(b, ENDTEST_KEYWORD) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (general_case_section)*
  private static boolean test_statement_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "test_statement_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!test_statement_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "test_statement_2", c)) break;
    }
    return true;
  }

  // (general_case_section)
  private static boolean test_statement_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "test_statement_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = general_case_section(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // visibility? 'TRAP' identifier parameter_list? statement_list [backward_clause] [error_clause] [undo_clause] 'ENDTRAP'
  static boolean trap(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "trap")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = trap_0(b, l + 1);
    r = r && consumeToken(b, TRAP_KEYWORD);
    p = r; // pin = 2
    r = r && report_error_(b, identifier(b, l + 1));
    r = p && report_error_(b, trap_3(b, l + 1)) && r;
    r = p && report_error_(b, statement_list(b, l + 1)) && r;
    r = p && report_error_(b, trap_5(b, l + 1)) && r;
    r = p && report_error_(b, trap_6(b, l + 1)) && r;
    r = p && report_error_(b, trap_7(b, l + 1)) && r;
    r = p && consumeToken(b, ENDTRAP_KEYWORD) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // visibility?
  private static boolean trap_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "trap_0")) return false;
    visibility(b, l + 1);
    return true;
  }

  // parameter_list?
  private static boolean trap_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "trap_3")) return false;
    parameter_list(b, l + 1);
    return true;
  }

  // [backward_clause]
  private static boolean trap_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "trap_5")) return false;
    backward_clause(b, l + 1);
    return true;
  }

  // [error_clause]
  private static boolean trap_6(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "trap_6")) return false;
    error_clause(b, l + 1);
    return true;
  }

  // [undo_clause]
  private static boolean trap_7(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "trap_7")) return false;
    undo_clause(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // 'TRYNEXT' ';'
  public static boolean try_next_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "try_next_statement")) return false;
    if (!nextTokenIs(b, TRYNEXT_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, TRY_NEXT_STATEMENT, null);
    r = consumeTokens(b, 1, TRYNEXT_KEYWORD, SEMICOLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // unqualified_reference_expression
  public static boolean type_element(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "type_element")) return false;
    if (!nextTokenIsFast(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unqualified_reference_expression(b, l + 1);
    exit_section_(b, m, TYPE_ELEMENT, r);
    return r;
  }

  /* ********************************************************** */
  // 'UNDO' inner_statement_list
  public static boolean undo_clause(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "undo_clause")) return false;
    if (!nextTokenIs(b, UNDO_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, STATEMENT_LIST, null);
    r = consumeToken(b, UNDO_KEYWORD);
    p = r; // pin = 1
    r = r && inner_statement_list(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // '<VAR>' | empty_expression
  static boolean variable_target(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_target")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, VAR_PLACEHOLDER);
    if (!r) r = empty_expression(b, l + 1, -1);
    exit_section_(b, l, m, r, false, RapidParser::variable_target_recovery);
    return r;
  }

  /* ********************************************************** */
  // !(':=' | ';') statement_recovery
  static boolean variable_target_recovery(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_target_recovery")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = variable_target_recovery_0(b, l + 1);
    r = r && statement_recovery(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // !(':=' | ';')
  private static boolean variable_target_recovery_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_target_recovery_0")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !variable_target_recovery_0_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ':=' | ';'
  private static boolean variable_target_recovery_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_target_recovery_0_0")) return false;
    boolean r;
    r = consumeTokenFast(b, CEQ);
    if (!r) r = consumeTokenFast(b, SEMICOLON);
    return r;
  }

  /* ********************************************************** */
  // 'LOCAL' | 'TASK'
  static boolean visibility(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "visibility")) return false;
    if (!nextTokenIs(b, "", LOCAL_KEYWORD, TASK_KEYWORD)) return false;
    boolean r;
    r = consumeToken(b, LOCAL_KEYWORD);
    if (!r) r = consumeToken(b, TASK_KEYWORD);
    return r;
  }

  /* ********************************************************** */
  // 'WHILE' empty_expression 'DO' statement_list 'ENDWHILE'
  public static boolean while_statement(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "while_statement")) return false;
    if (!nextTokenIs(b, WHILE_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, WHILE_STATEMENT, null);
    r = consumeToken(b, WHILE_KEYWORD);
    p = r; // pin = 1
    r = r && report_error_(b, empty_expression(b, l + 1, -1));
    r = p && report_error_(b, consumeToken(b, DO_KEYWORD)) && r;
    r = p && report_error_(b, statement_list(b, l + 1)) && r;
    r = p && consumeToken(b, ENDWHILE_KEYWORD) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
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
  public static boolean empty_expression(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "empty_expression")) return false;
    addVariant(b, "<empty expression>");
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, "<empty expression>");
    r = unary_not_expression(b, l + 1);
    if (!r) r = unary_additive_expression(b, l + 1);
    if (!r) r = literal_expression(b, l + 1);
    if (!r) r = unqualified_reference_expression(b, l + 1);
    if (!r) r = aggregate_expression(b, l + 1);
    if (!r) r = parenthesised_expression(b, l + 1);
    p = r;
    r = r && empty_expression_0(b, l + 1, g);
    exit_section_(b, l, m, null, r, p, null);
    return r || p;
  }

  public static boolean empty_expression_0(PsiBuilder b, int l, int g) {
    if (!recursion_guard_(b, l, "empty_expression_0")) return false;
    boolean r = true;
    while (true) {
      Marker m = enter_section_(b, l, _LEFT_, null);
      if (g < 0 && consumeTokenSmart(b, OR_KEYWORD)) {
        r = empty_expression(b, l, 0);
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 0 && consumeTokenSmart(b, XOR_KEYWORD)) {
        r = empty_expression(b, l, 0);
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 1 && consumeTokenSmart(b, AND_KEYWORD)) {
        r = empty_expression(b, l, 1);
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 2 && binary_relational_expression_0(b, l + 1)) {
        r = empty_expression(b, l, 2);
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 3 && binary_additive_expression_0(b, l + 1)) {
        r = empty_expression(b, l, 3);
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 4 && binary_multiplicative_expression_0(b, l + 1)) {
        r = empty_expression(b, l, 4);
        exit_section_(b, l, m, BINARY_EXPRESSION, r, true, null);
      }
      else if (g < 6 && qualified_reference_expression_0(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, REFERENCE_EXPRESSION, r, true, null);
      }
      else if (g < 6 && array(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, INDEX_EXPRESSION, r, true, null);
      }
      else if (g < 6 && leftMarkerIs(b, REFERENCE_EXPRESSION) && function_call_argument_list(b, l + 1)) {
        r = true;
        exit_section_(b, l, m, FUNCTION_CALL_EXPRESSION, r, true, null);
      }
      else {
        exit_section_(b, l, m, null, false, false, null);
        break;
      }
    }
    return r;
  }

  public static boolean unary_not_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_not_expression")) return false;
    if (!nextTokenIsSmart(b, NOT_KEYWORD)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = consumeTokenSmart(b, NOT_KEYWORD);
    p = r;
    r = p && empty_expression(b, l, 0);
    exit_section_(b, l, m, UNARY_EXPRESSION, r, p, null);
    return r || p;
  }

  // '<' | '<=' | '=' | '>' | '>=' | '<>'
  private static boolean binary_relational_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binary_relational_expression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, LT);
    if (!r) r = consumeTokenSmart(b, LE);
    if (!r) r = consumeTokenSmart(b, EQ);
    if (!r) r = consumeTokenSmart(b, GT);
    if (!r) r = consumeTokenSmart(b, GE);
    if (!r) r = consumeTokenSmart(b, LTGT);
    return r;
  }

  // '+' | '-'
  private static boolean binary_additive_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binary_additive_expression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, PLUS);
    if (!r) r = consumeTokenSmart(b, MINUS);
    return r;
  }

  // '*' | '/' | 'DIV' | 'MOD'
  private static boolean binary_multiplicative_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binary_multiplicative_expression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, ASTERISK);
    if (!r) r = consumeTokenSmart(b, DIV);
    if (!r) r = consumeTokenSmart(b, DIV_KEYWORD);
    if (!r) r = consumeTokenSmart(b, MOD_KEYWORD);
    return r;
  }

  public static boolean unary_additive_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_additive_expression")) return false;
    if (!nextTokenIsSmart(b, MINUS, PLUS)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, null);
    r = unary_additive_expression_0(b, l + 1);
    p = r;
    r = p && empty_expression(b, l, 5);
    exit_section_(b, l, m, UNARY_EXPRESSION, r, p, null);
    return r || p;
  }

  // '+' | '-'
  private static boolean unary_additive_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_additive_expression_0")) return false;
    boolean r;
    r = consumeTokenSmart(b, PLUS);
    if (!r) r = consumeTokenSmart(b, MINUS);
    return r;
  }

  // 'TRUE' | 'FALSE' | INTEGER_LITERAL | STRING_LITERAL
  public static boolean literal_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL_EXPRESSION, "<literal expression>");
    r = consumeTokenSmart(b, TRUE_KEYWORD);
    if (!r) r = consumeTokenSmart(b, FALSE_KEYWORD);
    if (!r) r = consumeTokenSmart(b, INTEGER_LITERAL);
    if (!r) r = consumeTokenSmart(b, STRING_LITERAL);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // IDENTIFIER
  public static boolean unqualified_reference_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unqualified_reference_expression")) return false;
    if (!nextTokenIsSmart(b, IDENTIFIER)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, IDENTIFIER);
    exit_section_(b, m, REFERENCE_EXPRESSION, r);
    return r;
  }

  // '.' qualified_tail
  private static boolean qualified_reference_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "qualified_reference_expression_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, DOT);
    r = r && qualified_tail(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '[' (empty_expression (',' empty_expression)*)? ']'
  public static boolean aggregate_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregate_expression")) return false;
    if (!nextTokenIsSmart(b, LBRACKET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, LBRACKET);
    r = r && aggregate_expression_1(b, l + 1);
    r = r && consumeToken(b, RBRACKET);
    exit_section_(b, m, AGGREGATE_EXPRESSION, r);
    return r;
  }

  // (empty_expression (',' empty_expression)*)?
  private static boolean aggregate_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregate_expression_1")) return false;
    aggregate_expression_1_0(b, l + 1);
    return true;
  }

  // empty_expression (',' empty_expression)*
  private static boolean aggregate_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregate_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = empty_expression(b, l + 1, -1);
    r = r && aggregate_expression_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (',' empty_expression)*
  private static boolean aggregate_expression_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregate_expression_1_0_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!aggregate_expression_1_0_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "aggregate_expression_1_0_1", c)) break;
    }
    return true;
  }

  // ',' empty_expression
  private static boolean aggregate_expression_1_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "aggregate_expression_1_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokenSmart(b, COMMA);
    r = r && empty_expression(b, l + 1, -1);
    exit_section_(b, m, null, r);
    return r;
  }

  // '(' empty_expression ')'
  public static boolean parenthesised_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parenthesised_expression")) return false;
    if (!nextTokenIsSmart(b, LPARENTH)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARENTHESISED_EXPRESSION, null);
    r = consumeTokenSmart(b, LPARENTH);
    p = r; // pin = 1
    r = r && report_error_(b, empty_expression(b, l + 1, -1));
    r = p && consumeToken(b, RPARENTH) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

}
