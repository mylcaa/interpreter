package lox;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    private final Interpreter interpreter; 
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }

    private enum FunctionType{
        NONE,
        FUNCTION,
        METHOD,
        INIT
    };

    private enum ClassType{
        NONE,
        CLASS
    };


    @Override
    public Void visitBlockStmt(Stmt.Block block){
        
        beginScope();
        resolve(block.statements);
        endScope();
        
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var var){
        declare(var.name);

        if(var.initializer != null)
            resolve(var.initializer);
        
        define(var.name);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr){
        if(!scopes.isEmpty() && scopes.peek().get(expr.name._lexeme) == Boolean.FALSE)
            Lox.error(expr.name._line, "Can't read local var in its own initializer!");

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr){
        
        if(currentClass == ClassType.NONE){
            Lox.error(expr.keyword._line, "Keyword 'this' must be inside a method.");
            return null;
        }
        
        resolveLocal(expr, expr.keyword);

        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr){
        resolve(expr.value);
        resolveLocal(expr, expr.name);

        return null;
    }

    private void resolveFunction(Stmt.Function funct, FunctionType type){
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        
        beginScope();
        for(Token param: funct.params){
            declare(param);
            define(param);
        }

        resolve(funct.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    private void resolveLocal(Expr expr, Token name){
    
        for(int i = scopes.size()-1; i >= 0; --i){          //go through the scopes, starting from the innermost one to the farthest
            if(scopes.get(i).containsKey(name._lexeme)){    //if a scope contains the variable then track how many steps away it is from the current scope
                interpreter.resolve(expr, scopes.size()-1-i);
                return;
            }
        }
    }

    private void declare(Token name){
        if(scopes.isEmpty())
            return;

        Map<String, Boolean> scope = scopes.peek();
        if(scope.containsKey(name._lexeme)){
            Lox.error(name._line, "Already have a var declaration with this name within the given scope!");
        }

        scope.put(name._lexeme, false);
        
    }

    private void define(Token name){
        if(scopes.isEmpty())
            return;

        //looking at the innermost scope with peek()
        scopes.peek().put(name._lexeme, true);
        
    }

    void resolve(List<Stmt> statements){
        for(Stmt statement: statements){
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt){
        stmt.accept(this);
    }

    private void resolve(Expr expr){
        expr.accept(this);
    }

    private void beginScope(){
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope(){
        scopes.pop();
    }

    //------------------------------------------------------------------------------UNINTERESTING PARTS:
    @Override
    public Void visitClassStmt(Stmt.Class stmt){
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);
        define(stmt.name);

        beginScope();
        scopes.peek().put("this", true);

        for(Stmt.Function method: stmt.methods){
            FunctionType declaration = FunctionType.METHOD;
            if(method.name._lexeme.equals("init"))
                declaration = FunctionType.INIT;

            resolveFunction(method, declaration);
        }

        endScope();
        currentClass = enclosingClass;

        return null;
    }


    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
       if(currentFunction == FunctionType.NONE)
            Lox.error(stmt.keyword._line, "Cannot return from top-level code!");
       
        if(currentFunction == FunctionType.INIT)
            Lox.error(stmt.keyword._line, "Cannot return from initializer!");
       

       if(stmt.value != null)
            resolve(stmt.value);
       return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print print){
        resolve(print.expression);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While statement){
        resolve(statement.condition);
        resolve(statement.body);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If statement){
        resolve(statement.condition);
        resolve(statement.thenBranch);
        if(statement.elseBranch != null)
            resolve(statement.elseBranch);
        return null;
    }


    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr){
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr){
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr){
        resolve(expr.callee);

        for(Expr arg: expr.arguments)
            resolve(arg);
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr){
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr){
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr){
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr){
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

}