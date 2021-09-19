package com.metagxd.filetodbparser.factory.reader.xml;

import java.io.InputStream;

public interface XMLReaderFactory<T> {
    T getReader(InputStream inputStream) throws Exception;
}
