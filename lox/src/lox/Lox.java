package lox;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {
	//so lexical analyzer will just give all the errors
	//i dont want to continue giving that info to the parser
	public static Boolean hadError =false;
	public static Boolean hadRuntimeError =false;

	public static final Interpreter interpreter = new Interpreter();

	public static void main(String[] args) throws IOException {
		if (args.length > 1){
			System.out.println("Usage: jlox [script]");
			System.exit(64);
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}

	public static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));

		if (hadError) System.exit(65);
		if (hadRuntimeError) System.exit(70);
	}

	public static void runPrompt() throws IOException{
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);

		for (;;){
			System.out.print("> ");
			String line = reader.readLine();
			if (line == null) break;
			run(line);
			hadError =false;
			hadRuntimeError =false;
		}
		System.out.println("");
	}

	public static void run(String source){
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		Parser parser = new Parser(tokens);
		List<Stmt> stmts = parser.parse();

		if (hadError) return;

		Resolver resolver = new Resolver(interpreter);
		resolver.resolve(stmts);

		if (hadError) return;

		interpreter.interpret(stmts);
		// System.out.println(new AstPrinter().print(expr));
		// for (Token token : tokens)
			// System.out.println(token);
		//System.out.println(source);
	}

	static void error(int line, String msg){
		report(line, "", msg);
	}

	static void error(Token token, String msg){
		if (token.type == TokenType.EOF)
			report(token.line, " at the end", msg);
		else 
			report(token.line, " at '" + token.lexeme + "'", msg);
	}

	static void runtimeError(RuntimeError error){
		System.err.println(error.getMessage() + 
		"\n[line " + error.token.line + "]");
		hadRuntimeError = true;
	}

	private static void report(int line, String where, String msg){
		System.out.println(
			"[line " + line + " ] + Error" + where + ": " + msg
		);
		hadError = true;
	}
}
