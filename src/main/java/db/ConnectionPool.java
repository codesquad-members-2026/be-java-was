package db;

import java.sql.Connection;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPool {

    private final Lock lock = new ReentrantLock();
    private final Condition qAvailabilityCond = lock.newCondition();

    private final Queue<Connection> connPool;

    public ConnectionPool(Queue<Connection> pool){
        this.connPool = pool;
    }

    public Connection getConnection(){

        lock.lock();

        try{
            while(connPool.isEmpty()){
                qAvailabilityCond.await();
            }
            return connPool.poll();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread is interrupted while waiting for connPool",e);
        } finally {
            lock.unlock();
        }

    }


    public void returnConnection(Connection conn){
        lock.lock();
        try{
            this.connPool.add(conn);
            this.qAvailabilityCond.signal();
        }
        finally {
            lock.unlock();
        }

    }

}
