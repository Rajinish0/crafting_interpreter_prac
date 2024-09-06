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
		R visitPostOpExpr(PostOp expr);
		R visitPreOpExpr(PreOp expr);
		R visitCallExpr(Call expr);
		R visitGetExpr(Get expr);
		R visitSetExpr(Set expr);
		R visitThisExpr(This expr);
		R visitSuperExpr(Super expr);
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

	static public class PostOp extends Expr{
		public PostOp(Token identifier,Token operator){
			this.identifier = identifier;
			this.operator = operator;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitPostOpExpr(this);
		}

		 final Token identifier;
		 final Token operator;
	}

	static public class PreOp extends Expr{
		public PreOp(Token identifier,Token operator){
			this.identifier = identifier;
			this.operator = operator;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitPreOpExpr(this);
		}

		 final Token identifier;
		 final Token operator;
	}

	static public class Call extends Expr{
		public Call(Expr callee,Token paren,List<Expr> arguments){
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitCallExpr(this);
		}

		 final Expr callee;
		 final Token paren;
		 final List<Expr> arguments;
	}

	static public class Get extends Expr{
		public Get(Expr object,Token name){
			this.object = object;
			this.name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitGetExpr(this);
		}

		 final Expr object;
		 final Token name;
	}

	static public class Set extends Expr{
		public Set(Expr object,Token name,Expr value){
			this.object = object;
			this.name = name;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitSetExpr(this);
		}

		 final Expr object;
		 final Token name;
		 final Expr value;
	}

	static public class This extends Expr{
		public This(Token keyword){
			this.keyword = keyword;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitThisExpr(this);
		}

		 final Token keyword;
	}

	static public class Super extends Expr{
		public Super(Token keyword,Token method){
			this.keyword = keyword;
			this.method = method;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitSuperExpr(this);
		}

		 final Token keyword;
		 final Token method;
	}

}
