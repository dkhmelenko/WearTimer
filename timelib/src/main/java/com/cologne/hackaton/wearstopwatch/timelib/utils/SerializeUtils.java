package com.cologne.hackaton.wearstopwatch.timelib.utils;

import com.cologne.hackaton.wearstopwatch.timelib.model.Lap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

/**
 * Provides serialization utilities
 *
 * @author Dmytro Khmelenko
 */
public final class SerializeUtils {

    // denied constructor
    private SerializeUtils() {
    }

    /**
     * Serializes ArrayList of Laps to byte array
     *
     * @param laps List of laps
     * @return Byte array
     */
    public static byte[] lapsToByteArray(ArrayList<Lap> laps) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        byte[] buff = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(laps);
            buff = byteArrayOutputStream.toByteArray();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buff;
    }

    /**
     * Deserializes byte array to the ArrayList of Laps
     *
     * @param bytes Byte array
     * @return List of laps
     */
    public static ArrayList<Lap> byteArrayToLaps(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = null;
        ArrayList<Lap> laps = new ArrayList<>();
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            laps = (ArrayList<Lap>) objectInputStream.readObject();
            objectInputStream.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return laps;
    }
}
