
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Very basic Java to Swift syntax converter.
 * See test/Test.java and test/Test.swift for an idea of what this produces.
 * This is a work in progress...
 *
 * TODO:
 *  Class (static) method invocations should be qualified.
 *  Method and fields with the same name should be disambiguated.
 *  Final vars should become "let" vars.
 *
 * @author Pat Niemeyer (pat@pat.net)
 */
public class J2SwiftListener extends Java8BaseListener
{
    CommonTokenStream tokens;
    TokenStreamRewriter rewriter;

    boolean inConstructor;
    int formalParameterPosition;

    // Some basic type mappings
    static Map<String,String> typeMap = new HashMap<>();
    static {
        typeMap.put("float", "Float");
        typeMap.put("Float", "Float");
        typeMap.put("int", "Int");
        typeMap.put("Integer", "Int");
        typeMap.put("long", "Int64");
        typeMap.put("Long", "Int64");
        typeMap.put("boolean", "Bool");
        typeMap.put("Boolean", "Bool");
        typeMap.put("Map", "Dictionary");
        typeMap.put("HashSet", "Set");
        typeMap.put("HashMap", "Dictionary");
        typeMap.put("List", "Array");
        typeMap.put("ArrayList", "Array");
    }

    // Some basic modifier mappings (others in context)
    static Map<String,String> modifierMap = new HashMap<>();
    static {
        modifierMap.put("protected", "internal");
        modifierMap.put("volatile", "/*volatile*/");
    }

    public J2SwiftListener( CommonTokenStream tokens )
    {
        this.tokens = tokens;
        this.rewriter = new TokenStreamRewriter( tokens );
    }

    Java8Parser.UnannTypeContext unannType;
    @Override public void enterFieldDeclaration( Java8Parser.FieldDeclarationContext ctx ) {
        //:	fieldModifier* unannType variableDeclaratorList ';'
        // Store the unannType for the variableDeclarator (convenience)
        unannType = ctx.unannType();
    }
    @Override public void exitFieldDeclaration( Java8Parser.FieldDeclarationContext ctx ) {
        // replace on exit because the unannType rules will rewrite it
        replace( ctx.unannType(), "var" );
        unannType = null;
    }

    @Override public void enterLocalVariableDeclaration( Java8Parser.LocalVariableDeclarationContext ctx ) {
        //:	variableModifier* unannType variableDeclaratorList
        unannType = ctx.unannType();
    }
    @Override public void exitLocalVariableDeclaration( Java8Parser.LocalVariableDeclarationContext ctx ) {
        replace( ctx.unannType(), "var" );
        unannType = null;
    }

    @Override public void enterConstantDeclaration( Java8Parser.ConstantDeclarationContext ctx ) {
        //:	constantModifier* unannType variableDeclaratorList ';'
        unannType = ctx.unannType();
    }
    @Override public void exitConstantDeclaration( Java8Parser.ConstantDeclarationContext ctx ) {
        replace( ctx.unannType(), "var" );
        unannType = null;
    }

    @Override
    public void exitVariableDeclarator( Java8Parser.VariableDeclaratorContext ctx )
    {
        //:	variableDeclaratorId ('=' variableInitializer)?
        // We could search the parent contexts for unannType but since we have to remove it anyway we store it.
        // Use the rewritten text, not the original.
        // todo: not sure what's up here, crashing on lambdas
        try {
            rewriter.insertAfter( ctx.variableDeclaratorId().stop, " : " + getText( unannType ) );
        } catch ( Exception e ) {
            // do nothing
        }
    }

    @Override
    public void enterConstructorDeclaration( Java8Parser.ConstructorDeclarationContext ctx )
    {
        //:	constructorModifier* constructorDeclarator throws_? constructorBody
        // Search children of constructorBody for any explicit constructor invocations
        List<Java8Parser.ExplicitConstructorInvocationContext> eci =
                ctx.constructorBody().getRuleContexts( Java8Parser.ExplicitConstructorInvocationContext.class );
        if ( !eci.isEmpty() ) {
            rewriter.insertBefore( ctx.constructorDeclarator().start, "convenience " );
        }
    }

    @Override
    public void enterConstructorDeclarator( Java8Parser.ConstructorDeclaratorContext ctx )
    {
        //:	typeParameters? simpleTypeName '(' formalParameterList? ')'
        replace( ctx.simpleTypeName(), "init");
        inConstructor = true;
    }

    @Override
    public void exitConstructorDeclaration( Java8Parser.ConstructorDeclarationContext ctx ) {
        inConstructor = false;
    }

