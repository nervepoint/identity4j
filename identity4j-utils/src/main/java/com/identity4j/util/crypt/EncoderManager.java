package com.identity4j.util.crypt;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.identity4j.util.crypt.impl.PlainEncoder;

public class EncoderManager {

	private Map<String, Encoder> encoders = new TreeMap<String, Encoder>();

	public EncoderManager() {
	}

	public Encoder getEncoderById(String id) {
		return encoders.get(id);
	}

	public void addEncoder(Encoder encoder) {
		if (encoders.containsKey(encoder.getId())) {
			throw new IllegalArgumentException("An encoder with the ID of " + encoder.getId() + " has already been registered.");
		}
		encoders.put(encoder.getId(), encoder);
	}

	public void removeEncoder(Encoder encoder) {
		if (!encoders.containsValue(encoder)) {
			throw new IllegalArgumentException("An encoder with the ID of " + encoder.getId() + " is not registered.");
		}
		encoders.remove(encoder.getId());
	}

	public Encoder getEncoderForEncodedString(char[] string, String charset, Collection<String> supportedEncoders) {
		try {
			byte[] data = new String(string).getBytes(charset);
			for (Encoder encoder : encoders.values()) {
				if (encoder.isOfType(data, null)) {
					return encoder;
				}
			}
		} catch (UnsupportedEncodingException uee) {
			throw new Error(uee);
		}
		return null;
	}

	public Encoder getEncoderForEncodedData(byte[] encodedData, String charSet) {
		for (Encoder encoder : encoders.values()) {
			if (encoder.isOfType(encodedData, charSet)) {
				return encoder;
			}
		}
		return null;
	}

	public String[] getEncoderIds() {
		return encoders.keySet().toArray(new String[encoders.size()]);
	}

	public byte[] encode(char[] unencodedString, String encoderId, String charset, byte[] salt, byte[] passphrase)
			throws EncoderException {
		Encoder encoder = getEncoder(encoderId);
		try {
			return encoder.encode(new String(unencodedString).getBytes(charset), salt, passphrase, charset);
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

	public char[] decode(byte[] encodedBytes, String encoderId, String charset, byte[] salt, byte[] passphrase)
			throws EncoderException {
		Encoder encoder = getEncoder(encoderId);
		try {
			return new String(encoder.decode(encodedBytes, null, passphrase, charset), charset).toCharArray();
		} catch (UnsupportedEncodingException e) {
			throw new EncoderException(e);
		}
	}

	protected Encoder getEncoder(String encoderId) {
		if (encoderId == null) {
			encoderId = PlainEncoder.ID;
		}
		Encoder encoder = getEncoderById(encoderId);
		if (encoder == null) {
			throw new IllegalArgumentException("Invalid encoder " + encoderId + ".");
		}
		return encoder;
	}
}
