package com.xrbpowered.jparsedown.mdtool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xrbpowered.jparsedown.JParsedown;

public class MDTool {
	
	public static String style;
	public static String template;
	public static HashMap<String, String> vars = new HashMap<>();
	
	public static boolean errors;
	
	public static long styleTime;
	public static long templateTime;
	public static String templateExt;
	public static String mdUrlReplacement;
	
	public static String loadFile(File file) {
		try {
			FileInputStream in = new FileInputStream(file);
			byte[] buf = new byte[in.available()];
			in.read(buf);
			in.close();
			return new String(buf, StandardCharsets.UTF_8);
		}
		catch(IOException e) {
			return null;
		}
	}
	
	public static boolean saveFile(File file, String text) {
		try {
			PrintStream out = new PrintStream(file, "UTF-8");
			out.println(text);
			out.close();
			return true;
		}
		catch(IOException e) {
			return false;
		}
	}

	public static String processTemplate(String template, HashMap<String, String> variables) {
		StringBuilder sb = new StringBuilder();
		Matcher m = Pattern.compile("\\{\\{\\s*([^\\']*?|\\'.*?\\')\\s*\\}\\}").matcher(template);
		int end = 0;
		while(m.find()) {
			sb.append(template.substring(end, m.start()));
			String token = m.group(1);
			if(token.startsWith("'") && token.endsWith("'")) {
				sb.append(token.substring(1, token.length()-1));
			}
			else {
				String value = variables!=null ? variables.get(token.toLowerCase()) : null;
				if(value!=null)
					sb.append(value);
			}
			end = m.end();
		}
		sb.append(template.substring(end));
		return sb.toString();
	}

	public static void processFile(File src, File out, boolean checkTime) {
		long outTime = out.exists() ? out.lastModified() : -1L;
		if(checkTime && outTime>=0L &&
				styleTime>=0L && styleTime<outTime &&
				templateTime>=0L && templateTime<outTime &&
				src.exists() && src.lastModified()<outTime) {
			System.out.println(out.toString()+" is up to date");
			return;
		}
		
		String source = loadFile(src);
		if(source==null) {
			System.err.println("*Error* Cannot load "+src.toString());
			errors = true;
			return;
		}
		
		JParsedown parsedown = new JParsedown().setMdUrlReplacement(mdUrlReplacement);
		String body = parsedown.text(source);
		vars.put("body", body);
		vars.put("title", parsedown.title);
		String output;
		if(template!=null)
			output = processTemplate(template, vars);
		else
			output = body;
		
		if(!saveFile(out, output)) {
			System.err.println("*Error* Cannot save "+out.toString());
			errors = true;
		}
		else {
			System.out.println("Processed "+out.toString());
		}
	}

	public static void processFile(File src, File outDir, boolean checkTime, boolean checkExtension) {
		if(outDir==null) {
			outDir = src.getParentFile();
			if(outDir==null)
				outDir = new File(".");
		}
		String filename = src.getName();
		int dotIndex = filename.lastIndexOf('.');
		String name, ext;
		if(dotIndex>0 && dotIndex<filename.length()-1) {
			name = filename.substring(0, dotIndex);
			ext = filename.substring(dotIndex);
		}
		else {
			name = filename;
			ext = "";
		}

		if(!checkExtension || ext.equalsIgnoreCase(".md")) {
			if(!outDir.exists()) {
				System.out.println("Creating directory "+outDir.toString());
				outDir.mkdirs();
			}
			processFile(src, new File(outDir, name+templateExt), checkTime);
		}
	}

	public static void scanFolder(File srcDir, File outDir, boolean checkTime) {
		File[] files = srcDir.listFiles();
		for(File f : files) {
			if(f.isDirectory()) {
				if(!f.getName().startsWith("."))
					scanFolder(f, new File(outDir, f.getName()), checkTime);
			}
			else {
				processFile(f, outDir, checkTime, true);
			}
		}
	}

	public static void benchmark(String sourcePath, int n) {
		String source = loadFile(new File(sourcePath));
		if(source==null) {
			System.err.println("*Error* Cannot load source file.");
			System.exit(1);
		}
		
		long tstart = System.currentTimeMillis();
		for(int i=0; i<n; i++) {
			JParsedown parsedown = new JParsedown();
			parsedown.text(source);
		}
		long time = System.currentTimeMillis() - tstart;
		System.out.printf("File %s\n\tParsed %d times in %d ms (%.1fms per iteration)\n",
				sourcePath, n, time, (double)time/(double)n);
	}
	
