package com.galaxyzeta.blog;

import java.math.BigInteger;

public class CommonTest {

	public static void main(String[] args) {
		String k = hash("123", 133);
		System.out.println(k);
	}

	private static String hash(String toHash, int factor) {
		BigInteger hash = BigInteger.ZERO;
		char[] array = toHash.toCharArray();
		for(char c: array) {
			hash = hash.multiply(new BigInteger(String.valueOf(factor))).add(new BigInteger(String.valueOf(c)));
		}
		return hash.toString();
	}


}
