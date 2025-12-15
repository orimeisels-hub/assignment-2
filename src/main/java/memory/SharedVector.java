package memory;

import java.sql.RowId;
import java.util.Vector;
import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        
        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        lock.readLock().lock();
        try{
            return vector[index];
        } finally {
            lock.readLock().unlock();
        }
    }

    public int length() {
        lock.readLock().lock();
        try{
            return vector.length;
        }finally{
            lock.readLock().unlock();
        }
    }

    public VectorOrientation getOrientation() {
         lock.readLock().lock();
        try{
            return this.orientation;
        }finally{
            lock.readLock().unlock();
        }
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        lock.writeLock().lock();
        try{
            if (orientation == VectorOrientation.ROW_MAJOR) {
                orientation = VectorOrientation.COLUMN_MAJOR;
            }
            else{
                orientation = VectorOrientation.ROW_MAJOR;
            }
        }finally{
            lock.writeLock().unlock();
        }
    }

    public void add(SharedVector other) {
        lock.writeLock().lock();
        other.readLock();
        try{
            if (other.vector.length != vector.length) {
               throw new IllegalArgumentException("vectors not of the same length");
            }
            for (int i = 0; i < vector.length; i++){
                vector[i] += other.vector[i];
            }
        }finally{
            lock.writeLock().unlock();
            other.readUnlock();
        }

    }

    public void negate() {
        lock.writeLock().lock();
        try{
            for(int i = 0; i < vector.length; i++){
                vector[i] = -vector[i];
            }
        } finally{
            lock.writeLock().unlock();
        }
        
    }

    public double dot(SharedVector other) {
        lock.readLock().lock();
        other.readLock();
        try{
            if (this.length() != other.length()) {
                throw new IllegalArgumentException("vectors not of the same lengthe");
            }
            if (orientation == other.orientation ) {
                throw new IllegalArgumentException("vectors not of the oposite orientation");
            }
            double answer = 0.0;
            for(int i = 0; i < vector.length; i++){
                answer += vector[i] * other.vector[i];
            }  
            return answer; 
        }finally{
            lock.readLock().unlock();
            other.readUnlock();

        }
    }

    public void vecMatMul(SharedMatrix matrix) {
        lock.readLock().lock();
        double[] newVector;
        try{
            if (orientation != VectorOrientation.ROW_MAJOR ) {
                throw new IllegalArgumentException ("vector is not a row major");
            }  
            if (matrix.getOrientation() != VectorOrientation.COLUMN_MAJOR) {
                throw new IllegalArgumentException("vecMatMul supports only COLUMN_MAJOR matrices");
            }
            if (matrix.get(0).length() != length()) {
                throw new IllegalArgumentException("the multuply is not definded");
            }
            newVector = new double[matrix.length()];
                for(int i = 0; i < matrix.length(); i++){
                    newVector[i] = this.dot(matrix.get(i)); 
                }
        }finally{
            lock.readLock().unlock();
        }
        lock.writeLock().lock(); 
        try{
            this.vector = newVector;

        }finally{
            lock.writeLock().unlock();
        }
    }
}

