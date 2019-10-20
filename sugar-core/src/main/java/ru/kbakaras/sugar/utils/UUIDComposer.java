package ru.kbakaras.sugar.utils;

import java.util.UUID;

/**
 * Класс позволяет получать с помощью композиции двух UUID третий UUID.
 * При создании в конструкторе необходимо задать настроечный UUID. Именно он
 * будет применяться к UUID, передаваемым методу {@link UUIDComposer#compose}
 * для их модификации.<br/>
 * Потребность в данном функционале встречается в задачах интеграции. Когда элемент
 * одной сущности при выгрузке в другую систему должен превратиться в два элемента двух
 * разных сущностей. При этом не хочется создавать два элемента (пусть и разных типов)
 * с одинаковым идентификатором. Но возможность вычислить второй идентификатор на основании
 * первого нужна.<br/><br/>
 * Фактически, над двумя идентификаторами выполняется операция XOR.
 */
public class UUIDComposer {
    private long mostSignificant;
    private long leastSignificant;

    public UUIDComposer(UUID xorUid) {
        if (xorUid.version() != 4) {
            throw new IllegalArgumentException("Only random UUIDs (type 4) supported as xor-UUIDs!");
        }

        if (xorUid.variant() != 2) {
            throw new IllegalArgumentException("Only IETF RFC 4122 UUIDs (variant 2) supported as xor-UUIDs.!");
        }

        mostSignificant  = xorUid.getMostSignificantBits()  ^ (4L << 12);
        leastSignificant = xorUid.getLeastSignificantBits() ^ (2L << 62);
    }

    public UUID compose(UUID uid) {
        return new UUID(
                uid.getMostSignificantBits() ^ mostSignificant,
                uid.getLeastSignificantBits() ^ leastSignificant);
    }
}
