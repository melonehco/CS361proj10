/*
 * File: Scanner.java
 * F18 CS361 Project 10
 * Names: Melody Mao, Zena Abulhab, Yi Feng, Evan Savillo
 * Date: 12/6/18
 * This file contains the Scanner class, which reads through a file character by
 * character and generates a stream of Bantam Java tokens.
 */

package proj10AbulhabFengMaoSavillo.bantam.lexer;

import java.io.Reader;

import proj10AbulhabFengMaoSavillo.bantam.util.*;
import proj10AbulhabFengMaoSavillo.bantam.util.Error; //imported explicitly to distinguish from java.lang.Error


/**
 * The Scanner class reads in a stream of characters from a file
 * and converts them into Bantam Java tokens.
 *
 * @author Zena Abulhab
 * @author Yi Feng
 * @author Melody Mao
 * @author Evan Savillo
 */
public class Scanner
{
    private SourceFile sourceFile;
    private ErrorHandler errorHandler;

    private char currentChar;

    /**
     * Creates a new Scanner that registers errors to the given ErrorHandler
     *
     * @param handler ErrorHandler to register to
     */
    public Scanner(ErrorHandler handler)
    {
        this.errorHandler = handler;
        this.currentChar = ' ';
        this.sourceFile = null;
    }

    /**
     * Creates a new Scanner that lexes the given file into tokens
     * and registers errors to the given ErrorHandler
     *
     * @param filename file to scan
     * @param handler  ErrorHandler to register to
     */
    public Scanner(String filename, ErrorHandler handler)
    {
        this.errorHandler = handler;
        this.currentChar = ' ';
        this.sourceFile = new SourceFile(filename);
    }

    /**
     * Creates a new Scanner that lexes the characters from the given Reader into tokens
     * and registers errors to the given ErrorHandler
     *
     * @param reader  Reader to read characters from
     * @param handler ErrorHandler to register to
     */
    public Scanner(Reader reader, ErrorHandler handler)
    {
        this.errorHandler = handler;
        this.currentChar = ' ';
        this.sourceFile = new SourceFile(reader);
    }

    /**
     * Test code for Scanner methods
     * To test, run Scanner with one or more command-line arguments listing
     * files to scan.
     */
    public static void main(String[] args)
    {
        //make sure at least one filename was given
        if (args.length < 1)
        {
            System.err.println("Missing input filename");
            System.exit(-1);
        }

        //for each file given, scan
        ErrorHandler errorHandler = new ErrorHandler();
        for (String filename : args)
        {
            System.out.println("Scanning file: " + filename + "\n");

            //scan tokens
            try
            {
                Scanner scanner = new Scanner(filename, errorHandler);
                Token currentToken = scanner.scan();
                while (currentToken.kind != Token.Kind.EOF)
                {
                    System.out.println(currentToken.spelling);
                    currentToken = scanner.scan();
                }
            }
            catch (CompilationException e)
            {
                errorHandler.register(Error.Kind.LEX_ERROR, "Failed to read in source file");
            }

            //check for errors
            if (errorHandler.errorsFound())
            {
                System.out.println(String.format("\n%d errors found", errorHandler.getErrorList().size()));
            }
            else
            {
                System.out.println("\nScanning was successful");
            }

            System.out.println("-----------------------------------------------");

            //clear errors to scan next file
            errorHandler.clear();
        }
    }

