package tfc.flame.loader.asm;

import java.util.ArrayList;
import java.util.function.Consumer;

public class PriorityPhaseList<T> {
    ArrayList<T>[] phases = new ArrayList[6];
    
    public PriorityPhaseList() {
        for (int i = 0; i < phases.length; i++) {
            phases[i] = new ArrayList<>();
        }
    }
    
    public void add(Phase phase, T obj) {
        phases[phase.ordinal()].add(obj);
    }
    
    public void forEach(Consumer<T> function) {
        for (ArrayList<T> phase : phases)
            for (T t : phase)
                function.accept(t);
    }
}