	public static void help() {
		System.out.println("Usage:\n"
			+ "java -jar md.jar sourcefile [-o outputpath] [options]\n\n"
			+ "Recursive mode:\n"
			+ "java -jar md.jar -r sourcepath [-o outputpath] [options]\n");
		System.out.println("Options:\n"
			+ "-o path\n\toutput path or filename\n"
			+ "-t filename\n\tHTML template file name\n"
			+ "-s filename\n\tCSS stylesheet file name\n"
			+ "-e\tembed stylesheet\n"
			+ "-r\trecursive mode\n"
			+ "-m\tcheck files for modification\n"
			+ "-u\tenable MD links conversion\n");
	}
	
	public static void main(String[] args) {
		boolean recursiveMode = false;
		boolean checkTime = false;
		String sourcePath = null;
		String outputPath = null;
		String templatePath = null;
		String stylePath = null;
		boolean embedStyle = false;
		int benchmark = 0;
		boolean replaceMdUrls = false;

		try {
			for(int i=0; i<args.length; i++) {
				if(args[i].charAt(0)=='-') {
					switch(args[i]) {
						case "-r":
							recursiveMode = true;
							break;
						case "-m":
							checkTime = true;
							break;
						case "-o":
							outputPath = args[++i];
							break;
						case "-t":
							templatePath = args[++i];
							break;
						case "-s":
							stylePath = args[++i];
							break;
						case "-e":
							embedStyle = true;
							break;
						case "-u":
							replaceMdUrls = true;
							break;
						case "--help":
							help();
							System.exit(0);
							return;
						case "--version":
							System.out.println("JParsedown library version: "+JParsedown.version);
							System.exit(0);
							return;
						case "--benchmark":
							benchmark = Integer.parseInt(args[++i]);
							break;
						default:
							throw new RuntimeException();
					}
				}
				else {
					sourcePath = args[i];
				}
			}
		}
		catch(Exception e) {
			System.err.println("*Error* Bad command line parameters.");
			help();
			System.exit(1);
		}
		if(sourcePath==null) {
			System.err.println("*Error* No source file or path");
			help();
			System.exit(1);
		}
		if(benchmark>0) {
			benchmark(sourcePath, benchmark);
			System.exit(0);
		}

		style = null;
		styleTime = -1L;
		if(stylePath!=null) {
			if(embedStyle) {
				File cssFile = new File(stylePath);
				String css = loadFile(cssFile);
				if(css==null) {
					System.err.println("*Error* Cannot load stylesheet.");
					System.exit(1);
				}
				else {
					style = "<style><!--\n"+css+"\n--></style>";
					styleTime = cssFile.lastModified();
				}
			}
			else
				style = "<link rel=\"stylesheet\" href=\""+stylePath+"\" />";
		}
		vars.put("style", style);

		template = null;
		templateTime = -1L;
		templateExt = ".html";
		if(templatePath!=null) {
			File templateFile = new File(templatePath);
			template = loadFile(templateFile);
			if(template==null) {
				System.err.println("*Error* Cannot load template file.");
				System.exit(1);
			}
			else {
				templateTime = templateFile.lastModified();
			}
		}
		
		mdUrlReplacement = replaceMdUrls ? templateExt : null;

		errors = false;
		if(recursiveMode) {
			File srcDir = new File(sourcePath);
			if(!srcDir.isDirectory()) {
				System.err.println("*Error* Source path is not a directory");
				System.exit(1);
			}
			File outDir = new File(sourcePath);
			if(outputPath!=null) {
				outDir = new File(outputPath);
				if(!outDir.isDirectory()) {
					System.err.println("*Error* Output path is not a directory");
					System.exit(1);
				}
			}
			scanFolder(srcDir, outDir, checkTime);
		}
		else {
			if(outputPath!=null) {
				File outDir = new File(outputPath);
				if(!outDir.isDirectory())
					processFile(new File(sourcePath), outDir, checkTime);
				else
					processFile(new File(sourcePath), outDir, checkTime, false);
			}
			else
				processFile(new File(sourcePath), null, checkTime, false);
		}

		if(errors) {
			System.out.println("Done with errors");
			System.exit(1);
		}
		else
			System.out.println("Done");
	}

}