    /**
     * Each call of this method builds the next Token from the contents
     * of the file being scanned and returns it. When it reaches the end of the file,
     * any calls to scan() result in a Token of kind EOF.
     *
     * @return the next token or EOF if has already reached EOF
     */
    public Token scan()
    {
        if (this.currentChar == SourceFile.eof)
            return new Token(Token.Kind.EOF,
                             Character.toString(SourceFile.eof),
                             this.sourceFile.getCurrentLineNumber());

        this.advancePastWhitespace();

        //check for single-char tokens that can be identified at once
        Token singleCharToken = this.attemptCompleteSingleCharToken();
        if (singleCharToken != null) //if was identified as a single-char token
            return singleCharToken;

        //check for longer tokens that can be identified at once by first char
        Token attemptedLongerToken = this.attemptCompleteTokenByFirstChar();
        if (attemptedLongerToken != null) //if was identified by first char
            return attemptedLongerToken;

        int lineNumber = this.sourceFile.getCurrentLineNumber();
        //integer constant
        if (Character.isDigit(this.currentChar))
        {
            return this.completeIntconstToken(lineNumber);
        }
        //identifier/boolean/keyword
        else if (Character.isLetter(this.currentChar))
        {
            return this.completeIdentifierToken(lineNumber);
        }

        //attempt to find a match for remaining token types
        attemptedLongerToken = this.completeLongerToken();

        if (attemptedLongerToken != null)
            return attemptedLongerToken;
            //if first char doesn't match any of above cases, is illegal char
        else
        {
            this.errorHandler.register(Error.Kind.LEX_ERROR,
                                       this.sourceFile.getFilename(),
                                       lineNumber,
                                       "Unexpected character: " + this.currentChar);

            this.currentChar = this.sourceFile.getNextChar();
            return new Token(Token.Kind.ERROR, Character.toString(this.currentChar), lineNumber);
        }

    }

    /**
     * checks whether the first char is a single-char token that can be identified at once,
     * i.e. that could not form the beginning of another token
     *
     * @return the single-char Token, or null if not a single-char token
     */
    private Token attemptCompleteSingleCharToken()
    {
        Token.Kind kind = null;

        boolean isTokenComplete = true; // start as true, and set to false if not a single char token
        switch (this.currentChar)
        {
            //punctuation
            case '.':
                kind = Token.Kind.DOT;
                break;
            case ':':
                kind = Token.Kind.COLON;
                break;
            case ';':
                kind = Token.Kind.SEMICOLON;
                break;
            case ',':
                kind = Token.Kind.COMMA;
                break;
            //brackets
            case '(':
                kind = Token.Kind.LPAREN;
                break;
            case ')':
                kind = Token.Kind.RPAREN;
                break;
            case '[':
                kind = Token.Kind.LBRACKET;
                break;
            case ']':
                kind = Token.Kind.RBRACKET;
                break;
            case '{':
                kind = Token.Kind.LCURLY;
                break;
            case '}':
                kind = Token.Kind.RCURLY;
                break;
            //end of file
            case SourceFile.eof:
                kind = Token.Kind.EOF;
                break;
            //some operators
            case '*':
                kind = Token.Kind.MULDIV;
                break;
            case '!':
                kind = Token.Kind.UNARYNOT;
                break;
            case '%':
                kind = Token.Kind.MULDIV;
                break;
            //otherwise, is not single-char token that can be identified at once
            default:
                isTokenComplete = false;
                break;
        }

        if (isTokenComplete)
        {
            String spelling = Character.toString(this.currentChar);
            int lineNum = this.sourceFile.getCurrentLineNumber();

            this.currentChar = this.sourceFile.getNextChar();
            return new Token(kind, spelling, lineNum);
        }
        else
            return null;
    }

