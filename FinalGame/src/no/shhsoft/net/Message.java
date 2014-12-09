package no.shhsoft.net;

import java.net.*;
import java.io.*;

/**
 * Maintains a byte buffer that may be sent across the network as packets.
 * Contains methods for reading and writing various data types from/to the
 * buffer.  The buffer will automatically be expanded when necessary.
 * <P>
 * Please note that an instance of this class should be used <I>either</I> for
 * reading a message, <I>or</I> for writing a message.  If you mix the use of
 * put- and get-methods, the result is undefined (unless you use the
 * <CODE>clear</CODE> method).
 */
public final class Message {
/*-----------------------------------------------------------------------+
|  PRIVATE PART                                                          |
+-----------------------------------------------------------------------*/
    /** Number of reserved bytes at the start of the buffer. */
    private static final int NUM_RESERVED_BYTES = 3;

    /** An address, typically set to the source of the message. */
    private InetAddress addr;
    /** A port, typically set to the source of the message. */
    private int         port;

/*-----------------------------------------------------------------------+
|  INHERITANTS' INTERFACE                                                |
+-----------------------------------------------------------------------*/
    /** Number of bytes to add for each resizing of the buffer. */
    protected static final int BUFFER_CHUNK_SIZE = 64;

    /** The message contents buffer. */
    protected byte[] buff;

    /** Number of used bytes in the buffer. */
    protected int buffLen;

    /** Index of next byte when getting values from the buffer. */
    protected int idx;


    /**
     * Makes room for at least the given number of additional bytes.
     * Note that <CODE>buffLen</CODE> is not updated.
     * <P>
     *
     * Bytes are allocated in chunks to avoid having to resize the
     * array every time.  This method checks if there are enough preallocated
     * entries to match the number of new entries.  If there is, this
     * method does nothing.  If there is not, this method reallocates
     * the buffer.
     *
     * @param      numNew  the number of new bytes we want to make room
     *                     for.
     * @author     Sverre H. Huseby
     */
    protected final void ensureAvailable(int numNew) {
        int    q, newLen;
        byte[] tmp;

        if (buffLen + numNew <= buff.length)
            return;
        newLen = ((buffLen + numNew - 1)
                  / BUFFER_CHUNK_SIZE + 1) * BUFFER_CHUNK_SIZE;
        tmp = new byte[newLen];
        if (buff.length > 0)
            System.arraycopy(buff, 0, tmp, 0, buff.length);
        buff = tmp;
    }

/*-----------------------------------------------------------------------+
|  PUBLIC INTERFACE                                                      |
+-----------------------------------------------------------------------*/
    /**
     * Constructs a new message.
     *
     * @param      type    the type of the message.
     * @param      id      an id to associate with the message.
     * @author     Sverre H. Huseby
     */
    public Message(byte type, short id) {
        buff = new byte[BUFFER_CHUNK_SIZE];
        clear();
        setType(type);
        setId(id);
    }

    /**
     * Constructs a new message.
     *
     * @param      type    the type of the message.
     * @author     Sverre H. Huseby
     */
    public Message(byte type) {
        this(type, (short) -1);
    }

    /**
     * Constructs a new message.
     *
     * @author     Sverre H. Huseby
     */
    public Message() {
        this((byte) -1, (short) -1);
    }

    /**
     * Fetches a copy of the current buffer.  Please note that the returned
     * array is a <I>copy</I>.  You may change it without affecting the
     * state of the <CODE>Message</CODE>.
     * <P>
     * You will want to use this method when sending the message.
     *
     * @return     a copy of the byte buffer in this <CODE>Message</CODE>.
     * @author     Sverre H. Huseby
     */
    public byte[] getBuffer() {
        byte[] ret;

        ret = new byte[buffLen];
        System.arraycopy(buff, 0, ret, 0, buffLen);
        return ret;
    }

    /**
     * Sets the contents of the message byte buffer.  Discards whatever
     * was in the buffer.  The provided byte array is copied into the
     * <CODE>Message</CODE>, so you're free to change the array after
     * calling this method, without disturbing the state of the message.
     * <P>
     * You will want to use this method when you have received a message
     * from the network.
     *
     * @param      b       the byte buffer to copy into the
     *                     <CODE>Message</CODE>.
     * @param      n       the number of bytes to copy from the array.
     * @author     Sverre H. Huseby
     */
    public void setBuffer(byte[] b, int n) {
        buff = new byte[BUFFER_CHUNK_SIZE];
        ensureAvailable(n);
        System.arraycopy(b, 0, buff, 0, n);
        buffLen = n;
        idx = NUM_RESERVED_BYTES;
    }

