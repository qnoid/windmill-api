package io.windmill.windmill.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

final class MustacheWriter {
	
    private final MustacheFactory mf = new DefaultMustacheFactory();

	private ByteArrayOutputStream parse(String value, Map<String, Object> scopes) throws IOException {
		
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    final Writer writer = new OutputStreamWriter(out);
	    final Mustache mustache = this.mf.compile(new StringReader(value), "plist");
	    mustache.execute(writer, scopes);
	    writer.flush();
	
	    return out;
	}
	
	private ByteArrayOutputStream parse(Reader reader, Map<String, Object> scopes) throws IOException {
		
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    final Writer writer = new OutputStreamWriter(out);
	    final Mustache mustache = this.mf.compile(reader, "plist");
	    mustache.execute(writer, scopes);
	    writer.flush();
	
	    return out;
	}
	
	public ByteArrayOutputStream urlString(Reader reader, String urlString) throws IOException {
		final HashMap<String, Object> substitutions = new HashMap<>();
		substitutions.put("URL", urlString);
	
		return parse(reader, substitutions);
	}

	public ByteArrayOutputStream urlString(String value, String urlString) throws IOException {
		final HashMap<String, Object> substitutions = new HashMap<>();
		substitutions.put("URL", urlString);
	
		return parse(value, substitutions);
	}
}