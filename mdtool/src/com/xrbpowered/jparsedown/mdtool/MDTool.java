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
	
	public static String loadFile(String path) {
		try {
			FileInputStream f = new FileInputStream(path);
			byte[] buf = new byte[f.available()];
			f.read(buf);
			f.close();
			return new String(buf, StandardCharsets.UTF_8);
		}
		catch(IOException e) {
			return null;
		}
	}
	
	public static boolean saveFile(String path, String text) {
		try {
			PrintStream out = new PrintStream(new File(path), "UTF-8");
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
	
	public static void main(String[] args) {
		String sourcePath = null;
		String outputPath = null;
		String templatePath = null;
		String stylePath = null;
		boolean embedStyle = false;
		int benchmark = 0;

		try {
			for(int i=0; i<args.length; i++) {
				if(args[i].charAt(0)=='-') {
					switch(args[i]) {
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
						case "-benchmark":
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
			System.err.println("Bad command line parameters.");
			System.exit(1);
		}
		if(sourcePath==null) {
			System.err.println("No source file");
			System.exit(1);
		}

		String source = loadFile(sourcePath);
		if(source==null) {
			System.err.println("Cannot load source file.");
			System.exit(1);
		}
		
		if(benchmark>0) {
			long tstart = System.currentTimeMillis();
			for(int i=0; i<benchmark; i++) {
				JParsedown parsedown = new JParsedown();
				parsedown.text(source);
			}
			long time = System.currentTimeMillis() - tstart;
			System.out.printf("Parsed %d times in %d ms (%.1fms per iteration)\n",
					benchmark, time, (double)time/(double)benchmark);
			System.exit(0);
		}

		if(outputPath==null) {
			int ext = sourcePath.lastIndexOf('.');
			outputPath = ext<0 ? sourcePath : sourcePath.substring(0, ext);
			outputPath += ".html";
		}
		
		JParsedown parsedown = new JParsedown();
		String body = parsedown.text(source);

		String output = body;
		if(templatePath!=null) {
			HashMap<String, String> vars = new HashMap<>();
			
			vars.put("body", body);
			vars.put("title", parsedown.title);
			
			String style = null;
			if(stylePath!=null) {
				if(embedStyle) {
					String css = loadFile(stylePath);
					if(css==null)
						System.err.println("Cannot load stylesheet.");
					else
						style = "<style><!--\n"+css+"\n--></style>";
				}
				else
					style = "<link rel=\"stylesheet\" href=\""+stylePath+"\" />";
			}
			vars.put("style", style);
	
			String template = loadFile(templatePath);
			if(template==null) {
				System.err.println("Cannot load template file.");
				System.exit(1);
			}
			
			output = processTemplate(template, vars);
		}
		
		if(!saveFile(outputPath, output)) {
			System.err.println("Cannot save output file.");
			System.exit(1);
		}
		
		System.out.println("Done");
	}

}