    @Override
    public void enterFormalParameterList( Java8Parser.FormalParameterListContext ctx )
    {
        // called from methodDeclarator
        //:	formalParameters ',' lastFormalParameter
        //    |	lastFormalParameter
        formalParameterPosition = 0;
    }

    @Override
    public void enterFormalParameters( Java8Parser.FormalParametersContext ctx )
    {
        // called from formalParameterList
        //:	formalParameter (',' formalParameter)*
        //    |	receiverParameter (',' formalParameter)*
        formalParameterPosition = 0;
    }

    @Override
    public void exitFormalParameter( Java8Parser.FormalParameterContext ctx )
    {
        rewriter.insertAfter( ctx.variableDeclaratorId().stop, " : " + getText( ctx.unannType() ) );

        //:	variableModifier* unannType variableDeclaratorId
        if ( formalParameterPosition++ > 0 || inConstructor ) {
            replace( ctx.unannType(), "_" );
        } else {
            removeRight( ctx.unannType() );
        }
    }

    @Override
    public void exitMethodHeader( Java8Parser.MethodHeaderContext ctx )
    {
        //:	result methodDeclarator throws_?
        //|	typeParameters annotation* result methodDeclarator throws_?
        if ( !ctx.result().getText().equals( "void" )) {
            rewriter.insertAfter( ctx.methodDeclarator().stop, " -> " + getText( ctx.result() ) );
        }
        replace( ctx.result(), "func" );
    }

    @Override
    public void enterPackageDeclaration( Java8Parser.PackageDeclarationContext ctx ) {
        rewriter.insertBefore( ctx.start, "// " );
    }

    @Override
    public void enterPrimaryNoNewArray_lfno_primary( Java8Parser.PrimaryNoNewArray_lfno_primaryContext ctx ) {
        if ( ctx.getText().equals( "this" )) { replace( ctx, "self" ); }
    }

    @Override
    public void enterFieldModifier( Java8Parser.FieldModifierContext ctx ) {
        // changed in 1.2
        //if ( ctx.getText().equals( "static" )) { replace( ctx, "class" ); }
    }
    @Override
    public void enterMethodModifier( Java8Parser.MethodModifierContext ctx ) {
        if ( ctx.getText().equals( "static" )) { replace( ctx, "class" ); }
    }

    @Override
    public void enterLiteral( Java8Parser.LiteralContext ctx )
    {
        //IntegerLiteral
        //        |	FloatingPointLiteral
        //        |	BooleanLiteral
        //        |	CharacterLiteral
        //        |	StringLiteral
        //        |	NullLiteral
        if ( ctx.getText().equals( "null" ) ) {
            replace(ctx, "nil");
        } else
        if ( ctx.FloatingPointLiteral() != null ) {
            String text = ctx.getText();
            if ( text.toLowerCase().endsWith( "f" ) ) {
                text = text.substring( 0, text.length()-1 );
                replace(ctx, text);
            }
        }
    }


