package io.windmill.windmill.persistence.web;

public class Receipt {
	
    private String data;    
    
	public Receipt() {
		super();
	}

	public Receipt(String data) {
		super();
		this.data = data;
	}

    public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object that) {
		if (this == that)
			return true;
		
		if (!(that instanceof Receipt))
			return false;
		
		Receipt receipt = (Receipt) that;
		
		return this.data.equals(receipt.data);
	}
	
	@Override
	public String toString() {
		return String.format("{data:%s}", this.data);
	}
}
