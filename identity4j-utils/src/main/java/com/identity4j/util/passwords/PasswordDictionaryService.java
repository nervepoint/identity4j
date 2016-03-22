package com.identity4j.util.passwords;

import java.util.Locale;

public interface PasswordDictionaryService {

	boolean containsWord(Locale locale, String word);

}
