package net.mizukilab.pit.util.functions;
@FunctionalInterface
public interface Func3<A,B,C,D> {
    public D invoke(A arg0, B arg1, C arg2);
}
