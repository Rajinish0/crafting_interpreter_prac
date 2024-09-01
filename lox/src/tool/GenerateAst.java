package tool;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException{
        if (args.length != 1){
            System.err.println("Usage: generate_ast <output_dir>");
            System.exit(64);
        }

        String outputDir = args[0];
        /*
         * Storing paren token for Call
         * to report runtime errors (it's the closing parenthesis)
         * for function calls
         */
        defineAst(outputDir, "Expr", Arrays.asList(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Unary    : Token operator, Expr right",
            "Variable : Token name",
            "Assignment : Token identifier, Expr expression",
            "Logical : Expr left, Token operator, Expr right",
            "PostOp : Token identifier, Token operator",
            "PreOp : Token identifier, Token operator",
            "Call : Expr callee, Token paren, List<Expr> arguments"
          ));

        /* 
         * TO DO: refactor if to allow elif
        */
        defineAst(outputDir, "Stmt", Arrays.asList(
            "Expression : Expr expression",
            "Print : Expr expression",
            "Var : Token identifier, Expr expression",
            "Block : List<Stmt> statements",
            "If : Expr condition, Stmt thenBranch, Stmt elseBranch",
            "While : Expr condition, Stmt body",
            "Function : Token name, List<Token> params, List<Stmt> body",
            "Return : Token keyword, Expr expression"
        ));
    }

    private static void defineAst(String outputDir, String absClass, List<String> subclasses) 
    throws IOException
    {
        String path = outputDir + "/" + absClass + ".java";
        try (PrintWriter writer = new PrintWriter(path, "UTF-8")) {
            writer.println("package lox;\n\n");

            //imports
            writer.println("import java.util.List;");

            writer.println("");


            writer.println("public abstract class " + absClass + "{");
            defineVisitor(writer, absClass, subclasses);
            writer.println("\tabstract <R> R accept(Visitor<R> visitor);");
            writer.println("");
            for (String clsData : subclasses){
                String[] moreData = clsData.split(":");
                String clsName = moreData[0].strip();
                writer.println("\tstatic public class " + clsName + " extends " + absClass + "{");
                
                writer.print("\t\tpublic " + clsName + "(");
                String[] typesData = moreData[1].split(",");
                for (int i =0; i < typesData.length; i++){
                    String[] tData = typesData[i].strip().split(" ");
                    writer.print(tData[0] + " " + tData[1] + (i == typesData.length - 1 ? "" : ",") );
                }
                writer.println("){");

                for (String typeData : typesData){
                    String[] tData = typeData.strip().split(" ");
                    writer.println("\t\t\tthis."+tData[1] + " = " + tData[1] + ";");
                }
                writer.println("\t\t}\n");

                writer.println("\t\t@Override");
                writer.println("\t\t<R> R accept(Visitor<R> visitor){");
                writer.println("\t\t\treturn visitor.visit"+clsName+absClass+"(this);");
                writer.println("\t\t}\n");

                for (String typeData : typesData){
                    String[] tData = typeData.strip().split(" ");
                    writer.println("\t\t final " + tData[0] + " " + tData[1] + ";");
                }

                writer.println("\t}\n");
            }
            writer.println("}");
        }
    }

    private static void defineVisitor(PrintWriter writer, String absClass, List<String> subclasses)
    throws IOException
    {
        writer.println("\tpublic interface Visitor<R>{");
        for (String type : subclasses){
            String typeName = type.split(":")[0].trim();
            writer.println("\t\tR visit"+typeName+absClass+"(" +
                          typeName + " " + absClass.toLowerCase() + ");");
        }
        writer.println("\t}\n");
    }
}
