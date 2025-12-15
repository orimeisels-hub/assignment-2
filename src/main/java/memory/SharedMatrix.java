package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors

        if (matrix == null) {
            throw new IllegalArgumentException("matrix is null");
        }
        if (matrix.length == 0){
            vectors = new SharedVector[0];
            return;
        }
        if (matrix[0] == null) {
            throw new IllegalArgumentException("matrix row 0 is null");
        }
        int size = matrix[0].length; 
        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i] == null) {
                throw new IllegalArgumentException("matrix row " + i + " is null");
            }
            if (matrix[i].length != size) {
                throw new IllegalArgumentException("matrix is not defined");
            }
        } 

        vectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            double[] clone = matrix[i].clone();
            SharedVector vector = new SharedVector(clone, VectorOrientation.ROW_MAJOR);
            vectors[i] = vector;
        }
    }

    public void loadRowMajor(double[][] matrix) {
         if (matrix == null) {
            throw new IllegalArgumentException("matrix is null");
        }
        if (matrix.length == 0){
            vectors = new SharedVector[0];
            return;
        }
        if (matrix[0] == null) {
            throw new IllegalArgumentException("matrix row 0 is null");
        }
        int size = matrix[0].length; 
        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i] == null) {
                throw new IllegalArgumentException("matrix row " + i + " is null");
            }
            if (matrix[i].length != size) {
                throw new IllegalArgumentException("matrix is not defined");
            }
        } 

        SharedVector[] newVectors = new SharedVector[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            double[] clone = matrix[i].clone();
            SharedVector vector = new SharedVector(clone, VectorOrientation.ROW_MAJOR);
            newVectors[i] = vector;
        }
        vectors = newVectors;
    }

    public void loadColumnMajor(double[][] matrix) {
         if (matrix == null) {
            throw new IllegalArgumentException("matrix is null");
        }
        if (matrix.length == 0){
            vectors = new SharedVector[0];
            return;
        }
        if (matrix[0] == null) {
            throw new IllegalArgumentException("matrix row 0 is null");
        }
        int rows = matrix.length;
        int cols = matrix[0].length; 
        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i] == null) {
                throw new IllegalArgumentException("matrix row " + i + " is null");
            }
            if (matrix[i].length != cols) {
                throw new IllegalArgumentException("matrix is not defined");
            }
        } 
        SharedVector[] newVectors = new SharedVector[cols];

        for (int c = 0; c < cols; c++) {
            double[] col = new double[rows];

            for (int r = 0; r < rows; r++) {
                col[r] = matrix[r][c];
            }

            newVectors[c] = new SharedVector(col, VectorOrientation.COLUMN_MAJOR);
        }
        vectors = newVectors;     
    }

    public double[][] readRowMajor() {
    SharedVector[] local = vectors;
        if (local.length == 0) {
            return new double[0][0];
        }

    acquireAllVectorReadLocks(local);
    try {
        VectorOrientation o = local[0].getOrientation();

        for (int i = 1; i < local.length; i++) {
            if (local[i].getOrientation() != o) {
                throw new IllegalStateException("Inconsistent vector orientations in matrix");
            }
        }

        if (o == VectorOrientation.ROW_MAJOR) {
            int rows = local.length;
            int cols = local[0].length();

            for (int r = 1; r < rows; r++) {
                if (local[r].length() != cols) {
                    throw new IllegalStateException("Inconsistent row lengths in matrix");
                }
            }

            double[][] out = new double[rows][cols];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    out[r][c] = local[r].get(c);
                }
            }
            return out;
        } else {
            int cols = local.length;
            int rows = local[0].length();

            for (int c = 1; c < cols; c++) {
                if (local[c].length() != rows) {
                    throw new IllegalStateException("Inconsistent column lengths in matrix");
                }
            }

            double[][] out = new double[rows][cols];
            for (int c = 0; c < cols; c++) {
                for (int r = 0; r < rows; r++) {
                    out[r][c] = local[c].get(r);
                }
            }
            return out;
        }
    } finally {
        releaseAllVectorReadLocks(local);
    }
}

    public SharedVector get(int index) {
        return vectors[index];
    }

    public int length() {
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        if (vectors.length == 0) {
            return null;
        }
        return vectors[0].getOrientation();
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        for(SharedVector v : vecs){
            v.readLock();
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
         for(SharedVector v : vecs){
            v.readUnlock();
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
         for(SharedVector v : vecs){
            v.writeLock();
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
         for(SharedVector v : vecs){
            v.writeUnlock();
        }
    }
}
