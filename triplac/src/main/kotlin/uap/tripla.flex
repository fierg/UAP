
// Usercode Section ===========================================================

package de.unitrier.st.uap;

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

Identifier = /* TODO */ .*

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

    // Keyword tokens
	"let"   { return createSymbol(sym.LET); }

    // TODO 
    // ... 

	// Positive integers
    {PositiveInteger} { return createSymbol( sym.CONST, new Integer(yytext()) ); }

    // Identifiers
    //{Identifier} { return createSymbol( sym.ID, yytext() ); }

    // White space
    {WhiteSpace} { /* do nothing */ }
	
	// Comments
	{Comment}     { /* do nothing */ }

}

// If the input did not match one of the rules above, throw an illegal character IOException
[^] { throw new java.io.IOException("Illegal character <" + yytext() + "> at line " + yyline + ", column " + yycolumn); }

