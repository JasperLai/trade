package com.example.trade.demo.domain.fx.aggregate;

import java.util.Objects;
import java.util.UUID;

public final class FlatOrderId {
	private final String value;
	public FlatOrderId(String value) { this.value = Objects.requireNonNull(value); }
	public static FlatOrderId newId() { return new FlatOrderId(UUID.randomUUID().toString()); }
	public String value() { return value; }
	@Override public String toString() { return value; }
	@Override public boolean equals(Object o) {
		if (this == o) return true; if (!(o instanceof FlatOrderId other)) return false; return value.equals(other.value);
	}
	@Override public int hashCode() { return value.hashCode(); }
}