    /**
     * Sets the contents of the message byte buffer.  Discards whatever
     * was in the buffer.  The provided byte array is copied into the
     * <CODE>Message</CODE>, so you're free to change the array after
     * calling this method, without disturbing the state of the message.
     * <P>
     * You will want to use this method when you have received a message
     * from the network.
     *
     * @param      b       the byte buffer to copy into the
     *                     <CODE>Message</CODE>.
     * @author     Sverre H. Huseby
     */
    public void setBuffer(byte[] b) {
        setBuffer(b, b.length);
    }

    /**
     * Resets this <CODE>Message</CODE>.  In effect this is equivalent
     * to making a new <CODE>Message</CODE> using the null constructor,
     * but saves the object creation overhead.
     *
     * @author     Sverre H. Huseby
     */
    public void clear() {
        addr = null;
        port = -1;
        setType((byte) -1);
        setId((short) -1);
        buffLen = NUM_RESERVED_BYTES;
        idx = NUM_RESERVED_BYTES;
    }

    /**
     * Resets the "read head" to the start of the <CODE>Message</CODE>.
     *
     * @author     Sverre H. Huseby
     */
    public void rewind() {
        idx = NUM_RESERVED_BYTES;
    }

    /**
     * Sets the address associated with this <CODE>Message</CODE>.
     * You will probably want to call this method after reading the
     * message from the network.  Note that setting the address
     * is not required, unless you want to call the
     * <CODE>getAddress</CODE> method later.
     *
     * @param      addr    the address.
     * @author     Sverre H. Huseby
     */
    public void setAddress(InetAddress addr) {
        this.addr = addr;
    }

    /**
     * Fetches the address associated with this <CODE>Message</CODE>.
     * Calling this method is only meaningful if <CODE>setAddress</CODE>
     * was called earlier, probably when the message was received from
     * the network.
     *
     * @return     the address, or <CODE>null</CODE> if no address was set.
     * @author     Sverre H. Huseby
     */
    public InetAddress getAddress() {
        return addr;
    }

    /**
     * Sets the port associated with this <CODE>Message</CODE>.
     * You will probably want to call this method after reading the
     * message from the network.  Note that setting the port
     * is not required, unless you want to call the
     * <CODE>getPort</CODE> method later.
     *
     * @param      port    the port.
     * @author     Sverre H. Huseby
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Fetches the port associated with this <CODE>Message</CODE>.
     * Calling this method is only meaningful if <CODE>setPort</CODE>
     * was called earlier, probably when the message was received from
     * the network.
     *
     * @return     the port, or <CODE>-1</CODE> if no port was set.
     * @author     Sverre H. Huseby
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the type of this <CODE>Message</CODE>.  The type is a user
     * defined <CODE>byte</CODE>, that need not be set.
     *
     * @param      type    the type.
     * @author     Sverre H. Huseby
     */
    public void setType(byte type) {
        buff[0] = type;
    }

    /**
     * Fetches the type of this <CODE>Message</CODE>.
     *
     * @return     the type.
     * @author     Sverre H. Huseby
     */
    public byte getType() {
        return buff[0];
    }

    /**
     * Sets the identification of this <CODE>Message</CODE>.  The
     * identification is a user defined <CODE>short</CODE>, that need
     * not be set.
     *
     * @param      id      the identification.
     * @author     Sverre H. Huseby
     */
    public void setId(short id) {
        buff[1] = (byte) ((id >> 8) & 0xFF);
        buff[2] = (byte) (id & 0xFF);
    }

    /**
     * Fetches the identification of this <CODE>Message</CODE>.
     *
     * @return     the identification.
     * @author     Sverre H. Huseby
     */
    public short getId() {
        return (short) ((buff[1] << 8) | (buff[2] & 0xFF));
    }

    /**
     * Adds a sequence of bytes to the byte buffer.
     *
     * @param      b       the bytes to add.
     * @param      n       the number of bytes to add.
     * @author     Sverre H. Huseby
     */
    public void putBytes(byte[] b, int n) {
        ensureAvailable(n);
        System.arraycopy(b, 0, buff, buffLen, n);
        buffLen += n;
    }

    /**
     * Adds a sequence of bytes to the byte buffer.
     *
     * @param      b       the bytes to add.
     * @author     Sverre H. Huseby
     */
    public void putBytes(byte[] b) {
        putBytes(b, b.length);
    }

    /**
     * Fetches a sequence of bytes from the byte buffer.
     *
     * @param      n       number of bytes to fetch.
     * @return     an array containing the bytes.
     * @author     Sverre H. Huseby
     */
    public byte[] getBytes(int n) {
        byte[] ret;

        ret = new byte[n];
        System.arraycopy(buff, idx, ret, 0, n);
        idx += n;
        return ret;
    }

