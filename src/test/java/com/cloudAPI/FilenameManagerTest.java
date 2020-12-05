package com.cloudAPI;

import com.enums.Gender;
import com.enums.Language;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FilenameManagerTest {
    FilenameManager manager;

    @BeforeEach
    void before() {
        manager = new FilenameManager();
    }

    @Test
    @Tag("setPathToFile")
    void setPathToFile_ForEmptyString_ReturnsDefaultPath() {
        manager.setPathToFile("");
        String defaultPath = System.getProperty("user.dir") + File.separator + "audioFiles" + File.separator;
        assertEquals(defaultPath, manager.getPathToFile());
    }

    @Test
    @Tag("setPathToFile")
    void setPathToFile_ForStringWithoutTrailingSlash_ReturnsPathWithSlash() {
        manager.setPathToFile("some path");
        assertEquals("some path" + File.separator, manager.getPathToFile());
    }

    @Test
    @Tag("setPathToFile")
    void setPathToFile_ForStringWithTrailingSlash_ReturnsSamePathAsArgument() {
        String pathWithSlash = "path with file separator" + File.separator;
        manager.setPathToFile(pathWithSlash);
        assertEquals(pathWithSlash, manager.getPathToFile());
    }

    @Test
    @Tag("getDownloadPath")
    void getDownloadPath_BeforeInit_ThrowException() {
        assertThrows(IllegalStateException.class, () -> manager.getDownloadPath(false));
    }

    @Test
    @Tag("getDownloadPath")
    void getDownloadPath_AfterInit_ReturnPathOfDownloadFile() {
        manager.init("test.pdf");
        String expectedPath = System.getProperty("user.dir") + File.separator + "audioFiles" + File.separator + "test.mp3";
        assertEquals(expectedPath, manager.getDownloadPath(false));
    }

    @Test
    @Tag("getDownloadName")
    void getDownloadName_BeforeInit_ThrownException() {
        assertThrows(IllegalStateException.class, () -> manager.getDownloadName());
    }

    @Test
    @Tag("getDownloadName")
    void getDownloadName_AfterInit_ReturnNameOfDownloadFileWithID() {
        manager.init("test.paf");
        assertEquals(manager.getID() + ".test.mp3", manager.getDownloadName());
    }

    @Test
    @Tag("getUploadName")
    void getUploadName_BeforeInit_ThrowException() {
        assertThrows(IllegalStateException.class, () -> manager.getUploadName());
    }

    @Test
    @Tag("getUploadName")
    void getUploadName_AfterInti_ReturnNameOfUploadFileWithIDAndOption() {
        manager.init("test.pdf");
        String expectedName = manager.getID() + ".test.pdf" + manager.getOption();
        assertEquals(expectedName, manager.getUploadName());
    }

    @Test
    @Tag("init")
    void init_SetsUniqueToTrue() {
        manager.init("test");
        assertTrue(manager.isUnique());
    }

    @Test
    @Tag("init")
    void init_GetsFileBasename() {
        manager.init("test.pdf");
        assertEquals("test", manager.getFileBaseName());
    }

    @Test
    @Tag("init")
    void init_GetsFileExtension() {
        manager.init("test.pdf");
        assertEquals(".pdf", manager.getRawFileExtension());
    }

    @Test
    @Tag("timestampBaseName")
    void timestampBaseName_BeforeInit_ThrowException() {
        assertThrows(IllegalStateException.class, () -> manager.timestampBaseName());
    }

    @Test
    void timestampBaseName() {
        manager.init("test.pdf");
        manager.timestampBaseName();
        assertNotEquals("test.pdf", manager.getFileBaseName());
    }

    @Test
    @Tag("packageOpt")
    void packageOpt_packsOptionsTo25Characters() {
        manager.packageOpt(Language.ja_JP, Gender.MALE, "1.1", "0.0");
        assertEquals(25, manager.getOption().length());
    }

    @Test
    @Tag("packageOpt")
    void packageOpt_packsOptionsToCommaSeparatedString() {
        manager.packageOpt(Language.ja_JP, Gender.MALE, "1.1", "0.0");
        assertEquals("-----(ja_JP,MALE,1.1,0.0)", manager.getOption());
    }
}