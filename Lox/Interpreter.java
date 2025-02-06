package lox;

include

class Interpreter implements Expr.Visitor<Object>{
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
        Object right = evaluate(expr.expression);
        
        switch(expr.operator.type){
            case MINUS:
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Expr right = evaluate(expr.right);
        Expr left = evaluate(expr.left);

        switch(expr.operator.type){
            case PLUS:
                if(right instanceof Double && left instanceof Double)
                    return (double)left + (double)right;
                
                if(right instanceof String && left instanceof String)
                    return (String)left + (String)right;

            case MINUS:
                return (double)left - (double)right;

            case SLASH:
                return (double)left / (double)right;

            case STAR:
                return (double)left * (double)right;
            
            case MINUS:
                return (double)left - (double)right;

            case GREATER:
                return (double)left > (double)right;

            case GREATER_EQUAL:
                return (double)left >= (double)right;
                
            case LESSER:
                return (double)left < (double)right;
            
            case LESSER_EQUAL:
                return (double)left <= (double)right;

            case BANG_EQUAL:
                return !isEqual(left, right);
            
            case EQUAL_EQUAL:
                return isEqual(left, right);
            
        }

        return null;
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
        return expr.accept(this);
    }

}