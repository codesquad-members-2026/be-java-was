package db;

import java.sql.Connection;

public interface DBInterface {
    public Connection getConnection();
    public void returnConnection(Connection conn);
}
