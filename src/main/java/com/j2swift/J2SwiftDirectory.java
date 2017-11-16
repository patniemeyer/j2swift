package com.j2swift;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Very basic Java to Swift syntax converter.
 * See test/Test.java and test/Test.swift for an idea of what this produces.
 * This is a work in progress...
 * @author Pat Niemeyer (pat@pat.net)
 */
public class J2SwiftDirectory
{
	public static void main( String [] args ) throws Exception {
        if (args.length != 1){
        	System.out.println("Usage: gradle convertDirectory -Pdir={directory}");
        	System.exit(2);
        }
        String inputFile = args[0];
        convertFiles(inputFile);
    }

    private static void convertFiles(String inputDir){
	    Collection<String> files = listFileTree(new File(inputDir));
        int i = 0;
        double d = 0;
	    double size = files.size();
	    System.out.println("Total files: "+files.size());
	    for (String a : files){
	        i++;
	        d++;
	        int percent = (int)(d/size*100);//+" "+i+" of "+files.size()+
	        System.out.println(a.replace(inputDir,""));
	        System.out.println(percent+"%");
	        convertFile(a);
        }
        System.out.println("Done");
    }

    private static Pattern javaPattern = Pattern.compile(".java$");
    public static Collection<String> listFileTree(File dir) {
        Set<String> fileTree = new HashSet<>();
        if (dir == null) {
            return fileTree;
        }
        File[] files = dir.listFiles();
        if (files == null){
            return fileTree;
        }
        for (File entry : files) {
            if (entry.isFile()) {
                String filename = entry.getAbsolutePath();
                if (javaPattern.matcher(filename).find()) {
                    fileTree.add(filename);
                }
            } else fileTree.addAll(listFileTree(entry));
        }
        return fileTree;
    }

    private static void convertFile(String inputFile){
	    try {
            InputStream is = System.in;
            if (inputFile != null) {
                is = new FileInputStream(inputFile);
            }
            ANTLRInputStream input = new ANTLRInputStream(is);
            Java8Lexer lexer = new Java8Lexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            Java8Parser parser = new Java8Parser(tokens);
            ParseTree tree = parser.compilationUnit();
            ParseTreeWalker walker = new ParseTreeWalker();
            J2SwiftListener swiftListener = new J2SwiftListener(tokens);
            walker.walk(swiftListener, tree);

            String result = swiftListener.rewriter.getText();
            String fileOut = inputFile.replace(".java", ".swift");
            writeToFile(result, fileOut);
        } catch(IOException e){
	        e.printStackTrace();
        }
	}

    public static void writeToFile(String text, String targetFilePath) throws IOException
    {
        if (text == null){
            text = "";
        }
        Path targetPath = Paths.get(targetFilePath);
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        Files.write(targetPath, bytes, StandardOpenOption.CREATE);
    }
}
