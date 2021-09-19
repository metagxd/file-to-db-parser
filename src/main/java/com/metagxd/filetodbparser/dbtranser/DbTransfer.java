package com.metagxd.filetodbparser.dbtranser;

public interface DbTransfer {
    void transferToDb(String file, String elementName, String... params);
}