    /**
     * checks whether the current token is a longer token
     * that can be identified at once by first char
     * if it is, attempts to complete the token
     *
     * @return the resulting Token, or null if not a token handled by this method
     */
    private Token attemptCompleteTokenByFirstChar()
    {
        Token.Kind kind = null;
        StringBuilder spelling = new StringBuilder();
        int lineNumber = this.sourceFile.getCurrentLineNumber();

        boolean isTokenComplete = true; // set to true, and set to false if fails next check
        spelling.append(this.currentChar);
        switch (this.currentChar)
        {
            case '&':
                kind = Token.Kind.BINARYLOGIC;
                //add a second &
                this.currentChar = this.sourceFile.getNextChar();

                if (this.currentChar != '&') //if not second &
                {
                    kind = Token.Kind.ERROR;
                    this.errorHandler.register(Error.Kind.LEX_ERROR,
                                               this.sourceFile.getFilename(),
                                               lineNumber,
                                               "Badly formed binary logic operator: &");
                    //has read in start of next token, so store in buffer
                    //this.buffer.add(this.currentChar);
                    this.currentChar = this.sourceFile.getNextChar();
                }
                else
                {
                    spelling.append(this.currentChar);
                }
                break;
            case '|':
                kind = Token.Kind.BINARYLOGIC;
                //add a second |
                this.currentChar = this.sourceFile.getNextChar();

                if (this.currentChar != '|') //if not second |
                {
                    kind = Token.Kind.ERROR;
                    this.errorHandler.register(Error.Kind.LEX_ERROR,
                                               this.sourceFile.getFilename(),
                                               lineNumber,
                                               "Badly formed binary logic operator: |");
                    //has read in start of next token, so store in buffer
                    //this.buffer.add(this.currentChar);
                    this.currentChar = this.sourceFile.getNextChar();
                }
                else
                {
                    spelling.append(this.currentChar);
                }
                break;
            case '\"':
                this.currentChar = this.sourceFile.getNextChar();
                return this.completeStringToken();
            default:
                isTokenComplete = false; // (it has failed this check)
                break;
        }

        if (isTokenComplete)
        {
            this.currentChar = this.sourceFile.getNextChar();
            return new Token(kind, spelling.toString(), lineNumber);
        }
        else
            return null;
    }

    /**
     * Builds and returns a string token starting from the current character
     *
     * @return the string token, or error token if error encountered
     */
    private Token completeStringToken()
    {
        //init string with opening " because scan method already read it in
        StringBuilder spellingBuilder = new StringBuilder("\"");
        Token.Kind kind = Token.Kind.STRCONST;

        //collect chars until closing double quote
        while (this.currentChar != '\"')
        {
            //check if too long
            if (spellingBuilder.length() > 5000)
            {
                this.errorHandler.register(Error.Kind.LEX_ERROR,
                                           this.sourceFile.getFilename(),
                                           this.sourceFile.getCurrentLineNumber(),
                                           "String exceeds maximum length");
                kind = Token.Kind.ERROR;
                break;
            }
            //check for escaped chars
            else if (this.currentChar == '\\')
            {
                spellingBuilder.append(Character.toString(this.currentChar));
                this.currentChar = this.sourceFile.getNextChar();

                //handle having escaped quote \"
                if (this.currentChar == '"')
                {
                    spellingBuilder.append(Character.toString(this.currentChar));
                    this.currentChar = this.sourceFile.getNextChar();
                }

                //check for invalid escape chars
                else if (this.currentChar != 'n' && this.currentChar != 't' &&
                        this.currentChar != '"' && this.currentChar != '\\' &&
                        this.currentChar != 'f' && this.currentChar != 'r')
                {
                    this.errorHandler.register(Error.Kind.LEX_ERROR,
                                               this.sourceFile.getFilename(),
                                               this.sourceFile.getCurrentLineNumber(),
                                               "Illegal escape char in string: \\" + this.currentChar);
                    kind = Token.Kind.ERROR;
                    break;
                }
            }
            //check if not terminated correctly
            else if (this.currentChar == SourceFile.eof || this.currentChar == SourceFile.eol)
            {
                this.errorHandler.register(Error.Kind.LEX_ERROR,
                                           this.sourceFile.getFilename(),
                                           this.sourceFile.getCurrentLineNumber(),
                                           "String not terminated");
                kind = Token.Kind.ERROR;
                break;
            }
            else
            {
                spellingBuilder.append(Character.toString(this.currentChar));
                this.currentChar = this.sourceFile.getNextChar();
            }
        }
        //append closing quote
        spellingBuilder.append(Character.toString(this.currentChar));

        int linenum = this.sourceFile.getCurrentLineNumber();

        this.currentChar = this.sourceFile.getNextChar();
        return new Token(kind, spellingBuilder.toString(), linenum);
    }

