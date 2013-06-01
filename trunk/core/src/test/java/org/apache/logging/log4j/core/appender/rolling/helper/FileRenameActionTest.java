/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core.appender.rolling.helper;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class FileRenameActionTest {

    private static final String DIR = "target/fileRename";

    @BeforeClass
    public static void beforeClass() throws Exception {
        File file = new File(DIR);
        file.mkdirs();
    }

    @AfterClass
    public static void afterClass() {
        deleteDir();
    }

    @After
    public void after() {
        deleteFiles();
    }

    @Test
    public void testRename1() throws Exception {
        File file = new File("target/fileRename/fileRename.log");
        PrintStream pos = new PrintStream(file);
        for (int i = 0; i < 100; ++i) {
            pos.println("This is line " + i);
        }
        pos.close();

        File dest = new File("target/fileRename/newFile.log");
        FileRenameAction action = new FileRenameAction(file, dest, false);
        action.execute();
        assertTrue("Renamed file does not exist", dest.exists());
        assertTrue("Old file exists", !file.exists());
    }

    @Test
    public void testEmpty() throws Exception {
        File file = new File("target/fileRename/fileRename.log");
        PrintStream pos = new PrintStream(file);
        pos.close();

        File dest = new File("target/fileRename/newFile.log");
        FileRenameAction action = new FileRenameAction(file, dest, false);
        action.execute();
        assertTrue("Renamed file does not exist", !dest.exists());
        assertTrue("Old file does not exist", !file.exists());
    }


    @Test
    public void testNoParent() throws Exception {
        File file = new File("fileRename.log");
        PrintStream pos = new PrintStream(file);
        for (int i = 0; i < 100; ++i) {
            pos.println("This is line " + i);
        }
        pos.close();

        File dest = new File("newFile.log");
        try {
            FileRenameAction action = new FileRenameAction(file, dest, false);
            action.execute();
            assertTrue("Renamed file does not exist", dest.exists());
            assertTrue("Old file exists", !file.exists());
        } finally {
            try {
                dest.delete();
                file.delete();
            } catch (Exception ex) {
                System.out.println("Unable to cleanup files written to main directory");
            }
        }
    }


    private static void deleteDir() {
        final File dir = new File(DIR);
        if (dir.exists()) {
            final File[] files = dir.listFiles();
            for (final File file : files) {
                file.delete();
            }
            dir.delete();
        }
    }

    private static void deleteFiles() {
        final File dir = new File(DIR);
        if (dir.exists()) {
            final File[] files = dir.listFiles();
            for (final File file : files) {
                file.delete();
            }
        }
    }
}
