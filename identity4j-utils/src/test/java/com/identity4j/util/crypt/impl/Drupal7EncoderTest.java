package com.identity4j.util.crypt.impl;

import java.io.UnsupportedEncodingException;

import com.identity4j.util.crypt.AbstractEncoderTest;

public class Drupal7EncoderTest extends AbstractEncoderTest {

    public Drupal7EncoderTest() throws UnsupportedEncodingException {
        super(Drupal7Encoder.ID,
                        new byte[][] { "$S$DnO4ij9KOjnBioZhI6.t.JLitZVShF7bkN/fFbUaua8nf27yTsc2".getBytes("UTF-8"),
                                        "$S$D3M39kOc.7Z1EpCad8FZfeTBJqFWyDfuMdxZuZFptqDL8HZKuz7x".getBytes("UTF-8"),
                                        "$S$Dl7IOt27lwHIIEvpCFjJnnE2qkIKaiYx8MXJxH9NxH/kN.e1BAwC".getBytes("UTF-8") },
                        new byte[][] { "$S$DnO4ij9KO".getBytes("UTF-8"), "$S$D3M39kOc.".getBytes("UTF-8"),
                                        "$S$Dl7IOt27l".getBytes("UTF-8") },
                        null, false, true);

    }

}
