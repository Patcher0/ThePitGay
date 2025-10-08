package net.mizukilab.pit.util.exception;

public class CancelOp extends RuntimeException{
    public static CancelOp INST = new CancelOp();
    public static void op(){
        throw INST;
    }
}
