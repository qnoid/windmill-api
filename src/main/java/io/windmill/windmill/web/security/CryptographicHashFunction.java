package io.windmill.windmill.web.security;

public interface CryptographicHashFunction {

	public enum Algorithm {
		SHA256("SHA-256");
		
		private final String algorithm;

		private Algorithm(String algorithm) {
			this.algorithm = algorithm;
		}

		public String getAlgorithm() {
			return algorithm;
		}
	}
	
	public byte[] hash(byte[] bytes);
}
