package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Queue;


public class CodestagramDBManager implements  DBInterface{

    private static final String url = "jdbc:h2:file:~/Desktop/h2db/codestagram";
    private static final String user = "jon";
    private static final String password = "0000";
    private final ConnectionPool connectionPool;

    public CodestagramDBManager(int connectionCount) throws SQLException {
        Queue<Connection> conns = new ArrayDeque<>();
        for( int i =0; i < connectionCount; i++){
            conns.add(DriverManager.getConnection(url,user,password));
        }
        connectionPool = new ConnectionPool(conns);
    }

    public Connection getConnection(){
        return this.connectionPool.getConnection();
    }

    public void returnConnection(Connection conn){
        this.connectionPool.returnConnection(conn);
    }

}