    /**
     * Adds a byte to the byte buffer.
     *
     * @param      b       the <CODE>byte</CODE> to add.
     * @author     Sverre H. Huseby
     */
    public void putByte(byte b) {
        ensureAvailable(1);
        buff[buffLen++] = b;
    }

    public void putByte(short b) {
        putByte((byte) b);
    }

    public void putByte(int b) {
        putByte((byte) b);
    }

    /**
     * Fetches a byte from the byte buffer.
     *
     * @return     the next <CODE>byte</CODE> from the buffer.
     * @author     Sverre H. Huseby
     */
    public byte getByte() {
        return buff[idx++];
    }

    /**
     * Adds a boolean to the byte buffer.
     *
     * @param      b       the <CODE>boolean</CODE> to add.
     * @author     Sverre H. Huseby
     */
    public void putBoolean(boolean b) {
        ensureAvailable(1);
        buff[buffLen++] = (b ? (byte) 1 : (byte) 0);
    }

    /**
     * Fetches a boolean from the byte buffer.
     *
     * @return     the next <CODE>boolean</CODE> from the buffer.
     * @author     Sverre H. Huseby
     */
    public boolean getBoolean() {
        return (buff[idx++] != 0);
    }

    /**
     * Adds a short (16 bit) integer to the byte buffer.
     *
     * @param      s       the <CODE>short</CODE> to add.
     * @author     Sverre H. Huseby
     */
    public void putShort(short s) {
        ensureAvailable(2);
        buff[buffLen++] = (byte) ((s >> 8) & 0xFF);
        buff[buffLen++] = (byte) (s & 0xFF);
    }

    public void putShort(int s) {
        putShort((short) s);
    }

    /**
     * Fetches a short (16 bit) integer from the byte buffer.
     *
     * @return     the next <CODE>short</CODE> from the buffer.
     * @author     Sverre H. Huseby
     */
    public short getShort() {
        short ret;

        ret = (short) (buff[idx++] << 8);
        ret |= buff[idx++] & 0xFF;
        return ret;
    }

    /**
     * Adds a 32 bit integer to the byte buffer.
     *
     * @param      i       the <CODE>int</CODE> to add.
     * @author     Sverre H. Huseby
     */
    public void putInt(int i) {
        ensureAvailable(4);
        buff[buffLen++] = (byte) ((i >> 24) & 0xFF);
        buff[buffLen++] = (byte) ((i >> 16) & 0xFF);
        buff[buffLen++] = (byte) ((i >> 8) & 0xFF);
        buff[buffLen++] = (byte) (i & 0xFF);
    }

    /**
     * Fetches a 32 bit integer from the byte buffer.
     *
     * @return     the next <CODE>int</CODE> from the buffer.
     * @author     Sverre H. Huseby
     */
    public int getInt() {
        int ret;

        ret = (int) (buff[idx++] << 24);
        ret |= (int) ((buff[idx++] & 0xFF) << 16);
        ret |= (int) ((buff[idx++] & 0xFF) << 8);
        ret |= buff[idx++] & 0xFF;
        return ret;
    }

    /**
     * Adds a string to the byte buffer.  The string is coded using UTF.
     *
     * @param      s       the <CODE>String</CODE> to add.
     * @author     Sverre H. Huseby
     */
    public void putString(String s) {
        ByteArrayOutputStream byteStream;
        DataOutputStream      stream;
        byte[]                bytes;

        /* if you know a simpler way to convert a String to UTF,
         * please enlighten me.  this just sucks when it comes to
         * speed. */
        byteStream = new ByteArrayOutputStream(s.length());
        stream = new DataOutputStream(byteStream);
        try {
            stream.writeUTF(s);
            stream.close();
        } catch (IOException e) {
            /* if this happens, I no longer believe in Java. */
        }
        bytes = byteStream.toByteArray();
        putShort((short) bytes.length);
        putBytes(bytes);
    }

    /**
     * Fetches an UTF encoded string from the byte buffer.
     *
     * @return     the next <CODE>String</CODE> from the buffer.
     * @author     Sverre H. Huseby
     */
    public String getString() {
        int                  n;
        ByteArrayInputStream byteStream;
        DataInputStream      stream;
        String               ret = null;

        n = getShort();
        byteStream = new ByteArrayInputStream(buff, idx, n);
        stream = new DataInputStream(byteStream);
        try {
            ret = stream.readUTF();
            stream.close();
        } catch (IOException e) {
            /* plonk! */
        }
        idx += n;
        return ret;
    }



    /*** Testing ***/
//      public static void main(String[] args) {
//          Message m;
//          int     q, w;
//          String  s1, s2;
//          char[]  c = new char[1];

//          m = new Message();
//          for (q = -40000; q <= 40000; q++) {
//              m.putInt(q);
//              w = m.getInt();
//              if (q != w)
//                  System.err.println(q + " != " + w);
//          }
//      }
}
