/*
 * This file is part of RedstoneLamp.
 *
 * RedstoneLamp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RedstoneLamp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RedstoneLamp.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.redstonelamp.nio;


import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * An NIO buffer class to wrap around a java.nio.ByteBuffer.
 * <br>
 * This buffer is dynamic, as it changes size when the allocated amount is too small.
 *
 * @author RedstoneLamp Team
 */
public class BinaryBuffer{
    private ByteBuffer bb;

    protected BinaryBuffer(ByteBuffer bb){
        this.bb = bb;
    }

    /**
     * Create a new DynamicByteBuffer wrapped around a byte array with the specified <code>order</code>
     *
     * @param bytes The byte array to be wrapped around
     * @param order The ByteOrder of the buffer, Big Endian or Little Endian.
     * @return A new DynamicByteBuffer class, at position zero wrapped around the byte array in the specified <code>order</code>
     */
    public static BinaryBuffer wrapBytes(byte[] bytes, ByteOrder order){
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(order);
        bb.position(0);
        return new BinaryBuffer(bb);
    }

    /**
     * Create a new DynamicByteBuffer with the specified <code>initalSize</code> and <code>order</code>
     * <br>
     * The Buffer will grow if an attempt is to put more data than the <code>initalSize</code>
     *
     * @param initalSize The inital size of the buffer
     * @param order      The ByteOrder of the buffer, Big Endian or Little Endian
     * @return A new DynamicByteBuffer class, at position zero with the specified <code>order</code> and <code>initalSize</code>
     */
    public static BinaryBuffer newInstance(int initalSize, ByteOrder order){
        ByteBuffer bb = ByteBuffer.allocate(initalSize);
        bb.order(order);
        bb.position(0);
        return new BinaryBuffer(bb);
    }

    /**
     * Get <code>len</code> of bytes from the buffer.
     *
     * @param len The length of bytes to get from the buffer
     * @return A byte array of <code>len</code> bytes
     * @throws java.nio.BufferUnderflowException If there is not enough bytes in the buffer to read
     */
    public byte[] get(int len){
        byte[] b = new byte[len];
        bb.get(b);
        return b;
    }

    /**
     * Put an amount of bytes into the buffer. The buffer will resize to fit the bytes if the buffer is too small.
     *
     * @param bytes The byte array to be put into the buffer
     */
    public void put(byte[] bytes){
        try{
            bb.put(bytes);
        }catch(BufferOverflowException e){
            setPosition(0);
            byte[] all = get(remaining());
            bb = ByteBuffer.allocate(all.length + bytes.length);
            bb.put(all);
            bb.put(bytes);
        }
    }

    /**
     * Get a single signed byte from the buffer
     *
     * @return A single unsigned byte
     */
    public byte getByte(){
        return bb.get();
    }

    /**
     * Get a single signed boolean from the buffer (one byte)
     *
     * @return A single boolean
     */
    public boolean getBoolean(){
        return bb.get() > 0;
    }

    /**
     * Get a single unsigned byte from the buffer
     *
     * @return A single unsigned byte
     */
    public short getUnsignedByte(){
        return (short) (bb.get() & 0xFF);
    }

    /**
     * Get a single signed short (2 bytes) from the buffer
     *
     * @return A single signed short
     */
    public short getShort(){
        return bb.getShort();
    }

    /**
     * Get a single unsigned short (2 bytes) from the buffer
     *
     * @return A single unsigned short
     */
    public int getUnsignedShort(){
        return bb.getShort() & 0xFFFF;
    }

    /**
     * Get a single signed integer (4 bytes) from the buffer
     *
     * @return A single signed integer
     */
    public int getInt(){
        return bb.getInt();
    }

    /**
     * Get a single singed long (8 bytes) from the buffer
     *
     * @return A single signed long
     */
    public long getLong(){
        return bb.getLong();
    }

    public float getFloat(){
        return bb.getFloat();
    }

    public double getDouble(){
        return bb.getDouble();
    }

    /**
     * Gets a Google Protocol Buffers VarInt from the buffer.
     * Code is from: https://gist.github.com/thinkofdeath/e975ddee04e9c87faf22
     *
     * @return The VarInt, as an integer.
     */
    public int getVarInt(){
        int i = 0;
        int j = 0;
        while(true){
            int k = getByte();

            i |= (k & 0x7F) << j++ * 7;

            if(j > 5) throw new RuntimeException("VarInt too big");

            if((k & 0x80) != 128) break;
        }
        return i;
    }

