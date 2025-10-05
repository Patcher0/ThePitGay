package net.mizukilab.pit.data.operator;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import nya.Skip;

import java.util.function.Consumer;

@Skip
public class SuPromise<E> extends ObjectArraySet<Consumer<E>> {
    E e;
    boolean done = false;
    public void ret(E e) {
        this.e = e;
        synchronized (this) {
            done = true;
            this.notifyAll();
        }
        this.forEach(i -> i.accept(e));
    }

    public SuPromise promise(Consumer<E> run) {
        synchronized (this) {
            if (done) {
                run.accept(e);
                return this;
            }
        }
        this.add(run);
        return this;
    }

    public SuPromise join() {
        try {
            synchronized (this) {
                if (done) {
                    return this;
                }
                this.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean isDone() {
        return done;
    }
}