    /**
     * completes any longer token not handled by another method in this class
     *
     * @return the resulting Token, or null if not a token handled by this method
     */
    private Token completeLongerToken()
    {
        Token.Kind kind = null;
        StringBuilder spelling = new StringBuilder();
        int lineNumber = this.sourceFile.getCurrentLineNumber();

        boolean isTokenComplete = true; // set to true, and set to false if fails next check
        spelling.append(this.currentChar);
        switch (this.currentChar)
        {
            case '+': //token can be + or ++
                this.currentChar = this.sourceFile.getNextChar();
                if (this.currentChar == '+') //check whether has second +
                {
                    spelling.append(this.currentChar);
                    kind = Token.Kind.UNARYINCR;
                }
                else
                {
                    kind = Token.Kind.PLUSMINUS;
                    //has read in start of next token, so store in buffer
                    //this.buffer.add(this.currentChar);
                    return new Token(kind, spelling.toString(), lineNumber);
                }
                break;
            case '-': //token can be - or --
                this.currentChar = this.sourceFile.getNextChar();
                if (this.currentChar == '-') //check whether has second -
                {
                    spelling.append(this.currentChar);
                    kind = Token.Kind.UNARYDECR;
                }
                else
                {
                    kind = Token.Kind.PLUSMINUS;
                    //has read in start of next token, so store in buffer
                    //this.buffer.add(this.currentChar);
                    return new Token(kind, spelling.toString(), lineNumber);
                }
                break;
            case '<': //token can be < or <=
                kind = Token.Kind.COMPARE;

                this.currentChar = this.sourceFile.getNextChar();
                //check whether has =
                if (this.currentChar == '=')
                {
                    spelling.append(this.currentChar);
                }
                else
                {
                    return new Token(kind, spelling.toString(), lineNumber);
                    //has read in start of next token, so store in buffer
                    //this.buffer.add(this.currentChar);
                }

                break;
            case '>': //token can be > or >=
                kind = Token.Kind.COMPARE;

                //check whether has =
                this.currentChar = this.sourceFile.getNextChar();
                if (this.currentChar == '=')
                {
                    spelling.append(this.currentChar);
                }
                else
                {
                    return new Token(kind, spelling.toString(), lineNumber);
                    //has read in start of next token, so store in buffer
                    //this.buffer.add(this.currentChar);
                }
                break;
            case '=': //token can be = or ==
                //check whether has =
                this.currentChar = this.sourceFile.getNextChar();
                if (this.currentChar == '=')
                {
                    spelling.append(this.currentChar);
                    kind = Token.Kind.COMPARE;
                }
                else //otherwise, is just assignment operator
                {
                    kind = Token.Kind.ASSIGN;
                    return new Token(kind, spelling.toString(), lineNumber);
                    //has read in start of next token, so store in buffer
                    //this.buffer.add(this.currentChar);
                }
                break;
            case '/': //token can be / or a comment
                //check whether next char starts a comment
                this.currentChar = this.sourceFile.getNextChar();
                if (this.currentChar == '*') //block comment
                {
                    return this.completeBlockCommentToken(lineNumber);
                }
                else if (this.currentChar == '/') //single-line comment
                {
                    return this.completeLineCommentToken(lineNumber);
                }
                else
                {
                    kind = Token.Kind.MULDIV;
                    return new Token(kind, spelling.toString(), lineNumber);
                    //has read in start of next token, so store in buffer
                    //this.buffer.add(this.currentChar);
                }
            default:
                isTokenComplete = false;
                break;
        }

        if (isTokenComplete)
        {
            this.currentChar = this.sourceFile.getNextChar();
            return new Token(kind, spelling.toString(), lineNumber);
        }
        else
            return null;
    }