    @Override
    public void exitClassInstanceCreationExpression( Java8Parser.ClassInstanceCreationExpressionContext ctx )
    {
        //:	'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        //|	expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        //|	primary '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        if ( ctx.start.getText().equals( "new" ) ) {
            replaceFirst( ctx, Java8Lexer.Identifier, mapType(ctx.Identifier().get(0).getText()) );
            rewriter.delete( ctx.start );
            rewriter.delete( ctx.start.getTokenIndex() + 1 ); // space
        }
    }

    @Override
    public void enterClassInstanceCreationExpression_lfno_primary( Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext ctx )
    {
        //:	'new' typeArguments? annotation* Identifier ('.' annotation* Identifier)* typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        //|	expressionName '.' 'new' typeArguments? annotation* Identifier typeArgumentsOrDiamond? '(' argumentList? ')' classBody?
        if ( ctx.start.getText().equals( "new" ) ) {
            replaceFirst( ctx, Java8Lexer.Identifier, mapType(ctx.Identifier().get(0).getText()) );
            rewriter.delete( ctx.start );
            rewriter.delete( ctx.start.getTokenIndex() + 1 ); // space
        }
    }

    @Override
    public void enterThrowStatement( Java8Parser.ThrowStatementContext ctx )
    {
        //:	'throw' expression ';'
        rewriter.insertBefore( ctx.start, "throwException() /* " );
        rewriter.insertAfter( ctx.stop, " */" );
    }

    @Override
    public void enterCastExpression( Java8Parser.CastExpressionContext ctx )
    {
        //:	'(' primitiveType ')' unaryExpression
        //    |	'(' referenceType additionalBound* ')' unaryExpressionNotPlusMinus
        //    |	'(' referenceType additionalBound* ')' lambdaExpression
        if ( ctx.primitiveType() != null ) {
            replace( ctx.primitiveType(), mapType( ctx.primitiveType() ) );
        }
    }

    @Override
    public void exitUnannType( Java8Parser.UnannTypeContext ctx )
    {
        // mapping may already have been done by more specific rule but this shouldn't hurt it
        // todo: this needs to be more specific, preventing rewrites on generic type args
        //if ( !ctx.getText().contains( "<" ) && !ctx.getText().contains( "[" )) {
            replace( ctx, mapType( getText( ctx ) ) );
        //}
    }

    @Override
    public void exitArrayType( Java8Parser.ArrayTypeContext ctx ) {
        //:	primitiveType dims
        //|	classOrInterfaceType dims
        //|	typeVariable dims
        ParserRuleContext rule;
        if ( ctx.primitiveType() != null ) {
            rule = ctx.primitiveType();
        } else if ( ctx.classOrInterfaceType() != null ) {
            rule = ctx.classOrInterfaceType();
        } else {
            rule = ctx.typeVariable();
        }
        replace( ctx, "["+mapType(rule)+"]" );
    }

    @Override
    public void exitUnannArrayType( Java8Parser.UnannArrayTypeContext ctx ) {
        //:	unannPrimitiveType dims
        //|	unannClassOrInterfaceType dims
        //|	unannTypeVariable dims
        ParserRuleContext rule;
        if ( ctx.unannPrimitiveType() != null ) {
            rule = ctx.unannPrimitiveType();
        } else if ( ctx.unannClassOrInterfaceType() != null ) {
            rule = ctx.unannClassOrInterfaceType();
        } else {
            rule = ctx.unannTypeVariable();
        }
        replace( ctx, "["+mapType(rule)+"]" );
    }

    @Override
    public void enterExplicitConstructorInvocation( Java8Parser.ExplicitConstructorInvocationContext ctx )
    {
        //:	typeArguments? 'this' '(' argumentList? ')' ';'
        //    |	typeArguments? 'super' '(' argumentList? ')' ';'
        //    |	expressionName '.' typeArguments? 'super' '(' argumentList? ')' ';'
        //    |	primary '.' typeArguments? 'super' '(' argumentList? ')' ';'
        List<TerminalNode> thisTokens = ctx.getTokens( Java8Lexer.THIS );
        if ( thisTokens != null && !thisTokens.isEmpty() ) {
            rewriter.replace( thisTokens.get( 0 ).getSymbol().getTokenIndex(), "self.init" );
        }

    }

    @Override
    public void enterImportDeclaration( Java8Parser.ImportDeclarationContext ctx )
    {
        rewriter.insertBefore( ctx.start, "// " );
    }

    @Override
    public void enterSuperclass( Java8Parser.SuperclassContext ctx )
    {
        //:	'extends' classType
        replaceFirst( ctx, Java8Lexer.EXTENDS, " : " );
    }

    @Override
    public void enterSuperinterfaces( Java8Parser.SuperinterfacesContext ctx )
    {
        //:	'implements' interfaceTypeList
        replaceFirst( ctx, Java8Lexer.IMPLEMENTS, " : " );
    }

    @Override
    public void exitFieldModifier( Java8Parser.FieldModifierContext ctx ) {
        replace( ctx, mapModifier( ctx ) );
    }
    @Override
    public void exitMethodModifier( Java8Parser.MethodModifierContext ctx ) {
        replace( ctx, mapModifier( ctx ) );
    }
    @Override
    public void exitClassModifier( Java8Parser.ClassModifierContext ctx ) {
        replace( ctx, mapModifier( ctx ) );
    }

    @Override
    public void enterNormalInterfaceDeclaration( Java8Parser.NormalInterfaceDeclarationContext ctx )
    {
        //:	interfaceModifier* 'interface' Identifier typeParameters? extendsInterfaces? interfaceBody
        List<TerminalNode> intfTokens = ctx.getTokens( Java8Lexer.INTERFACE );
        rewriter.replace( intfTokens.get( 0 ).getSymbol().getTokenIndex(), "protocol" );
    }

    @Override
    public void exitBasicForStatement( Java8Parser.BasicForStatementContext ctx )
    {
        //:	'for' '(' forInit? ';' expression? ';' forUpdate? ')' statement
        deleteFirst( ctx, Java8Lexer.RPAREN );
        replaceFirst( ctx, Java8Lexer.LPAREN, " " ); // todo: should check spacing here
        if ( !ctx.statement().start.getText().equals( "{" ) ) {
            rewriter.insertBefore( ctx.statement().start, "{ " );
            rewriter.insertAfter( ctx.statement().stop, " }" );
        }
    }

    @Override
    public void exitWhileStatement( Java8Parser.WhileStatementContext ctx )
    {
        //:	'while' '(' expression ')' statement
        deleteFirst( ctx, Java8Lexer.RPAREN );
        deleteFirst( ctx, Java8Lexer.LPAREN );
        if ( !ctx.statement().start.getText().equals( "{" ) ) {
            rewriter.insertBefore( ctx.statement().start, "{ " );
            rewriter.insertAfter( ctx.statement().stop, " }" );
        }
    }

    @Override
    public void exitMethodInvocation( Java8Parser.MethodInvocationContext ctx )
    {
        // todo: make a map for these
        if ( ctx.getText().startsWith( "System.out.println" ) ) {
            replace( ctx, "println(" + getText( ctx.argumentList() ) + ")" );
        }
    }

    @Override
    public void exitEnhancedForStatement( Java8Parser.EnhancedForStatementContext ctx )
    {
        //:	'for' '(' variableModifier* unannType variableDeclaratorId ':' expression ')' statement
        if ( !ctx.statement().start.getText().equals( "{" ) ) {
            rewriter.insertBefore( ctx.statement().start, "{ " );
            rewriter.insertAfter( ctx.statement().stop, " }" );
        }
        String st = getText( ctx.statement() );

        String out = "for "+getText(ctx.variableDeclaratorId())+" : "+getText( ctx.unannType() )
                +" in "+getText( ctx.expression() ) + " " +st;

        replace( ctx, out );

    }

    @Override
    public void exitUnannClassType_lfno_unannClassOrInterfaceType( Java8Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx )
    {
        //unannClassType_lfno_unannClassOrInterfaceType
        //:	Identifier typeArguments?
        replaceFirst( ctx, ctx.Identifier().getSymbol().getType(), mapType( ctx.Identifier().getText() ) );
    }

    @Override
    public void exitRelationalExpression( Java8Parser.RelationalExpressionContext ctx )
    {
        //:	shiftExpression
        //    |	relationalExpression '<' shiftExpression
        //    |	relationalExpression '>' shiftExpression
        //    |	relationalExpression '<=' shiftExpression
        //    |	relationalExpression '>=' shiftExpression
        //    |	relationalExpression 'instanceof' referenceType
        replaceFirst( ctx, Java8Lexer.INSTANCEOF, "is" );
    }

    @Override
    public void exitTypeArgumentsOrDiamond( Java8Parser.TypeArgumentsOrDiamondContext ctx )
    {
        //:	typeArguments
        //        |	'<' '>'
        if ( ctx.typeArguments() == null ) {
            deleteFirst( ctx, Java8Lexer.GT );
            deleteFirst( ctx, Java8Lexer.LT );
        }
    }

    //
    // util
    //
    private void deleteFirst( ParserRuleContext ctx, int token ) {
        List<TerminalNode> tokens = ctx.getTokens( token );
        rewriter.delete( tokens.get(0).getSymbol().getTokenIndex() );
    }
    private void replaceFirst( ParserRuleContext ctx, int token, String str ) {
        List<TerminalNode> tokens = ctx.getTokens( token );
        if ( tokens == null || tokens.isEmpty() ) { return; }
        rewriter.replace( tokens.get( 0 ).getSymbol().getTokenIndex(), str );
    }


    // Get possibly rewritten text
    private String getText( ParserRuleContext ctx ) {
        if ( ctx == null ) { return ""; }
        return rewriter.getText( new Interval( ctx.start.getTokenIndex(), ctx.stop.getTokenIndex() ) );
    }

    private void replace( ParserRuleContext ctx, String s ) {
        rewriter.replace( ctx.start, ctx.stop, s );
    }

    // remove context and hidden tokens to right
    private void removeRight( ParserRuleContext ctx )
    {
        rewriter.delete( ctx.start, ctx.stop );
        List<Token> htr = tokens.getHiddenTokensToRight( ctx.stop.getTokenIndex() );
        for (Token token : htr) { rewriter.delete( token ); }
    }

    public String mapType( ParserRuleContext ctx )
    {
        //if ( ctx instanceof Java8Parser.UnannArrayTypeContext ) { }
        //String text = ctx.getText();
        String text = getText(ctx);
        return mapType( text );
    }
    public String mapType( String text )
    {
        String mapText = typeMap.get( text );
        return mapText == null ? text : mapText;
    }

    public String mapModifier( ParserRuleContext ctx )
    {
        //if ( ctx instanceof Java8Parser.UnannArrayTypeContext ) { }
        //String text = ctx.getText();
        String text = getText( ctx );
        return mapModifier( text );
    }
    public String mapModifier( String text )
    {
        String mapText = modifierMap.get( text );
        return mapText == null ? text : mapText;
    }
}

