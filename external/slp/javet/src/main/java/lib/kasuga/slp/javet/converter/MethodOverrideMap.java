package lib.kasuga.slp.javet.converter;

import java.lang.reflect.Method;
import java.util.*;

public class MethodOverrideMap {
    public HashMap<Integer, BitSet> converterMask = new HashMap<>();
    public HashMap<Integer, List<Method>> methods = new HashMap<>();

    public Set<Method> varArgsMethods = new HashSet<>();

    public boolean isVoidReturn = true;

    public MethodOverrideMap(){}

    public void initIfAbsent(int parameterCount){

        converterMask
                .computeIfAbsent(parameterCount, (i)->{
                    BitSet bitSet = new BitSet();
                    bitSet.set(0, i,true);
                    return bitSet;
                });

        methods.computeIfAbsent(parameterCount, (i)->new ArrayList<>());
    }
}
