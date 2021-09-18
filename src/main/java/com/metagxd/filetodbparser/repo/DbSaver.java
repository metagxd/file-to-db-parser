package com.metagxd.filetodbparser.repo;

import java.util.List;

public interface DbSaver<T> {
    void saveToDb(List<T> objectList);
}
