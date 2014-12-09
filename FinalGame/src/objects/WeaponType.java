package objects;

/**
 * This class holds constants for the various weapon types, and the
 * mapping to textual names.
 */
public final class WeaponType {
/*-----------------------------------------------------------------------+
 |  PUBLIC INTERFACE                                                     |
 +----------------------------------------------------------------------*/
    /** No weapon. */
    public static final byte NONE   = 0;
    /** The phaser */
    public static final byte PHASER = 1;
    /** The targeting bomb */
    public static final byte BOMB   = 2;

    /**
     * Gets a textual name given a weapon constant.
     *
     * @param      weapon  the weapon constant.
     * @return     a string.
     */
    public static final String getName(byte weapon) {
        switch (weapon) {
          case NONE:
            return new String("magic");
          case PHASER:
            return new String("phaser");
          case BOMB:
            return new String("bomb");
        }
        return null;
    }
}
