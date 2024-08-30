package lox;


import java.util.List;

public abstract class Expr{
	public interface Visitor<R>{
		R visitBinaryExpr(Binary expr);
		R visitGroupingExpr(Grouping expr);
		R visitLiteralExpr(Literal expr);
		R visitUnaryExpr(Unary expr);
		R visitVariableExpr(Variable expr);
		R visitAssignmentExpr(Assignment expr);
		R visitLogicalExpr(Logical expr);
	}

	abstract <R> R accept(Visitor<R> visitor);

	static public class Binary extends Expr{
		public Binary(Expr left,Token operator,Expr right){
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitBinaryExpr(this);
		}

		 final Expr left;
		 final Token operator;
		 final Expr right;
	}

	static public class Grouping extends Expr{
		public Grouping(Expr expression){
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitGroupingExpr(this);
		}

		 final Expr expression;
	}

	static public class Literal extends Expr{
		public Literal(Object value){
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitLiteralExpr(this);
		}

		 final Object value;
	}

	static public class Unary extends Expr{
		public Unary(Token operator,Expr right){
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitUnaryExpr(this);
		}

		 final Token operator;
		 final Expr right;
	}

	static public class Variable extends Expr{
		public Variable(Token name){
			this.name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitVariableExpr(this);
		}

		 final Token name;
	}

	static public class Assignment extends Expr{
		public Assignment(Token identifier,Expr expression){
			this.identifier = identifier;
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitAssignmentExpr(this);
		}

		 final Token identifier;
		 final Expr expression;
	}

	static public class Logical extends Expr{
		public Logical(Expr left,Token operator,Expr right){
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitLogicalExpr(this);
		}

		 final Expr left;
		 final Token operator;
		 final Expr right;
	}

}
