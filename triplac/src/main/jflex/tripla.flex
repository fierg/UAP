
// Usercode Section ===========================================================

import java_cup.runtime.Symbol;

%%

// Options and Declarations Section ===========================================

/*
 * Options
 */
%class Lexer  // Name of the lexer class will be Lexer.java
%line         // Enables line count, accessed by yyline
%column       // Enables column count, accessed by yycolumn
%cup          // CUP compatibility mode

/*
 * Declarations
 */
%{
    /*
     * Creates a new java_cup.runtime.Symbol with given type
     * and no value, saves current line and column count
     * @param type - the type of the new symbol
     */
    private Symbol createSymbol(int type)
    {
        return new Symbol(type, yyline, yycolumn);
    }


    /*
     * Creates a new java_cup.runtime.Symbol with given type
     * and value, saves current line and column count
     * @param type - the type of the new symbol
     * @param attribute - the attribute of the new symbol
     */
    private Symbol createSymbol(int type, Object attribute)
    {
        return new Symbol(type, yyline, yycolumn, attribute);
    }
%}


/*
 * Macro Declarations
 */

LineTerminator  = \r | \n | \r\n
WhiteSpace      = {LineTerminator} | [ \t\f]
PositiveInteger = 0 | [1-9][0-9]*
 /* TODO */
Identifier = [:jletter:] [:jletterdigit:]*

/* Comments */
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
InputCharacter = [^\r\n]
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?
DocumentationComment = "/*" "*"+ [^/*] ~"*/"
Comment = {TraditionalComment} | {EndOfLineComment} |
          {DocumentationComment}

%%

// Lexical Rules Section ======================================================

/* Lexical Rules */
<YYINITIAL> {

	"let"   { return createSymbol(sym.LET); }
    "in"	{return sf.newSymbol("IN", sym.IN);}
    "if"	{return sf.newSymbol("IF", sym.IF);}
    "else"	{return sf.newSymbol("ELSE", sym.ELSE);}
    "while" {return sf.newSymbol("WHILE", sym.WHILE);}
    "then"	{return sf.newSymbol("THEN", sym.THEN);}
    "do"    {return sf.newSymbol("DO", sym.DO);}
    "false" {return sf.newSymbol("FBOOL",sym.BOOL,false);}
    "true"  {return sf.newSymbol("TBOOL", sym.BOOL,true);}
    "="		{return sf.newSymbol("ASSIGN", sym.ASSIGN);}
    "("		{return sf.newSymbol("LPAR", sym.LPAR);}
    ")"		{return sf.newSymbol("RPAR", sym.RPAR);}
    "{"		{return sf.newSymbol("LBRA", sym.LBRA);}
    "}"		{return sf.newSymbol("RBRA", sym.RBRA);}
    ","		{return sf.newSymbol("COMMA", sym.COMMA);}
    ";"		{return sf.newSymbol("SMICOLON", sym.SEMICOLON);}
    "+"		{return sf.newSymbol("ADD", sym.ADD);}
    "-"		{return sf.newSymbol("SUB", sym.SUB);}
    "*"		{return sf.newSymbol("MUL", sym.MUL);}
    "/"		{return sf.newSymbol("DIV", sym.DIV);}
    "=="	{return sf.newSymbol("EQ", sym.EQ);}
    "!="	{return sf.newSymbol("NEQ", sym.NEQ);}
    "<"		{return sf.newSymbol("LT", sym.LT);}
    ">"		{return sf.newSymbol("GT", sym.GT);}
    "=<"	{return sf.newSymbol("LTE", sym.LTE);}
    "=>"	{return sf.newSymbol("GTE", sym.GTE);}
    "||"    {return sf.newSymbol("OR", sym.OR);}
    "&&"    {return sf.newSymbol("AND", sym.AND);}


	// Positive integers
    {PositiveInteger} { return createSymbol( sym.CONST, new Integer(yytext()) ); }

    // Identifiers
    {Identifier} { return createSymbol( sym.ID, yytext() ); }

    // White space
    {WhiteSpace} { /* do nothing */ }
	
	// Comments
	{Comment}     { /* do nothing */ }

}

// If the input did not match one of the rules above, throw an illegal character IOException
[^] { throw new java.io.IOException("Illegal character <" + yytext() + "> at line " + yyline + ", column " + yycolumn); }

