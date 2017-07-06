package io.windmill.windmill.web.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import io.windmill.windmill.services.MustacheWriter;

public interface InputStreamOperations {

	static InputStreamOperation<ByteArrayOutputStream> replaceURL(String urlString) {
		return new InputStreamOperation<ByteArrayOutputStream>() {

			@Override
			public ByteArrayOutputStream apply(InputStream stream) {
				InputStreamReader reader = new InputStreamReader(stream, Charset.forName("UTF-8"));

				try {
					return new MustacheWriter().urlString(reader, urlString);
				} catch (IOException e) {
					throw new RuntimeException(e.getCause());
				}
			}
		};
	}
}
