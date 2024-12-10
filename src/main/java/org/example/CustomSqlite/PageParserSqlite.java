package org.example.CustomSqlite;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class PageParserSqlite {

    private final int pageSize;

    PageParserSqlite(int pageSize) {
        this.pageSize = pageSize;
    }

    byte[] readPage(byte[] fileBytes, int pageIndex) {
        byte[] p = new byte[pageSize];
        System.arraycopy(fileBytes, pageIndex * pageSize, p, 0, pageSize);
        return p;
    }

    List<Integer> getCellOffsets(byte[] page, int offset) {
        int cellCount = ((page[offset+3] & 0xFF) << 8) | (page[offset+4] & 0xFF);
        List<Integer> offs = new ArrayList<>();
        for (int i = 0; i < cellCount; i++) {
            int co = ((page[offset+8 + i*2] & 0xFF) << 8) | (page[offset+8 + i*2 + 1] & 0xFF);
            offs.add(co);
        }
        return offs;
    }

    Record parseTableLeafCell(byte[] page, int cellOffset, boolean isSqliteMaster) throws IOException {
        VarintResult payloadSizeVar = readVarint(page, cellOffset);
        long payloadSize = payloadSizeVar.value;
        int pos = cellOffset + payloadSizeVar.length;

        VarintResult rowidVar = readVarint(page, pos);
        pos += rowidVar.length;

        byte[] payload = new byte[(int)payloadSize];
        System.arraycopy(page, pos, payload, 0, (int)payloadSize);
        return parseRecord(payload, rowidVar.value, isSqliteMaster);
    }

    private Record parseRecord(byte[] payload, long rowid, boolean isSqliteMaster) throws IOException {
        VarintResult hv = readVarint(payload, 0);
        int headerSize = (int) hv.value;
        int p = hv.length;

        List<Long> sts = new ArrayList<>();
        while (p < headerSize) {
            VarintResult stv = readVarint(payload, p);
            sts.add(stv.value);
            p += stv.length;
        }

        int dataPos = headerSize;
        List<Object> vals = new ArrayList<>();
        int limit = isSqliteMaster ? Math.min(sts.size(), 4) : sts.size();
        for (int i = 0; i < limit; i++) {
            long st = sts.get(i);
            Object val = readValue(payload, st, dataPos);
            dataPos += serialTypeLength(st, payload, dataPos);
            vals.add(val);
        }
        return new Record(rowid, vals);
    }

    private Object readValue(byte[] data, long st, int pos) throws IOException {
        if (st == 0) return null;
        if (st == 1) return (long) data[pos];
        if (st == 2) return (long) (((data[pos]&0xFF)<<8)|(data[pos+1]&0xFF));
        if (st == 3) return ((data[pos]&0xFFL)<<16)|((data[pos+1]&0xFFL)<<8)|(data[pos+2]&0xFFL);
        if (st == 4) return (long)(((data[pos]&0xFF)<<24)|((data[pos+1]&0xFF)<<16)|((data[pos+2]&0xFF)<<8)|(data[pos+3]&0xFF));
        if (st == 8) return 0L;
        if (st == 9) return 1L;
        if (st >= 13) {
            boolean text = (st % 2 == 1);
            int length = (int)((st - (text ? 13 : 12))/2);
            byte[] arr = new byte[length];
            System.arraycopy(data, pos, arr, 0, length);
            return text ? new String(arr, StandardCharsets.UTF_8) : arr;
        }
        throw new IOException("Неизвестный тип: " + st);
    }

    private int serialTypeLength(long st, byte[] data, int pos) {
        if (st == 0) return 0;
        if (st == 1) return 1;
        if (st == 2) return 2;
        if (st == 3) return 3;
        if (st == 4) return 4;
        if (st == 8 || st == 9) return 0;
        if (st >= 13) {
            boolean text = (st % 2 == 1);
            return (int)((st - (text ? 13 : 12))/2);
        }
        return 0;
    }

    private VarintResult readVarint(byte[] data, int pos) {
        long val = 0;
        int shift = 0, length = 0;
        for (int i = 0; i < 9 && pos+i < data.length; i++) {
            int b = data[pos+i] & 0xFF;
            length++;
            if (b < 0x80) { val |= ((long)b << shift); break; }
            else { val |= ((long)(b & 0x7F) << shift); shift += 7; }
        }
        return new VarintResult(val, length);
    }

    static class VarintResult {
        long value; int length;
        VarintResult(long v, int l) { value=v; length=l; }
    }

    static class Record {
        long rowid; List<Object> values;
        Record(long rid, List<Object> vals) { rowid=rid; values=vals; }
    }
}