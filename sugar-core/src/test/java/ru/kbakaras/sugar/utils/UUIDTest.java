package ru.kbakaras.sugar.utils;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Создано: kbakaras, в день: 16.02.2018.
 */
public class UUIDTest {
    @Test
    public void encodeTest() {
        final String str1c  = "0c0bef94-c5ec-11e6-820f-005056912b96";
        final String strSQL = "50000F82-9156-962B-11E6-C5EC0C0BEF94";

        byte[] order1 = new byte[] {
                (byte) 0x82,
                (byte) 0x0F,

                (byte) 0x00,
                (byte) 0x50,
                (byte) 0x56,
                (byte) 0x91,
                (byte) 0x2B,
                (byte) 0x96,

                (byte) 0x11,
                (byte) 0xE6,

                (byte) 0xC5,
                (byte) 0xEC,

                (byte) 0x0C,
                (byte) 0x0B,
                (byte) 0xEF,
                (byte) 0x94
        };

        String stringUtils = ArrayUtils.toHexStringWithDashes(order1);
        System.out.println(stringUtils + " - ArrayUtils");

        ByteBuffer buf = ByteBuffer.wrap(order1);
        UUID uuid = new UUID(buf.getLong(), buf.getLong());
        System.out.println(uuid.toString());
        System.out.println(uuid.variant());
        System.out.println("");

        uuid = UUID.fromString(str1c);
        System.out.println(uuid.toString());
        System.out.println(uuid.variant());
        System.out.println("");

        uuid.getMostSignificantBits();
        byte[] bytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE * 2)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits())
                .array();
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(Integer.toHexString(Byte.toUnsignedInt(bytes[i])) + " ");
        }
        System.out.println("");

        uuid.getMostSignificantBits();
        bytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE * 2)
                .putLong(uuid.getLeastSignificantBits())
                .putLong(uuid.getMostSignificantBits())
                .array();
        for (int i = 0; i < bytes.length; i++) {
            System.out.print(Integer.toHexString(Byte.toUnsignedInt(bytes[i])) + " ");
        }
        System.out.println("");
    }
}