    /**
     * Builds and returns a block comment token starting from the current char
     * with position at the given line number
     *
     * @param lineNumber starting line number of token
     * @return the block comment token, or error token if error encountered
     */
    private Token completeBlockCommentToken(int lineNumber)
    {
        //init string with starting / because scan read it
        StringBuilder spellingBuilder = new StringBuilder("/");
        Token.Kind kind = Token.Kind.COMMENT;

        boolean atTentativeEnd = false; // a '*' has been seen
        boolean terminated = false; // a '*' and '/' have been seen in sequence

        while (!terminated && this.currentChar != SourceFile.eof)
        {
            spellingBuilder.append(this.currentChar);
            this.currentChar = this.sourceFile.getNextChar();

            if (atTentativeEnd) // if '*' has been seen
            {
                if (this.currentChar == '/')    // block comment indeed terminated
                {
                    spellingBuilder.append(this.currentChar);
                    terminated = true;
                }
                else                            // otherwise just a '*' in the middle somewhere
                    atTentativeEnd = false;
            }
            else if (this.currentChar == '*')
            {
                atTentativeEnd = true;
            }
        }

        //if left loop before seeing "*/", block comment was not terminated correctly
        if (!terminated)
        {
            this.errorHandler.register(Error.Kind.LEX_ERROR,
                                       this.sourceFile.getFilename(),
                                       this.sourceFile.getCurrentLineNumber(),
                                       "Block comment not terminated");
            kind = Token.Kind.ERROR;
        }

        this.currentChar = this.sourceFile.getNextChar();
        return new Token(kind, spellingBuilder.toString(), lineNumber);
    }

    /**
     * Builds and returns a single-line comment token starting from the current char
     *
     * @param lineNumber starting line number of token
     * @return the line comment token
     */
    private Token completeLineCommentToken(int lineNumber)
    {
        //init string with starting / because scan already read it in
        StringBuilder spellingBuilder = new StringBuilder("/");

        //collect chars until end of line or file
        while (this.currentChar != '\n' && this.currentChar != SourceFile.eof)
        {
            spellingBuilder.append(this.currentChar);
            this.currentChar = this.sourceFile.getNextChar();
        }

        //this.buffer.add(this.currentChar);
        return new Token(Token.Kind.COMMENT, spellingBuilder.toString(), lineNumber);
    }

    /**
     * Builds and returns an intconst token starting from the current char
     * Returns upon reading in any non-digit char
     *
     * @param lineNumber starting line number of token
     * @return the intconst token, or error token if error encountered
     */
    private Token completeIntconstToken(int lineNumber)
    {
        //start string with first digit read in by scan method
        StringBuilder spellingBuilder = new StringBuilder();
        Token.Kind kind = Token.Kind.INTCONST;

        //collect chars until non-digit char
        while (Character.isDigit(this.currentChar))
        {
            spellingBuilder.append(this.currentChar);
            this.currentChar = this.sourceFile.getNextChar();
        }

        //this.buffer.add(this.currentChar);

        //check whether int is too long
        try
        {
            Integer.parseInt(spellingBuilder.toString());
        }
        catch (NumberFormatException e)
        {
            this.errorHandler.register(Error.Kind.LEX_ERROR,
                                       this.sourceFile.getFilename(),
                                       this.sourceFile.getCurrentLineNumber(),
                                       "Invalid integer constant");
            kind = Token.Kind.ERROR;
        }

        return new Token(kind, spellingBuilder.toString(), lineNumber);
    }

    /**
     * Builds and returns an identifier token (or boolean or keyword)
     * starting from the current character
     * Returns upon reading in any non-identifier char
     *
     * @param lineNumber starting line number of token
     * @return the identifier token
     */
    private Token completeIdentifierToken(int lineNumber)
    {
        StringBuilder spellingBuilder = new StringBuilder();

        //collect chars until non-identifier char
        while (Character.isLetter(this.currentChar) ||
                Character.isDigit(this.currentChar) ||
                this.currentChar == '_')
        {
            spellingBuilder.append(this.currentChar);
            this.currentChar = this.sourceFile.getNextChar();
        }

        //this.buffer.add(this.currentChar);

        return new Token(Token.Kind.IDENTIFIER, spellingBuilder.toString(), lineNumber);
    }

    private void advancePastWhitespace()
    {
        while (Character.isWhitespace(this.currentChar))
        {
            this.currentChar = this.sourceFile.getNextChar();
        }
    }

    /**
     * set source file from file name
     *
     * @param filename
     */
    public void setSourceFile(String filename)
    {
        this.sourceFile = new SourceFile(filename);
    }
}