/* Generated By:JavaCC: Do not edit this line. ParserConstants.java */
package org.w3c.flute.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface ParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int S = 1;
  /** RegularExpression Id. */
  int CDO = 5;
  /** RegularExpression Id. */
  int CDC = 6;
  /** RegularExpression Id. */
  int LBRACE = 7;
  /** RegularExpression Id. */
  int RBRACE = 8;
  /** RegularExpression Id. */
  int DASHMATCH = 9;
  /** RegularExpression Id. */
  int INCLUDES = 10;
  /** RegularExpression Id. */
  int EQ = 11;
  /** RegularExpression Id. */
  int PLUS = 12;
  /** RegularExpression Id. */
  int MINUS = 13;
  /** RegularExpression Id. */
  int COMMA = 14;
  /** RegularExpression Id. */
  int SEMICOLON = 15;
  /** RegularExpression Id. */
  int PRECEDES = 16;
  /** RegularExpression Id. */
  int TILDA = 17;
  /** RegularExpression Id. */
  int DIV = 18;
  /** RegularExpression Id. */
  int LBRACKET = 19;
  /** RegularExpression Id. */
  int RBRACKET = 20;
  /** RegularExpression Id. */
  int ANY = 21;
  /** RegularExpression Id. */
  int DOT = 22;
  /** RegularExpression Id. */
  int LPARAN = 23;
  /** RegularExpression Id. */
  int RPARAN = 24;
  /** RegularExpression Id. */
  int COLON = 25;
  /** RegularExpression Id. */
  int NONASCII = 26;
  /** RegularExpression Id. */
  int H = 27;
  /** RegularExpression Id. */
  int UNICODE = 28;
  /** RegularExpression Id. */
  int ESCAPE = 29;
  /** RegularExpression Id. */
  int NMSTART = 30;
  /** RegularExpression Id. */
  int NMCHAR = 31;
  /** RegularExpression Id. */
  int STRINGCHAR = 32;
  /** RegularExpression Id. */
  int D = 33;
  /** RegularExpression Id. */
  int NAME = 34;
  /** RegularExpression Id. */
  int STRING = 35;
  /** RegularExpression Id. */
  int IDENT = 36;
  /** RegularExpression Id. */
  int NUMBER = 37;
  /** RegularExpression Id. */
  int _URL = 38;
  /** RegularExpression Id. */
  int URL = 39;
  /** RegularExpression Id. */
  int PERCENTAGE = 40;
  /** RegularExpression Id. */
  int PT = 41;
  /** RegularExpression Id. */
  int MM = 42;
  /** RegularExpression Id. */
  int CM = 43;
  /** RegularExpression Id. */
  int PC = 44;
  /** RegularExpression Id. */
  int IN = 45;
  /** RegularExpression Id. */
  int PX = 46;
  /** RegularExpression Id. */
  int EMS = 47;
  /** RegularExpression Id. */
  int EXS = 48;
  /** RegularExpression Id. */
  int DEG = 49;
  /** RegularExpression Id. */
  int RAD = 50;
  /** RegularExpression Id. */
  int GRAD = 51;
  /** RegularExpression Id. */
  int MS = 52;
  /** RegularExpression Id. */
  int SECOND = 53;
  /** RegularExpression Id. */
  int HZ = 54;
  /** RegularExpression Id. */
  int KHZ = 55;
  /** RegularExpression Id. */
  int DIMEN = 56;
  /** RegularExpression Id. */
  int HASH = 57;
  /** RegularExpression Id. */
  int IMPORT_SYM = 58;
  /** RegularExpression Id. */
  int MEDIA_SYM = 59;
  /** RegularExpression Id. */
  int CHARSET_SYM = 60;
  /** RegularExpression Id. */
  int PAGE_SYM = 61;
  /** RegularExpression Id. */
  int FONT_FACE_SYM = 62;
  /** RegularExpression Id. */
  int ATKEYWORD = 63;
  /** RegularExpression Id. */
  int IMPORTANT_SYM = 64;
  /** RegularExpression Id. */
  int RANGE0 = 65;
  /** RegularExpression Id. */
  int RANGE1 = 66;
  /** RegularExpression Id. */
  int RANGE2 = 67;
  /** RegularExpression Id. */
  int RANGE3 = 68;
  /** RegularExpression Id. */
  int RANGE4 = 69;
  /** RegularExpression Id. */
  int RANGE5 = 70;
  /** RegularExpression Id. */
  int RANGE6 = 71;
  /** RegularExpression Id. */
  int RANGE = 72;
  /** RegularExpression Id. */
  int UNI = 73;
  /** RegularExpression Id. */
  int UNICODERANGE = 74;
  /** RegularExpression Id. */
  int FUNCTION = 75;
  /** RegularExpression Id. */
  int UNKNOWN = 76;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int IN_COMMENT = 1;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "<S>",
    "\"/*\"",
    "\"*/\"",
    "<token of kind 4>",
    "\"<!--\"",
    "\"-->\"",
    "\"{\"",
    "\"}\"",
    "\"|=\"",
    "\"~=\"",
    "\"=\"",
    "\"+\"",
    "\"-\"",
    "\",\"",
    "\";\"",
    "\">\"",
    "\"~\"",
    "\"/\"",
    "\"[\"",
    "\"]\"",
    "\"*\"",
    "\".\"",
    "\")\"",
    "\"(\"",
    "\":\"",
    "<NONASCII>",
    "<H>",
    "<UNICODE>",
    "<ESCAPE>",
    "<NMSTART>",
    "<NMCHAR>",
    "<STRINGCHAR>",
    "<D>",
    "<NAME>",
    "<STRING>",
    "<IDENT>",
    "<NUMBER>",
    "<_URL>",
    "<URL>",
    "<PERCENTAGE>",
    "<PT>",
    "<MM>",
    "<CM>",
    "<PC>",
    "<IN>",
    "<PX>",
    "<EMS>",
    "<EXS>",
    "<DEG>",
    "<RAD>",
    "<GRAD>",
    "<MS>",
    "<SECOND>",
    "<HZ>",
    "<KHZ>",
    "<DIMEN>",
    "<HASH>",
    "\"@import\"",
    "\"@media\"",
    "\"@charset\"",
    "\"@page\"",
    "\"@font-face\"",
    "<ATKEYWORD>",
    "<IMPORTANT_SYM>",
    "<RANGE0>",
    "<RANGE1>",
    "<RANGE2>",
    "<RANGE3>",
    "<RANGE4>",
    "<RANGE5>",
    "<RANGE6>",
    "<RANGE>",
    "<UNI>",
    "<UNICODERANGE>",
    "<FUNCTION>",
    "<UNKNOWN>",
  };

}
