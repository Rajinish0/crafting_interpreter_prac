package lox;


import java.util.List;

public abstract class Stmt{
	public interface Visitor<R>{
		R visitExpressionStmt(Expression stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
		R visitBlockStmt(Block stmt);
		R visitIfStmt(If stmt);
		R visitWhileStmt(While stmt);
		R visitFunctionStmt(Function stmt);
		R visitReturnStmt(Return stmt);
		R visitClassStmt(Class stmt);
	}

	abstract <R> R accept(Visitor<R> visitor);

	static public class Expression extends Stmt{
		public Expression(Expr expression){
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitExpressionStmt(this);
		}

		 final Expr expression;
	}

	static public class Print extends Stmt{
		public Print(Expr expression){
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitPrintStmt(this);
		}

		 final Expr expression;
	}

	static public class Var extends Stmt{
		public Var(Token identifier,Expr expression){
			this.identifier = identifier;
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitVarStmt(this);
		}

		 final Token identifier;
		 final Expr expression;
	}

	static public class Block extends Stmt{
		public Block(List<Stmt> statements){
			this.statements = statements;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitBlockStmt(this);
		}

		 final List<Stmt> statements;
	}

	static public class If extends Stmt{
		public If(Expr condition,Stmt thenBranch,Stmt elseBranch){
			this.condition = condition;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitIfStmt(this);
		}

		 final Expr condition;
		 final Stmt thenBranch;
		 final Stmt elseBranch;
	}

	static public class While extends Stmt{
		public While(Expr condition,Stmt body){
			this.condition = condition;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitWhileStmt(this);
		}

		 final Expr condition;
		 final Stmt body;
	}

	static public class Function extends Stmt{
		public Function(Token name,List<Token> params,List<Stmt> body){
			this.name = name;
			this.params = params;
			this.body = body;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitFunctionStmt(this);
		}

		 final Token name;
		 final List<Token> params;
		 final List<Stmt> body;
	}

	static public class Return extends Stmt{
		public Return(Token keyword,Expr expression){
			this.keyword = keyword;
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitReturnStmt(this);
		}

		 final Token keyword;
		 final Expr expression;
	}

	static public class Class extends Stmt{
		public Class(Token name,List<Stmt.Function> methods){
			this.name = name;
			this.methods = methods;
		}

		@Override
		<R> R accept(Visitor<R> visitor){
			return visitor.visitClassStmt(this);
		}

		 final Token name;
		 final List<Stmt.Function> methods;
	}

}