    /**
     * Get a single short prefixed string from the buffer (2 + str bytes)
     *
     * @return A single short prefixed string
     */
    public String getString(){
        return new String(get(getUnsignedShort()));
    }

    /**
     * Get a single varint prefixed string from the buffer (varint bytes + str bytes)
     *
     * @return A single varint prefixed string
     */
    public String getVarString(){
        return new String(get(getVarInt()));
    }

    public UUID getUUID(){
        return new UUID(bb.getLong(), bb.getLong());
    }

    public void putByte(byte b){
        put(new byte[]{b});
    }

    public void putBoolean(boolean b){
        put(new byte[]{(byte) (b ? 1 : 0)});
    }

    public void putShort(short s){
        put(ByteBuffer.allocate(2).order(getOrder()).putShort(s).array());
    }

    public void putInt(int i){
        put(ByteBuffer.allocate(4).order(getOrder()).putInt(i).array());
    }

    public void putLong(long l){
        put(ByteBuffer.allocate(8).order(getOrder()).putLong(l).array());
    }

    public void putFloat(float f){
        put(ByteBuffer.allocate(4).order(getOrder()).putFloat(f).array());
    }

    public void putDouble(double d){
        put(ByteBuffer.allocate(8).order(getOrder()).putDouble(d).array());
    }

    public void putString(String s){
        putShort((short) s.getBytes().length);
        put(s.getBytes());
    }

    public void putVarString(String s){
        putVarInt(s.getBytes().length);
        put(s.getBytes());
    }

    public void putUUID(UUID uuid){
        putLong(uuid.getMostSignificantBits());
        putLong(uuid.getLeastSignificantBits());
    }

    /**
     * Puts a Google Protocol Buffers VarInt into the buffer
     * Code is from: https://gist.github.com/thinkofdeath/e975ddee04e9c87faf22
     *
     * @param i The VarInt as an Integer.
     */
    public void putVarInt(int i){
        while(true){
            if((i & 0xFFFFFF80) == 0){
                putByte((byte) i);
                return;
            }

            putByte((byte) (i & 0x7F | 0x80));
            i >>>= 7;
        }
    }

    /**
     * Get a single line string containing each byte of the buffer in hexadecimal
     *
     * @return A String containing each byte of the buffer in hexadecimal with no newlines.
     */
    public String singleLineHexDump(){
        StringBuilder sb = new StringBuilder();
        byte[] data = bb.array();
        for(byte b : data){
            sb.append(String.format("%02X", b)).append(" ");
        }
        return sb.toString();
    }

    /**
     * Get the ByteOrder of the underlying ByteBuffer
     *
     * @return The ByteOrder of the ByteBuffer
     */
    public ByteOrder getOrder(){
        return bb.order();
    }

    /**
     * Set the position of the underlying ByteBuffer
     *
     * @param position The position in the buffer to be set to
     */
    public void setPosition(int position){
        bb.position(position);
    }

    /**
     * Get the position of the underyling ByteBuffer
     *
     * @return The position in the buffer
     */
    public int getPosition(){
        return bb.position();
    }

    /**
     * Get the amount of bytes remaining in the buffer
     *
     * @return The amount of remaining bytes in the buffer
     */
    public int remaining(){
        return bb.remaining();
    }

    /**
     * Get the remaining bytes in the buffer.
     * <br>
     * NOTE: This DOES increase the position in the buffer.
     *
     * @return The reamaining bytes in the buffer.
     */
    public byte[] remainingBytes(){
        return get(remaining());
    }

    /**
     * Get a byte array of the buffer
     *
     * @return A byte array containing all the bytes in the buffer
     */
    public byte[] toArray(){
        return bb.array();
    }

    /**
     * Skip <code>bytes</code> amount of bytes in the buffer (equivalent to setPosition(getPosition() + len))
     *
     * @param bytes The amount of bytes to skip in the buffer
     */
    public void skip(int bytes){
        setPosition(getPosition() + bytes);
    }
}
