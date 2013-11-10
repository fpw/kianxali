package org.solhost.folko.dasm.decoder;

public interface DecodeListener {
    public void onDecode(long offset, int length, DecodedEntity entity);
}
