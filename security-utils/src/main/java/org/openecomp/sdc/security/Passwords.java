/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;


public class Passwords {

	private static Logger log = LoggerFactory.getLogger( Passwords.class.getName());
	private static final Random RANDOM = new SecureRandom();
	private static final int SALT = 0;
	private static final int HASH = 1;
	private static final String HASH_ALGORITHM = "SHA-256";

	/**
	 * static utility class
	 */
	private Passwords() {
	}

	/**
	 * the method calculates a hash with a generated salt for the given password
	 * 
	 * @param password
	 * @return a "salt:hash" value
	 */
	public static String hashPassword(String password) {
		if (password!=null){
			byte[] salt = getNextSalt();
			byte[] byteData = hash(salt, password.getBytes());
			if (byteData != null) {
				return toHex(salt) + ":" + toHex(byteData);
			}
		}
		return null;
	}

	/**
	 * the method checks if the given password matches the calculated hash
	 * 
	 * @param password
	 * @param expectedHash
	 * @return
	 */
	public static boolean isExpectedPassword(String password, String expectedHash) {
		if (password==null && expectedHash==null)
			return true;
		if (password==null || expectedHash==null)	//iff exactly 1 is null
			return false;
		if (!expectedHash.contains(":")){
			log.error("invalid password expecting hash at the prefix of the password (ex. e0277df331f4ff8f74752ac4a8fbe03b:6dfbad308cdf53c9ff2ee2dca811ee92f1b359586b33027580e2ff92578edbd0)\n" +
					"\t\t\t");
			return false;
		}
		String[] params = expectedHash.split(":");
		return isExpectedPassword(password, params[SALT], params[HASH]);
	}

	/**
	 * the method checks if the given password matches the calculated hash
	 * 
	 * @param password
	 * @param salt
	 * @param hash
	 *            the hash generated using the salt
	 * @return true if the password matched the hash
	 */
	public static boolean isExpectedPassword(String password, String salt, String hash) {
		if ( password == null &&  hash == null )
			return true;
		if ( salt == null ){
			log.error("salt must be initialized");
			return false;
		}
		//unintialized params
		if ( password == null ||  hash == null )
			return false;
		byte[] saltBytes = fromHex(salt);
		byte[] hashBytes = fromHex(hash);

		byte[] byteData = hash(saltBytes, password.getBytes());
		if (byteData != null) {
			return Arrays.equals(byteData, hashBytes);
		}
		return false;
	}

	public static void main(String[] args) {
		if (args.length > 1 || args.length > 0) {
			System.out.println("[" + hashPassword(args[0]) + "]");
		} else {
			System.out.println("no passward passed.");
		}

	}

	/**
	 * Returns a random salt to be used to hash a password.
	 * 
	 * @return a 16 bytes random salt
	 */
	private static byte[] getNextSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
		return salt;
	}

	/**
	 * hase's the salt and value using the chosen algorithm
	 * 
	 * @param salt
	 * @param password
	 * @return an array of bytes resulting from the hash
	 */
	private static byte[] hash(byte[] salt, byte[] password) {
		MessageDigest md;
		byte[] byteData = null;
		try {
			md = MessageDigest.getInstance(HASH_ALGORITHM);
			md.update(salt);
			md.update(password);
			byteData = md.digest();
		} catch (NoSuchAlgorithmException e) {
		    log.error("invalid algorithm name {}", e);
			System.out.println("invalid algorithm name");
		}
		return byteData;
	}

	/**
	 * Converts a string of hexadecimal characters into a byte array.
	 *
	 * @param hex
	 *            the hex string
	 * @return the hex string decoded into a byte array
	 */
	private static byte[] fromHex(String hex) {
		if ( hex == null )
			return null;
		byte[] binary = new byte[hex.length() / 2];
		for (int i = 0; i < binary.length; i++) {
			binary[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return binary;
	}

	/**
	 * Converts a byte array into a hexadecimal string.
	 *
	 * @param array
	 *            the byte array to convert
	 * @return a length*2 character string encoding the byte array
	 */
	private static String toHex(byte[] array) {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0)
			return String.format("%0" + paddingLength + "d", 0) + hex;
		else
			return hex;
	}
}
