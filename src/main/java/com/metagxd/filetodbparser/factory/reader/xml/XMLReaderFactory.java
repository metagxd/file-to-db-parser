package com.metagxd.filetodbparser.factory.reader.xml;

import java.io.Closeable;
import java.io.InputStream;

public interface XMLReaderFactory<T> extends Closeable {
    T getReader(InputStream inputStream);
}
