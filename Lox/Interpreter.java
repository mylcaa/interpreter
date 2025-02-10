package lox;

import java.util.List;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    private Environment environment = new Environment();

    void interpret(List<Stmt> statements){
        try{
            for(Stmt statement: statements){
                execute(statement);
            }

        }catch(RuntimeError error){
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt statement){
        statement.accept(this);
    }

    private void executeBlock(List<Stmt> statements, Environment environment){
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for(Stmt statement: statements){
                execute(statement);
            }
            
        } finally {
            this.environment = previous;
        }
    
    }

    private String stringify(Object object){
        if(object == null)
            return "nil";

        if(object instanceof Double){
            String text = object.toString();
            if(text.endsWith(".0"))
                text = text.substring(0, text.length() - 2);
            return text;
        }

        return object.toString();
    }

    @Override
    public Void visitVarStmt(Stmt.Var var){
        Object value = null;

        if(var.initializer != null){
            value = evaluate(var.initializer);
        }

        environment.define(var.name._lexeme, value);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print print){
        Object value = evaluate(print.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block block){
        
        executeBlock(block.statements, new Environment(environment));
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);

        return value;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr){
        return environment.get(expr.name);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr){
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr){
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr){
        Object right = evaluate(expr.right);
        
        switch(expr.operator._type){
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Object right = evaluate(expr.right);
        Object left = evaluate(expr.left);

        switch(expr.operator._type){
            case PLUS:
                if(right instanceof Double && left instanceof Double)
                    return (double)left + (double)right;
                
                if(right instanceof String && left instanceof String)
                    return (String)left + (String)right;
                
                throw new RuntimeError(expr.operator, "Operands must be numbers or strings!");

            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;

            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;

            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;

            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
                
            case LESSER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            
            case LESSER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;

            case BANG_EQUAL:
                return !isEqual(left, right);
            
            case EQUAL_EQUAL:
                return isEqual(left, right);
            
        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object operand){
        if(operand instanceof Double)
            return;

        throw new RuntimeError(operator, "operand must be a number!");
    }

    private void checkNumberOperands(Token operator, Object operand_left, Object operand_right){
        if(operand_left instanceof Double && operand_right instanceof Double)
            return;

        throw new RuntimeError(operator, "operand must be a number!");
    }

    private boolean isEqual(Object left, Object right){
        if(left == null && right == null)
            return true;
        
        //check if left is null before calling equals on left to avoid error
        if(left == null)
            return false;
        return left.equals(right);
    }

    private boolean isTruthy(Object object){
        //if null return false since only null and false are false
        if(object == null) 
            return false;

        //if object is boolean it can only have two values
        //true and false, return whichever one it was but cast as boolean
        //as the language is dynamically typed
        if(object instanceof Boolean) 
            return (boolean)object;

        //if the object wasn't boolean or null then it is true -> return true
        return true;
    }

    //whatever subclass of Expr the "expression" belongs to
    //calls the appropriate accept function and it in turn
    //calls the appropriate visit function
    //it recursively evaluates subexpressions and returns them
    private Object evaluate(Expr expression){
        return expression.accept(this);
    }

}