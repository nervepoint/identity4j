package com.identity4j.util;

/*
 * #%L
 * Identity4J Utils
 * %%
 * Copyright (C) 2013 - 2017 LogonBox
 * %%
 * Regression test: CWE-22/CWE-23 – Zip Slip path traversal in Util.unzip().
 * Before fix: entry names containing ../ were resolved against the filesystem
 * without canonical-path validation, allowing writes outside the target dir.
 * After fix: a canonical-path check rejects any entry that escapes the target.
 * #L%
 */

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UtilCwe22Test {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    /** Creates a zip file with a single entry using the given name. */
    private File makeZip(String entryName) throws IOException {
        File zip = tmp.newFile("test.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write("content".getBytes());
            zos.closeEntry();
        }
        return zip;
    }

    @Test
    public void normalEntryExtractsSuccessfully() throws IOException {
        File zip = makeZip("subdir/file.txt");
        File dest = tmp.newFolder("dest");
        int count = Util.unzip(zip, dest);
        assertTrue("should extract 1 file", count == 1);
        assertTrue("extracted file must exist", new File(dest, "subdir/file.txt").exists());
    }

    @Test
    public void zipSlipEntryIsRejected() throws IOException {
        File zip = makeZip("../../evil.txt");
        File dest = tmp.newFolder("safe");
        try {
            Util.unzip(zip, dest);
            fail("Zip Slip entry must throw IOException");
        } catch (IOException e) {
            assertTrue("exception message must mention Zip Slip or entry",
                    e.getMessage().contains("Zip Slip") || e.getMessage().contains("evil"));
        }
    }

    @Test
    public void absolutePathEntryIsRejected() throws IOException {
        File zip = makeZip("/tmp/evil-absolute.txt");
        File dest = tmp.newFolder("safe2");
        try {
            Util.unzip(zip, dest);
            fail("Absolute-path entry must throw IOException");
        } catch (IOException e) {
            // expected
        }
    }
}
