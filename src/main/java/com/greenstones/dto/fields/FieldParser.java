package com.greenstones.dto.fields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FieldParser {

	Stack<String> stack = new Stack<String>();

	public static List<Field> parse(String in) {
		Stack<String> tokens = new Stack<String>();
		List<String> t = tokenize(in);
		Collections.reverse(t);
		tokens.addAll(t);
		FieldParser p = new FieldParser(tokens);
		return p.parseGroup();
	}

	public List<Field> parseGroup() {
		// System.err.println("startGroup");
		List<Field> fields = new ArrayList<Field>();

		if (!stack.pop().equals("{"))
			throw new RuntimeException("Parse exception. Group should starts with '{'.");

		while (!stack.peek().equals("}")) {
			fields.add(parseField());
		}

		if (!stack.pop().equals("}"))
			throw new RuntimeException("Parse exception. Group should ends with '}'. ");

		// System.err.println("endGroup");
		return fields;
	}

	public Field parseField() {

		String name = stack.pop();
		// System.err.println("startField " + name);

		Field f = new Field(name);

		if (stack.peek().equals("{")) {
			f.fields = parseGroup();
		}

		// System.err.println("endField " + name);
		return f;
	}

	public static Stack<String> stackTokens(List<String> tokens) {
		Stack<String> stack = new Stack<String>();
		ArrayList<String> r = new ArrayList<String>();
		r.addAll(tokens);
		Collections.reverse(r);
		stack.addAll(r);
		return stack;
	}

	public static List<String> tokenize(String in) {
		Pattern p = Pattern.compile("[\\{|\\w+|\\}]+");
		Matcher m = p.matcher(in.replaceAll("\\{", " { ").replaceAll("\\}", " } "));
		List<String> tokens = new ArrayList<>();
		while (m.find()) {
			tokens.add(m.group(0));
		}
		return tokens;
	}

}
