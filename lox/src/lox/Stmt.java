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

}
