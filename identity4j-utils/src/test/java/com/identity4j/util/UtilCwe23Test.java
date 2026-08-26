package com.identity4j.util;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * Regression tests: CWE-23 – Relative Path Traversal in Util.unzip().
 * CWE-23 is the relative-path variant of path traversal: an attacker
 * embeds "../" sequences inside a ZIP entry name to write outside the
 * intended extraction directory.
 *
 * These tests exercise patterns that are exclusively relative-path
 * traversal (CWE-23) and complement the absolute-path and basic Zip
 * Slip cases already covered by UtilCwe22Test.
 * #L%
 */

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UtilCwe23Test {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private File makeZip(String entryName) throws IOException {
        File zip = tmp.newFile("cwe23-test.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write("cwe23-payload".getBytes());
            zos.closeEntry();
        }
        return zip;
    }

    /** A normal relative path must extract successfully. */
    @Test
    public void nestedRelativeEntryExtractsSuccessfully() throws IOException {
        File zip = makeZip("a/b/c/file.txt");
        File dest = tmp.newFolder("dest-ok");
        int count = Util.unzip(zip, dest);
        assertTrue("should extract 1 file", count == 1);
        assertTrue("extracted file must exist", new File(dest, "a/b/c/file.txt").exists());
    }

    /**
     * Entry "subdir/../../../evil.txt" resolves outside the destination even
     * though it begins with a legitimate subdirectory component.
     */
    @Test
    public void relativeTraversalWithSubdirPrefixIsRejected() throws IOException {
        File zip = makeZip("subdir/../../../evil.txt");
        File dest = tmp.newFolder("dest-sub");
        try {
            Util.unzip(zip, dest);
            fail("Relative traversal entry with subdir prefix must throw IOException");
        } catch (IOException e) {
            assertTrue("exception message must mention Zip Slip or entry",
                    e.getMessage().contains("Zip Slip") || e.getMessage().contains("evil"));
        }
    }

    /**
     * Entry "a/b/../../../../../../evil.txt" — multiple up-traversals after
     * descending into a nested path.
     */
    @Test
    public void deeplyNestedRelativeTraversalIsRejected() throws IOException {
        File zip = makeZip("a/b/../../../../../../evil.txt");
        File dest = tmp.newFolder("dest-deep");
        try {
            Util.unzip(zip, dest);
            fail("Deeply nested relative traversal entry must throw IOException");
        } catch (IOException e) {
            assertTrue("exception message must mention Zip Slip or entry",
                    e.getMessage().contains("Zip Slip") || e.getMessage().contains("evil"));
        }
    }

    /**
     * Entry whose traversal lands exactly one level above the destination —
     * just barely outside the allowed directory.
     */
    @Test
    public void singleLevelUpTraversalIsRejected() throws IOException {
        File zip = makeZip("../sibling-evil.txt");
        File dest = tmp.newFolder("dest-single");
        try {
            Util.unzip(zip, dest);
            fail("Single-level-up relative traversal must throw IOException");
        } catch (IOException e) {
            // expected
            assertTrue("exception message must be non-empty", e.getMessage() != null && !e.getMessage().isEmpty());
        }
    }

    /**
     * A traversal that stays within the destination directory is safe.
     * "a/../b/file.txt" resolves canonically to "<dest>/b/file.txt" which is inside dest.
     * The entry is written to the canonical path, so we check "b/file.txt", not "a/../b/file.txt".
     */
    @Test
    public void internalTraversalThatStaysInsideDestIsAllowed() throws IOException {
        File zip = makeZip("a/../b/file.txt");
        File dest = tmp.newFolder("dest-internal");
        int count = Util.unzip(zip, dest);
        assertTrue("should extract 1 file", count == 1);
        // canonical resolution: a/../b → b, so file lands at dest/b/file.txt
        assertTrue("file must exist inside dest at canonical path", new File(dest, "b/file.txt").exists());
    }
}
