package org.solhost.folko.dasm.decoder;

public interface DecodeListener {
    public void onDecode(long virtualAddress, int length, DecodedEntity entity);
}
