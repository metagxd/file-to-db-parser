package com.metagxd.filetodbparser.db.creator.query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class InsertQueryCreatorImpl implements QueryCreator {
    private static final Logger logger = LoggerFactory.getLogger(InsertQueryCreatorImpl.class);

    @Override
    public String getQuery(String tableName, String... columnNames) {
        logger.debug("Creating query for table {}, {}", tableName, columnNames);
        var sql = new StringBuilder();
        var values = new StringBuilder("(");
        sql.append("INSERT INTO ")
                .append(tableName)
                .append(" (");
        for (int i = 0; i < columnNames.length - 1; i++) {
            sql.append(columnNames[i]).append(',');
            values.append("?,");
        }
        //last item w/o ','
        values.append("?)");
        sql.append(columnNames[columnNames.length - 1]).append(')')
                .append(" VALUES ")
                .append(values)
                .append(" ON CONFLICT DO NOTHING");
        logger.debug("Created query: {}", sql);
        return sql.toString();
    }
}