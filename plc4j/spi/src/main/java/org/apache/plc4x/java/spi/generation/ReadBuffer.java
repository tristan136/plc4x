/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.plc4x.java.spi.generation;

import com.github.jinahya.bit.io.ArrayByteInput;
import org.apache.plc4x.java.api.exceptions.PlcRuntimeException;
import org.apache.plc4x.java.spi.generation.io.MyDefaultBitInput;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadBuffer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadBuffer.class);

    private final MyDefaultBitInput bi;
    private final boolean littleEndian;
    private final long totalBytes;

    public ReadBuffer(byte[] input) {
        this(input, false);
    }

    public ReadBuffer(byte[] input, boolean littleEndian) {
        LOGGER.info("Creating read buffer " + input.length)
        ArrayByteInput abi = new ArrayByteInput(input);
        this.bi = new MyDefaultBitInput(abi);
        this.littleEndian = littleEndian;
        this.totalBytes = input.length;
    }

    public int getPos() {
        return (int) bi.getPos();
    }

    public byte[] getBytes(int startPos, int endPos) {
        int numBytes = endPos - startPos;
        byte[] data = new byte[numBytes];
        System.arraycopy(bi.getDelegate().getSource(), startPos, data, 0, numBytes);
        return data;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public boolean hasMore(int numBits) {
        return (numBits / 8) <= (totalBytes - getPos());
    }

    public byte peekByte(int offset) throws ParseException {
        // Remember the old index.
        int oldIndex = bi.getDelegate().getIndex();
        try {
            // Set the delegate to the desired position.
            bi.getDelegate().index(oldIndex + offset);
            // Read the byte.
            return bi.readByte(false, 8);
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        } finally {
            // Reset the delegate to the old index.
            bi.getDelegate().index(oldIndex);
        }
    }

    public boolean readBit() throws ParseException {
        try {
            boolean ret = bi.readBoolean();
            LOGGER.info("Reading Bit:- " + ret);
            return ret;
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public byte readUnsignedByte(int bitLength) throws ParseException {
        if (bitLength <= 0) {
            throw new ParseException("unsigned byte must contain at least 1 bit");
        }
        if (bitLength > 7) {
            throw new ParseException("unsigned byte can only contain max 4 bits");
        }
        try {
            byte ret = bi.readByte(true, bitLength);
            LOGGER.info("Reading Unsigned Byte:- " + ret);
            return ret;
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public short readUnsignedShort(int bitLength) throws ParseException {
        if (bitLength <= 0) {
            throw new ParseException("unsigned short must contain at least 1 bit");
        }
        if (bitLength > 15) {
            throw new ParseException("unsigned short can only contain max 8 bits");
        }
        try {
            // No need to flip here as we're only reading one byte.
            short ret =  bi.readShort(true, bitLength);
            LOGGER.info("Reading Unsigned Short:- " + ret);
            return ret;
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public int readUnsignedInt(int bitLength) throws ParseException {
        if (bitLength <= 0) {
            throw new ParseException("unsigned int must contain at least 1 bit");
        }
        if (bitLength > 31) {
            throw new ParseException("unsigned int can only contain max 16 bits");
        }
        try {
            if (littleEndian) {
                int intValue = bi.readInt(true, bitLength);
                int ret =  Integer.reverseBytes(intValue) >>> 16;
                LOGGER.info("Reading Unsigned Int:- " + ret);
                return ret;
            }
            int ret =  bi.readInt(true, bitLength);
            LOGGER.info("Reading Unsigned Int:- " + ret);
            return ret;
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public long readUnsignedLong(int bitLength) throws ParseException {
        if (bitLength <= 0) {
            throw new ParseException("unsigned long must contain at least 1 bit");
        }
        if (bitLength > 63) {
            throw new ParseException("unsigned long can only contain max 32 bits");
        }
        try {
            if (littleEndian) {
                final long longValue = bi.readLong(true, bitLength);
                long ret = Long.reverseBytes(longValue) >>> 32;
                LOGGER.info("Reading Unsigned Long:- " + ret);
                return ret;
            }
            long ret =  bi.readLong(true, bitLength);
            LOGGER.info("Reading Unsigned Long:- " + ret);
            return ret;
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public BigInteger readUnsignedBigInteger(int bitLength) throws ParseException {
        //Support specific case where value less than 64 bits and big endian.
        if (bitLength <= 0) {
            throw new ParseException("unsigned long must contain at least 1 bit");
        }
        if (bitLength > 64) {
            throw new ParseException("unsigned long can only contain max 64 bits");
        }
        try {
            // Read as signed value
            Long val = bi.readLong(false, bitLength);
            if (littleEndian) {
                val = Long.reverseBytes(val);
            }
            if (val >= 0) {
                return BigInteger.valueOf(val);
            } else {
                BigInteger constant = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(2)).add(BigInteger.valueOf(2));
                return BigInteger.valueOf(val).add(constant);
            }
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public byte readByte(int bitLength) throws ParseException {
        if (bitLength <= 0) {
            throw new ParseException("byte must contain at least 1 bit");
        }
        if (bitLength > 8) {
            throw new ParseException("byte can only contain max 8 bits");
        }
        try {
            byte ret = bi.readByte(false, bitLength);
            LOGGER.info("Reading Byte:- " + ret);
            return ret;
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public short readShort(int bitLength) throws ParseException {
        if (bitLength <= 0) {
            throw new ParseException("short must contain at least 1 bit");
        }
        if (bitLength > 16) {
            throw new ParseException("short can only contain max 16 bits");
        }
        try {
            if (littleEndian) {
                short ret =  Short.reverseBytes(bi.readShort(false, bitLength));
                LOGGER.info("Reading Short:- " + ret);
                return ret;
            }
            short ret = bi.readShort(false, bitLength);
            LOGGER.info("Reading Short:- " + ret);
            return ret;
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public int readInt(int bitLength) throws ParseException {
        if (bitLength <= 0) {
            throw new ParseException("int must contain at least 1 bit");
        }
        if (bitLength > 32) {
            throw new ParseException("int can only contain max 32 bits");
        }
        try {
            if (littleEndian) {
                int ret =  Integer.reverseBytes(bi.readInt(false, bitLength));
                LOGGER.info("Reading Integer:- " + ret);
                return ret;
            }
            int ret = bi.readInt(false, bitLength);
            LOGGER.info("Reading Int:- " + ret);
            return ret;
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public long readLong(int bitLength) throws ParseException {
        if (bitLength <= 0) {
            throw new ParseException("long must contain at least 1 bit");
        }
        if (bitLength > 64) {
            throw new ParseException("long can only contain max 64 bits");
        }
        try {
            if (littleEndian) {
                long ret = Long.reverseBytes(bi.readLong(false, bitLength));
                LOGGER.info("Reading Read Long:- " + ret);
                return ret;
            }
            long ret = bi.readLong(false, bitLength);
            LOGGER.info("Reading Read Long:- " + ret);
            return ret;
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public BigInteger readBigInteger(int bitLength) throws ParseException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public float readFloat(int bitLength) throws ParseException {
        try {
            if (bitLength == 16) {
                // https://en.wikipedia.org/wiki/Half-precision_floating-point_format
                final boolean sign = bi.readBoolean();
                final byte exponent = bi.readByte(true, 5);
                final short fraction = bi.readShort(true, 10);
                if ((exponent >= 1) && (exponent <= 30)) {
                    return (sign ? 1 : -1) * (2 ^ (exponent - 15)) * (1 + (fraction / 10f));
                } else if (exponent == 0) {
                    if (fraction == 0) {
                        return 0.0f;
                    } else {
                        return (sign ? 1 : -1) * (2 ^ (-14)) * (fraction / 10f);
                    }
                } else if (exponent == 31) {
                    if (fraction == 0) {
                        return sign ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
                    } else {
                        return Float.NaN;
                    }
                } else {
                    throw new NumberFormatException();
                }
            } else if (bitLength == 32) {
                int intValue = readInt(32);
                return Float.intBitsToFloat(intValue);
            } else {
                throw new UnsupportedOperationException("unsupported bit length (only 16 and 32 supported)");
            }
        } catch (IOException e) {
            throw new ParseException("Error reading", e);
        }
    }

    public double readDouble(int bitLength) throws ParseException {
        if(bitLength == 64) {
            long longValue = readLong(64);
            double ret = Double.longBitsToDouble(longValue);
            LOGGER.info("Reading Double:- " + ret);
            return ret;
        } else {
            throw new UnsupportedOperationException("unsupported bit length (only 64 supported)");
        }
    }

    public BigDecimal readBigDecimal(int bitLength) throws ParseException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public String readString(int bitLength, String encoding) {
        byte[] strBytes = new byte[bitLength / 8];
        for (int i = 0; (i < (bitLength / 8)) && hasMore(8); i++) {
            try {
                strBytes[i] = readByte(8);
            } catch (Exception e) {
                throw new PlcRuntimeException(e);
            }
        }
        //replaceAll function removes and leading ' char or hypens.
        String ret =  new String(strBytes, Charset.forName(encoding.replaceAll("[^a-zA-Z0-9]","")));
        LOGGER.info("Reading String:- " + ret);
        return ret;
    }

}
