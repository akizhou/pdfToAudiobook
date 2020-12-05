package com.cloudAPI;

import com.enums.Gender;
import com.enums.Language;
import com.exceptions.CredentialsKeyError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.naming.TimeLimitExceededException;

import java.io.File;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GoogleCloudAPITest {
    GoogleCloudAPI myAPI;

    @BeforeEach
    void before() {
        String keyPath = System.getProperty("user.dir")+ File.separator + "resources" + File.separator + "key.json";
        myAPI = new GoogleCloudAPI(keyPath);
    }

    @Test
    void GoogleCloudAPI_ForNoKeyGiven_ThrowError() {
        assertThrows(CredentialsKeyError.class, () -> new GoogleCloudAPI());
    }

    final double MIN_SPEED = 0.25;
    final double MAX_SPEED = 4;
    @Test
    @Tag("reflectOptions")
    void reflectOptions_ForSpeedLessThanMIN_SPEED_ThrowException() {
        Random rand = new Random();
        double speed = MIN_SPEED - rand.nextDouble();
        assertThrows(IllegalArgumentException.class, () -> myAPI.reflectOptions(Language.en_US,
                Gender.FEMALE, speed, 0));
    }

    @Test
    @Tag("reflectOptions")
    void reflectOptions_ForSpeedMoreThanMAX_SPEED_ThrowException() {
        Random rand = new Random();
        double speed = MAX_SPEED + rand.nextDouble();
        assertThrows(IllegalArgumentException.class, () -> myAPI.reflectOptions(Language.en_US,
                Gender.FEMALE, speed, 0));
    }

    final double MIN_PITCH = -20;
    final double MAX_PITCH = 20;
    @Test
    @Tag("reflectOptions")
    void reflectOptions_ForSpeedLessThanMIN_PITCH_ThrowException() {
        Random rand = new Random();
        double pitch = MIN_PITCH - rand.nextDouble();
        assertThrows(IllegalArgumentException.class, () -> myAPI.reflectOptions(Language.en_US,
                Gender.FEMALE, 1, pitch));
    }

    @Test
    @Tag("reflectOptions")
    void reflectOptions_ForSpeedMoreThanMAX_PITCH_ThrowException() {
        Random rand = new Random();
        double pitch = MAX_PITCH + rand.nextDouble();
        assertThrows(IllegalArgumentException.class, () -> myAPI.reflectOptions(Language.en_US,
                Gender.FEMALE, 1, pitch));
    }
}