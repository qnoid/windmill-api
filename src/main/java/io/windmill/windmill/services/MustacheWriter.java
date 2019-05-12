package io.windmill.windmill.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

public final class MustacheWriter {
	
    private final MustacheFactory mf = new DefaultMustacheFactory();

	private ByteArrayOutputStream parse(Reader reader, Map<String, Object> scopes) throws IOException {		
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    final Writer writer = new OutputStreamWriter(out);
	    final Mustache mustache = this.mf.compile(reader, "plist");
	    mustache.execute(writer, scopes);
	    writer.flush();
	
	    return out;
	}
	
	public ByteArrayOutputStream substitute(ByteArrayOutputStream buffer, Map<String, Object> substitutions) throws IOException {
		
		try {
			InputStream is = new ByteArrayInputStream(buffer.toByteArray());
			InputStreamReader reader = new InputStreamReader(is, Charset.forName("UTF-8"));

			return parse(reader, substitutions);		
		} catch (IOException e) {
			throw new RuntimeException(e.getCause());
		}
	}

	public ByteArrayOutputStream substitute(String value, Map<String, Object> substitutions) throws IOException {
		return parse(new StringReader(value), substitutions);
	}
}